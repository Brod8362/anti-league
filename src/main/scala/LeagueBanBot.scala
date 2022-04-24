package pw.byakuren.nolol

import Util.ReplyCallbackUtil

import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.events.interaction.command.{CommandAutoCompleteInteractionEvent, SlashCommandInteractionEvent}
import net.dv8tion.jda.api.events.user.{UserActivityEndEvent, UserActivityStartEvent}
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.utils.cache.CacheFlag

import java.util.concurrent.{Executors, ScheduledExecutorService, TimeUnit}
import scala.collection.mutable
import scala.jdk.CollectionConverters.SeqHasAsJava
import scala.collection.mutable.HashMap

object LeagueBanBot extends ListenerAdapter {

  private implicit val sql: SQLConnection = new SQLConnection()

  private val commands: Seq[LBCommand] = Seq(new SettingCommand, new ChannelCommand)

  private val executor: ScheduledExecutorService = Executors.newScheduledThreadPool(128)
  private val threads: mutable.HashMap[Member, Unit] = new mutable.HashMap[Member, Unit]()

  def main(args: Array[String]): Unit = {
    //token read in from LB_TOKEN env
    val token = System.getenv("LB_TOKEN")
    if (token == null) {
      System.err.println("LB_TOKEN environment variable is not set. Please set to the bot's token.")
      System.exit(1)
    }

    val jda = JDABuilder.createDefault(token)
      .enableIntents(GatewayIntent.GUILD_BANS, GatewayIntent.GUILD_PRESENCES)
      .addEventListeners(this)
      .enableCache(CacheFlag.ACTIVITY)
      .build()
    commands.foldRight(jda.updateCommands())((cmd, act) => act.addCommands(cmd.apply())).queue()
  }

  override def onUserActivityStart(event: UserActivityStartEvent): Unit = {
    println(s"the thing happened ${event.getNewActivity.getName}")
    if (event.getNewActivity.getName.toLowerCase == "league of legends") {
      val dest_channel = sql(event.getGuild, Setting.ALERT_CHANNEL) match {
        case Some(str) if Option(event.getGuild.getGuildChannelById(str)).isDefined =>
          event.getGuild.getTextChannelById(str)
        case _ =>
          event.getGuild.getOwner.getUser.openPrivateChannel().queue(v => v.sendMessage(
            "The league ban bot is misconfigured. Please set the dest_channel setting to a valid channel."
          ).queue() )
          return
      }

      val ban_delay = sql(event.getGuild, Setting.BAN_DELAY) match {
        case Some(str) if str == "instant" =>
          0
        case Some(str) if str.endsWith("m") =>
          //todo this doesn't account for bad syntax but that should really be dealt with somewhere else lmaoooo
          str.substring(0, str.length()-1).toInt
        case None => 15
      }

      if (ban_delay > 0) {
        event.getUser.openPrivateChannel.queue(v => v.sendMessage("It appears you have started playing **League of Legends." +
          s"Please close the game, or you will be banned from __${event.getGuild.getName}__ in **$ban_delay minutes.**").queue())
      }

      val scheduled_event = executor.schedule(new Runnable() {
        override def run(): Unit = {
          try {
            dest_channel.sendMessage(f"${event.getMember} has been banned for playing League of Legends! They will not be missed.").queue()
            event.getMember.ban(0).queue()
          } catch {
            case e: InsufficientPermissionException =>
              //public message to name and shame
            case _ =>
              //some other exception
          }
        }
      }, ban_delay, TimeUnit.MINUTES)
      threads.put(event.getMember, scheduled_event)
    }
  }

  override def onUserActivityEnd(event: UserActivityEndEvent): Unit = {
    if (event.getOldActivity.getName.toLowerCase == "league of legends") {
      if (threads.contains(event.getMember)) {
        event.getUser.openPrivateChannel().queue(v => v.sendMessage("You have been spared.... for now.").queue())
        threads.remove(event.getMember)
      }
    }
  }

  override def onSlashCommandInteraction(event: SlashCommandInteractionEvent): Unit = {
    commands.find(_.name == event.getName) match {
      case Some(cmd) =>
        if (event.getMember.hasPermission(cmd.permission)) {
          cmd.run(event)
        } else {
          event.reply("You do not have permission to use that command.").seq()
        }
      case None =>
    }
  }

  override def onCommandAutoCompleteInteraction(event: CommandAutoCompleteInteractionEvent): Unit = {
    commands.find(_.name == event.getCommandId).foreach(_.autocomplete(event))
  }
}
