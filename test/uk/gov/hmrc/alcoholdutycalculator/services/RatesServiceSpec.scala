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

import org.mockito.ArgumentMatchers.any
import play.api.Environment
import play.api.libs.json.{JsArray, Json}
import uk.gov.hmrc.alcoholdutycalculator.base.SpecBase
import uk.gov.hmrc.alcoholdutycalculator.config.AppConfig
import uk.gov.hmrc.alcoholdutycalculator.models.AlcoholRegime.{Beer, Cider, Spirits, Wine}
import uk.gov.hmrc.alcoholdutycalculator.models.RateType.Core
import uk.gov.hmrc.alcoholdutycalculator.models.{AlcoholByVolume, RateBand, RatePeriod}

import java.io.ByteArrayInputStream
import java.time.YearMonth

class RatesServiceSpec extends SpecBase {

  "getAllRates" should {
    "return the list of rates from a file" in {

      val mockEnv    = mock[Environment]
      val mockConfig = mock[AppConfig]
      when(mockConfig.alcoholDutyRatesFile).thenReturn("foo")

      val ratePeriods = Json
        .obj(
          "periods" -> JsArray(
            Seq(
              Json.obj(
                "name"              -> "2023-1",
                "isLatest"          -> true,
                "validityStartDate" -> "2023-01",
                "validityEndDate"   -> "2024-01",
                "rateBands"         -> JsArray(
                  Seq(
                    Json.obj(
                      "taxType"       -> "301",
                      "description"   -> "Low Alcohol - not exc 1.2%",
                      "rateType"      -> "Core",
                      "alcoholRegime" -> Seq(
                        "Beer",
                        "Wine",
                        "Cider",
                        "Spirits"
                      ),
                      "minABV"        -> 3,
                      "maxABV"        -> 9.9,
                      "rate"          -> 100.99
                    )
                  )
                )
              )
            )
          )
        )
        .toString()

      when(mockEnv.resourceAsStream(any())).thenReturn(Some(new ByteArrayInputStream(ratePeriods.getBytes)))

      val service = new RatesService(mockEnv, mockConfig)

      val result = service.alcoholDutyRates
      result shouldBe List(
        RatePeriod(
          "2023-1",
          isLatest = true,
          YearMonth.parse("2023-01"),
          Some(YearMonth.parse("2024-01")),
          List(
            RateBand(
              "301",
              "Low Alcohol - not exc 1.2%",
              Core,
              Set(Beer, Wine, Cider, Spirits),
              AlcoholByVolume(3),
              AlcoholByVolume(9.9),
              BigDecimal(100.99)
            )
          )
        )
      )
    }

    "throws an exception when the file can't be opened" in {

      val mockEnv    = mock[Environment]
      val mockConfig = mock[AppConfig]
      when(mockConfig.alcoholDutyRatesFile).thenReturn("foo")

      when(mockEnv.resourceAsStream(any())).thenReturn(None)

      the[Exception] thrownBy {
        val service = new RatesService(mockEnv, mockConfig)
        service.alcoholDutyRates
      } should have message "Could not open Alcohol Duty Rate file"
    }
  }
}
