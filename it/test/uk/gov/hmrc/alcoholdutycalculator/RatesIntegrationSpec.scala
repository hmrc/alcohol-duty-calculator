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

package uk.gov.hmrc.alcoholdutycalculator

import play.api.libs.json.Json
import play.api.test.FakeRequest
import uk.gov.hmrc.alcoholdutycalculator.base.ISpecBase
import uk.gov.hmrc.alcoholdutycalculator.controllers.routes
import uk.gov.hmrc.alcoholdutycalculator.models.AlcoholRegime.{Beer, OtherFermentedProduct, Wine}
import uk.gov.hmrc.alcoholdutycalculator.models.RateType.Core
import uk.gov.hmrc.alcoholdutycalculator.models.{AlcoholRegime, RateBand, RatePeriod, RateType}

import java.time.YearMonth

class RatesIntegrationSpec extends ISpecBase {

  "service rates endpoint" should {
    "respond with 200 status" in {
      stubAuthorised()

      val urlParams =
        s"?ratePeriod=${Json.toJson(YearMonth.of(2023, 5))(RatePeriod.yearMonthFormat).toString()}&rateType=${Json
          .toJson[RateType](Core)
          .toString}&abv=${Json.toJson(3).toString}&alcoholRegimes=${Json
          .toJson(Set(Json.toJson[AlcoholRegime](Beer), Json.toJson[AlcoholRegime](Wine), Json.toJson[AlcoholRegime](OtherFermentedProduct)))
          .toString()}"

      lazy val result =
        callRoute(FakeRequest("GET", routes.RatesController.rates().url + urlParams))

      status(result) shouldBe OK
      val rateBandList = Json.parse(contentAsString(result)).as[Seq[RateBand]]
      rateBandList should have size 3
    }
  }
}
