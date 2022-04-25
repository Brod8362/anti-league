package pw.byakuren.nolol

import net.dv8tion.jda.api.{EmbedBuilder, Permission}
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import pw.byakuren.nolol.util.PackageInfo
import pw.byakuren.nolol.util.Util.ReplyCallbackUtil

class InviteCommand extends LBCommand("invite", "Get an invite link for the bot, and the help server.", Permission.MESSAGE_SEND) {
  override def run(event: SlashCommandInteractionEvent): Unit = {
    val eb = new EmbedBuilder
    eb.setThumbnail(event.getJDA.getSelfUser.getEffectiveAvatarUrl)
    eb.setTitle("Anti-League Links")
    eb.setDescription(
      """
        |Need help with Anti-League? [Join the help server here](https://discord.gg/3Scnd3GvCn)
        |
        |Want to invite Anti-League to your server? [Click here](https://discord.com/api/oauth2/authorize?client_id=967601110326050816&permissions=2052&scope=bot%20applications.commands)
        |
        |Source code available on [Github](https://github.com/Brod8362/anti-league)
        |
        |Thanks for using Anti-League!
        |""".stripMargin)
      eb.setFooter(s"Git Build: ${PackageInfo.VERSION.take(10)}")
    event.replyEmbeds(eb.build()).q()
  }
}
