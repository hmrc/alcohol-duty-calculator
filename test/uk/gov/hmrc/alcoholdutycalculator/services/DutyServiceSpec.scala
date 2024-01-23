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

class DutyServiceSpec extends SpecBase {

  "dutyService" should {
    "calculate duty" in {
      val dutyService = new DutyService()
      val result      = dutyService.calculateDuty(BigDecimal(5), BigDecimal(5), BigDecimal(5))
      result.pureAlcoholVolume shouldBe BigDecimal(0.25)
      result.duty              shouldBe BigDecimal(1.25)
    }
  }

}
