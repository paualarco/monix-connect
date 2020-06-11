package monix.connect.benchmarks.bytes

import java.util.concurrent.TimeUnit

import akka.util.ByteString
import org.openjdk.jmh.annotations._
import zio.Chunk

import scala.collection.immutable

@State(Scope.Thread)
@BenchmarkMode(Array(Mode.Throughput))
@OutputTimeUnit(TimeUnit.SECONDS)
@Measurement(iterations = 5)
@Warmup(iterations = 1)
@Fork(1)
@Threads(1)
class CollectBenchmark {

  @Param(Array("500"))
  var size: Int = _

  var array: Array[Byte] = (1 to size).flatMap(_.toString.getBytes).toArray
  var chunk: Chunk[Byte] = Chunk.fromArray(array)
  var byteString: ByteString = ByteString.fromArray(array)

  //filter
  @Benchmark
  def arrayZip: Array[Byte] = array.collect { case b: Byte => b }

  @Benchmark
  def chunkZip: Chunk[Byte] = chunk.collect { case b: Byte => b }

  @Benchmark
  def bSZip: ByteString = byteString.collect { case b: Byte => b }

}
