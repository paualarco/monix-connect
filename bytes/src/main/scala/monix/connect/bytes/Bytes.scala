package monix.connect.bytes

import scala.collection.IndexedSeqOptimized


trait BytesLike[T] extends IndexedSeq[T] with IndexedSeqOptimized[T, Bytes[T]]{



}

object Bytes {

  def apply(bytes: Array[Byte]): Bytes = (bytes) = {
    if (bytes.isEmpty)
      bytes ++
  }



}

class Bytes[T] {

}





