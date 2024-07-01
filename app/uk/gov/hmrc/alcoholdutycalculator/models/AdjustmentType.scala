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
import play.api.libs.json._

sealed trait AdjustmentType

object AdjustmentType {
  case object Underdeclaration extends AdjustmentType

  case object Overdeclaration extends AdjustmentType

  case object Spoilt extends AdjustmentType

  case object RepackagedDraughtProducts extends AdjustmentType

  case object Drawback extends AdjustmentType

  implicit val format: Format[AdjustmentType] = new Format[AdjustmentType] {
    override def reads(json: JsValue): JsResult[AdjustmentType] = json.validate[String] match {
      case JsSuccess(value, _) =>
        value match {
          case "under-declaration"                  => JsSuccess(Underdeclaration)
          case "over-declaration"                 => JsSuccess(Overdeclaration)
          case "spoilt"                  => JsSuccess(Spoilt)
          case "repackaged-draught-products"               => JsSuccess(RepackagedDraughtProducts)
          case "drawback" => JsSuccess(Drawback)
          case s                       => JsError(s"$s is not a valid AdjustmentType")
        }
      case e: JsError          => e
    }

    override def writes(o: AdjustmentType): JsValue = JsString(o.toString)
  }
}
