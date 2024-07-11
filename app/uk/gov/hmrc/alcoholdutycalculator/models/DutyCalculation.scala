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

import play.api.libs.json.{Json, OFormat}

case class AdjustmentDutyCalculation(duty: BigDecimal)

object AdjustmentDutyCalculation {
  implicit val formats: OFormat[AdjustmentDutyCalculation] = Json.format[AdjustmentDutyCalculation]
}

case class AdjustmentDutyCalculationRequest(
  adjustmentType: AdjustmentType,
  pureAlcoholVolume: BigDecimal,
  rate: BigDecimal
)
object AdjustmentDutyCalculationRequest {
  implicit val formats: OFormat[AdjustmentDutyCalculationRequest] = Json.format[AdjustmentDutyCalculationRequest]
}
case class RepackagedDutyChangeRequest(
  newDuty: BigDecimal,
  oldDuty: BigDecimal
)
object RepackagedDutyChangeRequest {
  implicit val formats: OFormat[RepackagedDutyChangeRequest] = Json.format[RepackagedDutyChangeRequest]
}
case class AdjustmentTotalCalculationRequest(
  dutyList: Seq[BigDecimal]
)
object AdjustmentTotalCalculationRequest {
  implicit val formats: OFormat[AdjustmentTotalCalculationRequest] = Json.format[AdjustmentTotalCalculationRequest]
}
