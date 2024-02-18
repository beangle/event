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

package org.beangle.event.bus

import org.beangle.commons.bean.Initializing
import org.beangle.commons.logging.Logging
import org.beangle.event.mq.{ChannelQueue, EventSubscriber}

final class DefaultDataEventBus(val name: String, queue: ChannelQueue[DataEvent])
  extends DataEventBus, EventSubscriber[DataEvent], Initializing, Logging {

  private val subscribers = new collection.mutable.HashMap[String, collection.mutable.Set[EventSubscriber[DataEvent]]]

  override def init(): Unit = {
    queue.subscribe(this)
  }

  def subscribe(pattern: String, subscriber: EventSubscriber[DataEvent]): Unit = {
    subscribers.getOrElseUpdate(pattern, new collection.mutable.HashSet) += subscriber
  }

  def unsubscribe(pattern: String, subscriber: EventSubscriber[DataEvent]): Unit = {
    subscribers.get(pattern).foreach(_.remove(subscriber))
  }

  def publish(event: DataEvent): Unit = {
    queue.publish(event)
  }

  /** 响应事件
   * FIXME multiple thread
   *
   * @param event
   */
  override def process(event: DataEvent): Unit = {
    val matched = subscribers.filter(x => event.isMatch(x._1)).flatten(_._2).toList
    matched.foreach { s =>
      try {
        s.process(event)
      } catch
        case e: Throwable => logger.error(e.getMessage)
    }
  }

}
