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

import java.util.concurrent.TimeUnit

import dev.miku.r2dbc.mysql.constant.ZeroDateOption
import dev.miku.r2dbc.mysql.{MySqlConnectionConfiguration, MySqlConnectionFactory}
import io.r2dbc.spi.ConnectionFactory
import monix.connect.mysql.moni.Query
import monix.eval.Task
import monix.execution.Scheduler
import org.openjdk.jmh.annotations._

import scala.concurrent.Await
import scala.concurrent.duration.Duration
@State(Scope.Thread)
@BenchmarkMode(Array(Mode.Throughput))
@OutputTimeUnit(TimeUnit.SECONDS)
@Measurement(iterations = 2)
@Warmup(iterations = 1)
@Fork(2)
@Threads(2)
class BenchmarkTest {

  val connectionFactory: ConnectionFactory = MySqlConnectionFactory.from(conf)
  val connection = Task.fromReactivePublisher(connectionFactory.create)

  val conf = MySqlConnectionConfiguration
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

  implicit val scheduler: Scheduler = Scheduler.io()
  var size: Int = 5

  //val nonEmptyString: Gen[String] = Gen.nonEmptyListOf(Gen.alphaChar).map(chars => "test-" + chars.mkString.take(20))
  Query("CREATE TABLE IF NOT EXISTS person_1 (id varchar(100) primary key, first_name varchar(100))").run(
    connectionFactory)

  //syntax (P, P, RF) === (Parallelism factor, Partitions, Replication Factor)
  @BenchmarkTest
  def monixSink_1P_1RF(): Unit = {
    val id = "nonEmptyString.sample.get"
    val first_name = "nonEmptyString.sample.get"

    val f = for {
      insert <- Query(s"INSERT INTO person_1 (id, first_name) VALUES('${id}', '${first_name}')").run(connectionFactory)
      select <- Select("SELECT id, first_name from person_1").where(s"WHERE id='${id}'").run(connectionFactory)
    } yield select
    Await.result(f.runToFuture(scheduler), Duration.Inf)
  }

}
