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

import org.beangle.commons.logging.Logging
import org.beangle.event.mq.*
import redis.clients.jedis.exceptions.JedisConnectionException
import redis.clients.jedis.{JedisPool, JedisPubSub}

class RedisChannelQueue[T](channelName: String, pool: JedisPool, serializer: EventSerializer[T])
  extends AbstractChannelQueue(channelName, serializer) {

  private var daemon: RedisPolling[T] = _

  override def publish(event: T): Unit = {
    val jedis = pool.getResource
    try {
      jedis.publish(channelName, serializer.toJson(event))
    } finally {
      jedis.close()
    }
  }

  override def destroy(): Unit = {
    if (null != daemon) {
      daemon.unsubscribe(this.channelName)
    }
  }

  override def init(): Unit = {
    if (daemon == null) {
      daemon = new RedisPolling[T](this, pool)
      val t = new Thread(daemon, "redis-polling-" + channelName)
      t.setDaemon(true)
      t.start()
    }
  }
}

class RedisPolling[T](queue: RedisChannelQueue[T], pool: JedisPool) extends JedisPubSub, Runnable, Logging {
  override def onMessage(channel: String, msg: String): Unit = {
    queue.onMessage(msg)
  }

  override def run(): Unit = {
    try {
      val jedis = pool.getResource
      logger.info("Subscribing redis on channel:" + queue.name)
      jedis.subscribe(this, queue.name)
      jedis.close()
    } catch {
      case e: JedisConnectionException => logger.error("Connect redis failed.")
    }
  }
}
