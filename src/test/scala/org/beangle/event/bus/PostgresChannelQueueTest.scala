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

import org.beangle.event.bus.DataEventType.Deletion
import org.beangle.event.mq.impl.{NullEventSubscriber, PostgresChannelQueue}
import org.beangle.jdbc.ds.{DataSourceUtils, DatasourceConfig}

import java.time.Instant

object PostgresChannelQueueTest {

  def main(args: Array[String]): Unit = {
    val dbconfig = new DatasourceConfig("postgresql")
    dbconfig.props.put("jdbcUrl", "jdbc:postgresql://localhost:5432/ecupl")
    dbconfig.props.put("maxLifetime", "30000")
    dbconfig.props.put("connectionTimeout", "3000")
    dbconfig.user = "postgres"
    dbconfig.password = "postgres"

    val ds = DataSourceUtils.build(dbconfig)
    //val serializer = new TestEventSerializer
    //val queue = new PostgresChannelQueue("test", ds, serializer)
    //queue.subscribe(new NullEventSubscriber[TestEvent])

    val serializer = new DataEventSerializer
    val queue = new PostgresChannelQueue("test", ds, serializer)
    queue.subscribe(new NullEventSubscriber[DataEvent])
    queue.init()
    var i = 0
    while (i < 10) {
      Thread.sleep(1000)
      try
        val e = DataEvent("org.beangle.security.User", Map("id" -> "1,2,3"), Deletion, Instant.now, None)
        queue.publish(e)
      catch
        case e: Exception => println(e.getMessage)
      i += 1
    }
    queue.destroy()
    println("shutdown")
  }
}
