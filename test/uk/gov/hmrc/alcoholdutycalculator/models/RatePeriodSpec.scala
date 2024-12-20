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
  "RateType when" - {
    "writing to json must" - {
      "return the correct string representation" in {
        Json.toJson[RateType](RateType.Core)                          mustBe JsString("Core")
        Json.toJson[RateType](RateType.DraughtRelief)                 mustBe JsString("DraughtRelief")
        Json.toJson[RateType](RateType.SmallProducerRelief)           mustBe JsString("SmallProducerRelief")
        Json.toJson[RateType](RateType.DraughtAndSmallProducerRelief) mustBe JsString("DraughtAndSmallProducerRelief")
      }
    }

    "reading from json must" - {
      "translate from the string rep, to the correct case object" in {
        JsString("Core").as[RateType]                          mustBe RateType.Core
        JsString("DraughtRelief").as[RateType]                 mustBe RateType.DraughtRelief
        JsString("SmallProducerRelief").as[RateType]           mustBe RateType.SmallProducerRelief
        JsString("DraughtAndSmallProducerRelief").as[RateType] mustBe RateType.DraughtAndSmallProducerRelief
      }

      "return a JsError in response to an unrecognised string" in {
        JsString("some-other").validate[RateType] mustBe JsError("some-other is not a valid RateType")
      }

      "return a JsError when passed a type that is not a string" in {
        val result = Json.fromJson[RateType](JsBoolean(true))
        result mustBe a[JsError]
      }
    }
  }

  "AlcoholRegime when" - {
    "converting from a string must" - {
      "translate from the string representation, to the correct case object" in {
        AlcoholRegime.fromString("Beer")                  mustBe Right(AlcoholRegime.Beer)
        AlcoholRegime.fromString("Cider")                 mustBe Right(AlcoholRegime.Cider)
        AlcoholRegime.fromString("Wine")                  mustBe Right(AlcoholRegime.Wine)
        AlcoholRegime.fromString("Spirits")               mustBe Right(AlcoholRegime.Spirits)
        AlcoholRegime.fromString("OtherFermentedProduct") mustBe Right(AlcoholRegime.OtherFermentedProduct)
      }

      "return an error in response to an unrecognised string" in {
        AlcoholRegime.fromString("some-other") mustBe Left("some-other is not a valid AlcoholRegime")
      }
    }

    "writing to json must" - {
      "return the correct string representation" in {
        Json.toJson[AlcoholRegime](AlcoholRegime.Beer)                  mustBe JsString("Beer")
        Json.toJson[AlcoholRegime](AlcoholRegime.Cider)                 mustBe JsString("Cider")
        Json.toJson[AlcoholRegime](AlcoholRegime.Wine)                  mustBe JsString("Wine")
        Json.toJson[AlcoholRegime](AlcoholRegime.Spirits)               mustBe JsString("Spirits")
        Json.toJson[AlcoholRegime](AlcoholRegime.OtherFermentedProduct) mustBe JsString("OtherFermentedProduct")
      }
    }

    "reading from json must" - {
      "translate from the string representation, to the correct case object" in {
        JsString("Beer").as[AlcoholRegime]                  mustBe AlcoholRegime.Beer
        JsString("Cider").as[AlcoholRegime]                 mustBe AlcoholRegime.Cider
        JsString("Wine").as[AlcoholRegime]                  mustBe AlcoholRegime.Wine
        JsString("Spirits").as[AlcoholRegime]               mustBe AlcoholRegime.Spirits
        JsString("OtherFermentedProduct").as[AlcoholRegime] mustBe AlcoholRegime.OtherFermentedProduct
      }

      "return a JsError in response to an unrecognised string" in {
        JsString("some-other").validate[AlcoholRegime] mustBe JsError("some-other is not a valid AlcoholRegime")
      }

      "return a JsError when passed a type that is not a string" in {
        val result = Json.fromJson[AlcoholRegime](JsBoolean(true))
        result mustBe a[JsError]
      }
    }
  }

  "AlcoholRate when" - {
    "creating an instance must" - {
      "successfully save the BigDecimal value when the value is in range" in {
        forAll(genAlcoholByVolumeValue) { validValue: BigDecimal =>
          AlcoholByVolume(validValue).value mustBe validValue
        }
      }

      "throw an exception when the value is not in range" in {
        forAll(genAlcoholByVolumeValueOutOfRange) { invalidValue: BigDecimal =>
          val exception = intercept[IllegalArgumentException] {
            AlcoholByVolume(invalidValue)
          }
          exception.getMessage must include("Percentage must be between 0 and 100")
        }
      }

      "throw an exception when the value has more decimal points than 1" in {
        forAll(genAlcoholByVolumeValueTooBigScale) { invalidValue: BigDecimal =>
          val exception = intercept[IllegalArgumentException] {
            AlcoholByVolume(invalidValue)
          }
          exception.getMessage must include("Alcohol By Volume must have maximum 1 decimal place")
        }
      }
    }

    "writing to json must" - {
      "return the correct number representation with decimal points" in {
        forAll(genAlcoholByVolumeValue) { validValue =>
          Json.toJson(AlcoholByVolume(validValue)) mustBe JsNumber(validValue)
        }
      }
    }

    "reading from json must" - {
      "translate from the number representation when it is a valid Alcohol By Volume value" in {
        forAll(genAlcoholByVolumeValue) { validValue =>
          JsNumber(validValue).as[AlcoholByVolume] mustBe AlcoholByVolume(validValue)
        }
      }

      "return a JsError in response to an invalid rate value" in {
        forAll(genAlcoholByVolumeValueTooBigScale) { invalidValue =>
          JsNumber(invalidValue).validate[AlcoholByVolume] mustBe a[JsError]
        }
      }

      "return a JsError when passed a type that is not a number" in {
        forAll(Gen.oneOf(Gen.alphaStr, Gen.calendar)) { invalidJsValue =>
          val result = Json.fromJson[AlcoholByVolume](JsString(invalidJsValue.toString))
          result mustBe a[JsError]
        }
      }
    }
  }

  "YearMonth formats must" - {
    implicit val yearMonthFormats: Format[YearMonth]               = RatePeriod.yearMonthFormat
    implicit val optionYearMonthFormats: Format[Option[YearMonth]] = RatePeriod.optionYearMonthFormat

    "serialise and deserialise YearMonth" in {
      forAll { (yearMonth: YearMonth) =>
        val json   = Json.toJson(yearMonth)
        val result = json.validate[YearMonth]
        result     mustBe a[JsSuccess[_]]
        result.get mustBe yearMonth
      }
    }

    "serialise and deserialise Option[YearMonth]" in {
      forAll { (optionYearMonth: Option[YearMonth]) =>
        val json   = Json.toJson(optionYearMonth)
        val result = json.validate[Option[YearMonth]]

        result     mustBe a[JsSuccess[_]]
        result.get mustBe optionYearMonth
      }
    }
  }
}
