package pw.byakuren.nolol

import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
import pw.byakuren.nolol.util.Setting
import pw.byakuren.nolol.util.Util.ReplyCallbackUtil

class ChannelCommand(implicit sql: SQLConnection) extends
  LBCommand("alert_channel", "Set the alert channel", Permission.MANAGE_SERVER) {

  override def apply(): SlashCommandData = {
    super.apply().addOption(OptionType.CHANNEL, "channel", "Text channel to send alerts to", true)
  }

  override def run(event: SlashCommandInteractionEvent): Unit = {
    val channel = event.getOption("channel").getAsGuildChannel
    sql.set(event.getGuild, Setting.ALERT_CHANNEL, channel.getId)
    event.reply(f"Alert channel set to ${channel.getAsMention}").seq()
  }
}
