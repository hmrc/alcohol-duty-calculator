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
import uk.gov.hmrc.alcoholdutycalculator.models.AlcoholRegimeName.{Beer, Cider, Spirits, Wine}
import uk.gov.hmrc.alcoholdutycalculator.models.RateType.{Core, DraughtAndSmallProducerRelief, DraughtRelief, SmallProducerRelief}
import uk.gov.hmrc.alcoholdutycalculator.models.{ABVRange, ABVRangeName, AlcoholByVolume, AlcoholRegime, AlcoholRegimeName, RateBand, RatePeriod}

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
                    "taxType"        -> "301",
                    "description"    -> "Low Alcohol - not exc 1.2%",
                    "rateType"       -> "Core",
                    "alcoholRegimes" -> JsArray(
                      Seq(
                        Json.obj(
                          "name"      -> AlcoholRegimeName.Beer.toString,
                          "abvRanges" -> JsArray(
                            Seq(
                              Json.obj(
                                "name"   -> ABVRangeName.Beer.toString,
                                "minABV" -> 3,
                                "maxABV" -> 9.9
                              )
                            )
                          )
                        )
                      )
                    ),
                    "rate"           -> 100.99
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
              Set(
                AlcoholRegime(
                  name = Beer,
                  Seq(
                    ABVRange(
                      name = ABVRangeName.Beer,
                      minABV = AlcoholByVolume(3),
                      maxABV = AlcoholByVolume(9.9)
                    )
                  )
                )
              ),
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
      Set(
        AlcoholRegime(
          name = Beer,
          Seq(
            ABVRange(
              name = ABVRangeName.Beer,
              maxABV = AlcoholByVolume(3),
              minABV = AlcoholByVolume(9.9)
            )
          )
        )
      ),
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
          baseRateBand.copy(
            taxType = "2023-1",
            alcoholRegimes = Set(
              AlcoholRegime(
                name = Beer,
                abvRanges =
                  Seq(ABVRange(name = ABVRangeName.Beer, minABV = AlcoholByVolume(0), maxABV = AlcoholByVolume(5)))
              )
            )
          ),
          baseRateBand.copy(
            taxType = "2023-2",
            alcoholRegimes = Set(
              AlcoholRegime(
                name = Beer,
                abvRanges =
                  Seq(ABVRange(name = ABVRangeName.Beer, minABV = AlcoholByVolume(5.1), maxABV = AlcoholByVolume(7)))
              )
            )
          ),
          baseRateBand.copy(
            taxType = "2023-3",
            alcoholRegimes = Set(
              AlcoholRegime(
                name = Beer,
                abvRanges =
                  Seq(ABVRange(name = ABVRangeName.Beer, minABV = AlcoholByVolume(7.1), maxABV = AlcoholByVolume(9)))
              )
            )
          ),
          baseRateBand.copy(
            taxType = "2023-4",
            alcoholRegimes = Set(
              AlcoholRegime(
                name = Beer,
                abvRanges =
                  Seq(ABVRange(name = ABVRangeName.Beer, minABV = AlcoholByVolume(8), maxABV = AlcoholByVolume(18)))
              )
            )
          )
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
          baseRateBand.copy(
            taxType = "2025-1",
            alcoholRegimes = Set(
              AlcoholRegime(
                name = Beer,
                abvRanges =
                  Seq(ABVRange(name = ABVRangeName.Beer, minABV = AlcoholByVolume(0), maxABV = AlcoholByVolume(5)))
              )
            ),
            rateType = DraughtRelief
          ),
          baseRateBand.copy(
            taxType = "2025-2",
            alcoholRegimes = Set(
              AlcoholRegime(
                name = Beer,
                abvRanges =
                  Seq(ABVRange(name = ABVRangeName.Beer, minABV = AlcoholByVolume(0), maxABV = AlcoholByVolume(5)))
              ),
              AlcoholRegime(
                name = Wine,
                abvRanges =
                  Seq(ABVRange(name = ABVRangeName.Wine, minABV = AlcoholByVolume(0), maxABV = AlcoholByVolume(5)))
              )
            )
          ),
          baseRateBand.copy(
            taxType = "2025-3",
            alcoholRegimes = Set(
              AlcoholRegime(
                name = Beer,
                abvRanges =
                  Seq(ABVRange(name = ABVRangeName.Beer, minABV = AlcoholByVolume(0), maxABV = AlcoholByVolume(5)))
              ),
              AlcoholRegime(
                name = Wine,
                abvRanges =
                  Seq(ABVRange(name = ABVRangeName.Wine, minABV = AlcoholByVolume(0), maxABV = AlcoholByVolume(5)))
              ),
              AlcoholRegime(
                name = Cider,
                abvRanges =
                  Seq(ABVRange(name = ABVRangeName.Cider, minABV = AlcoholByVolume(0), maxABV = AlcoholByVolume(5)))
              )
            )
          ),
          baseRateBand.copy(
            taxType = "2025-4",
            alcoholRegimes = Set(
              AlcoholRegime(
                name = Beer,
                abvRanges =
                  Seq(ABVRange(name = ABVRangeName.Beer, minABV = AlcoholByVolume(0), maxABV = AlcoholByVolume(5)))
              ),
              AlcoholRegime(
                name = Wine,
                abvRanges =
                  Seq(ABVRange(name = ABVRangeName.Wine, minABV = AlcoholByVolume(0), maxABV = AlcoholByVolume(5)))
              ),
              AlcoholRegime(
                name = Cider,
                abvRanges =
                  Seq(ABVRange(name = ABVRangeName.Cider, minABV = AlcoholByVolume(0), maxABV = AlcoholByVolume(5)))
              ),
              AlcoholRegime(
                name = Spirits,
                abvRanges =
                  Seq(ABVRange(name = ABVRangeName.Spirits, minABV = AlcoholByVolume(0), maxABV = AlcoholByVolume(5)))
              )
            )
          )
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
        .rateBands(YearMonth.of(2023, 1), Set(Beer))
        .head
        .taxType shouldBe "2023-1"

      service
        .rateBands(YearMonth.of(2023, 12), Set(Beer))
        .head
        .taxType shouldBe "2023-1"

      service
        .rateBands(YearMonth.of(2024, 1), Set(Beer))
        .head
        .taxType shouldBe "2024-1"

      service
        .rateBands(YearMonth.of(2024, 12), Set(Beer))
        .head
        .taxType shouldBe "2024-1"

      service
        .rateBands(YearMonth.of(2025, 1), Set(Beer))
        .head
        .taxType shouldBe "2025-1"

      service
        .rateBands(YearMonth.of(2025, 12), Set(Beer))
        .head
        .taxType shouldBe "2025-1"

      service
        .rateBands(YearMonth.of(2030, 6), Set(Beer))
        .head
        .taxType shouldBe "2025-1"
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
          Set(Spirits)
        ) should have size 1

      service
        .rateBands(YearMonth.of(2025, 1), Set(Spirits))
        .head
        .taxType shouldBe "2025-4"

      service
        .rateBands(
          YearMonth.of(2025, 1),
          Set(Beer)
        ) should have size 4

      service
        .rateBands(
          YearMonth.of(2025, 1),
          Set(Wine)
        ) should have size 3

      service
        .rateBands(
          YearMonth.of(2025, 1),
          Set(Wine, Cider, Spirits)
        ) should have size 3

      service
        .rateBands(
          YearMonth.of(2025, 1),
          Set(Beer, Wine, Cider, Spirits)
        ) should have size 4
    }

    "filter rateBands by year for the rateType request" in {

      val mockEnv    = mock[Environment]
      val mockConfig = mock[AppConfig]
      when(mockConfig.alcoholDutyRatesFile).thenReturn("foo")

      val rateFileContent = Json.toJson(ratePeriods).toString()
      when(mockEnv.resourceAsStream(any())).thenReturn(Some(new ByteArrayInputStream(rateFileContent.getBytes)))

      val service = new RatesService(mockEnv, mockConfig)

      service
        .rateTypes(YearMonth.of(2023, 1), Set(Beer))
        .rateType shouldBe Core

      service
        .rateTypes(YearMonth.of(2023, 12), Set(Beer))
        .rateType shouldBe Core

      service
        .rateTypes(YearMonth.of(2024, 1), Set(Beer))
        .rateType shouldBe DraughtAndSmallProducerRelief

      service
        .rateTypes(YearMonth.of(2024, 12), Set(Beer))
        .rateType shouldBe DraughtAndSmallProducerRelief

      service
        .rateTypes(YearMonth.of(2025, 1), Set(Beer))
        .rateType shouldBe DraughtRelief

      service
        .rateTypes(YearMonth.of(2025, 12), Set(Beer))
        .rateType shouldBe DraughtRelief

      service
        .rateTypes(YearMonth.of(2030, 6), Set(Beer))
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
          Set(Beer)
        )
        .rateType shouldBe Core

      service
        .rateTypes(YearMonth.of(2023, 1), Set(Beer))
        .rateType shouldBe Core

      service
        .rateTypes(YearMonth.of(2024, 1), Set(Beer))
        .rateType shouldBe DraughtAndSmallProducerRelief

      service
        .rateTypes(YearMonth.of(2023, 1), Set(Beer))
        .rateType shouldBe Core

      service
        .rateTypes(YearMonth.of(2025, 1), Set(Beer))
        .rateType shouldBe DraughtRelief

      service
        .rateTypes(YearMonth.of(2023, 1), Set(Beer))
        .rateType shouldBe Core

      service
        .rateTypes(YearMonth.of(2023, 1), Set(Beer))
        .rateType shouldBe Core

      service
        .rateTypes(YearMonth.of(2023, 1), Set(Beer))
        .rateType shouldBe Core

      service
        .rateTypes(YearMonth.of(2023, 1), Set(Beer))
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
          Set(Beer)
        )
        .rateType shouldBe DraughtAndSmallProducerRelief

      service
        .rateTypes(YearMonth.of(2025, 1), Set(Beer))
        .rateType shouldBe DraughtRelief

      service
        .rateTypes(YearMonth.of(2023, 1), Set(Beer))
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
          Set(Spirits)
        )
        .rateType shouldBe Core

      service
        .rateTypes(YearMonth.of(2025, 1), Set(Spirits))
        .rateType shouldBe Core

      service
        .rateTypes(
          YearMonth.of(2025, 1),
          Set(Beer)
        )
        .rateType shouldBe DraughtRelief

      service
        .rateTypes(
          YearMonth.of(2025, 1),
          Set(Wine)
        )
        .rateType shouldBe Core

      service
        .rateTypes(
          YearMonth.of(2025, 1),
          Set(Wine, Cider, Spirits)
        )
        .rateType shouldBe Core

      service
        .rateTypes(
          YearMonth.of(2025, 1),
          Set(Beer, Wine, Cider, Spirits)
        )
        .rateType shouldBe DraughtRelief
    }

    "filter rateBands by year for the taxType request" in {
      val mockEnv    = mock[Environment]
      val mockConfig = mock[AppConfig]
      when(mockConfig.alcoholDutyRatesFile).thenReturn("foo")

      val rateFileContent = Json.toJson(ratePeriods).toString()
      when(mockEnv.resourceAsStream(any())).thenReturn(Some(new ByteArrayInputStream(rateFileContent.getBytes)))

      val service = new RatesService(mockEnv, mockConfig)

      service
        .taxType(YearMonth.of(2023, 1), "2023-1") shouldBe Some(ratePeriods.head.rateBands.head)
      service
        .taxType(YearMonth.of(2024, 1), "2024-1") shouldBe Some(ratePeriods(1).rateBands.head)
      service
        .taxType(YearMonth.of(2099, 1), "2024-1") shouldBe None
      service
        .taxType(YearMonth.of(2025, 1), "2025-1") shouldBe Some(ratePeriods(2).rateBands.head)
      service
        .taxType(YearMonth.of(2025, 1), "2025-3") shouldBe Some(ratePeriods(2).rateBands(2))

    }

    "filter rateBands by taxType for the taxType request" in {

      val mockEnv    = mock[Environment]
      val mockConfig = mock[AppConfig]
      when(mockConfig.alcoholDutyRatesFile).thenReturn("foo")

      val rateFileContent = Json.toJson(ratePeriods).toString()
      when(mockEnv.resourceAsStream(any())).thenReturn(Some(new ByteArrayInputStream(rateFileContent.getBytes)))

      val service = new RatesService(mockEnv, mockConfig)

      service
        .taxType(YearMonth.of(2023, 1), "2023-1") shouldBe Some(ratePeriods.head.rateBands.head)

      service
        .taxType(YearMonth.of(2023, 1), "2023-2") shouldBe Some(ratePeriods.head.rateBands(1))

    }
  }
}
