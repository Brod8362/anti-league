package pw.byakuren.nolol
package util

import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction

object Util {

  implicit class ReplyCallbackUtil(x: ReplyCallbackAction) {
    def seq(): Unit = {
      x.setEphemeral(true).queue()
    }

    def se(): Unit = {
      x.setEphemeral(true)
    }

    def q(): Unit = {
      x.queue()
    }
  }

  implicit class UserUtil(x: User) {
    def sendMessage(content: String, onFail: Option[Throwable => Unit] = None): Unit = {
      try {
        x.openPrivateChannel().complete.sendMessage(content).complete() //complete is INTENTIONAL, to ensure the message is delivered to the user.
      } catch {
        case e: Throwable =>
          onFail.foreach(cb => cb(e))
      }
    }
  }

}
