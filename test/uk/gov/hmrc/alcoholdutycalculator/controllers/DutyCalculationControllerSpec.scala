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
import org.mockito.Mockito.when
import play.api.libs.json.Json
import uk.gov.hmrc.alcoholdutycalculator.base.SpecBase
import uk.gov.hmrc.alcoholdutycalculator.models.*
import uk.gov.hmrc.alcoholdutycalculator.services.DutyService

class DutyCalculationControllerSpec extends SpecBase {
  "DutyCalculationController when" - {
    "calculating adjustment duty must" - {
      "return 200 OK with the AdjustmentDuty" in new SetUp {
        when(mockDutyService.calculateAdjustmentDuty(any())).thenReturn(duty)

        val result =
          controller.calculateAdjustmentDuty()(fakeRequestWithJsonBody(Json.toJson(adjustmentDutyCalculationRequest)))

        status(result)                           mustBe OK
        contentAsJson(result).as[AdjustmentDuty] mustBe duty
      }

      "return 400 BAD REQUEST when the AdjustmentType in the request is not valid" in new SetUp {
        val result = controller.calculateAdjustmentDuty()(
          fakeRequestWithJsonBody(Json.parse("""{"adjustmentType": -1.123, "volume": 1.0, "rate": 1.0}"""))
        )

        status(result) mustBe BAD_REQUEST
      }
    }

    "calculating adjustment repackaged duty must" - {
      "return 200 OK with the AdjustmentDuty" in new SetUp {
        when(mockDutyService.calculateRepackagedDutyChange(any())).thenReturn(duty)

        val result =
          controller.calculateRepackagedDutyChange()(fakeRequestWithJsonBody(Json.toJson(repackagedDutyChangeRequest)))

        status(result)                           mustBe OK
        contentAsJson(result).as[AdjustmentDuty] mustBe duty
      }

      "return 400 BAD REQUEST when the request is not valid" in new SetUp {
        val result = controller.calculateRepackagedDutyChange()(
          fakeRequestWithJsonBody(Json.parse("""{"newDuty": "sd", "oldDuty": "test"}"""))
        )

        status(result) mustBe BAD_REQUEST
      }
    }

    "calculating adjustment total must" - {
      "return 200 OK with the AdjustmentDuty" in new SetUp {
        when(mockDutyService.calculateAdjustmentTotal(any())).thenReturn(duty)

        val result =
          controller.calculateTotalAdjustment()(fakeRequestWithJsonBody(Json.toJson(adjustmentTotalCalculationRequest)))

        status(result)                           mustBe OK
        contentAsJson(result).as[AdjustmentDuty] mustBe duty
      }

      "return 400 BAD REQUEST when the request is not valid" in new SetUp {
        val result =
          controller.calculateTotalAdjustment()(fakeRequestWithJsonBody(Json.parse("""{"dutyList": "json"}""")))

        status(result) mustBe BAD_REQUEST
      }
    }

    "calculating total duty duty must" - {
      "return 200 OK with the total duty calculation" in new SetUp {
        when(mockDutyService.calculateTotalDuty(any())).thenReturn(totalCalculationResponse)

        val result = controller.calculateTotalDuty()(fakeRequestWithJsonBody(Json.toJson(totalCalculationRequest)))

        status(result)                                         mustBe OK
        contentAsJson(result).as[DutyTotalCalculationResponse] mustBe totalCalculationResponse
      }

      "return 400 BAD REQUEST when the request is not valid" in new SetUp {
        val result = controller.calculateTotalDuty()(
          fakeRequestWithJsonBody(Json.parse("""{"abv": -1.123, "volume": 1.0, "rate": 1.0}"""))
        )

        status(result) mustBe BAD_REQUEST
      }
    }

    "calculateTotalDutyDueByTaxType must" - {
      "return 200 OK with the total duty due by tax type calculation" in new SetUp {
        when(mockDutyService.calculateDutyByTaxType(any())).thenReturn(calculatedDutyDueByTaxType)

        val result = controller.calculateTotalDutyDueByTaxType()(
          fakeRequestWithJsonBody(Json.toJson(calculateDutyDueByTaxTypeRequest))
        )

        status(result)                                       mustBe OK
        contentAsJson(result).as[CalculatedDutyDueByTaxType] mustBe calculatedDutyDueByTaxType
      }

      "return 400 BAD REQUEST when the request is not valid" in new SetUp {
        val result = controller.calculateTotalDutyDueByTaxType()(
          fakeRequestWithJsonBody(Json.parse("""{"totalDutyDueByTaxType":1}"""))
        )

        status(result) mustBe BAD_REQUEST
      }
    }
  }

  class SetUp {
    implicit lazy val materializer: Materializer = NoMaterializer

    val mockDutyService = mock[DutyService]

    val controller = new DutyCalculationController(
      fakeAuthorisedAction,
      mockDutyService,
      cc
    )
  }
}
