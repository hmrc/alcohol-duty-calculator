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

import uk.gov.hmrc.alcoholdutycalculator.models.{AdjustmentDutyCalculationRequest, DutyCalculation, DutyCalculationRequest}

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton()
class DutyService @Inject() (implicit val ec: ExecutionContext) {
  def calculateDuty(dutyCalculationRequest: DutyCalculationRequest): DutyCalculation = { //calculateAdjustment
    val duty       =
      (dutyCalculationRequest.pureAlcoholVolume * dutyCalculationRequest.rate).setScale(2, BigDecimal.RoundingMode.DOWN)
    val signedDuty = checkDutyValue(duty, dutyCalculationRequest.adjustmentType)
    println(s"signedDuty $signedDuty")
    DutyCalculation(signedDuty)
  }

  def calculateAdjustmentDuty(adjustmentDutyCalculationRequest: AdjustmentDutyCalculationRequest): DutyCalculation =
    DutyCalculation(adjustmentDutyCalculationRequest.newDuty - adjustmentDutyCalculationRequest.oldDuty)

  private def checkDutyValue(duty: BigDecimal, adjustmentType: String): BigDecimal =
    if (adjustmentType.equals("under-declaration") || adjustmentType.equals("repackaged-draught-products")) {
      duty
    } else {
      duty * -1
    }
}
