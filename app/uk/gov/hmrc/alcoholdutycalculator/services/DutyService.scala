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

import uk.gov.hmrc.alcoholdutycalculator.models._
import uk.gov.hmrc.alcoholdutycalculator.models.AdjustmentType.{RepackagedDraughtProducts, Underdeclaration}
import uk.gov.hmrc.alcoholdutycalculator.models.{AdjustmentDuty, AdjustmentTotalCalculationRequest, AdjustmentType, RepackagedDutyChangeRequest}

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton()
class DutyService @Inject() (implicit val ec: ExecutionContext) {

  def calculateTotalDuty(dutyRates: Seq[DutyByTaxType]): DutyTotalCalculationResponse = {
    val totalsByTaxType = dutyRates.map(dutyRate =>
      DutyCalculationByTaxTypeResponse(
        taxType = dutyRate.taxType,
        totalLitres = dutyRate.totalLitres,
        pureAlcohol = dutyRate.pureAlcohol,
        dutyRate = dutyRate.dutyRate,
        dutyDue = (dutyRate.dutyRate * dutyRate.pureAlcohol).setScale(2, BigDecimal.RoundingMode.DOWN)
      )
    )

    val total = totalsByTaxType.map(_.dutyDue).sum
    DutyTotalCalculationResponse(totalDuty = total, dutiesByTaxType = totalsByTaxType)
  }

  def calculateAdjustmentDuty(
    adjustmentDutyCalculationRequest: AdjustmentDutyCalculationRequest
  ): AdjustmentDuty = {
    val duty       =
      (adjustmentDutyCalculationRequest.pureAlcoholVolume * adjustmentDutyCalculationRequest.rate)
        .setScale(2, BigDecimal.RoundingMode.DOWN)
    val signedDuty = checkDutyValue(duty, adjustmentDutyCalculationRequest.adjustmentType)
    AdjustmentDuty(signedDuty)
  }

  def calculateRepackagedDutyChange(
    repackagedDutyChangeRequest: RepackagedDutyChangeRequest
  ): AdjustmentDuty =
    AdjustmentDuty(repackagedDutyChangeRequest.newDuty - repackagedDutyChangeRequest.oldDuty)

  private def checkDutyValue(duty: BigDecimal, adjustmentType: AdjustmentType): BigDecimal =
    if (adjustmentType.equals(Underdeclaration) || adjustmentType.equals(RepackagedDraughtProducts)) {
      duty
    } else {
      duty * -1
    }
  def calculateAdjustmentTotal(
    adjustmentTotalCalculationRequest: AdjustmentTotalCalculationRequest
  ): AdjustmentDuty                                                                        =
    AdjustmentDuty(adjustmentTotalCalculationRequest.dutyList.sum)
}
