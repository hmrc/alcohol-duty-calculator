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

package uk.gov.hmrc.alcoholdutycalculator.controllers

import org.mockito.ArgumentMatchers.any
import play.api.libs.json.Json
import play.api.test.FakeRequest
import uk.gov.hmrc.alcoholdutycalculator.base.SpecBase
import uk.gov.hmrc.alcoholdutycalculator.models.TaxDuty
import uk.gov.hmrc.alcoholdutycalculator.services.DutyService

class DutyControllerSpec extends SpecBase {

  val mockDutyService = mock[DutyService]

  val controller = new DutyController(
    fakeAuthorisedAction,
    mockDutyService,
    cc
  )

  val duty = TaxDuty(BigDecimal(0.25), BigDecimal(1.25))

  "duty controller" should {
    "return 200 OK with duty and total volume based on query parameters" in {
      val abv: BigDecimal    = BigDecimal(1.0)
      val volume: BigDecimal = BigDecimal(1.0)
      val rate: BigDecimal   = BigDecimal(1.0)

      when(mockDutyService.calculateDuty(any(), any(), any())).thenReturn(duty)

      val urlWithParams = s"/calculate-duty?abv=${abv.toString}&volume=${volume.toString}&rate=${rate.toString}"

      val requestWithParams = FakeRequest("GET", urlWithParams)

      val result = controller.calculateDuty()(requestWithParams)

      status(result)        shouldBe OK
      contentAsJson(result) shouldBe Json.toJson(duty)

      verify(mockDutyService, times(1)).calculateDuty(abv, volume, rate)
    }

    "return Bad Request" when {
      "abv is missing" in {
        val volume: BigDecimal = BigDecimal(1.0)
        val rate: BigDecimal   = BigDecimal(1.0)

        val urlWithParams = s"/calculate-duty?volume=${volume.toString}&rate=${rate.toString}"

        val requestWithParams = FakeRequest("GET", urlWithParams)

        val result = controller.calculateDuty()(requestWithParams)

        status(result)        shouldBe BAD_REQUEST
        contentAsString(result) should include("Missing or invalid 'abv' parameter")
      }

      "volume is missing" in {
        val abv: BigDecimal  = BigDecimal(1.0)
        val rate: BigDecimal = BigDecimal(1.0)

        val urlWithParams = s"/calculate-duty?abv=${abv.toString}&rate=${rate.toString}"

        val requestWithParams = FakeRequest("GET", urlWithParams)

        val result = controller.calculateDuty()(requestWithParams)

        status(result)        shouldBe BAD_REQUEST
        contentAsString(result) should include("Missing or invalid 'volume' parameter")
      }

      "rate is missing" in {
        val abv: BigDecimal    = BigDecimal(1.0)
        val volume: BigDecimal = BigDecimal(1.0)

        val urlWithParams = s"/calculate-duty?abv=${abv.toString}&volume=${volume.toString}"

        val requestWithParams = FakeRequest("GET", urlWithParams)

        val result = controller.calculateDuty()(requestWithParams)

        status(result)        shouldBe BAD_REQUEST
        contentAsString(result) should include("Missing or invalid 'rate' parameter")
      }

      "abv is invalid" in {
        val abv: String        = "invalid"
        val volume: BigDecimal = BigDecimal(1.0)
        val rate: BigDecimal   = BigDecimal(1.0)

        val urlWithParams = s"/calculate-duty?abv=$abv&volume=${volume.toString}&rate=${rate.toString}"

        val requestWithParams = FakeRequest("GET", urlWithParams)

        val result = controller.calculateDuty()(requestWithParams)

        status(result)        shouldBe BAD_REQUEST
        contentAsString(result) should include("Invalid 'abv' parameter")
      }

      "volume is invalid" in {
        val abv: BigDecimal  = BigDecimal(1.0)
        val volume: String   = "invalid"
        val rate: BigDecimal = BigDecimal(1.0)

        val urlWithParams = s"/calculate-duty?abv=${abv.toString}&volume=$volume&rate=${rate.toString}"

        val requestWithParams = FakeRequest("GET", urlWithParams)

        val result = controller.calculateDuty()(requestWithParams)

        status(result)        shouldBe BAD_REQUEST
        contentAsString(result) should include("Invalid 'volume' parameter")
      }

      "rate is invalid" in {
        val abv: BigDecimal    = BigDecimal(1.0)
        val volume: BigDecimal = BigDecimal(1.0)
        val rate: String       = "invalid"

        val urlWithParams = s"/calculate-duty?abv=${abv.toString}&volume=${volume.toString}&rate=$rate"

        val requestWithParams = FakeRequest("GET", urlWithParams)

        val result = controller.calculateDuty()(requestWithParams)

        status(result)        shouldBe BAD_REQUEST
        contentAsString(result) should include("Invalid 'rate' parameter")
      }

    }

  }

}
