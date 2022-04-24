package pw.byakuren.nolol

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

}
