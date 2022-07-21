package pw.byakuren.nolol
package util

import java.net.{HttpURLConnection, URL}

class APIAnalytics(name: String, base: String = "http://127.0.0.1:9646") {
  private def post(uri: String): Int = {
    val url = new URL(uri)
    val conn = url.openConnection().asInstanceOf[HttpURLConnection]
    conn.setRequestMethod("POST")
    conn.setConnectTimeout(5000)
    conn.setReadTimeout(5000)
    conn.getResponseCode
  }

  def updateGuilds(count: Int): Unit = {
    post(s"$base/v1/guild/$name/$count")
  }

  def updateUsage(guild: Long, channel: Long): Unit = {
    post(s"$base/v1/data/$name/$guild/$channel")
  }
}