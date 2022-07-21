package pw.byakuren.nolol
package util

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.events.guild.{GuildJoinEvent, GuildLeaveEvent}
import net.dv8tion.jda.api.hooks.ListenerAdapter

import java.awt.Color

object Analytics extends ListenerAdapter {

  val api = new APIAnalytics("anti-league")

  override def onGuildJoin(event: GuildJoinEvent): Unit = {
    guildUpdate(joined = true, event.getGuild)

    val channel = event.getGuild.getDefaultChannel
    val embed = new EmbedBuilder()
      .setTitle("Thanks for adding Anti-League!")
      .setThumbnail(event.getJDA.getSelfUser.getEffectiveAvatarUrl)
      .setDescription("Before you can start using Anti-League, please visit the [setup guide.](https://github.com/Brod8362/anti-league/wiki/Initial-Setup-Guide)")
      .addField("Support Server", "https://discord.gg/3Scnd3GvCn", true)
      .addField("Official Invite Link", "[Click here](https://discord.com/api/oauth2/authorize?client_id=967601110326050816&permissions=2052&scope=bot%20applications.commands)", true)
      .setColor(Color.ORANGE)
      .build()
    try {
      channel.sendMessageEmbeds(embed).queue()
    } catch {
      case _: Throwable =>
    }
  }

  override def onGuildLeave(event: GuildLeaveEvent): Unit = {
    guildUpdate(joined = false, event.getGuild)
  }

  def guildUpdate(joined: Boolean, guild: Guild): Unit = {
    val owner = guild.getJDA.retrieveApplicationInfo().complete().getOwner
    val txt = s"${if (joined) "joined" else "left"} guild `${guild.getName}`:${guild.getId} (owned by <@${guild.getOwnerId}>) " +
      s"(${guild.getMemberCount} members) [${guild.getJDA.getGuilds.size()} servers]"
    val dm = owner.openPrivateChannel().complete()
    dm.sendMessage(txt).queue()
    try {
      api.updateGuilds(guild.getJDA.getGuilds.size())
    } catch {
      case e: Throwable =>
        dm.sendMessage(s"couldn't update guild analytics:\n$e").queue()
    }
  }


}
