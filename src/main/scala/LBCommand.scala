package pw.byakuren.nolol

import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.interaction.command.{CommandAutoCompleteInteractionEvent, SlashCommandInteractionEvent}
import net.dv8tion.jda.api.interactions.commands.build.{Commands, SlashCommandData}

abstract class LBCommand(val name: String, val desc: String, val permission: Permission) {

  def apply(): SlashCommandData = {
    Commands.slash(name, desc)
  }

  def run(event: SlashCommandInteractionEvent): Unit

  def autocomplete(event: CommandAutoCompleteInteractionEvent): Unit = {}
}
