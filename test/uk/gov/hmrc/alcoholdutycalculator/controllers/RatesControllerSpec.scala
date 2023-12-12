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

package uk.gov.hmrc.alcoholdutycalculator.controllers

import play.api.libs.json.Json
import play.api.mvc.Result
import uk.gov.hmrc.alcoholdutycalculator.base.SpecBase
import uk.gov.hmrc.alcoholdutycalculator.models.RatePeriod
import uk.gov.hmrc.alcoholdutycalculator.services.RatesService

import scala.concurrent.Future

class RatesControllerSpec extends SpecBase {

  val mockRatesService: RatesService = mock[RatesService]

  val controller = new RatesController(
    fakeAuthorisedAction,
    mockRatesService,
    cc
  )

  "rates" should {
    "return 200 OK with alcohol duty rates" in forAll { ratePeriodList: Seq[RatePeriod] =>
      when(mockRatesService.alcoholDutyRates).thenReturn(ratePeriodList)

      val result: Future[Result] =
        controller.rates()(fakeRequest)

      status(result)        shouldBe OK
      contentAsJson(result) shouldBe Json.toJson(ratePeriodList)
    }
  }
}
