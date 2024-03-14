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
import uk.gov.hmrc.alcoholdutycalculator.models.RateType.{Core, DraughtAndSmallProducerRelief, DraughtRelief, SmallProducerRelief}
import uk.gov.hmrc.alcoholdutycalculator.models.{AlcoholByVolume, RateBand, RatePeriod}

import java.io.ByteArrayInputStream
import java.time.YearMonth

class RatesServiceSpec extends SpecBase {

  "alcoholDutyRates" should {
    "return the list of rates from a file" in {

      val mockEnv    = mock[Environment]
      val mockConfig = mock[AppConfig]
      when(mockConfig.alcoholDutyRatesFile).thenReturn("foo")

      val ratePeriods =
        JsArray(
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
              Some(BigDecimal(100.99))
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

  "rateBands" should {

    val baseRateBand = RateBand(
      "taxTypeBase",
      "descriptionBase",
      Core,
      Set(Beer),
      AlcoholByVolume(3),
      AlcoholByVolume(9.9),
      Some(BigDecimal(100.99))
    )

    val baseRatePeriod = RatePeriod(
      "2023-base",
      isLatest = true,
      YearMonth.of(2023, 1),
      Some(YearMonth.of(2024, 1)),
      List(baseRateBand)
    )

    val ratePeriods = Seq(
      baseRatePeriod.copy(
        name = "2023",
        validityStartDate = YearMonth.of(2023, 1),
        validityEndDate = Some(YearMonth.of(2024, 1)),
        rateBands = List(
          baseRateBand.copy(taxType = "2023-1", minABV = AlcoholByVolume(0), maxABV = AlcoholByVolume(5)),
          baseRateBand.copy(taxType = "2023-2", minABV = AlcoholByVolume(5.1), maxABV = AlcoholByVolume(7)),
          baseRateBand.copy(taxType = "2023-3", minABV = AlcoholByVolume(7.1), maxABV = AlcoholByVolume(9)),
          baseRateBand.copy(taxType = "2023-4", minABV = AlcoholByVolume(8), maxABV = AlcoholByVolume(18))
        )
      ),
      baseRatePeriod.copy(
        name = "2024",
        validityStartDate = YearMonth.of(2024, 1),
        validityEndDate = Some(YearMonth.of(2025, 1)),
        rateBands = List(
          baseRateBand.copy(taxType = "2024-1"),
          baseRateBand.copy(taxType = "2024-2", rateType = SmallProducerRelief),
          baseRateBand.copy(taxType = "2024-3", rateType = DraughtRelief),
          baseRateBand.copy(taxType = "2024-4", rateType = DraughtAndSmallProducerRelief)
        )
      ),
      baseRatePeriod.copy(
        name = "2025",
        validityStartDate = YearMonth.of(2025, 1),
        validityEndDate = None,
        rateBands = List(
          baseRateBand.copy(taxType = "2025-1", alcoholRegime = Set(Beer), rateType = DraughtRelief),
          baseRateBand.copy(taxType = "2025-2", alcoholRegime = Set(Beer, Wine)),
          baseRateBand.copy(taxType = "2025-3", alcoholRegime = Set(Beer, Wine, Cider)),
          baseRateBand.copy(taxType = "2025-4", alcoholRegime = Set(Beer, Wine, Cider, Spirits))
        )
      )
    )

    "filter rateBands by year" in {
      val mockEnv    = mock[Environment]
      val mockConfig = mock[AppConfig]
      when(mockConfig.alcoholDutyRatesFile).thenReturn("foo")

      val rateFileContent = Json.toJson(ratePeriods).toString()
      when(mockEnv.resourceAsStream(any())).thenReturn(Some(new ByteArrayInputStream(rateFileContent.getBytes)))

      val service = new RatesService(mockEnv, mockConfig)

      service
        .rateBands(YearMonth.of(2023, 1), Core, AlcoholByVolume(5), Set(Beer))
        .head
        .taxType shouldBe "2023-1"

      service
        .rateBands(YearMonth.of(2023, 12), Core, AlcoholByVolume(5), Set(Beer))
        .head
        .taxType shouldBe "2023-1"

      service
        .rateBands(YearMonth.of(2024, 1), Core, AlcoholByVolume(5), Set(Beer))
        .head
        .taxType shouldBe "2024-1"

      service
        .rateBands(YearMonth.of(2024, 12), Core, AlcoholByVolume(5), Set(Beer))
        .head
        .taxType shouldBe "2024-1"

      service
        .rateBands(YearMonth.of(2025, 1), DraughtRelief, AlcoholByVolume(5), Set(Beer))
        .head
        .taxType shouldBe "2025-1"

      service
        .rateBands(YearMonth.of(2025, 12), DraughtRelief, AlcoholByVolume(5), Set(Beer))
        .head
        .taxType shouldBe "2025-1"

      service
        .rateBands(YearMonth.of(2030, 6), DraughtRelief, AlcoholByVolume(5), Set(Beer))
        .head
        .taxType shouldBe "2025-1"
    }

    "filter rateBands by abv" in {

      val mockEnv    = mock[Environment]
      val mockConfig = mock[AppConfig]
      when(mockConfig.alcoholDutyRatesFile).thenReturn("foo")

      val rateFileContent = Json.toJson(ratePeriods).toString()
      when(mockEnv.resourceAsStream(any())).thenReturn(Some(new ByteArrayInputStream(rateFileContent.getBytes)))

      val service = new RatesService(mockEnv, mockConfig)

      service
        .rateBands(
          YearMonth.of(2023, 1),
          Core,
          AlcoholByVolume(5),
          Set(Beer)
        ) should have size 1

      service
        .rateBands(YearMonth.of(2023, 1), Core, AlcoholByVolume(0), Set(Beer))
        .head
        .taxType shouldBe "2023-1"

      service
        .rateBands(YearMonth.of(2023, 1), Core, AlcoholByVolume(5), Set(Beer))
        .head
        .taxType shouldBe "2023-1"

      service
        .rateBands(YearMonth.of(2023, 1), Core, AlcoholByVolume(5.1), Set(Beer))
        .head
        .taxType shouldBe "2023-2"

      service
        .rateBands(YearMonth.of(2023, 1), Core, AlcoholByVolume(7), Set(Beer))
        .head
        .taxType shouldBe "2023-2"

      service
        .rateBands(YearMonth.of(2023, 1), Core, AlcoholByVolume(8), Set(Beer)) should have size 2

      service
        .rateBands(YearMonth.of(2023, 1), Core, AlcoholByVolume(9), Set(Beer)) should have size 2

      service
        .rateBands(YearMonth.of(2023, 1), Core, AlcoholByVolume(18), Set(Beer)) should have size 1

      service
        .rateBands(YearMonth.of(2023, 1), Core, AlcoholByVolume(18.1), Set(Beer)) should have size 0
    }

    "filter rateBands by rateType" in {

      val mockEnv    = mock[Environment]
      val mockConfig = mock[AppConfig]
      when(mockConfig.alcoholDutyRatesFile).thenReturn("foo")

      val rateFileContent = Json.toJson(ratePeriods).toString()
      when(mockEnv.resourceAsStream(any())).thenReturn(Some(new ByteArrayInputStream(rateFileContent.getBytes)))

      val service = new RatesService(mockEnv, mockConfig)

      service
        .rateBands(
          YearMonth.of(2024, 1),
          SmallProducerRelief,
          AlcoholByVolume(5),
          Set(Beer)
        ) should have size 1

      service
        .rateBands(YearMonth.of(2024, 1), SmallProducerRelief, AlcoholByVolume(5), Set(Beer))
        .head
        .taxType shouldBe "2024-2"

      service
        .rateBands(YearMonth.of(2024, 1), SmallProducerRelief, AlcoholByVolume(5), Set(Beer))
        .head
        .rateType shouldBe SmallProducerRelief
    }

    "filter rateBands by alcohol regimes" in {
      val mockEnv    = mock[Environment]
      val mockConfig = mock[AppConfig]
      when(mockConfig.alcoholDutyRatesFile).thenReturn("foo")

      val rateFileContent = Json.toJson(ratePeriods).toString()
      when(mockEnv.resourceAsStream(any())).thenReturn(Some(new ByteArrayInputStream(rateFileContent.getBytes)))

      val service = new RatesService(mockEnv, mockConfig)

      service
        .rateBands(
          YearMonth.of(2025, 1),
          Core,
          AlcoholByVolume(5),
          Set(Spirits)
        ) should have size 1

      service
        .rateBands(YearMonth.of(2025, 1), Core, AlcoholByVolume(5), Set(Spirits))
        .head
        .taxType shouldBe "2025-4"

      service
        .rateBands(
          YearMonth.of(2025, 1),
          Core,
          AlcoholByVolume(5),
          Set(Beer)
        ) should have size 3

      service
        .rateBands(
          YearMonth.of(2025, 1),
          Core,
          AlcoholByVolume(5),
          Set(Wine)
        ) should have size 3

      service
        .rateBands(
          YearMonth.of(2025, 1),
          Core,
          AlcoholByVolume(5),
          Set(Wine, Cider, Spirits)
        ) should have size 3

      service
        .rateBands(
          YearMonth.of(2025, 1),
          Core,
          AlcoholByVolume(5),
          Set(Beer, Wine, Cider, Spirits)
        ) should have size 3
    }

    "filter rateBands by year for the rateType request" in {

      val mockEnv    = mock[Environment]
      val mockConfig = mock[AppConfig]
      when(mockConfig.alcoholDutyRatesFile).thenReturn("foo")

      val rateFileContent = Json.toJson(ratePeriods).toString()
      when(mockEnv.resourceAsStream(any())).thenReturn(Some(new ByteArrayInputStream(rateFileContent.getBytes)))

      val service = new RatesService(mockEnv, mockConfig)

      service
        .rateTypes(YearMonth.of(2023, 1), AlcoholByVolume(5), Set(Beer))
        .rateType shouldBe Core

      service
        .rateTypes(YearMonth.of(2023, 12), AlcoholByVolume(5), Set(Beer))
        .rateType shouldBe Core

      service
        .rateTypes(YearMonth.of(2024, 1), AlcoholByVolume(5), Set(Beer))
        .rateType shouldBe DraughtAndSmallProducerRelief

      service
        .rateTypes(YearMonth.of(2024, 12), AlcoholByVolume(5), Set(Beer))
        .rateType shouldBe DraughtAndSmallProducerRelief

      service
        .rateTypes(YearMonth.of(2025, 1), AlcoholByVolume(5), Set(Beer))
        .rateType shouldBe DraughtRelief

      service
        .rateTypes(YearMonth.of(2025, 12), AlcoholByVolume(5), Set(Beer))
        .rateType shouldBe DraughtRelief

      service
        .rateTypes(YearMonth.of(2030, 6), AlcoholByVolume(5), Set(Beer))
        .rateType shouldBe DraughtRelief
    }

    "filter rateBands by abv for the rateType request" in {

      val mockEnv    = mock[Environment]
      val mockConfig = mock[AppConfig]
      when(mockConfig.alcoholDutyRatesFile).thenReturn("foo")

      val rateFileContent = Json.toJson(ratePeriods).toString()
      when(mockEnv.resourceAsStream(any())).thenReturn(Some(new ByteArrayInputStream(rateFileContent.getBytes)))

      val service = new RatesService(mockEnv, mockConfig)

      service
        .rateTypes(
          YearMonth.of(2023, 1),
          AlcoholByVolume(5),
          Set(Beer)
        )
        .rateType shouldBe Core

      service
        .rateTypes(YearMonth.of(2023, 1), AlcoholByVolume(0), Set(Beer))
        .rateType shouldBe Core

      service
        .rateTypes(YearMonth.of(2024, 1), AlcoholByVolume(5), Set(Beer))
        .rateType shouldBe DraughtAndSmallProducerRelief

      service
        .rateTypes(YearMonth.of(2023, 1), AlcoholByVolume(5.1), Set(Beer))
        .rateType shouldBe Core

      service
        .rateTypes(YearMonth.of(2025, 1), AlcoholByVolume(7), Set(Beer))
        .rateType shouldBe DraughtRelief

      service
        .rateTypes(YearMonth.of(2023, 1), AlcoholByVolume(8), Set(Beer))
        .rateType shouldBe Core

      service
        .rateTypes(YearMonth.of(2023, 1), AlcoholByVolume(9), Set(Beer))
        .rateType shouldBe Core

      service
        .rateTypes(YearMonth.of(2023, 1), AlcoholByVolume(18), Set(Beer))
        .rateType shouldBe Core

      service
        .rateTypes(YearMonth.of(2023, 1), AlcoholByVolume(18.1), Set(Beer))
        .rateType shouldBe Core
    }

    "filter rateBands by rateType for the rateType request" in {

      val mockEnv    = mock[Environment]
      val mockConfig = mock[AppConfig]
      when(mockConfig.alcoholDutyRatesFile).thenReturn("foo")

      val rateFileContent = Json.toJson(ratePeriods).toString()
      when(mockEnv.resourceAsStream(any())).thenReturn(Some(new ByteArrayInputStream(rateFileContent.getBytes)))

      val service = new RatesService(mockEnv, mockConfig)

      service
        .rateTypes(
          YearMonth.of(2024, 1),
          AlcoholByVolume(5),
          Set(Beer)
        )
        .rateType shouldBe DraughtAndSmallProducerRelief

      service
        .rateTypes(YearMonth.of(2025, 1), AlcoholByVolume(5), Set(Beer))
        .rateType shouldBe DraughtRelief

      service
        .rateTypes(YearMonth.of(2023, 1), AlcoholByVolume(5), Set(Beer))
        .rateType shouldBe Core
    }

    "filter rateBands by alcohol regimes for the rateType request" in {
      val mockEnv    = mock[Environment]
      val mockConfig = mock[AppConfig]
      when(mockConfig.alcoholDutyRatesFile).thenReturn("foo")

      val rateFileContent = Json.toJson(ratePeriods).toString()
      when(mockEnv.resourceAsStream(any())).thenReturn(Some(new ByteArrayInputStream(rateFileContent.getBytes)))

      val service = new RatesService(mockEnv, mockConfig)

      service
        .rateTypes(
          YearMonth.of(2025, 1),
          AlcoholByVolume(5),
          Set(Spirits)
        )
        .rateType shouldBe Core

      service
        .rateTypes(YearMonth.of(2025, 1), AlcoholByVolume(5), Set(Spirits))
        .rateType shouldBe Core

      service
        .rateTypes(
          YearMonth.of(2025, 1),
          AlcoholByVolume(5),
          Set(Beer)
        )
        .rateType shouldBe DraughtRelief

      service
        .rateTypes(
          YearMonth.of(2025, 1),
          AlcoholByVolume(5),
          Set(Wine)
        )
        .rateType shouldBe Core

      service
        .rateTypes(
          YearMonth.of(2025, 1),
          AlcoholByVolume(5),
          Set(Wine, Cider, Spirits)
        )
        .rateType shouldBe Core

      service
        .rateTypes(
          YearMonth.of(2025, 1),
          AlcoholByVolume(5),
          Set(Beer, Wine, Cider, Spirits)
        )
        .rateType shouldBe DraughtRelief
    }
  }
}
