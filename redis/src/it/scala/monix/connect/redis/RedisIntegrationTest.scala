package monix.connect.redis

import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach}
import org.scalatest.matchers.should.Matchers
import io.lettuce.core.RedisClient
import io.lettuce.core.api.StatefulRedisConnection
import monix.eval.Task
import monix.execution.Scheduler.Implicits.global
import monix.reactive.Observable
import org.scalatest.flatspec.AnyFlatSpec

class RedisIntegrationTest
  extends AnyFlatSpec with Matchers with BeforeAndAfterEach with BeforeAndAfterAll with RedisFixture {

  val redisUrl = "redis://localhost:6379"

  implicit val connection: StatefulRedisConnection[String, String] = RedisClient.create(redisUrl).connect()

  s"${RedisString} " should "insert a string into the given key and get its size from redis" in new RedisFixture {
    //given
    val key: K = genRedisKey.sample.get
    val value: String = genRedisValue.sample.get.toString

    //when
    RedisString.set(key, value).runSyncUnsafe()

    //and
    val t: Task[Long] = RedisString.strlen(key)

    //then
    val lenght: Long = t.runSyncUnsafe()
    lenght shouldBe value.length
  }

  s"${RedisHash}" should "insert a single element into a hash and read it back" in new RedisFixture {
    //given
    val key: K = genRedisKey.sample.get
    val field: K = genRedisKey.sample.get
    val value: String = genRedisValue.sample.get.toString

    //when
    RedisHash.hset(key, field, value).runSyncUnsafe()

    //and
    val t: Task[String] = RedisHash.hget(key, field)

    //then
    val r: String = t.runSyncUnsafe()
    r shouldBe value
  }

  s"${RedisList}" should "insert elements into a list and reading back the same elements" in new RedisFixture {
    //given
    val key: K = genRedisKey.sample.get
    val values: List[String] = genRedisValues.sample.get.map(_.toString)

    //when
    RedisList.lpush(key, values: _*).runSyncUnsafe()

    //and
    val ob: Observable[String] = RedisList.lrange(key, 0, values.size)

    //then
    val result: List[String] = ob.toListL.runSyncUnsafe()
    result should contain theSameElementsAs values
  }

  s"${RedisSet}" should "allow to compose nice for comprehensions" in new RedisFixture {
    //given
    val k1: K = genRedisKey.sample.get
    val m1: List[String] = genRedisValues.sample.get.map(_.toString)
    val k2: K = genRedisKey.sample.get
    val m2: List[String] = genRedisValues.sample.get.map(_.toString)
    val k3: K = genRedisKey.sample.get
    println("m1: " + m1)

    //when
    val (size1, size2, moved) = {
      for {
        size1 <- RedisSet.sadd(k1, m1: _*)
        size2 <- RedisSet.sadd(k2, m2: _*)
        _     <- RedisSet.sadd(k3, m1: _*)
        moved <- RedisSet.smove(k1, k2, m1.head)
      } yield {
        (size1, size2, moved)
      }
    }.runSyncUnsafe()

    //and
    val (s1, s2, union, diff) = {
      for {
        s1    <- RedisSet.smembers(k1).toListL
        s2    <- RedisSet.smembers(k2).toListL
        union <- RedisSet.sunion(k1, k2).toListL
        diff  <- RedisSet.sdiff(k3, k1).toListL
      } yield (s1, s2, union, diff)
    }.runSyncUnsafe()

    //then
    size1 shouldBe m1.size
    s1.size shouldEqual (m1.size - 1)
    size2 shouldBe m2.size
    s2.size shouldEqual (m2.size + 1)
    moved shouldBe true
    s1 shouldNot contain theSameElementsAs m1
    s2 shouldNot contain theSameElementsAs m2
    union should contain theSameElementsAs m1 ++ m2
    //although the list are not equal as at the beginning because of the move operation, its union still is the same
    diff should contain theSameElementsAs List(m1.head)
    //the difference between the k3 and k1 is equal to the element that was moved
  }

  s"${Redis}" should "allow to composition of different Redis submodules" in new RedisFixture {
    //given
    val k1: K = genRedisKey.sample.get
    val value: String= genRedisValue.sample.get.toString
    val k2: K = genRedisKey.sample.get
    val values: List[String] = genRedisValues.sample.get.map(_.toString)
    val k3: K = genRedisKey.sample.get

   val (v: String, len: Long, l: List[String], keys: List[String]) = {
       for {
       _ <- Redis.flushall()
       _ <- RedisKey.touch(k1)
       _ <- RedisString.set(k1, value)
       _ <- RedisKey.rename(k1, k2)
       _ <- RedisList.lpush(k3, values: _*)
       v <- RedisString.get(k2): Task[String]
       _ <- RedisList.lpushx(k3, v)
       _ <- RedisKey.del(k2)
       len <- RedisList.llen(k3): Task[Long]
       l <- RedisList.lrange(k3, 0, len).toListL
       keys <- Redis.keys("*").toListL
     } yield (v, len, l, keys)
   }.runSyncUnsafe()

    v shouldBe value
    len shouldBe values.size + 1
    l should contain theSameElementsAs value :: values
    keys.size shouldBe 1
    keys.head shouldBe k3
  }

}
