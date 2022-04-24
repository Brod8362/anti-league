package pw.byakuren.nolol

object Setting extends Enumeration {
  type Setting = String
  val BAN_DELAY = "ban_delay"
  val SEND_REMINDER = "send_reminder"
  val ALERT_CHANNEL = "alert_channel_id"
  val MESSAGE_USER = "message_user"
  val BAN_MESSAGE = "show_ban_message"
}
