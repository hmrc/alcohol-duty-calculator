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

case class DutyByTaxType(taxType: String, pureAlcoholVolume: BigDecimal, rate: BigDecimal)

object DutyByTaxType {
  implicit val formats: OFormat[DutyByTaxType] = Json.format[DutyByTaxType]
}
case class DutyCalculationByTaxTypeResponse(taxType: String, pureAlcoholVolume: BigDecimal, dutyRate: BigDecimal, amount: BigDecimal)

object DutyCalculationByTaxTypeResponse {
  implicit val formats: OFormat[DutyCalculationByTaxTypeResponse] = Json.format[DutyCalculationByTaxTypeResponse]
}

case class DutyTotalCalculationResponse(totalAmount: BigDecimal, totalsByTaxType: Seq[DutyCalculationByTaxTypeResponse])

object DutyTotalCalculationResponse {
  implicit val formats: OFormat[DutyTotalCalculationResponse] = Json.format[DutyTotalCalculationResponse]
}

case class DutyTotalCalculationRequest(dutiesByTaxType: Seq[DutyByTaxType])

object DutyTotalCalculationRequest {
  implicit val formats: OFormat[DutyTotalCalculationRequest] = Json.format[DutyTotalCalculationRequest]
}


