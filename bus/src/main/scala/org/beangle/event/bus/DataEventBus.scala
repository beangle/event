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

import org.beangle.event.mq.EventSubscriber

trait DataEventBus {

  def subscribe(module: String, subscriber: EventSubscriber[DataEvent]): Unit

  def publish(event: DataEvent): Unit

  def publish(events: Iterable[DataEvent]): Unit

  def publishUpdate(clazz: Class[_], filters: Map[String, String], comment: Option[String] = None): Unit
}
