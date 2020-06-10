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

import com.google.protobuf.ByteString
import com.google.protobuf.Descriptors.FieldDescriptor
import com.google.protobuf.Descriptors.FieldDescriptor.JavaType
import scalapb.{GeneratedMessage, GeneratedMessageCompanion, Message}
import org.apache.parquet.io.api.{Binary, Converter, GroupConverter, PrimitiveConverter}
import org.apache.parquet.schema.GroupType

import scala.collection.JavaConverters._
import scala.language.existentials

class ProtoMessageConverter[T <: GeneratedMessage with Message[T]](
  cmp: GeneratedMessageCompanion[T],
  schema: GroupType,
  onEnd: T => Unit)
  extends GroupConverter {
  val fields: scala.collection.mutable.Map[FieldDescriptor, Any] =
    scala.collection.mutable.Map[FieldDescriptor, Any]()

  val converters = schema.getFields.asScala.map { t =>
    val fd = cmp.javaDescriptor.findFieldByName(t.getName)
    val e: (Any) => Unit = if (fd.isRepeated) addValue(fd) else setValue(fd)
    fd.getJavaType match {
      case JavaType.MESSAGE =>
        new ProtoMessageConverter(
          cmp
            .messageCompanionForField(fd)
            .asInstanceOf[GeneratedMessageCompanion[X] forSome {
              type X <: GeneratedMessage with Message[X]
            }],
          t.asGroupType(),
          e)
      case _ =>
        new ProtoPrimitiveConverter(fd, e)
    }
  }

  private def setValue[P](fd: FieldDescriptor)(v: P): Unit = { fields(fd) = v }

  private def addValue[P](fd: FieldDescriptor)(v: P): Unit = {
    fields(fd) = fields.getOrElse(fd, Seq.empty).asInstanceOf[Seq[P]] :+ v
  }

  override def getConverter(fieldIndex: Int): Converter = {
    converters(fieldIndex)
  }

  override def end(): Unit = onEnd(getCurrentRecord)

  override def start(): Unit = {
    fields.clear()
  }

  def getCurrentRecord: T = {
    cmp.fromFieldsMap(fields.toMap)
  }
}

class ProtoPrimitiveConverter(fd: FieldDescriptor, add: Any => Unit) extends PrimitiveConverter {
  override def addFloat(value: Float): Unit = add(value)

  override def addBinary(value: Binary): Unit = {
    if (fd.getJavaType == JavaType.STRING) add(value.toStringUsingUTF8)
    else if (fd.getJavaType == JavaType.ENUM)
      add(fd.getEnumType.findValueByName(value.toStringUsingUTF8))
    else if (fd.getJavaType == JavaType.BYTE_STRING)
      add(ByteString.copyFrom(value.getBytes))
    else throw new RuntimeException("Unexpected type")
  }

  override def addDouble(value: Double): Unit = add(value)

  override def addInt(value: Int): Unit = add(value)

  override def addBoolean(value: Boolean): Unit = add(value)

  override def addLong(value: Long): Unit = add(value)
}
