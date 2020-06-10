/*
 * Copyright (c) 2020-2020 by The Monix Connect Project Developers.
 * See the project homepage at: https://monix.io
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

package scalapb.parquet

import java.util

import scalapb.{GeneratedMessage, GeneratedMessageCompanion, Message}
import org.apache.hadoop.conf.Configuration
import org.apache.parquet.hadoop.api.{InitContext, ReadSupport}
import org.apache.parquet.hadoop.api.ReadSupport.ReadContext
import org.apache.parquet.io.api.{GroupConverter, RecordMaterializer}
import org.apache.parquet.schema.MessageType

class ScalaPBReadSupport[T <: GeneratedMessage with Message[T]] extends ReadSupport[T] {
  override def prepareForRead(
    configuration: Configuration,
    keyValueMetaData: util.Map[String, String],
    fileSchema: MessageType,
    readContext: ReadContext): RecordMaterializer[T] = {
    println("Key value metadata: " + keyValueMetaData.toString)
    val protoClass =
      Option(keyValueMetaData.get(ScalaPBReadSupport.PB_CLASS))
        .getOrElse(throw new RuntimeException(s"Value for ${ScalaPBReadSupport.PB_CLASS} not found."))
    val cmp: GeneratedMessageCompanion[T] = {
      import scala.reflect.runtime.universe

      val runtimeMirror = universe.runtimeMirror(getClass.getClassLoader)

      val module = runtimeMirror.staticModule(protoClass)

      val obj = runtimeMirror.reflectModule(module)

      obj.instance.asInstanceOf[GeneratedMessageCompanion[T]]
    }

    new RecordMaterializer[T] {
      val root = new ProtoMessageConverter[T](cmp, fileSchema, onEnd = _ => ())

      override def getRootConverter: GroupConverter = root

      override def getCurrentRecord: T = root.getCurrentRecord
    }
  }

  override def init(context: InitContext): ReadContext = {
    new ReadContext(context.getFileSchema)
  }
}

object ScalaPBReadSupport {
  val PB_CLASS = "parquet.scalapb.class"
}
