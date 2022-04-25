package pw.byakuren.nolol

import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.interaction.command.{CommandAutoCompleteInteractionEvent, SlashCommandInteractionEvent}
import net.dv8tion.jda.api.interactions.commands.{Command, OptionType}
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
import pw.byakuren.nolol.util.Setting
import pw.byakuren.nolol.util.Util.ReplyCallbackUtil

import scala.jdk.CollectionConverters.IterableHasAsJava

class BanDelayCommand(implicit sql: SQLConnection) extends LBCommand("ban_delay", "Adjust the ban delay.", Permission.MANAGE_SERVER) {

  override def apply(): SlashCommandData = {
    super.apply().addOption(OptionType.INTEGER, "new_delay", "The new ban delay (in minutes)", true)
  }

  override def run(event: SlashCommandInteractionEvent): Unit = {
    val amt = event.getOption("new_delay").getAsInt
    if (amt < 0) {
      event.reply("Value must be positive.").seq()
      return
    }
    sql.set(event.getGuild, Setting.BAN_DELAY, amt.toString)
    event.reply(s"Ban delay updated to **$amt minutes**.").seq()
  }

}
