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

import scala.compat.java8.FunctionConverters._

class Test extends AnyFlatSpec with Matchers {

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

  val connectionFactory: ConnectionFactory = MySqlConnectionFactory.from(conf)
  val connection = Task.fromReactivePublisher(connectionFactory.create)
  "Mysql r2dbc" should "create a table" in {

    connection
      .flatMap(conn =>
        Task.fromReactivePublisher(
          conn.get
            .createStatement("CREATE TABLE IF NOT EXISTS person2 (id int primary key, first_name varchar(100))")
            .execute()))
      .runSyncUnsafe()
  }

  it should "insert into a table" in {
    connection
      .flatMap(conn =>
        Task.fromReactivePublisher {
          conn.get
            .createStatement("INSERT INTO person2 (id, first_name) VALUES(2, 'Walter')")
            .execute()
        })
      .runSyncUnsafe()

    case class Person(id: Int, name: String)


    val r = connection
      .flatMap(conn =>
        Task.fromReactivePublisher {
          conn.get.createStatement("SELECT id, first_name from person2").execute()
        }.flatMap(result =>
            Task.fromReactivePublisher {
              result.get.map {
                val f: ((Row, RowMetadata) => Person) =
                  (row, rowMetadata) => Person(row.get("id", classOf[Int]), row.get("first_name", classOf[String]))
                f.asJava
              }
            }))
      .runSyncUnsafe()

    println("R: " + r)
  }

}
