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

import cats.data.NonEmptySet
import com.github.plokhotnyuk.jsoniter_scala.core.*
import com.github.plokhotnyuk.jsoniter_scala.macros.*
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.*

class NonEmptySetCodecSuite extends AnyFunSuite with Matchers {

  case class Nes(value: NonEmptySet[String]) derives ConfiguredJsonValueCodec

  test("decoding should fail for JSON null") {
    val thrown = the[JsonReaderException] thrownBy readFromString[Nes]("""{"value":null}""")
    thrown.getMessage should startWith("cannot create NonEmptySet without elements")
  }

  test("decoding should fail for JSON empty array") {
    val thrown = the[JsonReaderException] thrownBy readFromString[Nes]("""{"value":[]}""")
    thrown.getMessage should startWith("cannot create NonEmptySet without elements")
  }

  test("decoding should succeed for JSON array with elements") {
    val result = readFromString[Nes]("""{"value":["a"]}""")
    result shouldBe Nes(NonEmptySet.one("a"))
  }

  test("encoding should fail for null") {
    an[NullPointerException] should be thrownBy writeToString(Nes(null.asInstanceOf[NonEmptySet[String]]))
  }

  test("encoding should succeed for non-null") {
    writeToString(Nes(NonEmptySet.one("a"))) shouldBe """{"value":["a"]}"""
  }

  test("compilation fails for elements A without given Ordering[A]") {
    """sealed trait Sealed
      |case object A            extends Sealed
      |case class B(value: Int) extends Sealed
      |
      |case class NesOrder(value: NonEmptySet[Sealed]) derives ConfiguredJsonValueCodec
      |""".stripMargin shouldNot compile
  }

  test("compilation succeeds for elements A with given Ordering[A]") {
    """sealed trait Sealed
      |case object A            extends Sealed
      |case class B(value: Int) extends Sealed
      |
      |given Ordering[Sealed] = Ordering.by {
      |  case A    => 1
      |  case B(_) => 2
      |}
      |
      |case class NesOrder(value: NonEmptySet[Sealed]) derives ConfiguredJsonValueCodec
      |""".stripMargin should compile
  }

}
