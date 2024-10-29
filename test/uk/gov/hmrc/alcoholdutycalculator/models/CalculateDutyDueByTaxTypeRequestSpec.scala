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

import play.api.libs.json.Json
import uk.gov.hmrc.alcoholdutycalculator.base.SpecBase

class CalculateDutyDueByTaxTypeRequestSpec extends SpecBase {
  "CalculateDutyDueByTaxTypeRequest must" - {
    "serialise to json" in new SetUp {
      Json.toJson(calculateDutyDueByTaxTypeRequest).toString mustBe json
    }

    "deserialise from json" in new SetUp {
      Json.parse(json).as[CalculateDutyDueByTaxTypeRequest] mustBe calculateDutyDueByTaxTypeRequest
    }
  }

  class SetUp {
    val json =
      """{"declarationOrAdjustmentItems":[{"taxType":"331","dutyDue":115.11},{"taxType":"332","dutyDue":321.88},{"taxType":"332","dutyDue":245.79},{"taxType":"331","dutyDue":8.12}]}"""
  }
}
