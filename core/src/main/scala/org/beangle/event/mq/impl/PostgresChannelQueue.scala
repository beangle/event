/*
 * Copyright (C) 2005, The Beangle Software.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.beangle.event.mq.impl

import org.beangle.commons.io.IOs
import org.beangle.event.mq.*
import org.postgresql.PGConnection

import java.sql.{Connection, SQLException}
import javax.sql.DataSource

class PostgresChannelQueue[T](channelName: String, ds: DataSource, serializer: EventSerializer[T])
  extends AbstractChannelQueue(channelName, serializer) {

  private var daemon: PostgresPolling[T] = _

  override def init(): Unit = {
    listen()
  }

  def listen(): Unit = {
    var conn: Connection = null
    while (conn == null) {
      try {
        conn = ds.getConnection
        conn.setAutoCommit(true)
        conn.setReadOnly(true)
        conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED)
        val stmt = conn.createStatement()
        stmt.execute("LISTEN " + channelName)
        stmt.close()
      } catch
        case e: Exception =>
          logger.error("Failed to connect to database, retrying in 5 seconds...")
          IOs.close(conn)
          conn == null
          Thread.sleep(5000)
    }
    this.daemon = new PostgresPolling(this, conn)
    val t = new Thread(daemon, "postgres-polling-" + channelName)
    t.setDaemon(true)
    t.start()
  }

  override def destroy(): Unit = {
    this.daemon.shutdown(channelName)
  }

  override def publish(event: T): Unit = {
    var conn: Connection = null
    try {
      conn = ds.getConnection
      // 发布事件到PostgreSQL
      val notifyStmt = conn.prepareStatement("select pg_notify(?,?)")
      notifyStmt.setString(1, channelName)
      notifyStmt.setString(2, serializer.toJson(event))
      notifyStmt.execute()
    } finally {
      IOs.close(conn)
    }
  }
}

class PostgresPolling[T](queue: PostgresChannelQueue[T], conn: Connection) extends Runnable {
  private var listening: Boolean = true
  private var polling: Boolean = false

  private val pgCon: PGConnection = conn.unwrap(classOf[PGConnection])
  private val delayMillis = 500 //0.5s

  override def run(): Unit = {
    try
      while (listening) {
        polling = true
        val notifications = pgCon.getNotifications(delayMillis)
        polling = false
        if (null != notifications) {
          var i = 0
          while (i < notifications.length) {
            val notify = notifications(i)
            queue.onMessage(notify.getParameter)
            i += 1
          }
        }
      }
    catch
      case e: SQLException =>
        println("Polling error:" + e.getMessage)
        queue.listen()
  }

  def shutdown(channelName: String): Unit = {
    try {
      this.listening = false
      if (polling) Thread.sleep(delayMillis)
      val stmt = conn.createStatement()
      stmt.execute("UNLISTEN " + channelName)
      stmt.close()
    } finally {
      IOs.close(conn)
    }
  }
}
