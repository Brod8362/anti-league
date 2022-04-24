package pw.byakuren.nolol

import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData

class SettingCommand extends LBCommand("lbb", "Change settings related to the League ban bot.", Permission.MANAGE_SERVER) {

  override def apply(): SlashCommandData = {
    super.apply().addOption(OptionType.STRING, "setting", "The setting you're changing", true)
      .addOption(OptionType.STRING, "value", "The new value of the setting (blank to unset)", false)
  }

  override def run(event: SlashCommandInteractionEvent): Unit = {

  }
}
