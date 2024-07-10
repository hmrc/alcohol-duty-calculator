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

import org.apache.pekko.stream.Materializer
import org.apache.pekko.stream.testkit.NoMaterializer
import org.mockito.ArgumentMatchers.any
import play.api.libs.json.Json
import play.api.test.FakeRequest
import uk.gov.hmrc.alcoholdutycalculator.base.SpecBase
import uk.gov.hmrc.alcoholdutycalculator.models.AdjustmentType.Spoilt
import uk.gov.hmrc.alcoholdutycalculator.models.{AdjustmentDutyCalculationRequest, DutyByTaxType, DutyCalculation, DutyCalculationByTaxTypeResponse, DutyTotalCalculationRequest, DutyTotalCalculationResponse}
import uk.gov.hmrc.alcoholdutycalculator.services.DutyService

class DutyCalculationControllerSpec extends SpecBase {

  implicit lazy val materializer: Materializer = NoMaterializer

  val mockDutyService = mock[DutyService]

  val controller = new DutyCalculationController(
    fakeAuthorisedAction,
    mockDutyService,
    cc
  )

  val dutyCalculationRequest = AdjustmentDutyCalculationRequest(
    Spoilt,
    pureAlcoholVolume = BigDecimal(1.0),
    rate = BigDecimal(1.0)
  )
  val duty                   = DutyCalculation(BigDecimal(1.00))

  val totalCalculationRequest = DutyTotalCalculationRequest(
    Seq(
      DutyByTaxType(
        taxType = "taxType",
        totalLitres = BigDecimal(1.0),
        pureAlcohol = BigDecimal(1.0),
        dutyRate = BigDecimal(1.0)
      ),
      DutyByTaxType(
        taxType = "taxType2",
        totalLitres = BigDecimal(2.0),
        pureAlcohol = BigDecimal(2.0),
        dutyRate = BigDecimal(2.0)
      )
    )
  )

  val totalCalculationResponse = DutyTotalCalculationResponse(
    totalDuty = BigDecimal(3.0),
    dutiesByTaxType = Seq(
      DutyCalculationByTaxTypeResponse(
        taxType = "taxType",
        totalLitres = BigDecimal(1.0),
        pureAlcohol = BigDecimal(1.0),
        dutyRate = BigDecimal(1.0),
        dutyDue = BigDecimal(1.0)
      ),
      DutyCalculationByTaxTypeResponse(
        taxType = "taxType2",
        totalLitres = BigDecimal(2.0),
        pureAlcohol = BigDecimal(2.0),
        dutyRate = BigDecimal(2.0),
        dutyDue = BigDecimal(2.0)
      )
    )
  )

  "duty controller" should {
    "return 200 OK with the DutyCalculation" in {
      when(mockDutyService.calculateDuty(any())).thenReturn(duty)

      val fakeRequest = FakeRequest(method = "POST", path = "/calculate-duty")
        .withHeaders("Authorization" -> "Token some-token")
        .withBody(Json.toJson(dutyCalculationRequest))

      val result = controller.calculateDuty()(fakeRequest)

      status(result) shouldBe OK
      val body = contentAsJson(result).as[DutyCalculation]
      body shouldBe duty
    }

    "return 400 BAD REQUEST" when {
      "the AlcoholByVolume in the request is not valid" in {
        val fakeRequest = FakeRequest(method = "POST", path = "/calculate-duty")
          .withHeaders("Authorization" -> "Token some-token")
          .withBody(Json.parse("""{"abv": -1.123, "volume": 1.0, "rate": 1.0}"""))

        val result = controller.calculateDuty()(fakeRequest)

        status(result) shouldBe BAD_REQUEST
      }

      "the Volume in the request is not valid" in {
        val fakeRequest = FakeRequest(method = "POST", path = "/calculate-duty")
          .withHeaders("Authorization" -> "Token some-token")
          .withBody(Json.parse("""{"abv": 1.0, "volume": -1.12345, "rate": 1.0}"""))

        val result = controller.calculateDuty()(fakeRequest)

        status(result) shouldBe BAD_REQUEST
      }
    }

    "return 415 UNSUPPORTED MEDIA TYPE" when {
      "there is no body in the request" in {
        val fakeRequest = FakeRequest(method = "POST", path = "/calculate-duty")
          .withHeaders("Authorization" -> "Token some-token")

        val result      = controller.calculateDuty()(fakeRequest)

        status(result) shouldBe UNSUPPORTED_MEDIA_TYPE
      }
    }

    "return 200 OK with the total duty calculation" in {
      when(mockDutyService.calculateTotalDuty(any())).thenReturn(totalCalculationResponse)

      val fakeRequest = FakeRequest(method = "POST", path = "/calculate-total-duty")
        .withHeaders("Authorization" -> "Token some-token")
        .withBody(Json.toJson(totalCalculationRequest))

      val result = controller.calculateTotalDuty()(fakeRequest)

      status(result) shouldBe OK
      val body = contentAsJson(result).as[DutyTotalCalculationResponse]
      body shouldBe totalCalculationResponse
    }

    "return 400 BAD REQUEST the  the request is not valid" in {
      val fakeRequest = FakeRequest(method = "POST", path = "/calculate-duty")
        .withHeaders("Authorization" -> "Token some-token")
        .withBody(Json.parse("""{"abv": -1.123, "volume": 1.0, "rate": 1.0}"""))

      val result = controller.calculateTotalDuty()(fakeRequest)

      status(result) shouldBe BAD_REQUEST
    }
  }
}
