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

package uk.gov.hmrc.alcoholdutycalculator.services

import play.api.Environment
import play.api.libs.json.Json
import uk.gov.hmrc.alcoholdutycalculator.config.AppConfig

import uk.gov.hmrc.alcoholdutycalculator.models.{AlcoholByVolume, AlcoholRegimeName, RateBand, RatePeriod, RateTypeResponse}
import uk.gov.hmrc.alcoholdutycalculator.models.RateType.{Core, DraughtAndSmallProducerRelief, DraughtRelief, SmallProducerRelief}

import java.time.YearMonth
import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext
import scala.io.Source

@Singleton()
class RatesService @Inject() (env: Environment, appConfig: AppConfig)(implicit val ec: ExecutionContext) {

  val alcoholDutyRates: Seq[RatePeriod] = {
    val rateFileContent: String = env
      .resourceAsStream(appConfig.alcoholDutyRatesFile)
      .fold(throw new Exception("Could not open Alcohol Duty Rate file"))(Source.fromInputStream)
      .mkString
    val res                     = Json.parse(rateFileContent).as[Seq[RatePeriod]]

    println(res)

    res
  }

  def rateBands(
    ratePeriodYearMonth: YearMonth,
    alcoholRegimes: Set[AlcoholRegimeName]
  ): Seq[RateBand] =
    alcoholDutyRates
      .filter(rp =>
        !ratePeriodYearMonth.isBefore(rp.validityStartDate) &&
          rp.validityEndDate.forall(_.isAfter(ratePeriodYearMonth))
      )
      .flatMap { ratePeriod =>
        ratePeriod.rateBands
          .filter(rb => rb.alcoholRegimes.map(_.name).intersect(alcoholRegimes).nonEmpty)
      }

  def taxType(
    ratePeriodYearMonth: YearMonth,
    taxType: String
  ): Option[RateBand] =
    alcoholDutyRates
      .filter(rp =>
        !ratePeriodYearMonth.isBefore(rp.validityStartDate) &&
          rp.validityEndDate.forall(_.isAfter(ratePeriodYearMonth))
      )
      .flatMap(_.rateBands)
      .find(rb => rb.taxType == taxType)

  def rateTypes(
    ratePeriodYearMonth: YearMonth,
    alcoholRegimes: Set[AlcoholRegimeName]
  ): RateTypeResponse = {
    val rateTypes     = alcoholDutyRates
      .filter(rp =>
        !ratePeriodYearMonth.isBefore(rp.validityStartDate) &&
          rp.validityEndDate.forall(_.isAfter(ratePeriodYearMonth))
      )
      .flatMap { ratePeriod =>
        ratePeriod.rateBands
          .filter(rb => rb.alcoholRegimes.map(_.name).intersect(alcoholRegimes).nonEmpty)
          .map(_.rateType)
          .toSet
      }
    val rateTypesList = List(DraughtAndSmallProducerRelief, DraughtRelief, SmallProducerRelief)
    RateTypeResponse(rateTypesList.find(rateTypes.contains).getOrElse(Core))
  }

}
