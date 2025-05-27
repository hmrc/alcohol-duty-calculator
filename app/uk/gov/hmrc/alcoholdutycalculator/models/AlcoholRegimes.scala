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

package uk.gov.hmrc.alcoholdutycalculator.models

import enumeratum.{Enum, EnumEntry, PlayEnum}
import play.api.libs.json._

sealed trait AlcoholRegime

object AlcoholRegime {
  case object Beer extends AlcoholRegime
  case object Cider extends AlcoholRegime
  case object Wine extends AlcoholRegime
  case object Spirits extends AlcoholRegime
  case object OtherFermentedProduct extends AlcoholRegime

  def fromString(str: String): Either[String, AlcoholRegime] = str match {
    case "Beer"                  => Right(Beer)
    case "Cider"                 => Right(Cider)
    case "Wine"                  => Right(Wine)
    case "Spirits"               => Right(Spirits)
    case "OtherFermentedProduct" => Right(OtherFermentedProduct)
    case str                     => Left(s"$str is not a valid AlcoholRegime")
  }

  implicit val format: Format[AlcoholRegime] = new Format[AlcoholRegime] {
    override def reads(json: JsValue): JsResult[AlcoholRegime] = json.validate[String] match {
      case JsSuccess(value, _) =>
        value match {
          case "Beer"                  => JsSuccess(Beer)
          case "Cider"                 => JsSuccess(Cider)
          case "Wine"                  => JsSuccess(Wine)
          case "Spirits"               => JsSuccess(Spirits)
          case "OtherFermentedProduct" => JsSuccess(OtherFermentedProduct)
          case s                       => JsError(s"$s is not a valid AlcoholRegime")
        }
      case e: JsError          => e
    }

    override def writes(o: AlcoholRegime): JsValue = JsString(o.toString)
  }
}

sealed trait AlcoholType extends EnumEntry
object AlcoholType extends Enum[AlcoholType] with PlayEnum[AlcoholType] {
  val values = findValues

  case object Beer extends AlcoholType
  case object Cider extends AlcoholType
  case object SparklingCider extends AlcoholType
  case object Wine extends AlcoholType
  case object Spirits extends AlcoholType
  case object OtherFermentedProduct extends AlcoholType
}
