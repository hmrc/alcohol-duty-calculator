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
import uk.gov.hmrc.alcoholdutycalculator.models.AdjustmentType.Underdeclaration
import uk.gov.hmrc.alcoholdutycalculator.models.*

class DutyServiceSpec extends SpecBase {
  val dutyService = new DutyService()

  "calculateAdjustmentDuty must" - {
    "calculate a positive duty for an Underdeclaration or Repackaged adjustment type" in
      forAll(arbitraryPositiveDutyAdjustmentType) { adjustmentType =>
        val adjustmentDutyCalculationRequest =
          AdjustmentDutyCalculationRequest(
            adjustmentType.arbitrary.sample.get,
            BigDecimal(0.03),
            BigDecimal(2)
          )

        val result = dutyService.calculateAdjustmentDuty(adjustmentDutyCalculationRequest)
        result.duty mustBe BigDecimal(0.06)
      }

    "calculate a negative duty for a Spoilt, Overdeclared and Drawback adjustment types" in
      forAll(arbitraryNegativeDutyAdjustmentType) { adjustmentType =>
        val adjustmentDutyCalculationRequest =
          AdjustmentDutyCalculationRequest(
            adjustmentType.arbitrary.sample.get,
            BigDecimal(0.04),
            BigDecimal(2)
          )

        val result = dutyService.calculateAdjustmentDuty(adjustmentDutyCalculationRequest)
        result.duty mustBe BigDecimal(-0.08)
      }

    "calculate duty with decimal values" in {
      val adjustmentDutyCalculationRequest =
        AdjustmentDutyCalculationRequest(
          Underdeclaration,
          BigDecimal(2.8),
          BigDecimal(1.1)
        )

      val result = dutyService.calculateAdjustmentDuty(adjustmentDutyCalculationRequest)
      result.duty mustBe BigDecimal(3.08)
    }

    "calculate duty with negative values" in {
      val adjustmentDutyCalculationRequest =
        AdjustmentDutyCalculationRequest(
          Underdeclaration,
          BigDecimal(0.02),
          BigDecimal(-3)
        )

      val result = dutyService.calculateAdjustmentDuty(adjustmentDutyCalculationRequest)
      result.duty mustBe BigDecimal(-0.06)
    }

    "calculate duty rounding down to the nearest penny" in {
      val adjustmentDutyCalculationRequest =
        AdjustmentDutyCalculationRequest(
          Underdeclaration,
          BigDecimal(0.10),
          BigDecimal(2.1)
        )

      val result = dutyService.calculateAdjustmentDuty(adjustmentDutyCalculationRequest)
      result.duty mustBe BigDecimal(0.21)
    }

    "calculate duty rounding down to the nearest penny with large decimals" in {
      val adjustmentDutyCalculationRequest =
        AdjustmentDutyCalculationRequest(
          Underdeclaration,
          BigDecimal(0.105527),
          BigDecimal(1.9999)
        )

      val result = dutyService.calculateAdjustmentDuty(adjustmentDutyCalculationRequest)
      result.duty mustBe BigDecimal(0.21)
    }
  }

  "calculateRepackagedDutyChange must" - {
    "calculate duty" in {
      val repackagedDutyChangeRequest = RepackagedDutyChangeRequest(BigDecimal(5), BigDecimal(4))

      val result = dutyService.calculateRepackagedDutyChange(repackagedDutyChangeRequest)
      result.duty mustBe BigDecimal(1)
    }

    "calculate duty with decimal values" in {
      val repackagedDutyChangeRequest = RepackagedDutyChangeRequest(BigDecimal(5.55), BigDecimal(4.25))

      val result = dutyService.calculateRepackagedDutyChange(repackagedDutyChangeRequest)
      result.duty mustBe BigDecimal(1.3)
    }

    "calculate duty with negative values" in {
      val repackagedDutyChangeRequest = RepackagedDutyChangeRequest(BigDecimal(-5.45), BigDecimal(4.57))

      val result = dutyService.calculateRepackagedDutyChange(repackagedDutyChangeRequest)
      result.duty mustBe BigDecimal(-10.02)
    }
  }

  "calculateAdjustmentTotal must" - {
    "calculate duty" in {
      val adjustmentTotalCalculationRequest =
        AdjustmentTotalCalculationRequest(Seq(BigDecimal(10), BigDecimal(19), BigDecimal(18)))

      val result = dutyService.calculateAdjustmentTotal(adjustmentTotalCalculationRequest)
      result.duty mustBe BigDecimal(47)
    }

    "calculate duty with decimal values" in {
      val adjustmentTotalCalculationRequest =
        AdjustmentTotalCalculationRequest(Seq(BigDecimal(10.12), BigDecimal(19.45), BigDecimal(18.45)))

      val result = dutyService.calculateAdjustmentTotal(adjustmentTotalCalculationRequest)
      result.duty mustBe BigDecimal(48.02)
    }

    "calculate duty with negative values" in {
      val adjustmentTotalCalculationRequest =
        AdjustmentTotalCalculationRequest(Seq(BigDecimal(10.12), BigDecimal(-19.45), BigDecimal(18.45)))

      val result = dutyService.calculateAdjustmentTotal(adjustmentTotalCalculationRequest)
      result.duty mustBe BigDecimal(9.12)
    }
  }

  "returns calculateTotalDuty must" - {
    "calculate the correct alcohol duty" in {
      val adjustmentDutyCalculationRequest = Seq(
        DutyByTaxType(
          taxType = "taxType",
          totalLitres = BigDecimal(1.0),
          pureAlcohol = BigDecimal(1.0),
          dutyRate = BigDecimal(1.0)
        ),
        DutyByTaxType(
          taxType = "taxType2",
          totalLitres = BigDecimal(2.0),
          pureAlcohol = BigDecimal(2.0),
          dutyRate = BigDecimal(2.0)
        )
      )

      val result = dutyService.calculateTotalDuty(adjustmentDutyCalculationRequest)
      result.totalDuty            mustBe BigDecimal(5.0)
      result.dutiesByTaxType.size mustBe 2
      result.dutiesByTaxType      mustBe Seq(
        DutyCalculationByTaxTypeResponse(
          taxType = "taxType",
          totalLitres = BigDecimal(1.0),
          pureAlcohol = BigDecimal(1.0),
          dutyRate = BigDecimal(1.0),
          dutyDue = BigDecimal(1.0)
        ),
        DutyCalculationByTaxTypeResponse(
          taxType = "taxType2",
          totalLitres = BigDecimal(2.0),
          pureAlcohol = BigDecimal(2.0),
          dutyRate = BigDecimal(2.0),
          dutyDue = BigDecimal(4.0)
        )
      )
    }

    "calculate the correct alcohol duty rounded down to two decimal places" in {
      val adjustmentDutyCalculationRequest = Seq(
        DutyByTaxType(
          taxType = "taxType",
          totalLitres = BigDecimal(1.0),
          pureAlcohol = BigDecimal(1.01),
          dutyRate = BigDecimal(0.01)
        ),
        DutyByTaxType(
          taxType = "taxType2",
          totalLitres = BigDecimal(2.0),
          pureAlcohol = BigDecimal(2.4),
          dutyRate = BigDecimal(0.02)
        )
      )

      val result = dutyService.calculateTotalDuty(adjustmentDutyCalculationRequest)
      result.totalDuty                     mustBe BigDecimal(0.05)
      result.dutiesByTaxType.size          mustBe 2
      result.dutiesByTaxType.head.dutyRate mustBe BigDecimal(0.01)
      result.dutiesByTaxType.last.dutyDue  mustBe BigDecimal(0.04)
    }
  }

  "calculateDutyByTaxType must" - {
    "calculate the duty by tax type" in {
      dutyService.calculateDutyByTaxType(calculateDutyDueByTaxTypeRequest) mustBe calculatedDutyDueByTaxType
    }
  }
}
