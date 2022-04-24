package pw.byakuren.nolol

import pw.byakuren.nolol.util.Util.{ReplyCallbackUtil, UserUtil}
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.entities.Activity.ActivityType
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.events.interaction.command.{CommandAutoCompleteInteractionEvent, SlashCommandInteractionEvent}
import net.dv8tion.jda.api.events.user.{UserActivityEndEvent, UserActivityStartEvent}
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.utils.MemberCachePolicy
import net.dv8tion.jda.api.utils.cache.CacheFlag
import pw.byakuren.nolol.util.{Analytics, Setting}

import java.util.concurrent.{Executors, ScheduledExecutorService, ScheduledFuture, TimeUnit}
import scala.collection.mutable

object LeagueBanBot extends ListenerAdapter {

  private implicit val sql: SQLConnection = new SQLConnection()

  private val commands: Seq[LBCommand] = Seq(new BanDelayCommand, new ChannelCommand)

  private val executor: ScheduledExecutorService = Executors.newScheduledThreadPool(128)
  private val threads: mutable.HashMap[Member, ScheduledFuture[_]] = new mutable.HashMap()
  private val reminders: mutable.HashMap[Member, ScheduledFuture[_]] = new mutable.HashMap()
  private val addtime: mutable.HashMap[Member, Long] = new mutable.HashMap()

  def main(args: Array[String]): Unit = {
    //token read in from LB_TOKEN env
    val token = System.getenv("LB_TOKEN")
    if (token == null) {
      System.err.println("LB_TOKEN environment variable is not set. Please set to the bot's token.")
      System.exit(1)
    }

    val jda = JDABuilder.createDefault(token)
      .enableIntents(GatewayIntent.GUILD_BANS, GatewayIntent.GUILD_PRESENCES, GatewayIntent.GUILD_MEMBERS)
      .setMemberCachePolicy(MemberCachePolicy.ALL)
      .addEventListeners(this)
      .addEventListeners(Analytics)
      .enableCache(CacheFlag.ACTIVITY)
      .build()
    commands.foldRight(jda.updateCommands())((cmd, act) => act.addCommands(cmd.apply())).queue()
  }

  override def onUserActivityStart(event: UserActivityStartEvent): Unit = {
    if (event.getNewActivity.getType == ActivityType.PLAYING && event.getNewActivity.getName.toLowerCase == "league of legends"
    && !threads.contains(event.getMember)) {
      val dest_channel = sql(event.getGuild, Setting.ALERT_CHANNEL) match {
        case Some(str) if Option(event.getGuild.getGuildChannelById(str)).isDefined =>
          event.getGuild.getTextChannelById(str)
        case _ =>
//          event.getGuild.getOwner.getUser.openPrivateChannel().queue(v => v.sendMessage(
//            "The league ban bot is misconfigured. Please set the dest_channel setting to a valid channel."
//          ).queue() )
          return
      }

      val ban_delay = sql(event.getGuild, Setting.BAN_DELAY, "15").toInt

      if (ban_delay > 0) {
        event.getUser.openPrivateChannel.queue(v => v.sendMessage("It appears you have started playing **League of Legends.** " +
          s"Please close the game, or you will be banned from __${event.getGuild.getName}__ in **$ban_delay minutes.**").queue())
      }

      if (ban_delay > 10) {
        val remind = executor.schedule(new Runnable() {
          override def run(): Unit = {
            event.getUser.sendMessage(s"Only **5 minutes** remain until your ban from __${event.getGuild.getName}__.")
            reminders.remove(event.getMember)
          }
        }, ban_delay-5, TimeUnit.MINUTES)
        reminders.put(event.getMember, remind)
      }

      val scheduled_event: ScheduledFuture[_] = executor.schedule(new Runnable() {
        override def run(): Unit = {
          try {
            threads.remove(event.getMember)
            reminders.remove(event.getMember)
            event.getUser.sendMessage(s"Your time is up, and you have been banned from __${event.getGuild.getName}__. Consider bettering yourself, and don't play league anymore.")
            event.getMember.ban(0).queue()
            dest_channel.sendMessage(f"${event.getMember} has been banned for playing League of Legends! They will not be missed.").queue()
          } catch {
            case e: InsufficientPermissionException =>

            case _ =>
              //some other exception
          }
        }
      }, ban_delay, TimeUnit.MINUTES)
      threads.put(event.getMember, scheduled_event)
      addtime.put(event.getMember, System.currentTimeMillis())
    }
  }

  override def onUserActivityEnd(event: UserActivityEndEvent): Unit = {
    if (event.getOldActivity.getName.toLowerCase == "league of legends") {
      if (threads.contains(event.getMember) &&  System.currentTimeMillis() - addtime(event.getMember) > 1000) {
        event.getUser.openPrivateChannel().queue(v => v.sendMessage("You have been spared.... for now.").queue())
        threads(event.getMember).cancel(false)
        if (reminders.contains(event.getMember)) {
          reminders(event.getMember).cancel(true)
        }
        reminders.remove(event.getMember)
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
    commands.find(_.name == event.getName).foreach(_.autocomplete(event))
  }
}