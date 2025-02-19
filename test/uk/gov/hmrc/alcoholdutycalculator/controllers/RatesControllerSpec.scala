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

import java.time.YearMonth
import org.mockito.ArgumentMatchers.any
import play.api.libs.json.Json
import play.api.mvc.Result
import play.api.test.FakeRequest
import uk.gov.hmrc.alcoholdutycalculator.base.SpecBase
import uk.gov.hmrc.alcoholdutycalculator.models.{AlcoholRegime, RateBand, RateType}
import uk.gov.hmrc.alcoholdutycalculator.models.RatePeriod._
import uk.gov.hmrc.alcoholdutycalculator.services.RatesService

import scala.concurrent.Future

class RatesControllerSpec extends SpecBase {
  "rates must" - {
    "return 200 OK with alcohol duty rates based on query parameters" in forAll {
      (
        rateBandList: Seq[RateBand],
        ratePeriod: YearMonth,
        rateType: RateType,
        alcoholRegimes: Set[AlcoholRegime]
      ) =>
        val mockRatesService: RatesService = mock[RatesService]

        val controller = new RatesController(
          fakeAuthorisedAction,
          mockRatesService,
          cc
        )

        if (alcoholRegimes.nonEmpty) {
          when(mockRatesService.rateBands(any(), any())).thenReturn(rateBandList)

          val urlWithParams =
            s"/rates?ratePeriod=${Json.toJson(ratePeriod).toString()}&rateType=${Json
              .toJson(rateType)
              .toString}&alcoholRegimes=${alcoholRegimes.mkString(",")}"

          val requestWithParams = FakeRequest("GET", urlWithParams)

          val result: Future[Result] = controller.rates()(requestWithParams)

          status(result)        mustBe OK
          contentAsJson(result) mustBe Json.toJson(rateBandList)

          verify(mockRatesService).rateBands(ratePeriod, alcoholRegimes)
        }
    }

    "return BadRequest when" - {
      "'ratePeriod' parameter is missing" in forAll {
        (
          rateType: RateType,
          alcoholRegimes: Set[AlcoholRegime]
        ) =>
          val mockRatesService: RatesService = mock[RatesService]

          val controller = new RatesController(
            fakeAuthorisedAction,
            mockRatesService,
            cc
          )

          val urlWithParams                =
            s"/rates?rateType=${Json
              .toJson(rateType)
              .toString}&alcoholRegimes=${alcoholRegimes.mkString(",")}"
          val requestWithMissingRatePeriod = FakeRequest("GET", urlWithParams)
          val result: Future[Result]       = controller.rates()(requestWithMissingRatePeriod)

          status(result)          mustBe BAD_REQUEST
          contentAsString(result) mustBe "Missing or invalid 'ratePeriod' parameter"
      }

      "'ratePeriod' parameter is invalid" in forAll {
        (
          rateType: RateType,
          alcoholRegimes: Set[AlcoholRegime]
        ) =>
          val mockRatesService: RatesService = mock[RatesService]

          val controller = new RatesController(
            fakeAuthorisedAction,
            mockRatesService,
            cc
          )

          val urlWithParams                =
            s"/rates?ratePeriod=1234&rateType=${Json
              .toJson(rateType)
              .toString}&alcoholRegimes=${alcoholRegimes.toString}"
          val requestWithMissingRatePeriod = FakeRequest("GET", urlWithParams)
          val result: Future[Result]       = controller.rates()(requestWithMissingRatePeriod)

          status(result)          mustBe BAD_REQUEST
          contentAsString(result) mustBe "Invalid 'ratePeriod' parameter"
      }

      "'alcoholRegimes' parameter is missing" in forAll {
        (
          ratePeriod: YearMonth,
          rateType: RateType
        ) =>
          val mockRatesService: RatesService = mock[RatesService]

          val controller = new RatesController(
            fakeAuthorisedAction,
            mockRatesService,
            cc
          )

          val urlWithParams =
            s"/rates?ratePeriod=${Json.toJson(ratePeriod).toString()}&rateType=${Json
              .toJson(rateType)
              .toString}"

          val requestWithInvalidRegimes =
            FakeRequest("GET", urlWithParams)
          val result: Future[Result]    = controller.rates()(requestWithInvalidRegimes)

          status(result)          mustBe BAD_REQUEST
          contentAsString(result) mustBe "Missing or invalid 'alcoholRegimes' parameter"
      }

      "'alcoholRegimes' parameter is invalid" in forAll {
        (
          ratePeriod: YearMonth,
          rateType: RateType
        ) =>
          val mockRatesService: RatesService = mock[RatesService]

          val controller = new RatesController(
            fakeAuthorisedAction,
            mockRatesService,
            cc
          )

          val urlWithParams =
            s"/rates?ratePeriod=${Json.toJson(ratePeriod).toString()}&rateType=${Json
              .toJson(rateType)
              .toString}&alcoholRegimes=1234"

          val requestWithInvalidRegimes =
            FakeRequest("GET", urlWithParams)
          val result: Future[Result]    = controller.rates()(requestWithInvalidRegimes)

          status(result)          mustBe BAD_REQUEST
          contentAsString(result) mustBe "1234 is not a valid AlcoholRegime"
      }
    }
  }

  "rateBand must" - {
    "return 200 OK with rate band based on query parameters" in forAll {
      (
        ratePeriod: YearMonth,
        rateBand: RateBand
      ) =>
        val mockRatesService: RatesService = mock[RatesService]

        val controller = new RatesController(
          fakeAuthorisedAction,
          mockRatesService,
          cc
        )

        when(mockRatesService.taxType(any(), any())).thenReturn(Some(rateBand))

        val urlWithParams =
          s"/rate-band?ratePeriod=${Json.toJson(ratePeriod).toString()}&taxTypeCode=312"

        val requestWithParams = FakeRequest("GET", urlWithParams)

        val result: Future[Result] = controller.rateBand()(requestWithParams)

        status(result)        mustBe OK
        contentAsJson(result) mustBe Json.toJson(rateBand)

        verify(mockRatesService).taxType(ratePeriod, "312")
    }

    "return 404 NOT FOUND based on query parameters" in forAll {
      (
        ratePeriod: YearMonth
      ) =>
        val mockRatesService: RatesService = mock[RatesService]

        val controller = new RatesController(
          fakeAuthorisedAction,
          mockRatesService,
          cc
        )

        when(mockRatesService.taxType(any(), any())).thenReturn(None)

        val urlWithParams =
          s"/rate-band?ratePeriod=${Json.toJson(ratePeriod).toString()}&taxTypeCode=312"

        val requestWithParams = FakeRequest("GET", urlWithParams)

        val result: Future[Result] = controller.rateBand()(requestWithParams)

        status(result)          mustBe NOT_FOUND
        contentAsString(result) mustBe "RateBand not found"

        verify(mockRatesService).taxType(ratePeriod, "312")
    }

    "return BadRequest when" - {
      "'ratePeriod' parameter is missing" in forAll {
        (
          taxType: String
        ) =>
          val mockRatesService: RatesService = mock[RatesService]

          val controller = new RatesController(
            fakeAuthorisedAction,
            mockRatesService,
            cc
          )

          val urlWithParams                =
            s"/rate-band?taxTypeCode=$taxType"
          val requestWithMissingRatePeriod = FakeRequest("GET", urlWithParams)
          val result: Future[Result]       = controller.rateBand()(requestWithMissingRatePeriod)

          status(result)          mustBe BAD_REQUEST
          contentAsString(result) mustBe "Missing or invalid 'ratePeriod' parameter"
      }

      "'ratePeriod' parameter is invalid" in forAll {
        (
          taxType: String
        ) =>
          val mockRatesService: RatesService = mock[RatesService]

          val controller = new RatesController(
            fakeAuthorisedAction,
            mockRatesService,
            cc
          )

          val urlWithParams                =
            s"/rate-band?ratePeriod=1234&taxTypeCode=$taxType"
          val requestWithMissingRatePeriod = FakeRequest("GET", urlWithParams)
          val result: Future[Result]       = controller.rateBand()(requestWithMissingRatePeriod)

          status(result)          mustBe BAD_REQUEST
          contentAsString(result) mustBe "Invalid 'ratePeriod' parameter"
      }

      "'taxTypeCode' parameter is missing" in forAll {
        (
          ratePeriod: YearMonth
        ) =>
          val mockRatesService: RatesService = mock[RatesService]

          val controller = new RatesController(
            fakeAuthorisedAction,
            mockRatesService,
            cc
          )

          val urlWithParams =
            s"/rate-band?ratePeriod=${Json.toJson(ratePeriod).toString()}"

          val requestWithInvalidTaxType =
            FakeRequest("GET", urlWithParams)
          val result: Future[Result]    = controller.rateBand()(requestWithInvalidTaxType)

          status(result)          mustBe BAD_REQUEST
          contentAsString(result) mustBe "Missing or invalid 'taxTypeCode' parameter"
      }

      "'taxTypeCode' parameter is invalid" in forAll {
        (
          ratePeriod: YearMonth
        ) =>
          val mockRatesService: RatesService = mock[RatesService]

          val controller = new RatesController(
            fakeAuthorisedAction,
            mockRatesService,
            cc
          )

          val urlWithParams =
            s"/rate-band?ratePeriod=${Json.toJson(ratePeriod).toString()}&taxTypeCode=abcd"

          val requestWithInvalidTaxType =
            FakeRequest("GET", urlWithParams)
          val result: Future[Result]    = controller.rateBand()(requestWithInvalidTaxType)

          status(result)          mustBe BAD_REQUEST
          contentAsString(result) mustBe "Invalid 'taxTypeCode' parameter"
      }
    }
  }

  "rateBands must" - {
    "return 200 OK when" - {
      "all rate bands looked up from query parameters" in forAll {
        (
          ratePeriod: YearMonth,
          rateBand: RateBand,
          ratePeriod2: YearMonth,
          rateBand2: RateBand,
          ratePeriod3: YearMonth,
          rateBand3: RateBand
        ) =>
          val mockRatesService: RatesService = mock[RatesService]

          val controller = new RatesController(
            fakeAuthorisedAction,
            mockRatesService,
            cc
          )

          val ratePeriods  = Seq(ratePeriod, ratePeriod2, ratePeriod3)
          val taxTypeCodes = Seq("311", "312", "313")
          val rateBands    = Seq(rateBand, rateBand2, rateBand3)

          val ratePeriodsWithTaxTypeCodes            = ratePeriods.zip(taxTypeCodes)
          val ratePeriodsWithTaxTypeCodesToRateBands = ratePeriodsWithTaxTypeCodes.zip(rateBands).toMap

          ratePeriodsWithTaxTypeCodesToRateBands.foreach { case ((ratePeriod, taxTypeCode), rateBand) =>
            when(mockRatesService.taxType(ratePeriod, taxTypeCode)).thenReturn(Some(rateBand))
          }

          val urlWithParams =
            s"/rate-bands?ratePeriods=${ratePeriods.map(ratePeriod => Json.toJson(ratePeriod).toString()).mkString(",")}&taxTypeCodes=${taxTypeCodes
              .mkString(",")}"

          val requestWithParams = FakeRequest("GET", urlWithParams)

          val result: Future[Result] = controller.rateBands()(requestWithParams)

          status(result)        mustBe OK
          contentAsJson(result) mustBe Json.toJson(ratePeriodsWithTaxTypeCodesToRateBands)

          ratePeriodsWithTaxTypeCodes.foreach { case (ratePeriod, taxTypeCode) =>
            verify(mockRatesService).taxType(ratePeriod, taxTypeCode)
          }
      }

      "some rate bands couldn't be looked up" in forAll {
        (
          ratePeriod: YearMonth,
          rateBand: RateBand,
          ratePeriod2: YearMonth,
          rateBand2: RateBand,
          ratePeriod3: YearMonth,
          rateBand3: RateBand
        ) =>
          val mockRatesService: RatesService = mock[RatesService]

          val controller = new RatesController(
            fakeAuthorisedAction,
            mockRatesService,
            cc
          )

          val ratePeriods  = Seq(ratePeriod, ratePeriod2, ratePeriod3)
          val taxTypeCodes = Seq("311", "312", "313")
          val rateBands    = Seq(rateBand, rateBand2, rateBand3)

          val ratePeriodsWithTaxTypeCodes            = ratePeriods.zip(taxTypeCodes)
          val ratePeriodsWithTaxTypeCodesToRateBands = ratePeriodsWithTaxTypeCodes.zip(rateBands).toMap

          val i                                                      = (Math.random() * 3).toInt
          val ratePeriodsWithTaxTypeCodesToRateBandsWithMissingEntry =
            ratePeriodsWithTaxTypeCodesToRateBands - ratePeriodsWithTaxTypeCodes(i)

          ratePeriodsWithTaxTypeCodesToRateBands.foreach { case ((ratePeriod, taxTypeCode), maybeRateBand) =>
            when(mockRatesService.taxType(ratePeriod, taxTypeCode))
              .thenReturn(ratePeriodsWithTaxTypeCodesToRateBandsWithMissingEntry.get((ratePeriod, taxTypeCode)))
          }

          val urlWithParams =
            s"/rate-bands?ratePeriods=${ratePeriods.map(ratePeriod => Json.toJson(ratePeriod).toString()).mkString(",")}&taxTypeCodes=${taxTypeCodes
              .mkString(",")}"

          val requestWithParams = FakeRequest("GET", urlWithParams)

          val result: Future[Result] = controller.rateBands()(requestWithParams)

          status(result)        mustBe OK
          contentAsJson(result) mustBe Json.toJson(ratePeriodsWithTaxTypeCodesToRateBandsWithMissingEntry)

          ratePeriodsWithTaxTypeCodes.foreach { case (ratePeriod, taxTypeCode) =>
            verify(mockRatesService).taxType(ratePeriod, taxTypeCode)
          }
      }
    }

    "return 400 BadRequest when" - {
      "'ratePeriods' parameter is missing" in {
        val mockRatesService: RatesService = mock[RatesService]

        val controller = new RatesController(
          fakeAuthorisedAction,
          mockRatesService,
          cc
        )

        val urlWithParams                =
          s"/rate-bands?taxTypeCodes=311"
        val requestWithMissingRatePeriod = FakeRequest("GET", urlWithParams)
        val result: Future[Result]       = controller.rateBands()(requestWithMissingRatePeriod)

        status(result)          mustBe BAD_REQUEST
        contentAsString(result) mustBe "Missing or invalid 'ratePeriods' parameter"
      }

      "'ratePeriods' parameter is invalid" in {
        val mockRatesService: RatesService = mock[RatesService]

        val controller = new RatesController(
          fakeAuthorisedAction,
          mockRatesService,
          cc
        )

        val urlWithParams                =
          s"/rate-bands?ratePeriods=1234&taxTypeCodes=311"
        val requestWithMissingRatePeriod = FakeRequest("GET", urlWithParams)
        val result: Future[Result]       = controller.rateBands()(requestWithMissingRatePeriod)

        status(result)          mustBe BAD_REQUEST
        contentAsString(result) mustBe "Invalid 'ratePeriods' parameter"
      }

      "'taxTypeCodes' parameter is missing" in {
        val mockRatesService: RatesService = mock[RatesService]

        val controller = new RatesController(
          fakeAuthorisedAction,
          mockRatesService,
          cc
        )

        val urlWithParams =
          s"/rate-bands?ratePeriods=${Json.toJson(YearMonth.of(2024, 10)).toString()}"

        val requestWithInvalidTaxType =
          FakeRequest("GET", urlWithParams)
        val result: Future[Result]    = controller.rateBands()(requestWithInvalidTaxType)

        status(result)          mustBe BAD_REQUEST
        contentAsString(result) mustBe "Missing or invalid 'taxTypeCodes' parameter"
      }

      "'taxTypeCodes' parameter is invalid" in {
        val mockRatesService: RatesService = mock[RatesService]

        val controller = new RatesController(
          fakeAuthorisedAction,
          mockRatesService,
          cc
        )

        val urlWithParams =
          s"/rate-bands?ratePeriods=${Json.toJson(YearMonth.of(2024, 10)).toString()}&taxTypeCodes=abcd"

        val requestWithInvalidTaxType =
          FakeRequest("GET", urlWithParams)
        val result: Future[Result]    = controller.rateBands()(requestWithInvalidTaxType)

        status(result)          mustBe BAD_REQUEST
        contentAsString(result) mustBe "Invalid 'taxTypeCodes' parameter"
      }

      "if too few rate periods" in {
        val mockRatesService: RatesService = mock[RatesService]

        val controller = new RatesController(
          fakeAuthorisedAction,
          mockRatesService,
          cc
        )

        val ratePeriods  = Seq(YearMonth.of(2024, 10))
        val taxTypeCodes = Seq("311", "312")

        val urlWithParams =
          s"/rate-bands?ratePeriods=${ratePeriods.map(ratePeriod => Json.toJson(ratePeriod).toString()).mkString(",")}&taxTypeCodes=${taxTypeCodes
            .mkString(",")}"

        val requestWithParams = FakeRequest("GET", urlWithParams)

        val result: Future[Result] = controller.rateBands()(requestWithParams)

        status(result)          mustBe BAD_REQUEST
        contentAsString(result) mustBe "Expected the number of ratePeriods and taxTypeCodes to match"
      }

      "if too few tax codes" in {
        val mockRatesService: RatesService = mock[RatesService]

        val controller = new RatesController(
          fakeAuthorisedAction,
          mockRatesService,
          cc
        )

        val ratePeriods  = Seq(YearMonth.of(2024, 10), YearMonth.of(2024, 9))
        val taxTypeCodes = Seq("311")

        val urlWithParams =
          s"/rate-bands?ratePeriods=${ratePeriods.map(ratePeriod => Json.toJson(ratePeriod).toString()).mkString(",")}&taxTypeCodes=${taxTypeCodes
            .mkString(",")}"

        val requestWithParams = FakeRequest("GET", urlWithParams)

        val result: Future[Result] = controller.rateBands()(requestWithParams)

        status(result)          mustBe BAD_REQUEST
        contentAsString(result) mustBe "Expected the number of ratePeriods and taxTypeCodes to match"
      }
    }
  }
}
