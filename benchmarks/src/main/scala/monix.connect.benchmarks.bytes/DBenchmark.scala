package monix.connect.benchmarks.bytes

import java.util.concurrent.TimeUnit

import akka.util.ByteString
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
class DBenchmark {

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

  //take
  @Benchmark
  def arrayMkString: Array[Byte] = array.drop(size / 2).drop(size / 2)

  @Benchmark
  def chunkMkString: Chunk[Byte] = chunk.drop(size / 2).drop(size / 2)

  @Benchmark
  def bSMkString: ByteString = byteString.drop(size / 2).drop(size / 2)

}
