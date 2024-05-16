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

import uk.gov.hmrc.alcoholdutycalculator.models.{AlcoholByVolume, AlcoholRegime, RateBand, RatePeriod, RateType, RateTypeResponse}
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
    Json.parse(rateFileContent).as[Seq[RatePeriod]]
  }

  def rateBands(
    ratePeriodYearMonth: YearMonth,
    alcoholRegimes: Set[AlcoholRegime]
  ): Seq[RateBand] =
    alcoholDutyRates
      .filter(rp =>
        !ratePeriodYearMonth.isBefore(rp.validityStartDate) &&
          rp.validityEndDate.forall(_.isAfter(ratePeriodYearMonth))
      )
      .flatMap { ratePeriod =>
        ratePeriod.rateBands
          .filter(rb => rb.alcoholRegime.intersect(alcoholRegimes).nonEmpty)
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
    abv: AlcoholByVolume,
    alcoholRegimes: Set[AlcoholRegime]
  ): RateTypeResponse = {
    val rateTypes     = alcoholDutyRates
      .filter(rp =>
        !ratePeriodYearMonth.isBefore(rp.validityStartDate) &&
          rp.validityEndDate.forall(_.isAfter(ratePeriodYearMonth))
      )
      .flatMap { ratePeriod =>
        ratePeriod.rateBands
          .filter(rb =>
            rb.minABV.value <= abv.value &&
              rb.maxABV.value >= abv.value &&
              rb.alcoholRegime.intersect(alcoholRegimes).nonEmpty
          )
          .map(_.rateType)
          .toSet
      }
    val rateTypesList = List(DraughtAndSmallProducerRelief, DraughtRelief, SmallProducerRelief)
    RateTypeResponse(rateTypesList.find(rateTypes.contains).getOrElse(Core))
  }

}
