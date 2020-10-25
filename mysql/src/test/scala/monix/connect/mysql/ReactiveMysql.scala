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

import dev.miku.r2dbc.mysql.{MySqlConnection, MySqlConnectionConfiguration, MySqlConnectionFactory}
import dev.miku.r2dbc.mysql.constant.ZeroDateOption
import io.r2dbc.spi.{Connection, ConnectionFactory, Result}
import monix.connect.mysql.mysql.{Person, Query}
import monix.eval.Task
import org.reactivestreams.Publisher
import org.scalacheck.Gen

trait ReactiveMysql {


  val conf: MySqlConnectionConfiguration = MySqlConnectionConfiguration
    .builder()
    .host("127.0.0.1")
    .user("user")
    .port(3306) // optional, default 3306
    .password("pasword") // optional, default null, null means has no password
    .database("test") // optional, default null, null means not specifying the database
    .zeroDateOption(ZeroDateOption.USE_NULL) // optional, default ZeroDateOption.USE_NULL
    .useServerPrepareStatement() // Use server-preparing statements, default use client-preparing statements
    .tcpKeepAlive(true) // optional, controls TCP Keep Alive, default is false
    .tcpNoDelay(true) // optional, controls TCP No Delay, default is false
    .autodetectExtensions(false) // optional, controls extension auto-detect, default is true
    .build();

  val connection: Task[MySqlConnection] = Task.fromReactivePublisher(MySqlConnectionFactory.from(conf).create())
    .map(_.get)
    .memoize

  val genPerson = {
    for {
    uuid <- Gen.uuid
    name <- Gen.alphaLowerStr
    } yield Person(uuid.toString, name)
  }

  def write(tableName: String, person: Person): Task[Result] = {

    connection.flatMap { conn =>
      Query(s"INSERT INTO ${tableName} (id, first_name) VALUES('${person.id}', '${person.fist_name}')").run(conn)
    }
    //connection
    //  .flatMap(conn =>
    //    Task.fromReactivePublisher {
    //      conn.get
    //        .createStatement(s"INSERT INTO ${tableName} (id, first_name) VALUES(${person.age}, '${person.fist_name}')")
    //        .execute()
    //    })
  }


  def createPersonTable(tableName: String): Task[Result] = {
    connection.flatMap { conn =>
      Query(s"CREATE TABLE IF NOT EXISTS ${tableName} (id varchar(100) primary key, first_name varchar(100))").run(conn)
    }
  }

  def select(tableName: String, id: String): Task[Person] =
    connection.flatMap { conn =>
      Select(s"SELECT id, first_name from ${tableName}").where(s"WHERE id='${id}'").run(conn)

    }
      //connection
    //  .flatMap(conn =>
    //    Task.fromReactivePublisher(
    //      conn.get
    //        .createStatement(s"CREATE TABLE IF NOT EXISTS ${tableName} (id int primary key, first_name varchar(100))")
    //        .execute()))
}
