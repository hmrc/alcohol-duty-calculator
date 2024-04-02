/*
 * Copyright 2023 HM Revenue & Customs
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

package uk.gov.hmrc.alcoholdutycalculator.models

import org.scalacheck.Gen
import play.api.libs.json._
import uk.gov.hmrc.alcoholdutycalculator.base.SpecBase

import java.time.YearMonth

class RatePeriodSpec extends SpecBase {
  "RateType" when {
    "writing to json"   should {
      "return the correct string representation" in {
        Json.toJson[RateType](RateType.Core)                          shouldBe JsString("Core")
        Json.toJson[RateType](RateType.DraughtRelief)                 shouldBe JsString("DraughtRelief")
        Json.toJson[RateType](RateType.SmallProducerRelief)           shouldBe JsString("SmallProducerRelief")
        Json.toJson[RateType](RateType.DraughtAndSmallProducerRelief) shouldBe JsString("DraughtAndSmallProducerRelief")
      }
    }
    "reading from json" should {
      "translate from the string rep, to the correct case object" in {
        JsString("Core").as[RateType]                          shouldBe RateType.Core
        JsString("DraughtRelief").as[RateType]                 shouldBe RateType.DraughtRelief
        JsString("SmallProducerRelief").as[RateType]           shouldBe RateType.SmallProducerRelief
        JsString("DraughtAndSmallProducerRelief").as[RateType] shouldBe RateType.DraughtAndSmallProducerRelief
      }
      "return an exception in response to an unrecognised string" in {
        JsString("some-other").validate[RateType] shouldBe JsError("some-other is not a valid RateType")
      }
      "return a JsError when passed a type that is not a string" in {
        val result = Json.fromJson[RateType](JsBoolean(true))
        result shouldBe a[JsError]
      }
    }
  }

  "AlcoholRegime" when {
    "writing to json"   should {
      "return the correct string representation" in {
        Json.toJson[AlcoholRegime](AlcoholRegime.Beer)    shouldBe JsString("Beer")
        Json.toJson[AlcoholRegime](AlcoholRegime.Cider)   shouldBe JsString("Cider")
        Json.toJson[AlcoholRegime](AlcoholRegime.Wine)    shouldBe JsString("Wine")
        Json.toJson[AlcoholRegime](AlcoholRegime.Spirits) shouldBe JsString("Spirits")
      }
    }
    "reading from json" should {
      "translate from the string rep, to the correct case object" in {
        JsString("Beer").as[AlcoholRegime]    shouldBe AlcoholRegime.Beer
        JsString("Cider").as[AlcoholRegime]   shouldBe AlcoholRegime.Cider
        JsString("Wine").as[AlcoholRegime]    shouldBe AlcoholRegime.Wine
        JsString("Spirits").as[AlcoholRegime] shouldBe AlcoholRegime.Spirits
      }
      "return an exception in response to an unrecognised string" in {
        JsString("some-other").validate[AlcoholRegime] shouldBe JsError("some-other is not a valid AlcoholRegime")
      }
      "return a JsError when passed a type that is not a string" in {
        val result = Json.fromJson[AlcoholRegime](JsBoolean(true))
        result shouldBe a[JsError]
      }
    }
  }

  "AlcoholRate" when {
    "creating an instance" should {
      "successfully save the BigDecimal value when the value is in range" in {
        forAll(genAlcoholByVolumeValue) { validValue: BigDecimal =>
          AlcoholByVolume(validValue).value shouldBe validValue
        }
      }

      "throw an exception when the value is not in range" in {
        forAll(genAlcoholByVolumeValueOutOfRange) { invalidValue: BigDecimal =>
          val exception = intercept[IllegalArgumentException] {
            AlcoholByVolume(invalidValue)
          }
          exception.getMessage should include("Percentage must be between 0 and 100")
        }
      }

      "throw an exception when the value has more decimal points than 1" in {
        forAll(genAlcoholByVolumeValueTooBigScale) { invalidValue: BigDecimal =>
          val exception = intercept[IllegalArgumentException] {
            AlcoholByVolume(invalidValue)
          }
          exception.getMessage should include("Alcohol By Volume must have maximum 1 decimal place")
        }
      }
    }

    "writing to json" should {
      "return the correct number representation with decimal points" in {
        forAll(genAlcoholByVolumeValue) { validValue =>
          Json.toJson(AlcoholByVolume(validValue)) shouldBe JsNumber(validValue)
        }
      }
    }

    "reading from json" should {
      "translate from the number representation when it is a valid Alcohol By Volume value" in {
        forAll(genAlcoholByVolumeValue) { validValue =>
          JsNumber(validValue).as[AlcoholByVolume] shouldBe AlcoholByVolume(validValue)
        }
      }

      "return an exception in response to an invalid rate value" in {
        forAll(genAlcoholByVolumeValueTooBigScale) { invalidValue =>
          JsNumber(invalidValue).validate[AlcoholByVolume] shouldBe a[JsError]
        }
      }

      "return a JsError when passed a type that is not a number" in {
        forAll(Gen.oneOf(Gen.alphaStr, Gen.calendar)) { invalidJsValue =>
          val result = Json.fromJson[AlcoholByVolume](JsString(invalidJsValue.toString))
          result shouldBe a[JsError]
        }
      }
    }
  }

  "YearMonth formats" should {

    implicit val yearMonthFormats: Format[YearMonth]               = RatePeriod.yearMonthFormat
    implicit val optionYearMonthFormats: Format[Option[YearMonth]] = RatePeriod.optionYearMonthFormat

    "serialise and deserialise YearMonth" in {
      forAll { (yearMonth: YearMonth) =>
        val json   = Json.toJson(yearMonth)
        val result = json.validate[YearMonth]
        result     shouldBe a[JsSuccess[_]]
        result.get shouldBe yearMonth
      }
    }

    "serialise and deserialise Option[YearMonth]" in {
      forAll { (optionYearMonth: Option[YearMonth]) =>
        val json   = Json.toJson(optionYearMonth)
        val result = json.validate[Option[YearMonth]]

        result     shouldBe a[JsSuccess[_]]
        result.get shouldBe optionYearMonth
      }
    }
  }

}
