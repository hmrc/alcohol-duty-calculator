/*
 * Copyright 2023 HM Revenue & Customs
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

import java.time.YearMonth
import scala.util.{Failure, Success, Try}

sealed trait RateType

object RateType {

  case object Core extends RateType
  case object DraughtRelief extends RateType
  case object SmallProducerRelief extends RateType
  case object DraughtAndSmallProducerRelief extends RateType

  implicit val format: Format[RateType] = new Format[RateType] {
    override def reads(json: JsValue): JsResult[RateType] = json.validate[String] match {
      case JsSuccess(value, _) =>
        value match {
          case "Core"                          => JsSuccess(Core)
          case "DraughtRelief"                 => JsSuccess(DraughtRelief)
          case "SmallProducerRelief"           => JsSuccess(SmallProducerRelief)
          case "DraughtAndSmallProducerRelief" => JsSuccess(DraughtAndSmallProducerRelief)
          case s                               => JsError(s"$s is not a valid RateType")
        }
      case e: JsError          => e
    }

    override def writes(o: RateType): JsValue = JsString(o.toString)
  }
}

case class ABVRange(alcoholType: AlcoholType, minABV: AlcoholByVolume, maxABV: AlcoholByVolume)

object ABVRange {
  implicit val format: Format[ABVRange] = Json.format[ABVRange]
}

case class RangeDetailsByRegime(alcoholRegime: AlcoholRegime, abvRanges: Seq[ABVRange])

object RangeDetailsByRegime {
  implicit val format: Format[RangeDetailsByRegime] = Json.format[RangeDetailsByRegime]
}

case class AlcoholByVolume private (value: BigDecimal) {
  require(value >= 0 && value <= 100, "Percentage must be between 0 and 100")
}

object AlcoholByVolume {
  def apply(value: BigDecimal): AlcoholByVolume = {
    require(value.scale <= 1, "Alcohol By Volume must have maximum 1 decimal place")
    new AlcoholByVolume(value)
  }

  implicit val format: Format[AlcoholByVolume] = new Format[AlcoholByVolume] {
    override def reads(json: JsValue): JsResult[AlcoholByVolume] = json.validate[BigDecimal] match {
      case JsSuccess(value, _) =>
        Try(AlcoholByVolume(value)) match {
          case Success(v)         => JsSuccess(v)
          case Failure(exception) => JsError(s"$value is not a valid Alcohol By Volume value. Failed with: $exception")
        }
      case e: JsError          => e
    }

    override def writes(o: AlcoholByVolume): JsValue = JsNumber(o.value)
  }
}

case class RateBand(
  taxTypeCode: String,
  description: String,
  rateType: RateType,
  rangeDetails: Set[RangeDetailsByRegime],
  rate: Option[BigDecimal]
)

object RateBand {
  implicit val formats: OFormat[RateBand] = Json.format[RateBand]
}

case class RatePeriod(
  name: String,
  validityStartDate: YearMonth,
  validityEndDate: Option[YearMonth],
  rateBands: Seq[RateBand]
)

object RatePeriod {

  implicit val yearMonthFormat: Format[YearMonth] = new Format[YearMonth] {
    override def reads(json: JsValue): JsResult[YearMonth] =
      json.validate[String].map(YearMonth.parse)

    override def writes(yearMonth: YearMonth): JsValue =
      JsString(yearMonth.toString)
  }

  implicit val optionYearMonthFormat: Format[Option[YearMonth]] = new Format[Option[YearMonth]] {
    override def reads(json: JsValue): JsResult[Option[YearMonth]] =
      json.validateOpt[YearMonth](yearMonthFormat)

    override def writes(o: Option[YearMonth]): JsValue =
      o.map(yearMonthFormat.writes).getOrElse(JsNull)
  }
  implicit val formats: OFormat[RatePeriod]                     = Json.format[RatePeriod]
}
