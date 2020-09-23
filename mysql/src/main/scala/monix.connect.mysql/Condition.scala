/*
 * Copyright (c) 2020-2020 by The Monix Connect Project Developers.
 * See the project homepage at: https://connect.monix.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package monix.connect.mysql

import java.util.function.BiFunction

import io.r2dbc.spi.{ConnectionFactory, Row, RowMetadata}
import monix.connect.mysql.moni.Person
import monix.eval.Task

import scala.compat.java8.FunctionConverters._

class Condition(select: Select, val condition: String) {

  self =>

  def groupBy(fields: String*): Group = {
    Group(select, self, fields: _*)
  }

  def generateStatement() = {
    select.statement ++ " " ++ condition
  }

  def biFunction: BiFunction[Row, RowMetadata, Person] = {
    val f: ((Row, RowMetadata) => Person) =
      (row, rowMetadata) => {
        val idMetadata = rowMetadata.getColumnMetadata("id").getJavaType
        val firstNameMetadata = rowMetadata.getColumnMetadata("first_name").getJavaType
        Person(row.get("id", classOf[Int]), row.get("first_name", classOf[String]))
      }
    f.asJava
  }

  def run(cf: ConnectionFactory) = {
    for {
      conn <- Task.fromReactivePublisher(cf.create())
      result <- Task.fromReactivePublisher {
        conn.get.createStatement(generateStatement()).execute()
      }
      r <- Task.fromReactivePublisher { result.get.map(biFunction) }.map(_.get)
    } yield r
  }

}

class EmptyCondition(val condition: String) {}

object Condition {

  private[mysql] def apply(select: Select, condition: String): Condition = {
    new Condition(select, condition)
  }

  def apply(condition: String): EmptyCondition = {
    new EmptyCondition(condition)
  }

}
