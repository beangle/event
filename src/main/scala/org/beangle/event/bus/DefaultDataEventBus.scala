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

import org.beangle.commons.bean.{Disposable, Initializing}
import org.beangle.commons.concurrent.{Sidecar, Workers}
import org.beangle.commons.logging.Logging
import org.beangle.event.mq.{ChannelQueue, EventSubscriber}

/** Default DataEventBus
 *
 * @param queue outside global queue
 */
final class DefaultDataEventBus(queue: ChannelQueue[DataEvent])
  extends DataEventBus, EventSubscriber[DataEvent], Initializing, Disposable, Logging {

  private val subscribers = new collection.mutable.HashMap[String, collection.mutable.Set[EventSubscriber[DataEvent]]]

  /** inside publishing queue with sidecar */
  private var sidecar: Sidecar[DataEvent] = _

  override def init(): Unit = {
    queue.subscribe(this)
    sidecar = new Sidecar[DataEvent]("Beangle DataEventBus Sidecar", e => {
      queue.publish(e)
    })
  }

  def subscribe(pattern: String, subscriber: EventSubscriber[DataEvent]): Unit = {
    subscribers.getOrElseUpdate(pattern, new collection.mutable.HashSet) += subscriber
  }

  def unsubscribe(pattern: String, subscriber: EventSubscriber[DataEvent]): Unit = {
    subscribers.get(pattern).foreach(_.remove(subscriber))
  }

  def publish(event: DataEvent): Unit = {
    sidecar.offer(event)
  }

  override def publish(events: Iterable[DataEvent]): Unit = {
    events foreach { e => sidecar.offer(e) }
  }

  override def publishUpdate(clazz: Class[_], filters: Map[String, String], comment: Option[String] = None): Unit = {
    publish(DataEvent.update(clazz, filters, comment))
  }

  override def destroy(): Unit = {
    sidecar.destroy()
  }

  /** 响应事件
   *
   * @param event data event
   */
  override def process(event: DataEvent): Unit = {
    val matched = subscribers.filter(x => event.isMatch(x._1)).flatten(_._2).toList
    if matched.size > 10 then Workers.work(matched, s => s.process(event))
    else
      matched.foreach { s =>
        try
          s.process(event)
        catch
          case e: Throwable => logger.error(e.getMessage)
      }
  }

}
