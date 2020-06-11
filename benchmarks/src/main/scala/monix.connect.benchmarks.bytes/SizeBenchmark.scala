package monix.connect.benchmarks.bytes

import java.util.concurrent.TimeUnit

import akka.util.ByteString
import org.openjdk.jmh.annotations._
import zio.Chunk

@State(Scope.Thread)
@BenchmarkMode(Array(Mode.Throughput))
@OutputTimeUnit(TimeUnit.SECONDS)
@Measurement(iterations = 2)
@Warmup(iterations = 1)
@Fork(1)
@Threads(1)
class SizeBenchmark {

  @Param(Array("500"))
  var size: Int = _

  var array: Array[Byte] = (1 to size).flatMap(_.toString.getBytes).toArray
  var chunk: Chunk[Byte] = Chunk.fromArray(array)
  var byteString: ByteString = ByteString.fromArray(array)

  val byte = Byte.MaxValue
  //take
  @Benchmark
  def arrayTake: Unit = array.size

  @Benchmark
  def chunkTake: Int = chunk.size

  @Benchmark
  def bSTake: Int = byteString.size

}
