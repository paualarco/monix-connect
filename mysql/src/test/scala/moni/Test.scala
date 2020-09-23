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

package moni

import java.time.{Duration, ZoneId}
import java.util.function.BiFunction

import dev.miku.r2dbc.mysql.{MySqlConnectionConfiguration, MySqlConnectionFactory}
import dev.miku.r2dbc.mysql.constant.{SslMode, TlsVersions, ZeroDateOption}
import io.r2dbc.spi.{Connection, ConnectionFactory, Row, RowMetadata}
import monix.eval.Task
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import monix.execution.Scheduler.Implicits.global
import monix.reactive.Observable

import scala.compat.java8.FunctionConverters._

class Test extends AnyFlatSpec with Matchers with ReactiveMysql {

  //"Mysql r2dbc" should "create a table" in {

  //  connection
  //    .flatMap(conn =>
  //      Task.fromReactivePublisher(
  //        conn.get
  //          .createStatement("CREATE TABLE IF NOT EXISTS person2 (id int primary key, first_name varchar(100))")
  //          .execute()))
  //    .runSyncUnsafe()
  //}

  //it should "insert into a table" in {
  //  connection
  //    .flatMap(conn =>
  //      Task.fromReactivePublisher {
  //        conn.get
  //          .createStatement("INSERT INTO person2 (id, first_name) VALUES(4, 'Walter')")
  //          .execute()
  //      })
  //    .runSyncUnsafe()

  //  val r = connection
  //    .flatMap(conn =>
  //      Task.fromReactivePublisher {
  //        conn.get.createStatement("SELECT id, first_name from person2").execute()
  //      }.flatMap(result =>
  //        Task.fromReactivePublisher {
  //          result.get.map {
  //            val f: ((Row, RowMetadata) => Person) =
  //              (row, rowMetadata) => {
  //                val idMetadata = rowMetadata.getColumnMetadata("id").getJavaType
  //                val firstNameMetadata = rowMetadata.getColumnMetadata("first_name").getJavaType
  //                println(idMetadata)
  //                println(firstNameMetadata)
  //                Person(row.get("id", classOf[Int]), row.get("first_name", classOf[String]))
  //              }

  //            f.asJava
  //          }
  //        }))
  //    .runSyncUnsafe()

  //  println("R: " + r)
  //}

  //it should "use the new dsl" in {
  //  val s = Select("SELECT id, first_name from person2").where("WHERE id = 1").run(connectionFactory).runSyncUnsafe()
  //  println("Result: " + s)
  //}

}
