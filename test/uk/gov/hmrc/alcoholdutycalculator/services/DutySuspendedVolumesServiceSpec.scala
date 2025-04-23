/*
 * Copyright 2025 HM Revenue & Customs
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
import uk.gov.hmrc.alcoholdutycalculator.models._

class DutySuspendedVolumesServiceSpec extends SpecBase {
  val dutySuspendedVolumesService = new DutySuspendedVolumesService()

  "calculateDutySuspendedVolumes must" - {
    "calculate volumes" in {
      val dutySuspendedQuantities =
        DutySuspendedQuantities(
          totalLitresDeliveredInsideUK = BigDecimal(3),
          pureAlcoholDeliveredInsideUK = BigDecimal(1),
          totalLitresDeliveredOutsideUK = BigDecimal(7),
          pureAlcoholDeliveredOutsideUK = BigDecimal(2),
          totalLitresReceived = BigDecimal(0),
          pureAlcoholReceived = BigDecimal(0)
        )

      val result = dutySuspendedVolumesService.calculateDutySuspendedVolumes(dutySuspendedQuantities)
      result mustBe DutySuspendedFinalVolumes(
        totalLitresDelivered = BigDecimal(10),
        totalLitres = BigDecimal(10),
        pureAlcoholDelivered = BigDecimal(3),
        pureAlcohol = BigDecimal(3)
      )
    }

    "calculate volumes with decimal values" in {
      val result = dutySuspendedVolumesService.calculateDutySuspendedVolumes(dutySuspendedQuantities)
      result mustBe dutySuspendedFinalVolumes
    }
  }
}
