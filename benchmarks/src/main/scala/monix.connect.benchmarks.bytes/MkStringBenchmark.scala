package monix.connect.benchmarks.bytes

import java.util.concurrent.TimeUnit

import akka.util.ByteString
import monix.execution.Scheduler.Implicits.global
import monix.reactive.Observable
import org.openjdk.jmh.annotations._
import zio.Chunk

@State(Scope.Thread)
@BenchmarkMode(Array(Mode.Throughput))
@OutputTimeUnit(TimeUnit.SECONDS)
@Measurement(iterations = 2)
@Warmup(iterations = 1)
@Fork(1)
@Threads(1)
class MkStringBenchmark {

  @Param(Array("500"))
  var size: Int = _

  var array: Array[Byte] = _
  var chunk: Chunk[Byte] = _
  var byteString: ByteString = _
  var observable: Observable[Byte] = _

  @Setup(Level.Trial)
  def setup(): Unit = {
    array = (1 to size).flatMap(_.toString.getBytes).toArray
    chunk = Chunk.fromArray(array)
    byteString = ByteString.fromArray(array)
    observable = Observable.fromIterable(array)
  }

  //mkstring
  @Benchmark
  def arrayMkString: String = array.mkString

  @Benchmark
  def chunkMkString: String = chunk.mkString

  @Benchmark
  def bSMkString: String = byteString.mkString

}
