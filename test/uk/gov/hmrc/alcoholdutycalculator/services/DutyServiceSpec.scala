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

package uk.gov.hmrc.alcoholdutycalculator.services

import uk.gov.hmrc.alcoholdutycalculator.base.SpecBase
import uk.gov.hmrc.alcoholdutycalculator.models.{AlcoholByVolume, DutyCalculationRequest, Volume}

class DutyServiceSpec extends SpecBase {
  val dutyService = new DutyService()

  "dutyService" should {
    "calculate pure alcohol volume and duty" in {
      val dutyCalculationRequest =
        DutyCalculationRequest(
          AlcoholByVolume(40),
          Volume(1),
          BigDecimal(28.22)
        )

      val result = dutyService.calculateDuty(dutyCalculationRequest)
      result.pureAlcoholVolume shouldBe BigDecimal(0.40)
      result.duty              shouldBe BigDecimal(11.28)
    }

    "calculate duty rounding down to the nearest penny" in {
      val dutyCalculationRequest =
        DutyCalculationRequest(
          AlcoholByVolume(1),
          Volume(1.99),
          BigDecimal(10.99)
        )

      val result = dutyService.calculateDuty(dutyCalculationRequest)
      result.pureAlcoholVolume shouldBe BigDecimal(0.0199)
      result.duty              shouldBe BigDecimal(0.21)
    }
  }

}
