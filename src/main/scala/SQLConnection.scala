package pw.byakuren.nolol

import net.dv8tion.jda.api.entities.Guild
import scalikejdbc.{AutoSession, ConnectionPool, scalikejdbcSQLInterpolationImplicitDef}

class SQLConnection {
  Class.forName("org.sqlite.JDBC")
  ConnectionPool.singleton("jdbc:sqlite:nolol_bot.db", null, null)

  implicit val session: AutoSession.type = AutoSession

  sql"CREATE TABLE IF NOT EXISTS server_config(guild INTEGER NOT NULL, setting STRING NOT NULL, value STRING, PRIMARY KEY(guild,setting))".execute().apply()
  sql"CREATE TABLE IF NOT EXISTS analytics(key STRING PRIMARY KEY NOT NULL, value INTEGER NOT NULL)".execute().apply()
  sql"INSERT OR IGNORE INTO analytics VALUES('lifetime_bans', 0)".execute.apply()

  /**
   * Get a server's setting.
   * @param guild The guild ID to retrieve
   * @param setting The setting to retrieve
   * @return Some(str) if set, None if not set.
   */
  def apply(guild: Long, setting: String): Option[String] = {
    sql"SELECT value FROM server_config WHERE guild=$guild AND setting=$setting".map(_.string("value")).single().apply()
  }

  def apply(guild: Guild, setting: String): Option[String] = {
    this.apply(guild.getIdLong, setting)
  }

  def apply(guild: Guild, setting: String, default: String): String = {
    this.apply(guild, setting).getOrElse(default)
  }

  /**
   * Set a server's setting
   * @param guild The guild ID to set
   * @param setting The setting to set
   * @param value The value of the setting to set
   */
  def set(guild: Long, setting: String, value: String): Unit = {
    sql"INSERT OR REPLACE INTO server_config VALUES($guild, $setting, $value)".executeUpdate().apply()
  }

  def set(guild: Guild, setting: String, value: String): Unit = {
    this.set(guild.getIdLong, setting, value)
  }

  def incrementLifetimeBans(): Unit = {
    sql"UPDATE analytics SET value = value + 1 WHERE key='lifetime_bans'".executeUpdate().apply()
  }

  def lifetimeBans(): Int = {
    sql"SELECT value FROM analytics WHERE key='lifetime_bans'".map(_.int("value")).single().apply().getOrElse(0)
  }

}
