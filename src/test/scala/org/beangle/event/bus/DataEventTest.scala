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

import org.beangle.commons.lang.time.DateFormats.UTC
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

import java.time.Instant

class DataEventTest extends AnyFunSpec with Matchers {
  val e = DataEvent("org.beangle.security.user", "User", Map("id" -> "1,2,3"), DataEventType.Deletion, Instant.now, None)

  describe("DataEvent") {
    it("match") {
      e.isMatch("org.beangle.security.model") should be(false)
      e.isMatch("org.beangle.security") should be(true)
      e.isMatch("org.beangle.security.user") should be(true)
      e.isMatch("org.beangle.security.u") should be(false)
    }
    it("serialize") {
      val json = e.toJson
      val serializer = new DataEventSerializer()
      val e2 = serializer.fromJson(json)
      assert(e2.entityName == e.entityName)
      assert(e2.eventType == e.eventType)
      assert(e2.filters == e.filters)
      assert(UTC.format(java.util.Date.from(e2.updatedAt)) == UTC.format(java.util.Date.from(e.updatedAt)))
      assert(e2.comment == e.comment)
    }
  }

}
