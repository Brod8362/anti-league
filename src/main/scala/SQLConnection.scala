package pw.byakuren.nolol

import net.dv8tion.jda.api.entities.Guild
import scalikejdbc.{AutoSession, ConnectionPool, scalikejdbcSQLInterpolationImplicitDef}

class SQLConnection {
  Class.forName("org.sqlite.JDBC")
  ConnectionPool.singleton("jdbc:sqlite:nolol_bot.db", null, null)

  implicit val session: AutoSession.type = AutoSession

  sql"CREATE TABLE IF NOT EXISTS server_config(guild INTEGER NOT NULL, setting STRING NOT NULL, value STRING, PRIMARY KEY(guild,setting))".execute().apply()

  /**
   * Get a server's setting.
   * @param guild The guild ID to retrieve
   * @param setting The setting to retrieve
   * @return Some(str) if set, None if not set.
   */
  def apply(guild: Long, setting: String): Option[String] = {
    None
  }

  def apply(guild: Guild, setting: String): Option[String] = {
    this.apply(guild.getIdLong, setting)
  }

  /**
   * Set a server's setting
   * @param guild The guild ID to set
   * @param setting The setting to set
   * @param value The value of the setting to set
   */
  def set(guild: Long, setting: String, value: String): Unit = {

  }

  def set(guild: Guild, setting: String, value: String): Unit = {
    this.set(guild.getIdLong, setting, value)
  }

}
