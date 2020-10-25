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
import org.scalacheck.Gen
import org.scalatest.BeforeAndAfterAll

import scala.collection.immutable
import scala.compat.java8.FunctionConverters._
import scala.concurrent.Await
import scala.concurrent.duration._


class Test extends AnyFlatSpec with Matchers with ReactiveMysql with BeforeAndAfterAll {

  val tableName = "my_table2"
  createPersonTable(tableName).runSyncUnsafe()

  "Mysql r2dbc" should "insert into a table" in {
    val person = genPerson.sample.get
    write(tableName, person).runSyncUnsafe()
    val resultPerson = select(tableName, person.id).runSyncUnsafe()
    person shouldBe resultPerson
  }

  it should "insert and read in parallel" in {
    val persons: Seq[mysql.Person] = Gen.listOfN(6, genPerson).sample.get

    val f = (for {
      _ <- Task.parSequence(persons.map(write(tableName, _)))
      result <- Task.parSequence(persons.map(person => select(tableName, person.id)))
    } yield result).runToFuture


    val r = Await.result(f, 90.seconds)
    println("R: " + r.mkString)
    r should contain theSameElementsAs persons
  }

  override def afterAll() = {
    super.afterAll()
    connection.map(_.close()).runToFuture
  }


 //it should "read from a table" in {
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

}
