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

import play.api.libs.json.{Format, JsError, JsNumber, JsResult, JsSuccess, JsValue, Json, OFormat}

import scala.util.{Failure, Success, Try}

case class DutyCalculation(duty: BigDecimal)

object DutyCalculation {
  implicit val formats: OFormat[DutyCalculation] = Json.format[DutyCalculation]
}

case class Volume(value: BigDecimal) {
  require(value > BigDecimal(0) && value <= BigDecimal(999999999.99), "Volume must be between 0 and 999999999.99")
}

object Volume {

  def apply(value: BigDecimal): Volume = {
    require(value.scale <= 4, "Volume must have maximum 4 decimal place")
    new Volume(value)
  }

  implicit val format: Format[Volume] = new Format[Volume] {
    override def reads(json: JsValue): JsResult[Volume] = json.validate[BigDecimal] match {
      case JsSuccess(value, _) =>
        Try(Volume(value)) match {
          case Success(v)         => JsSuccess(v)
          case Failure(exception) => JsError(s"$value is not a valid Volume value. Failed with: $exception")
        }
      case e: JsError          => e
    }

    override def writes(o: Volume): JsValue = JsNumber(o.value)
  }
}

case class DutyCalculationRequest(
  pureAlcoholVolume: BigDecimal, //changed Volume to BigDecimal
  rate: BigDecimal,
  adjustmentType: String
)
case class AdjustmentDutyCalculationRequest(
  newDuty: BigDecimal,
  oldDuty: BigDecimal
)
object DutyCalculationRequest {
  implicit val formats: OFormat[DutyCalculationRequest] = Json.format[DutyCalculationRequest]
}
object AdjustmentDutyCalculationRequest {
  implicit val formats: OFormat[AdjustmentDutyCalculationRequest] = Json.format[AdjustmentDutyCalculationRequest]
}
