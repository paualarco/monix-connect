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
@Measurement(iterations = 5)
@Warmup(iterations = 1)
@Fork(1)
@Threads(1)
class FindBenchmark {

  @Param(Array("500"))
  var size: Int = _

  var array: Array[Byte] = (1 to size).flatMap(_.toString.getBytes).toArray
  var chunk: Chunk[Byte] = Chunk.fromArray(array)
  var byteString: ByteString = ByteString.fromArray(array)

  @Benchmark
  def arrayFind: Option[Byte] = array.find(b => b == array.last)

  @Benchmark
  def chunkFind: Option[Byte] = chunk.find(b => b == chunk.last)

  @Benchmark
  def bSFind: Option[Byte] = byteString.find(b => b == byteString.last)

}
