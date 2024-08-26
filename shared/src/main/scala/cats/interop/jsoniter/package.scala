/*
 * Copyright 2024 ScalaFreaks
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

package cats.interop.jsoniter

import scala.collection.immutable.SortedSet

import cats.data.{NonEmptyList, NonEmptySet, NonEmptyVector}
import com.github.plokhotnyuk.jsoniter_scala.core.{JsonReader, JsonValueCodec, JsonWriter}
import com.github.plokhotnyuk.jsoniter_scala.macros.{CodecMakerConfig, JsonCodecMaker}

inline given nonEmptyListCodec[A](
    using inline config: CodecMakerConfig = CodecMakerConfig
): JsonValueCodec[NonEmptyList[A]] = jsonValueCodec("NonEmptyList", NonEmptyList.fromList[A], _.toList)

inline given nonEmptySetCodec[A](
    using inline config: CodecMakerConfig = CodecMakerConfig
): JsonValueCodec[NonEmptySet[A]] = jsonValueCodec("NonEmptySet", NonEmptySet.fromSet[A], _.toSortedSet)

inline given nonEmptyVectorCodec[A](
    using inline config: CodecMakerConfig = CodecMakerConfig
): JsonValueCodec[NonEmptyVector[A]] = jsonValueCodec("NonEmptyVector", NonEmptyVector.fromVector[A], _.toVector)

private inline def jsonValueCodec[A, B[_], C[_]](name: String, from: B[A] => Option[C[A]], to: C[A] => B[A])(
    using inline config: CodecMakerConfig
): JsonValueCodec[C[A]] = new JsonValueCodec[C[A]] {

  private val codec = JsonCodecMaker.make[B[A]](config)

  override def decodeValue(in: JsonReader, default: C[A]): C[A] = {
    val decoded = codec.decodeValue(in, if default == nullValue then codec.nullValue else to(default))
    from(decoded) match {
      case Some(value) => value
      case None        => in.decodeError(s"cannot create $name without elements")
    }
  }

  override def encodeValue(x: C[A], out: JsonWriter): Unit = codec.encodeValue(to(x), out)

  override def nullValue: C[A] = null.asInstanceOf[C[A]]

}
