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
import uk.gov.hmrc.alcoholdutycalculator.models.{ABVRange, AlcoholByVolume, AlcoholRegime, AlcoholType, RangeDetailsByRegime, RateBand, RatePeriod}

import java.io.ByteArrayInputStream
import java.time.YearMonth

class RatesServiceSpec extends SpecBase {

  "alcoholDutyRates must" - {
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
                    "taxTypeCode"  -> "301",
                    "description"  -> "Low Alcohol - not exc 1.2%",
                    "rateType"     -> "Core",
                    "rate"         -> 100.99,
                    "rangeDetails" -> JsArray(
                      Seq(
                        Json.obj(
                          "alcoholRegime" -> AlcoholRegime.Beer.toString,
                          "abvRanges"     -> JsArray(
                            Seq(
                              Json.obj(
                                "alcoholType" -> AlcoholType.Beer.toString,
                                "minABV"      -> 3,
                                "maxABV"      -> 9.9
                              )
                            )
                          )
                        )
                      )
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
      result mustBe List(
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
                RangeDetailsByRegime(
                  alcoholRegime = Beer,
                  Seq(
                    ABVRange(
                      alcoholType = AlcoholType.Beer,
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

    "throw an exception when the file can't be opened" in {
      val mockEnv    = mock[Environment]
      val mockConfig = mock[AppConfig]
      when(mockConfig.alcoholDutyRatesFile).thenReturn("foo")

      when(mockEnv.resourceAsStream(any())).thenReturn(None)

      the[Exception] thrownBy {
        val service = new RatesService(mockEnv, mockConfig)
        service.alcoholDutyRates
      } must have message "Could not open Alcohol Duty Rate file"
    }
  }

  "rateBands must" - {
    val baseRateBand = RateBand(
      "taxTypeBase",
      "descriptionBase",
      Core,
      Set(
        RangeDetailsByRegime(
          alcoholRegime = Beer,
          Seq(
            ABVRange(
              alcoholType = AlcoholType.Beer,
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
            taxTypeCode = "2023-1",
            rangeDetails = Set(
              RangeDetailsByRegime(
                alcoholRegime = Beer,
                abvRanges = Seq(
                  ABVRange(alcoholType = AlcoholType.Beer, minABV = AlcoholByVolume(0), maxABV = AlcoholByVolume(5))
                )
              )
            )
          ),
          baseRateBand.copy(
            taxTypeCode = "2023-2",
            rangeDetails = Set(
              RangeDetailsByRegime(
                alcoholRegime = Beer,
                abvRanges = Seq(
                  ABVRange(alcoholType = AlcoholType.Beer, minABV = AlcoholByVolume(5.1), maxABV = AlcoholByVolume(7))
                )
              )
            )
          ),
          baseRateBand.copy(
            taxTypeCode = "2023-3",
            rangeDetails = Set(
              RangeDetailsByRegime(
                alcoholRegime = Beer,
                abvRanges = Seq(
                  ABVRange(alcoholType = AlcoholType.Beer, minABV = AlcoholByVolume(7.1), maxABV = AlcoholByVolume(9))
                )
              )
            )
          ),
          baseRateBand.copy(
            taxTypeCode = "2023-4",
            rangeDetails = Set(
              RangeDetailsByRegime(
                alcoholRegime = Beer,
                abvRanges = Seq(
                  ABVRange(alcoholType = AlcoholType.Beer, minABV = AlcoholByVolume(8), maxABV = AlcoholByVolume(18))
                )
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
          baseRateBand.copy(taxTypeCode = "2024-1"),
          baseRateBand.copy(taxTypeCode = "2024-2", rateType = SmallProducerRelief),
          baseRateBand.copy(taxTypeCode = "2024-3", rateType = DraughtRelief),
          baseRateBand.copy(taxTypeCode = "2024-4", rateType = DraughtAndSmallProducerRelief)
        )
      ),
      baseRatePeriod.copy(
        name = "2025",
        validityStartDate = YearMonth.of(2025, 1),
        validityEndDate = None,
        rateBands = List(
          baseRateBand.copy(
            taxTypeCode = "2025-1",
            rangeDetails = Set(
              RangeDetailsByRegime(
                alcoholRegime = Beer,
                abvRanges = Seq(
                  ABVRange(alcoholType = AlcoholType.Beer, minABV = AlcoholByVolume(0), maxABV = AlcoholByVolume(5))
                )
              )
            ),
            rateType = DraughtRelief
          ),
          baseRateBand.copy(
            taxTypeCode = "2025-2",
            rangeDetails = Set(
              RangeDetailsByRegime(
                alcoholRegime = Beer,
                abvRanges = Seq(
                  ABVRange(alcoholType = AlcoholType.Beer, minABV = AlcoholByVolume(0), maxABV = AlcoholByVolume(5))
                )
              ),
              RangeDetailsByRegime(
                alcoholRegime = Wine,
                abvRanges = Seq(
                  ABVRange(alcoholType = AlcoholType.Wine, minABV = AlcoholByVolume(0), maxABV = AlcoholByVolume(5))
                )
              )
            )
          ),
          baseRateBand.copy(
            taxTypeCode = "2025-3",
            rangeDetails = Set(
              RangeDetailsByRegime(
                alcoholRegime = Beer,
                abvRanges = Seq(
                  ABVRange(alcoholType = AlcoholType.Beer, minABV = AlcoholByVolume(0), maxABV = AlcoholByVolume(5))
                )
              ),
              RangeDetailsByRegime(
                alcoholRegime = Wine,
                abvRanges = Seq(
                  ABVRange(alcoholType = AlcoholType.Wine, minABV = AlcoholByVolume(0), maxABV = AlcoholByVolume(5))
                )
              ),
              RangeDetailsByRegime(
                alcoholRegime = Cider,
                abvRanges = Seq(
                  ABVRange(alcoholType = AlcoholType.Cider, minABV = AlcoholByVolume(0), maxABV = AlcoholByVolume(5))
                )
              )
            )
          ),
          baseRateBand.copy(
            taxTypeCode = "2025-4",
            rangeDetails = Set(
              RangeDetailsByRegime(
                alcoholRegime = Beer,
                abvRanges = Seq(
                  ABVRange(alcoholType = AlcoholType.Beer, minABV = AlcoholByVolume(0), maxABV = AlcoholByVolume(5))
                )
              ),
              RangeDetailsByRegime(
                alcoholRegime = Wine,
                abvRanges = Seq(
                  ABVRange(alcoholType = AlcoholType.Wine, minABV = AlcoholByVolume(0), maxABV = AlcoholByVolume(5))
                )
              ),
              RangeDetailsByRegime(
                alcoholRegime = Cider,
                abvRanges = Seq(
                  ABVRange(alcoholType = AlcoholType.Cider, minABV = AlcoholByVolume(0), maxABV = AlcoholByVolume(5))
                )
              ),
              RangeDetailsByRegime(
                alcoholRegime = Spirits,
                abvRanges = Seq(
                  ABVRange(alcoholType = AlcoholType.Spirits, minABV = AlcoholByVolume(0), maxABV = AlcoholByVolume(5))
                )
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
        .taxTypeCode mustBe "2023-1"

      service
        .rateBands(YearMonth.of(2023, 12), Set(Beer))
        .head
        .taxTypeCode mustBe "2023-1"

      service
        .rateBands(YearMonth.of(2024, 1), Set(Beer))
        .head
        .taxTypeCode mustBe "2024-1"

      service
        .rateBands(YearMonth.of(2024, 12), Set(Beer))
        .head
        .taxTypeCode mustBe "2024-1"

      service
        .rateBands(YearMonth.of(2025, 1), Set(Beer))
        .head
        .taxTypeCode mustBe "2025-1"

      service
        .rateBands(YearMonth.of(2025, 12), Set(Beer))
        .head
        .taxTypeCode mustBe "2025-1"

      service
        .rateBands(YearMonth.of(2030, 6), Set(Beer))
        .head
        .taxTypeCode mustBe "2025-1"
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
        ) must have size 1

      service
        .rateBands(YearMonth.of(2025, 1), Set(Spirits))
        .head
        .taxTypeCode mustBe "2025-4"

      service
        .rateBands(
          YearMonth.of(2025, 1),
          Set(Beer)
        ) must have size 4

      service
        .rateBands(
          YearMonth.of(2025, 1),
          Set(Wine)
        ) must have size 3

      service
        .rateBands(
          YearMonth.of(2025, 1),
          Set(Wine, Cider, Spirits)
        ) must have size 3

      service
        .rateBands(
          YearMonth.of(2025, 1),
          Set(Beer, Wine, Cider, Spirits)
        ) must have size 4
    }

    "filter rateBands by year for the taxType request" in {
      val mockEnv    = mock[Environment]
      val mockConfig = mock[AppConfig]
      when(mockConfig.alcoholDutyRatesFile).thenReturn("foo")

      val rateFileContent = Json.toJson(ratePeriods).toString()
      when(mockEnv.resourceAsStream(any())).thenReturn(Some(new ByteArrayInputStream(rateFileContent.getBytes)))

      val service = new RatesService(mockEnv, mockConfig)

      service
        .taxType(YearMonth.of(2023, 1), "2023-1") mustBe Some(ratePeriods.head.rateBands.head)
      service
        .taxType(YearMonth.of(2024, 1), "2024-1") mustBe Some(ratePeriods(1).rateBands.head)
      service
        .taxType(YearMonth.of(2099, 1), "2024-1") mustBe None
      service
        .taxType(YearMonth.of(2025, 1), "2025-1") mustBe Some(ratePeriods(2).rateBands.head)
      service
        .taxType(YearMonth.of(2025, 1), "2025-3") mustBe Some(ratePeriods(2).rateBands(2))

    }

    "filter rateBands by taxType for the taxType request" in {
      val mockEnv    = mock[Environment]
      val mockConfig = mock[AppConfig]
      when(mockConfig.alcoholDutyRatesFile).thenReturn("foo")

      val rateFileContent = Json.toJson(ratePeriods).toString()
      when(mockEnv.resourceAsStream(any())).thenReturn(Some(new ByteArrayInputStream(rateFileContent.getBytes)))

      val service = new RatesService(mockEnv, mockConfig)

      service
        .taxType(YearMonth.of(2023, 1), "2023-1") mustBe Some(ratePeriods.head.rateBands.head)

      service
        .taxType(YearMonth.of(2023, 1), "2023-2") mustBe Some(ratePeriods.head.rateBands(1))
    }
  }
}
