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

package uk.gov.hmrc.alcoholdutycalculator

import play.api.libs.json.Json
import play.api.test.FakeRequest
import uk.gov.hmrc.alcoholdutycalculator.controllers.routes
import uk.gov.hmrc.alcoholdutycalculator.base.ISpecBase
import uk.gov.hmrc.alcoholdutycalculator.models.AdjustmentType.Underdeclaration
import uk.gov.hmrc.alcoholdutycalculator.models._

class DutyCalculationIntegrationSpec extends ISpecBase {

  "service duty calculation endpoint" should {
    "respond with 200 status" in {
      stubAuthorised()

      lazy val result = callRoute(
        FakeRequest("POST", routes.DutyCalculationController.calculateDuty().url)
          .withBody(Json.toJson(DutyCalculationRequest(Underdeclaration, BigDecimal(1), BigDecimal(1))))
      )

      status(result) shouldBe OK
      val dutyCalculation = Json.parse(contentAsString(result)).as[DutyCalculation]
      dutyCalculation.duty shouldBe BigDecimal(1)
    }
  }

  "service total duties calculation endpoint" should {
    "respond with 200 status" in {
      stubAuthorised()

      lazy val result = callRoute(
        FakeRequest("POST", routes.DutyCalculationController.calculateTotalDuty().url)
          .withBody(
            Json.toJson(
              DutyTotalCalculationRequest(
                Seq(
                  DutyByTaxType("taxType", BigDecimal(1), BigDecimal(1), BigDecimal(1)),
                  DutyByTaxType("taxType2", BigDecimal(2), BigDecimal(2), BigDecimal(2))
                )
              )
            )
          )
      )

      status(result) shouldBe OK
      val dutyCalculation = Json.parse(contentAsString(result)).as[DutyTotalCalculationResponse]
      dutyCalculation.totalDuty            shouldBe BigDecimal(5.0)
      dutyCalculation.dutiesByTaxType.size shouldBe 2
      dutyCalculation.dutiesByTaxType      shouldBe Seq(
        DutyCalculationByTaxTypeResponse("taxType", BigDecimal(1), BigDecimal(1), BigDecimal(1), BigDecimal(1.0)),
        DutyCalculationByTaxTypeResponse("taxType2", BigDecimal(2), BigDecimal(2), BigDecimal(2), BigDecimal(4.0))
      )
    }
  }
}
