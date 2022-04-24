package pw.byakuren.nolol
package util

import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.events.guild.{GuildJoinEvent, GuildLeaveEvent}
import net.dv8tion.jda.api.hooks.ListenerAdapter

object Analytics extends ListenerAdapter {

  override def onGuildJoin(event: GuildJoinEvent): Unit = {
    guildUpdate(joined = true, event.getGuild)
  }

  override def onGuildLeave(event: GuildLeaveEvent): Unit = {
    guildUpdate(joined = false, event.getGuild)
  }

  def guildUpdate(joined: Boolean, guild: Guild): Unit = {
    val owner = guild.getJDA.retrieveApplicationInfo().complete().getOwner
    val txt = s"${if (joined) "joined" else "left"} guild `${guild.getName}`:${guild.getId} (owned by <@${guild.getOwnerId}>) " +
      s"(${guild.getMemberCount} members) [${guild.getJDA.getGuilds.size()} servers]"
    owner.openPrivateChannel().complete().sendMessage(txt).queue()
    println(txt)
  }


}
