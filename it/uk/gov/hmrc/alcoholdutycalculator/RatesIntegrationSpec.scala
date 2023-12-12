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
import uk.gov.hmrc.alcoholdutycalculator.models.RatePeriod

class RatesIntegrationSpec extends ISpecBase {

  "service health endpoint" should {
    "respond with 200 status" in {
      stubAuthorised()

      lazy val result =
        callRoute(FakeRequest(routes.RatesController.rates()))

      status(result) shouldBe OK
      val ratePeriodList = (Json.parse(contentAsString(result))).as[Seq[RatePeriod]]
      ratePeriodList shouldBe a[Seq[RatePeriod]]
      ratePeriodList   should not be empty
    }
  }
}
