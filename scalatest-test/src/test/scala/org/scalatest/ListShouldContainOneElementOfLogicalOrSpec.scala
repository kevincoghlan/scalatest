/*
* Copyright 2001-2013 Artima, Inc.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.scalatest

import org.scalactic.Equality
import org.scalactic.Uniformity
import org.scalactic.Prettifier
import org.scalactic.StringNormalizations._
import SharedHelpers._
import FailureMessages.decorateToStringValue
import Matchers._
import exceptions.TestFailedException

class ListShouldContainOneElementOfLogicalOrSpec extends FunSpec {

  private val prettifier = Prettifier.default

  val invertedStringEquality =
    new Equality[String] {
      def areEqual(a: String, b: Any): Boolean = a != b
    }

  val invertedListOfStringEquality =
    new Equality[List[String]] {
      def areEqual(a: List[String], b: Any): Boolean = a != b
    }

  val upperCaseStringEquality =
    new Equality[String] {
      def areEqual(a: String, b: Any): Boolean = a.toUpperCase == b
    }

  val upperCaseListOfStringEquality =
    new Equality[List[String]] {
      def areEqual(a: List[String], b: Any): Boolean = a.map(_.toUpperCase) == b
    }

  private def upperCase(value: Any): Any =
    value match {
      case l: List[_] => l.map(upperCase(_))
      case s: String => s.toUpperCase
      case c: Char => c.toString.toUpperCase.charAt(0)
      case (s1: String, s2: String) => (s1.toUpperCase, s2.toUpperCase)
      case e: java.util.Map.Entry[_, _] =>
        (e.getKey, e.getValue) match {
          case (k: String, v: String) => Entry(k.toUpperCase, v.toUpperCase)
          case _ => value
        }
      case _ => value
    }

  //ADDITIONAL//

  val fileName: String = "ListShouldContainOneElementOfLogicalOrSpec.scala"

  describe("a List") {

    val fumList: List[String] = List("fum")
    val toList: List[String] = List("to")

    describe("when used with (contain oneElementOf (..) or contain oneElementOf (..))") {

      it("should do nothing if valid, else throw a TFE with an appropriate error message") {
        fumList should (contain oneElementOf Seq("fee", "fie", "foe", "fum") or contain oneElementOf Seq("fie", "fee", "fum", "foe"))
        fumList should (contain oneElementOf Seq("fee", "fie", "foe", "fam") or contain oneElementOf Seq("fie", "fee", "fum", "foe"))
        fumList should (contain oneElementOf Seq("fee", "fie", "foe", "fum") or contain oneElementOf Seq("fie", "fee", "fam", "foe"))
        val e1 = intercept[TestFailedException] {
          fumList should (contain oneElementOf Seq("fee", "fie", "foe", "fam") or contain oneElementOf Seq("happy", "birthday", "to", "you"))
        }
        checkMessageStackDepth(e1, FailureMessages.didNotContainOneElementOf(prettifier, fumList, Seq("fee", "fie", "foe", "fam")) + ", and " + FailureMessages.didNotContainOneElementOf(prettifier, fumList, Seq("happy", "birthday", "to", "you")), fileName, thisLineNumber - 2)
      }

      it("should use the implicit Equality in scope") {
        implicit val ise = upperCaseStringEquality
        fumList should (contain oneElementOf Seq("FEE", "FIE", "FOE", "FUM") or contain oneElementOf Seq("FEE", "FIE", "FOE", "FUM"))
        fumList should (contain oneElementOf Seq("FEE", "FIE", "FOE", "FUM") or contain oneElementOf Seq("fie", "fee", "fum", "foe"))
        fumList should (contain oneElementOf Seq("fie", "fee", "fum", "foe") or contain oneElementOf Seq("FEE", "FIE", "FOE", "FUM"))
        val e1 = intercept[TestFailedException] {
          fumList should (contain oneElementOf Seq("fie", "fee", "fum", "foe") or (contain oneElementOf Seq("fie", "fee", "fum", "foe")))
        }
        checkMessageStackDepth(e1, FailureMessages.didNotContainOneElementOf(prettifier, fumList, Seq("fie", "fee", "fum", "foe")) + ", and " + FailureMessages.didNotContainOneElementOf(prettifier, fumList, Seq("fie", "fee", "fum", "foe")), fileName, thisLineNumber - 2)
      }

      it("should use an explicitly provided Equality") {
        (fumList should (contain oneElementOf Seq("FEE", "FIE", "FOE", "FUM") or contain oneElementOf Seq("FEE", "FIE", "FOE", "FUM"))) (decided by upperCaseStringEquality, decided by upperCaseStringEquality)
        (fumList should (contain oneElementOf Seq("FEE", "FIE", "FOE", "FUM") or contain oneElementOf Seq("fie", "fee", "fum", "foe"))) (decided by upperCaseStringEquality, decided by upperCaseStringEquality)
        (fumList should (contain oneElementOf Seq("fie", "fee", "fum", "foe") or contain oneElementOf Seq("FEE", "FIE", "FOE", "FUM"))) (decided by upperCaseStringEquality, decided by upperCaseStringEquality)
        val e1 = intercept[TestFailedException] {
          (fumList should (contain oneElementOf Seq("fie", "fee", "fum", "foe") or contain oneElementOf Seq("fie", "fee", "fum", "foe"))) (decided by upperCaseStringEquality, decided by upperCaseStringEquality)
        }
        checkMessageStackDepth(e1, FailureMessages.didNotContainOneElementOf(prettifier, fumList, Seq("fie", "fee", "fum", "foe")) + ", and " + FailureMessages.didNotContainOneElementOf(prettifier, fumList, Seq("fie", "fee", "fum", "foe")), fileName, thisLineNumber - 2)
        (fumList should (contain oneElementOf Seq(" FEE ", " FIE ", " FOE ", " FUM ") or contain oneElementOf Seq(" FEE ", " FIE ", " FOE ", " FUM "))) (after being lowerCased and trimmed, after being lowerCased and trimmed)
      }

      it("should allow RHS to contain duplicated value") {
        fumList should (contain oneElementOf Seq("fee", "fie", "foe", "fie", "fum") or contain oneElementOf Seq("fie", "fee", "fum", "foe"))
        fumList should (contain oneElementOf Seq("fie", "fee", "fum", "foe") or contain oneElementOf Seq("fee", "fie", "foe", "fie", "fum"))
      }
    }

    describe("when used with (equal (..) and contain oneElementOf (..))") {

      it("should do nothing if valid, else throw a TFE with an appropriate error message") {
        fumList should (equal (fumList) or contain oneElementOf Seq("fie", "fee", "fum", "foe"))
        fumList should (equal (toList) or contain oneElementOf Seq("fie", "fee", "fum", "foe"))
        fumList should (equal (fumList) or contain oneElementOf Seq("FEE", "FIE", "FOE", "FUM"))
        val e1 = intercept[TestFailedException] {
          fumList should (equal (toList) or contain oneElementOf Seq("FEE", "FIE", "FOE", "FUM"))
        }
        checkMessageStackDepth(e1, FailureMessages.didNotEqual(prettifier, fumList, toList) + ", and " + FailureMessages.didNotContainOneElementOf(prettifier, fumList, Seq("FEE", "FIE", "FOE", "FUM")), fileName, thisLineNumber - 2)
      }

      it("should use the implicit Equality in scope") {
        implicit val ise = upperCaseStringEquality
        fumList should (equal (fumList) or contain oneElementOf Seq("FEE", "FIE", "FOE", "FUM"))
        fumList should (equal (toList) or contain oneElementOf Seq("FEE", "FIE", "FOE", "FUM"))
        fumList should (equal (fumList) or contain oneElementOf Seq("fie", "fee", "fum", "foe"))
        val e1 = intercept[TestFailedException] {
          fumList should (equal (toList) or (contain oneElementOf Seq("fie", "fee", "fum", "foe")))
        }
        checkMessageStackDepth(e1, FailureMessages.didNotEqual(prettifier, fumList, toList) + ", and " + FailureMessages.didNotContainOneElementOf(prettifier, fumList, Seq("fie", "fee", "fum", "foe")), fileName, thisLineNumber - 2)
      }

      it("should use an explicitly provided Equality") {
        (fumList should (equal (toList) or contain oneElementOf Seq("FEE", "FIE", "FOE", "FUM"))) (decided by invertedListOfStringEquality, decided by upperCaseStringEquality)
        (fumList should (equal (fumList) or contain oneElementOf Seq("FEE", "FIE", "FOE", "FUM"))) (decided by invertedListOfStringEquality, decided by upperCaseStringEquality)
        (fumList should (equal (toList) or contain oneElementOf Seq("fie", "fee", "fum", "foe"))) (decided by invertedListOfStringEquality, decided by upperCaseStringEquality)
        val e1 = intercept[TestFailedException] {
          (fumList should (equal (fumList) or contain oneElementOf Seq("fie", "fee", "fum", "foe"))) (decided by invertedListOfStringEquality, decided by upperCaseStringEquality)
        }
        checkMessageStackDepth(e1, FailureMessages.didNotEqual(prettifier, fumList, fumList) + ", and " + FailureMessages.didNotContainOneElementOf(prettifier, fumList, Seq("fie", "fee", "fum", "foe")), fileName, thisLineNumber - 2)
        (fumList should (equal (toList) or contain oneElementOf Seq(" FEE ", " FIE ", " FOE ", " FUM "))) (decided by invertedListOfStringEquality, after being lowerCased and trimmed)
      }

      it("should allow RHS to contain duplicated value") {
        fumList should (equal (fumList) or contain oneElementOf Seq("fee", "fie", "foe", "fie", "fum"))
      }
    }

    describe("when used with (be (..) and contain oneElementOf (..))") {

      it("should do nothing if valid, else throw a TFE with an appropriate error message") {
        fumList should (be (fumList) or contain oneElementOf Seq("fie", "fee", "fum", "foe"))
        fumList should (be (toList) or contain oneElementOf Seq("fie", "fee", "fum", "foe"))
        fumList should (be (fumList) or contain oneElementOf Seq("FEE", "FIE", "FOE", "FUM"))
        val e1 = intercept[TestFailedException] {
          fumList should (be (toList) or contain oneElementOf Seq("FEE", "FIE", "FOE", "FUM"))
        }
        checkMessageStackDepth(e1, FailureMessages.wasNotEqualTo(prettifier, fumList, toList) + ", and " + FailureMessages.didNotContainOneElementOf(prettifier, fumList, Seq("FEE", "FIE", "FOE", "FUM")), fileName, thisLineNumber - 2)
      }

      it("should use the implicit Equality in scope") {
        implicit val ise = upperCaseStringEquality
        fumList should (be (fumList) or contain oneElementOf Seq("FEE", "FIE", "FOE", "FUM"))
        fumList should (be (toList) or contain oneElementOf Seq("FEE", "FIE", "FOE", "FUM"))
        fumList should (be (fumList) or contain oneElementOf Seq("fie", "fee", "fum", "foe"))
        val e1 = intercept[TestFailedException] {
          fumList should (be (toList) or (contain oneElementOf Seq("fie", "fee", "fum", "foe")))
        }
        checkMessageStackDepth(e1, FailureMessages.wasNotEqualTo(prettifier, fumList, toList) + ", and " + FailureMessages.didNotContainOneElementOf(prettifier, fumList, Seq("fie", "fee", "fum", "foe")), fileName, thisLineNumber - 2)
      }

      it("should use an explicitly provided Equality") {
        (fumList should (be (fumList) or contain oneElementOf Seq("FEE", "FIE", "FOE", "FUM"))) (decided by upperCaseStringEquality)
        (fumList should (be (toList) or contain oneElementOf Seq("FEE", "FIE", "FOE", "FUM"))) (decided by upperCaseStringEquality)
        (fumList should (be (fumList) or contain oneElementOf Seq("fie", "fee", "fum", "foe"))) (decided by upperCaseStringEquality)
        val e1 = intercept[TestFailedException] {
          (fumList should (be (toList) or contain oneElementOf Seq("fie", "fee", "fum", "foe"))) (decided by upperCaseStringEquality)
        }
        checkMessageStackDepth(e1, FailureMessages.wasNotEqualTo(prettifier, fumList, toList) + ", and " + FailureMessages.didNotContainOneElementOf(prettifier, fumList, Seq("fie", "fee", "fum", "foe")), fileName, thisLineNumber - 2)
        (fumList should (be (fumList) or contain oneElementOf Seq(" FEE ", " FIE ", " FOE ", " FUM "))) (after being lowerCased and trimmed)
      }

      it("should allow RHS to contain duplicated value") {
        fumList should (be (fumList) or contain oneElementOf Seq("fee", "fie", "foe", "fie", "fum"))
      }
    }

    describe("when used with (contain oneElementOf (..) and be (..))") {

      it("should do nothing if valid, else throw a TFE with an appropriate error message") {
        fumList should (contain oneElementOf Seq("fie", "fee", "fum", "foe") or be (fumList))
        fumList should (contain oneElementOf Seq("FEE", "FIE", "FOE", "FUM") or be (fumList))
        fumList should (contain oneElementOf Seq("fie", "fee", "fum", "foe") or be (toList))
        val e1 = intercept[TestFailedException] {
          fumList should (contain oneElementOf Seq("FEE", "FIE", "FOE", "FUM") or be (toList))
        }
        checkMessageStackDepth(e1, FailureMessages.didNotContainOneElementOf(prettifier, fumList, Seq("FEE", "FIE", "FOE", "FUM")) + ", and " + FailureMessages.wasNotEqualTo(prettifier, fumList, toList), fileName, thisLineNumber - 2)
      }

      it("should use the implicit Equality in scope") {
        implicit val ise = upperCaseStringEquality
        fumList should (contain oneElementOf Seq("FEE", "FIE", "FOE", "FUM") or be (fumList))
        fumList should (contain oneElementOf Seq("fie", "fee", "fum", "foe") or be (fumList))
        fumList should (contain oneElementOf Seq("FEE", "FIE", "FOE", "FUM") or be (toList))
        val e1 = intercept[TestFailedException] {
          fumList should (contain oneElementOf Seq("fie", "fee", "fum", "foe") or be (toList))
        }
        checkMessageStackDepth(e1, FailureMessages.didNotContainOneElementOf(prettifier, fumList, Seq("fie", "fee", "fum", "foe")) + ", and " + FailureMessages.wasNotEqualTo(prettifier, fumList, toList), fileName, thisLineNumber - 2)
      }

      it("should use an explicitly provided Equality") {
        (fumList should (contain oneElementOf Seq("FEE", "FIE", "FOE", "FUM") or be (fumList))) (decided by upperCaseStringEquality)
        (fumList should (contain oneElementOf Seq("fie", "fee", "fum", "foe") or be (fumList))) (decided by upperCaseStringEquality)
        (fumList should (contain oneElementOf Seq("FEE", "FIE", "FOE", "FUM") or be (toList))) (decided by upperCaseStringEquality)
        val e1 = intercept[TestFailedException] {
          (fumList should (contain oneElementOf Seq("fie", "fee", "fum", "foe") or be (toList))) (decided by upperCaseStringEquality)
        }
        checkMessageStackDepth(e1, FailureMessages.didNotContainOneElementOf(prettifier, fumList, Seq("fie", "fee", "fum", "foe")) + ", and " + FailureMessages.wasNotEqualTo(prettifier, fumList, toList), fileName, thisLineNumber - 2)
        (fumList should (contain oneElementOf Seq(" FEE ", " FIE ", " FOE ", " FUM ") or be (fumList))) (after being lowerCased and trimmed)
      }

      it("should allow RHS to contain duplicated value") {
        fumList should (contain oneElementOf Seq("fee", "fie", "foe", "fie", "fum") or be (fumList))
      }
    }

    describe("when used with (not contain oneElementOf (..) and not contain oneElementOf (..))") {

      it("should do nothing if valid, else throw a TFE with an appropriate error message") {
        fumList should (not contain oneElementOf (Seq("FEE", "FIE", "FOE", "FUM")) or not contain oneElementOf (Seq("FEE", "FIE", "FOE", "FUM")))
        fumList should (not contain oneElementOf (Seq("fee", "fie", "foe", "fum")) or not contain oneElementOf (Seq("FEE", "FIE", "FOE", "FUM")))
        fumList should (not contain oneElementOf (Seq("FEE", "FIE", "FOE", "FUM")) or not contain oneElementOf (Seq("fie", "fee", "fum", "foe")))
        val e1 = intercept[TestFailedException] {
          fumList should (not contain oneElementOf (Seq("fee", "fie", "foe", "fum")) or not contain oneElementOf (Seq("fee", "fie", "foe", "fum")))
        }
        checkMessageStackDepth(e1, FailureMessages.containedOneElementOf(prettifier, fumList, Seq("fee", "fie", "foe", "fum")) + ", and " + FailureMessages.containedOneElementOf(prettifier, fumList, Seq("fee", "fie", "foe", "fum")), fileName, thisLineNumber - 2)
      }

      it("should use the implicit Equality in scope") {
        implicit val ise = upperCaseStringEquality
        fumList should (not contain oneElementOf (Seq("fee", "fie", "foe", "fum")) or not contain oneElementOf (Seq("fee", "fie", "foe", "fum")))
        fumList should (not contain oneElementOf (Seq("fee", "fie", "foe", "fum")) or not contain oneElementOf (Seq("FEE", "FIE", "FOE", "FUM")))
        fumList should (not contain oneElementOf (Seq("FEE", "FIE", "FOE", "FUM")) or not contain oneElementOf (Seq("fee", "fie", "foe", "fum")))
        val e1 = intercept[TestFailedException] {
          fumList should (not contain oneElementOf (Seq("FEE", "FIE", "FOE", "FUM")) or not contain oneElementOf (Seq("FEE", "FIE", "FOE", "FUM")))
        }
        checkMessageStackDepth(e1, FailureMessages.containedOneElementOf(prettifier, fumList, Seq("FEE", "FIE", "FOE", "FUM")) + ", and " + FailureMessages.containedOneElementOf(prettifier, fumList, Seq("FEE", "FIE", "FOE", "FUM")), fileName, thisLineNumber - 2)
      }

      it("should use an explicitly provided Equality") {
        (fumList should (not contain oneElementOf (Seq("fee", "fie", "foe", "fum")) or not contain oneElementOf (Seq("fee", "fie", "foe", "fum")))) (decided by upperCaseStringEquality, decided by upperCaseStringEquality)
        (fumList should (not contain oneElementOf (Seq("fee", "fie", "foe", "fum")) or not contain oneElementOf (Seq("FEE", "FIE", "FOE", "FUM")))) (decided by upperCaseStringEquality, decided by upperCaseStringEquality)
        (fumList should (not contain oneElementOf (Seq("FEE", "FIE", "FOE", "FUM")) or not contain oneElementOf (Seq("fee", "fie", "foe", "fum")))) (decided by upperCaseStringEquality, decided by upperCaseStringEquality)
        val e1 = intercept[TestFailedException] {
          (fumList should (not contain oneElementOf (Seq("FEE", "FIE", "FOE", "FUM")) or not contain oneElementOf (Seq("FEE", "FIE", "FOE", "FUM")))) (decided by upperCaseStringEquality, decided by upperCaseStringEquality)
        }
        checkMessageStackDepth(e1, FailureMessages.containedOneElementOf(prettifier, fumList, Seq("FEE", "FIE", "FOE", "FUM")) + ", and " + FailureMessages.containedOneElementOf(prettifier, fumList, Seq("FEE", "FIE", "FOE", "FUM")), fileName, thisLineNumber - 2)
        (fumList should (contain oneElementOf Seq(" FEE ", " FIE ", " FOE ", " FUM ") or contain oneElementOf Seq(" FEE ", " FIE ", " FOE ", " FUM "))) (after being lowerCased and trimmed, after being lowerCased and trimmed)
      }

      it("should allow RHS to contain duplicated value") {
        fumList should (not contain oneElementOf (Seq("fee", "fie", "foe", "fie", "fum")) or not contain oneElementOf (Seq("FEE", "FIE", "FOE", "FUM")))
        fumList should (not contain oneElementOf (Seq("FEE", "FIE", "FOE", "FUM")) or not contain oneElementOf (Seq("fee", "fie", "foe", "fie", "fum")))
      }
    }

    describe("when used with (not equal (..) and not contain oneElementOf (..))") {

      it("should do nothing if valid, else throw a TFE with an appropriate error message") {
        fumList should (not equal (toList) or not contain oneElementOf (Seq("FEE", "FIE", "FOE", "FUM")))
        fumList should (not equal (fumList) or not contain oneElementOf (Seq("FEE", "FIE", "FOE", "FUM")))
        fumList should (not equal (toList) or not contain oneElementOf (Seq("fie", "fee", "fum", "foe")))
        val e1 = intercept[TestFailedException] {
          fumList should (not equal (fumList) or not contain oneElementOf (Seq("fee", "fie", "foe", "fum")))
        }
        checkMessageStackDepth(e1, FailureMessages.equaled(prettifier, fumList, fumList) + ", and " + FailureMessages.containedOneElementOf(prettifier, fumList, Seq("fee", "fie", "foe", "fum")), fileName, thisLineNumber - 2)
      }

      it("should use the implicit Equality in scope") {
        implicit val ise = upperCaseStringEquality
        fumList should (not equal (toList) or not contain oneElementOf (Seq("fie", "fee", "fum", "foe")))
        fumList should (not equal (fumList) or not contain oneElementOf (Seq("fie", "fee", "fum", "foe")))
        fumList should (not equal (toList) or not contain oneElementOf (Seq("FEE", "FIE", "FOE", "FUM")))
        val e2 = intercept[TestFailedException] {
          fumList should (not equal (fumList) or (not contain oneElementOf (Seq("FEE", "FIE", "FOE", "FUM"))))
        }
        checkMessageStackDepth(e2, FailureMessages.equaled(prettifier, fumList, fumList) + ", and " + FailureMessages.containedOneElementOf(prettifier, fumList, Seq("FEE", "FIE", "FOE", "FUM")), fileName, thisLineNumber - 2)
      }

      it("should use an explicitly provided Equality") {
        (fumList should (not equal (fumList) or not contain oneElementOf (Seq("fie", "fee", "fum", "foe")))) (decided by invertedListOfStringEquality, decided by upperCaseStringEquality)
        (fumList should (not equal (toList) or not contain oneElementOf (Seq("fie", "fee", "fum", "foe")))) (decided by invertedListOfStringEquality, decided by upperCaseStringEquality)
        (fumList should (not equal (fumList) or not contain oneElementOf (Seq("FEE", "FIE", "FOE", "FUM")))) (decided by invertedListOfStringEquality, decided by upperCaseStringEquality)
        val e1 = intercept[TestFailedException] {
          (fumList should (not equal (toList) or not contain oneElementOf (Seq("FEE", "FIE", "FOE", "FUM")))) (decided by invertedListOfStringEquality, decided by upperCaseStringEquality)
        }
        checkMessageStackDepth(e1, FailureMessages.equaled(prettifier, fumList, toList) + ", and " + FailureMessages.containedOneElementOf(prettifier, fumList, Seq("FEE", "FIE", "FOE", "FUM")), fileName, thisLineNumber - 2)
        (fumList should (not contain oneElementOf (Seq(" FEE ", " FIE ", " FOE ", " FUU ")) or not contain oneElementOf (Seq(" FEE ", " FIE ", " FOE ", " FUU ")))) (after being lowerCased and trimmed, after being lowerCased and trimmed)
      }

      it("should allow RHS to contain duplicated value") {
        fumList should (not equal (toList) or not contain oneElementOf (Seq("fee", "fie", "foe", "fie", "fum")))
      }
    }

    describe("when used with (not be (..) and not contain oneElementOf (..))") {

      it("should do nothing if valid, else throw a TFE with an appropriate error message") {
        fumList should (not be (toList) or not contain oneElementOf (Seq("FEE", "FIE", "FOE", "FUM")))
        fumList should (not be (fumList) or not contain oneElementOf (Seq("FEE", "FIE", "FOE", "FUM")))
        fumList should (not be (toList) or not contain oneElementOf (Seq("fee", "fie", "foe", "fum")))
        val e1 = intercept[TestFailedException] {
          fumList should (not be (fumList) or not contain oneElementOf (Seq("fee", "fie", "foe", "fum")))
        }
        checkMessageStackDepth(e1, FailureMessages.wasEqualTo(prettifier, fumList, fumList) + ", and " + FailureMessages.containedOneElementOf(prettifier, fumList, Seq("fee", "fie", "foe", "fum")), fileName, thisLineNumber - 2)
      }

      it("should use the implicit Equality in scope") {
        implicit val ise = upperCaseStringEquality
        fumList should (not be (toList) or not contain oneElementOf (Seq("fee", "fie", "foe", "fum")))
        fumList should (not be (fumList) or not contain oneElementOf (Seq("fee", "fie", "foe", "fum")))
        fumList should (not be (toList) or not contain oneElementOf (Seq("FEE", "FIE", "FOE", "FUM")))
        val e1 = intercept[TestFailedException] {
          fumList should (not be (fumList) or (not contain oneElementOf (Seq("FEE", "FIE", "FOE", "FUM"))))
        }
        checkMessageStackDepth(e1, FailureMessages.wasEqualTo(prettifier, fumList, fumList) + ", and " + FailureMessages.containedOneElementOf(prettifier, fumList, Seq("FEE", "FIE", "FOE", "FUM")), fileName, thisLineNumber - 2)
      }

      it("should use an explicitly provided Equality") {
        (fumList should (not be (toList) or not contain oneElementOf (Seq("fee", "fie", "foe", "fum")))) (decided by upperCaseStringEquality)
        (fumList should (not be (fumList) or not contain oneElementOf (Seq("fee", "fie", "foe", "fum")))) (decided by upperCaseStringEquality)
        (fumList should (not be (toList) or not contain oneElementOf (Seq("FEE", "FIE", "FOE", "FUM")))) (decided by upperCaseStringEquality)
        val e1 = intercept[TestFailedException] {
          (fumList should (not be (fumList) or not contain oneElementOf (Seq("FEE", "FIE", "FOE", "FUM")))) (decided by upperCaseStringEquality)
        }
        checkMessageStackDepth(e1, FailureMessages.wasEqualTo(prettifier, fumList, fumList) + ", and " + FailureMessages.containedOneElementOf(prettifier, fumList, Seq("FEE", "FIE", "FOE", "FUM")), fileName, thisLineNumber - 2)
        (fumList should (not contain oneElementOf (Seq(" FEE ", " FIE ", " FOE ", " FUU ")) or not contain oneElementOf (Seq(" FEE ", " FIE ", " FOE ", " FUU ")))) (after being lowerCased and trimmed, after being lowerCased and trimmed)
      }

      it("should allow RHS to contain duplicated value") {
        fumList should (not be (toList) or not contain oneElementOf (Seq("fee", "fie", "foe", "fie", "fum")))
      }
    }

  }

  describe("collection of Lists") {

    val list1s: Vector[List[Int]] = Vector(List(1), List(1), List(1))
    val lists: Vector[List[Int]] = Vector(List(1), List(1), List(2))
    val nils: Vector[List[Int]] = Vector(Nil, Nil, Nil)
    val listsNil: Vector[List[Int]] = Vector(List(1), List(1), Nil)
    val hiLists: Vector[List[String]] = Vector(List("hi"), List("hi"), List("hi"))
    val toLists: Vector[List[String]] = Vector(List("to"), List("to"), List("to"))

    def allErrMsg(index: Int, message: String, lineNumber: Int, left: Any): String =
      "'all' inspection failed, because: \n" +
        "  at index " + index + ", " + message + " (" + fileName + ":" + (lineNumber) + ") \n" +
        "in " + decorateToStringValue(prettifier, left)

    describe("when used with (contain oneElementOf (..) and contain oneElementOf (..))") {

      it("should do nothing if valid, else throw a TFE with an appropriate error message") {
        all (list1s) should (contain oneElementOf Seq(3, 2, 1) or contain oneElementOf Seq(1, 3, 4))
        all (list1s) should (contain oneElementOf Seq(3, 2, 5) or contain oneElementOf Seq(1, 3, 4))
        all (list1s) should (contain oneElementOf Seq(3, 2, 1) or contain oneElementOf Seq(2, 3, 4))

        atLeast (2, lists) should (contain oneElementOf Seq(3, 1, 5) or contain oneElementOf Seq(1, 3, 4))
        atLeast (2, lists) should (contain oneElementOf Seq(3, 6, 5) or contain oneElementOf Seq(1, 3, 4))
        atLeast (2, lists) should (contain oneElementOf Seq(3, 1, 5) or contain oneElementOf Seq(8, 3, 4))

        val e1 = intercept[TestFailedException] {
          all (lists) should (contain oneElementOf Seq(6, 7, 8) or contain oneElementOf Seq(1, 3, 4))
        }
        checkMessageStackDepth(e1, allErrMsg(2, FailureMessages.didNotContainOneElementOf(prettifier, lists(2), Seq(6, 7, 8)) + ", and " + FailureMessages.didNotContainOneElementOf(prettifier, lists(2), Seq(1, 3, 4)), thisLineNumber - 2, lists), fileName, thisLineNumber - 2)
      }

      it("should use the implicit Equality in scope") {
        implicit val ise = upperCaseStringEquality

        all (hiLists) should (contain oneElementOf Seq("HI", "HE") or contain oneElementOf Seq("HO", "HI"))
        all (hiLists) should (contain oneElementOf Seq("hi", "he") or contain oneElementOf Seq("HO", "HI"))
        all (hiLists) should (contain oneElementOf Seq("HI", "HE") or contain oneElementOf Seq("ho", "hi"))

        val e1 = intercept[TestFailedException] {
          all (hiLists) should (contain oneElementOf Seq("hi", "he") or contain oneElementOf Seq("ho", "hi"))
        }
        checkMessageStackDepth(e1, allErrMsg(0, FailureMessages.didNotContainOneElementOf(prettifier, hiLists(0), Seq("hi", "he")) + ", and " + FailureMessages.didNotContainOneElementOf(prettifier, hiLists(0), Seq("ho", "hi")), thisLineNumber - 2, hiLists), fileName, thisLineNumber - 2)
      }

      it("should use an explicitly provided Equality") {
        (all (hiLists) should (contain oneElementOf Seq("HI", "HE") or contain oneElementOf Seq("HO", "HI"))) (decided by upperCaseStringEquality, decided by upperCaseStringEquality)
        (all (hiLists) should (contain oneElementOf Seq("hi", "he") or contain oneElementOf Seq("HO", "HI"))) (decided by upperCaseStringEquality, decided by upperCaseStringEquality)
        (all (hiLists) should (contain oneElementOf Seq("HI", "HE") or contain oneElementOf Seq("ho", "hi"))) (decided by upperCaseStringEquality, decided by upperCaseStringEquality)

        val e1 = intercept[TestFailedException] {
          (all (hiLists) should (contain oneElementOf Seq("hi", "he") or contain oneElementOf Seq("ho", "hi"))) (decided by upperCaseStringEquality, decided by upperCaseStringEquality)
        }
        checkMessageStackDepth(e1, allErrMsg(0, FailureMessages.didNotContainOneElementOf(prettifier, hiLists(0), Seq("hi", "he")) + ", and " + FailureMessages.didNotContainOneElementOf(prettifier, hiLists(0), Seq("ho", "hi")), thisLineNumber - 2, hiLists), fileName, thisLineNumber - 2)
      }

      it("should allow RHS to contain duplicated value") {
        all (list1s) should (contain oneElementOf Seq(3, 2, 2, 1) or contain oneElementOf Seq(1, 3, 4))
        all (list1s) should (contain oneElementOf Seq(1, 3, 4) or contain oneElementOf Seq(3, 2, 2, 1))
      }
    }

    describe("when used with (be (..) and contain oneElementOf (..))") {

      it("should do nothing if valid, else throw a TFE with an appropriate error message") {
        all (list1s) should (be (List(1)) or contain oneElementOf Seq(1, 3, 4))
        all (list1s) should (be (List(2)) or contain oneElementOf Seq(1, 3, 4))
        all (list1s) should (be (List(1)) or contain oneElementOf Seq(2, 3, 4))

        val e1 = intercept[TestFailedException] {
          all (list1s) should (be (List(2)) or contain oneElementOf Seq(2, 3, 8))
        }
        checkMessageStackDepth(e1, allErrMsg(0, decorateToStringValue(prettifier, List(1)) + " was not equal to " + decorateToStringValue(prettifier, List(2)) + ", and " + FailureMessages.didNotContainOneElementOf(prettifier, list1s(0), Seq(2, 3, 8)), thisLineNumber - 2, list1s), fileName, thisLineNumber - 2)
      }

      it("should use the implicit Equality in scope") {
        implicit val ise = upperCaseStringEquality

        all (hiLists) should (be (List("hi")) or contain oneElementOf Seq("HI", "HE"))
        all (hiLists) should (be (List("ho")) or contain oneElementOf Seq("HI", "HE"))
        all (hiLists) should (be (List("hi")) or contain oneElementOf Seq("hi", "he"))

        val e1 = intercept[TestFailedException] {
          all (hiLists) should (be (List("ho")) or contain oneElementOf Seq("hi", "he"))
        }
        checkMessageStackDepth(e1, allErrMsg(0, decorateToStringValue(prettifier, List("hi")) + " was not equal to " + decorateToStringValue(prettifier, List("ho")) + ", and " + FailureMessages.didNotContainOneElementOf(prettifier, hiLists(0), Seq("hi", "he")), thisLineNumber - 2, hiLists), fileName, thisLineNumber - 2)
      }

      it("should use an explicitly provided Equality") {
        (all (hiLists) should (be (List("hi")) or contain oneElementOf Seq("HI", "HE"))) (decided by upperCaseStringEquality)
        (all (hiLists) should (be (List("ho")) or contain oneElementOf Seq("HI", "HE"))) (decided by upperCaseStringEquality)
        (all (hiLists) should (be (List("hi")) or contain oneElementOf Seq("hi", "he"))) (decided by upperCaseStringEquality)

        val e1 = intercept[TestFailedException] {
          (all (hiLists) should (be (List("ho")) or contain oneElementOf Seq("hi", "he"))) (decided by upperCaseStringEquality)
        }
        checkMessageStackDepth(e1, allErrMsg(0, decorateToStringValue(prettifier, List("hi")) + " was not equal to " + decorateToStringValue(prettifier, List("ho")) + ", and " + FailureMessages.didNotContainOneElementOf(prettifier, hiLists(0), Seq("hi", "he")), thisLineNumber - 2, hiLists), fileName, thisLineNumber - 2)
      }

      it("should allow RHS to contain duplicated value") {
        all (list1s) should (be (List(1)) or contain oneElementOf Seq(3, 2, 2, 1))
      }
    }

    describe("when used with (not contain oneElementOf (..) and not contain oneElementOf (..))") {

      it("should do nothing if valid, else throw a TFE with an appropriate error message") {
        all (list1s) should (not contain oneElementOf (Seq(3, 2, 8)) or not contain oneElementOf (Seq(8, 3, 4)))
        all (list1s) should (not contain oneElementOf (Seq(1, 2, 8)) or not contain oneElementOf (Seq(8, 3, 4)))
        all (list1s) should (not contain oneElementOf (Seq(3, 2, 8)) or not contain oneElementOf (Seq(8, 3, 1)))

        val e1 = intercept[TestFailedException] {
          all (lists) should (not contain oneElementOf (Seq(2, 6, 8)) or not contain oneElementOf (Seq(2, 3, 4)))
        }
        checkMessageStackDepth(e1, allErrMsg(2, FailureMessages.containedOneElementOf(prettifier, lists(2), Seq(2, 6, 8)) + ", and " + FailureMessages.containedOneElementOf(prettifier, lists(2), Seq(2, 3, 4)), thisLineNumber - 2, lists), fileName, thisLineNumber - 2)
      }

      it("should use the implicit Equality in scope") {
        implicit val ise = upperCaseStringEquality

        all (hiLists) should (not contain oneElementOf (Seq("hi", "he")) or not contain oneElementOf (Seq("hi", "he")))
        all (hiLists) should (not contain oneElementOf (Seq("HI", "HE")) or not contain oneElementOf (Seq("hi", "he")))
        all (hiLists) should (not contain oneElementOf (Seq("hi", "he")) or not contain oneElementOf (Seq("HI", "HE")))

        val e1 = intercept[TestFailedException] {
          all (hiLists) should (not contain oneElementOf (Seq("HI", "HE")) or not contain oneElementOf (Seq("HI", "HE")))
        }
        checkMessageStackDepth(e1, allErrMsg(0, FailureMessages.containedOneElementOf(prettifier, hiLists(0), Seq("HI", "HE")) + ", and " + FailureMessages.containedOneElementOf(prettifier, hiLists(0), Seq("HI", "HE")), thisLineNumber - 2, hiLists), fileName, thisLineNumber - 2)
      }

      it("should use an explicitly provided Equality") {
        (all (hiLists) should (not contain oneElementOf (Seq("hi", "he")) or not contain oneElementOf (Seq("hi", "he")))) (decided by upperCaseStringEquality, decided by upperCaseStringEquality)
        (all (hiLists) should (not contain oneElementOf (Seq("HI", "HE")) or not contain oneElementOf (Seq("hi", "he")))) (decided by upperCaseStringEquality, decided by upperCaseStringEquality)
        (all (hiLists) should (not contain oneElementOf (Seq("hi", "he")) or not contain oneElementOf (Seq("HI", "HE")))) (decided by upperCaseStringEquality, decided by upperCaseStringEquality)

        val e1 = intercept[TestFailedException] {
          (all (hiLists) should (not contain oneElementOf (Seq("HI", "HE")) or not contain oneElementOf (Seq("HI", "HE")))) (decided by upperCaseStringEquality, decided by upperCaseStringEquality)
        }
        checkMessageStackDepth(e1, allErrMsg(0, FailureMessages.containedOneElementOf(prettifier, hiLists(0), Seq("HI", "HE")) + ", and " + FailureMessages.containedOneElementOf(prettifier, hiLists(0), Seq("HI", "HE")), thisLineNumber - 2, hiLists), fileName, thisLineNumber - 2)
      }

      it("should allow RHS to contain duplicated value") {
        all (list1s) should (not contain oneElementOf (Seq(3, 2, 2, 1)) or not contain oneElementOf (Seq(8, 3, 4)))
        all (list1s) should (not contain oneElementOf (Seq(8, 3, 4)) or not contain oneElementOf (Seq(3, 2, 2, 1)))
      }
    }

    describe("when used with (not be (..) and not contain oneElementOf (..))") {

      it("should do nothing if valid, else throw a TFE with an appropriate error message") {
        all (list1s) should (not be (List(2)) or not contain oneElementOf (Seq(8, 3, 4)))
        all (list1s) should (not be (List(1)) or not contain oneElementOf (Seq(8, 3, 4)))
        all (list1s) should (not be (List(2)) or not contain oneElementOf (Seq(8, 3, 1)))

        val e1 = intercept[TestFailedException] {
          all (list1s) should (not be (List(1)) or not contain oneElementOf (Seq(2, 3, 1)))
        }
        checkMessageStackDepth(e1, allErrMsg(0, decorateToStringValue(prettifier, List(1)) + " was equal to " + decorateToStringValue(prettifier, List(1)) + ", and " + FailureMessages.containedOneElementOf(prettifier, list1s(0), Seq(2, 3, 1)), thisLineNumber - 2, list1s), fileName, thisLineNumber - 2)
      }

      it("should use the implicit Equality in scope") {
        implicit val ise = upperCaseStringEquality

        all (hiLists) should (not be (List("ho")) or not contain oneElementOf (Seq("hi", "he")))
        all (hiLists) should (not be (List("hi")) or not contain oneElementOf (Seq("hi", "he")))
        all (hiLists) should (not be (List("ho")) or not contain oneElementOf (Seq("HI", "HE")))

        val e1 = intercept[TestFailedException] {
          all (hiLists) should (not be (List("hi")) or not contain oneElementOf (Seq("HI", "HE")))
        }
        checkMessageStackDepth(e1, allErrMsg(0, decorateToStringValue(prettifier, List("hi")) + " was equal to " + decorateToStringValue(prettifier, List("hi")) + ", and " + FailureMessages.containedOneElementOf(prettifier, hiLists(0), Seq("HI", "HE")), thisLineNumber - 2, hiLists), fileName, thisLineNumber - 2)
      }

      it("should use an explicitly provided Equality") {
        (all (hiLists) should (not be (List("ho")) or not contain oneElementOf (Seq("hi", "he")))) (decided by upperCaseStringEquality)
        (all (hiLists) should (not be (List("hi")) or not contain oneElementOf (Seq("hi", "he")))) (decided by upperCaseStringEquality)
        (all (hiLists) should (not be (List("ho")) or not contain oneElementOf (Seq("HI", "HE")))) (decided by upperCaseStringEquality)

        val e1 = intercept[TestFailedException] {
          (all (hiLists) should (not be (List("hi")) or not contain oneElementOf (Seq("HI", "HE")))) (decided by upperCaseStringEquality)
        }
        checkMessageStackDepth(e1, allErrMsg(0, decorateToStringValue(prettifier, List("hi")) + " was equal to " + decorateToStringValue(prettifier, List("hi")) + ", and " + FailureMessages.containedOneElementOf(prettifier, hiLists(0), Seq("HI", "HE")), thisLineNumber - 2, hiLists), fileName, thisLineNumber - 2)
      }

      it("should allow RHS to contain duplicated value") {
        all (list1s) should (not be (List(2)) or not contain oneElementOf (Seq(3, 2, 2, 1)))
      }
    }
  }
}
