package monix.connect.bytes

import scala.collection.IndexedSeqOptimized


class Bytes[T] extends IndexedSeq[T] with IndexedSeqOptimized[T, Bytes[T]]{

  /**
    * The number of elements in the chunk.
    */
  override final def size: Int =
    length
}











