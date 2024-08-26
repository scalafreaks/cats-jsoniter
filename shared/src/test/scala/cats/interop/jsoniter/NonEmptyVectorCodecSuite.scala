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

import cats.data.NonEmptyVector
import com.github.plokhotnyuk.jsoniter_scala.core.*
import com.github.plokhotnyuk.jsoniter_scala.macros.*
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.*

class NonEmptyVectorCodecSuite extends AnyFunSuite with Matchers {

  case class Nev(value: NonEmptyVector[String]) derives ConfiguredJsonValueCodec

  test("decoding should fail for JSON null") {
    val thrown = the[JsonReaderException] thrownBy readFromString[Nev]("""{"value":null}""")
    thrown.getMessage should startWith("cannot create NonEmptyVector without elements")
  }

  test("decoding should fail for JSON empty array") {
    val thrown = the[JsonReaderException] thrownBy readFromString[Nev]("""{"value":[]}""")
    thrown.getMessage should startWith("cannot create NonEmptyVector without elements")
  }

  test("decoding should succeed for JSON array with elements") {
    val result = readFromString[Nev]("""{"value":["a"]}""")
    result shouldBe Nev(NonEmptyVector.one("a"))
  }

  test("encoding should fail for null") {
    an[NullPointerException] should be thrownBy writeToString(Nev(null.asInstanceOf[NonEmptyVector[String]]))
  }

  test("encoding should succeed for non-null") {
    writeToString(Nev(NonEmptyVector.one("a"))) shouldBe """{"value":["a"]}"""
  }

}
