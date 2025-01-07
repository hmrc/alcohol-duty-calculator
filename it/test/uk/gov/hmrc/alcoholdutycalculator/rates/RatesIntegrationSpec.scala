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

package uk.gov.hmrc.alcoholdutycalculator.rates

import play.api.libs.json.Json
import play.api.test.FakeRequest
import uk.gov.hmrc.alcoholdutycalculator.base.ISpecBase
import uk.gov.hmrc.alcoholdutycalculator.controllers.routes
import uk.gov.hmrc.alcoholdutycalculator.models._

import java.time.YearMonth
import scala.util.Random

class RatesIntegrationSpec extends ISpecBase {

  private lazy val year: Int                = 2023
  private lazy val startMonthInclusive: Int = 1
  private lazy val endMonthExclusive: Int   = 13
  private lazy val month: Int               = Random.between(startMonthInclusive, endMonthExclusive)

  private lazy val ratePeriod: String = Json
    .toJson(
      YearMonth.of(year, month)
    )(RatePeriod.yearMonthFormat)
    .toString()

  s"service rates endpoint for period $ratePeriod must" - {
    "respond with 200 status" in {
      stubAuthorised()

      val urlParams =
        s"?ratePeriod=$ratePeriod&alcoholRegimes=Beer,Wine,OtherFermentedProduct"

      lazy val result =
        callRoute(FakeRequest("GET", routes.RatesController.rates().url + urlParams))

      status(result) mustBe OK
      val rateBandList = Json.parse(contentAsString(result)).as[Seq[RateBand]]
      rateBandList must have size 30
    }

    "service rate-band endpoint must" - {
      "respond with 200 status" in {
        stubAuthorised()
        val taxType     = "321"
        val urlParams   =
          s"?ratePeriod=$ratePeriod&taxTypeCode=$taxType"
        lazy val result =
          callRoute(FakeRequest("GET", routes.RatesController.rateBand().url + urlParams))
        status(result) mustBe OK
        val rateBand: RateBand = Json.parse(contentAsString(result)).as[RateBand]
        rateBand.rateType mustBe RateType.Core
      }
    }
  }
}
