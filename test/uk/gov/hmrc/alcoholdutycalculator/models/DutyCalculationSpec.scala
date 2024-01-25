/*
 * Copyright 2024 HM Revenue & Customs
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

import play.api.libs.json.{JsBoolean, JsError, JsNumber, Json}
import uk.gov.hmrc.alcoholdutycalculator.base.SpecBase

class DutyCalculationSpec extends SpecBase {

  "Volume" when {

    "creating a new Volume" should {
      "throw an exception when the value is greater than 999999999.99" in {
        intercept[IllegalArgumentException] {
          Volume(1000000000.0)
        }
      }
      "throw an exception when the value has more than 4 decimal places" in {
        intercept[IllegalArgumentException] {
          Volume(1.12345)
        }
      }
    }

    "writing to json"   should {
      "return the correct string representation" in {
        Json.toJson[Volume](Volume(1.0)) shouldBe JsNumber(1.0)
      }
    }
    "reading from json" should {
      "translate from the string rep, to the correct case object" in {
        JsNumber(1.0).as[Volume] shouldBe Volume(1.0)
      }
      "return a JsError when passed a type that is not a number" in {
        val result = Json.fromJson[Volume](JsBoolean(true))
        result shouldBe a[JsError]
      }
    }
  }
}
