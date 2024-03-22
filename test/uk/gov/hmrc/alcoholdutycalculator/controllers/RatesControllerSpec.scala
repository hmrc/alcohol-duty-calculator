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
import uk.gov.hmrc.alcoholdutycalculator.models.RateType.DraughtAndSmallProducerRelief
import uk.gov.hmrc.alcoholdutycalculator.models.{AlcoholByVolume, AlcoholRegime, RateBand, RatePeriod, RateType, RateTypeResponse}
import uk.gov.hmrc.alcoholdutycalculator.services.RatesService

import scala.concurrent.Future

class RatesControllerSpec extends SpecBase {

  val mockRatesService: RatesService = mock[RatesService]

  val controller = new RatesController(
    fakeAuthorisedAction,
    mockRatesService,
    cc
  )

  "rates" should {
    "return 200 OK with alcohol duty rates based on query parameters" in forAll {
      (
        rateBandList: Seq[RateBand],
        ratePeriod: YearMonth,
        rateType: RateType,
        abv: AlcoholByVolume,
        alcoholRegimes: Set[AlcoholRegime]
      ) =>
        when(mockRatesService.rateBands(any(), any(), any(), any())).thenReturn(rateBandList)

        val urlWithParams =
          s"/rates?ratePeriod=${Json.toJson(ratePeriod)(RatePeriod.yearMonthFormat).toString()}&rateType=${Json
            .toJson(rateType)
            .toString}&abv=${Json.toJson(abv).toString}&alcoholRegimes=${Json.toJson(alcoholRegimes).toString()}"

        val requestWithParams = FakeRequest("GET", urlWithParams)

        val result: Future[Result] = controller.rates()(requestWithParams)

        status(result)        shouldBe OK
        contentAsJson(result) shouldBe Json.toJson(rateBandList)

        verify(mockRatesService).rateBands(ratePeriod, rateType, abv, alcoholRegimes)
    }

  }
  "rateTypes" should {
    "return 200 OK with rate type based on query parameters" in forAll {
      (
        ratePeriod: YearMonth,
        abv: AlcoholByVolume,
        alcoholRegimes: Set[AlcoholRegime],
        rateType: RateTypeResponse
      ) =>
        when(mockRatesService.rateTypes(any(), any(), any())).thenReturn(rateType)

        val urlWithParams =
          s"/rate-type?ratePeriod=${Json.toJson(ratePeriod)(RatePeriod.yearMonthFormat).toString()}&abv=${Json.toJson(abv).toString}" +
            s"&alcoholRegimes=${Json.toJson(alcoholRegimes).toString()}"

        val requestWithParams = FakeRequest("GET", urlWithParams)

        val result: Future[Result] = controller.rateType()(requestWithParams)

        status(result)        shouldBe OK
        contentAsJson(result) shouldBe Json.toJson(rateType)

        verify(mockRatesService).rateTypes(ratePeriod, abv, alcoholRegimes)
    }
  }

  "return BadRequest" when {
    "'ratePeriod' parameter is missing" in forAll {
      (
        rateType: RateType,
        abv: AlcoholByVolume,
        alcoholRegimes: Set[AlcoholRegime]
      ) =>
        val urlWithParams                =
          s"/rates?rateType=${Json
            .toJson(rateType)
            .toString}&abv=${Json.toJson(abv).toString}&alcoholRegimes=${Json.toJson(alcoholRegimes).toString()}"
        val requestWithMissingRatePeriod = FakeRequest("GET", urlWithParams)
        val result: Future[Result]       = controller.rates()(requestWithMissingRatePeriod)

        status(result)        shouldBe BAD_REQUEST
        contentAsString(result) should include("Missing or invalid 'ratePeriod' parameter")
    }

    "'ratePeriod' parameter is missing in rateTypes" in forAll {
      (
        abv: AlcoholByVolume,
        alcoholRegimes: Set[AlcoholRegime]
      ) =>
        val urlWithParams                =
          s"/rates?abv=${Json.toJson(abv).toString}&alcoholRegimes=${Json.toJson(alcoholRegimes).toString()}"
        val requestWithMissingRatePeriod = FakeRequest("GET", urlWithParams)
        val result: Future[Result]       = controller.rateType()(requestWithMissingRatePeriod)

        status(result)        shouldBe BAD_REQUEST
        contentAsString(result) should include("Missing or invalid 'ratePeriod' parameter")
    }

    "'ratePeriod' parameter is invalid" in forAll {
      (
        rateType: RateType,
        abv: AlcoholByVolume,
        alcoholRegimes: Set[AlcoholRegime]
      ) =>
        val urlWithParams                =
          s"/rates?ratePeriod=1234&rateType=${Json
            .toJson(rateType)
            .toString}&abv=${Json.toJson(abv).toString}&alcoholRegimes=${Json.toJson(alcoholRegimes).toString()}"
        val requestWithMissingRatePeriod = FakeRequest("GET", urlWithParams)
        val result: Future[Result]       = controller.rates()(requestWithMissingRatePeriod)

        status(result)        shouldBe BAD_REQUEST
        contentAsString(result) should include("Invalid 'ratePeriod' parameter")
    }

    "'ratePeriod' parameter is invalid in rateTypes" in forAll {
      (
        abv: AlcoholByVolume,
        alcoholRegimes: Set[AlcoholRegime]
      ) =>
        val urlWithParams                =
          s"/rates?ratePeriod=1234&abv=${Json.toJson(abv).toString}&alcoholRegimes=${Json.toJson(alcoholRegimes).toString()}"
        val requestWithMissingRatePeriod = FakeRequest("GET", urlWithParams)
        val result: Future[Result]       = controller.rateType()(requestWithMissingRatePeriod)

        status(result)        shouldBe BAD_REQUEST
        contentAsString(result) should include("Invalid 'ratePeriod' parameter")
    }

    "'rateType' parameter is missing" in forAll {
      (
        ratePeriod: YearMonth,
        abv: AlcoholByVolume,
        alcoholRegimes: Set[AlcoholRegime]
      ) =>
        val urlWithParams              =
          s"/rates?ratePeriod=${Json.toJson(ratePeriod)(RatePeriod.yearMonthFormat).toString()}&abv=${Json
            .toJson(abv)
            .toString}&alcoholRegimes=${Json.toJson(alcoholRegimes).toString()}"
        val requestWithInvalidRateType =
          FakeRequest("GET", urlWithParams)
        val result: Future[Result]     = controller.rates()(requestWithInvalidRateType)

        status(result)        shouldBe BAD_REQUEST
        contentAsString(result) should include("Missing or invalid 'rateType' parameter")
    }

    "'rateType' parameter is invalid" in forAll {
      (
        ratePeriod: YearMonth,
        abv: AlcoholByVolume,
        alcoholRegimes: Set[AlcoholRegime]
      ) =>
        val urlWithParams              =
          s"/rates?ratePeriod=${Json.toJson(ratePeriod)(RatePeriod.yearMonthFormat).toString()}&rateType=1234&abv=${Json
            .toJson(abv)
            .toString}&alcoholRegimes=${Json.toJson(alcoholRegimes).toString()}"
        val requestWithInvalidRateType =
          FakeRequest("GET", urlWithParams)
        val result: Future[Result]     = controller.rates()(requestWithInvalidRateType)

        status(result)        shouldBe BAD_REQUEST
        contentAsString(result) should include("Invalid 'rateType' parameter")
    }

    "'abv' parameter is missing" in forAll {
      (
        ratePeriod: YearMonth,
        rateType: RateType,
        alcoholRegimes: Set[AlcoholRegime]
      ) =>
        val urlWithParams =
          s"/rates?ratePeriod=${Json.toJson(ratePeriod)(RatePeriod.yearMonthFormat).toString()}&rateType=${Json
            .toJson(rateType)
            .toString}&alcoholRegimes=${Json.toJson(alcoholRegimes).toString()}"

        val requestWithMissingAbv  = FakeRequest("GET", urlWithParams)
        val result: Future[Result] = controller.rates()(requestWithMissingAbv)

        status(result)        shouldBe BAD_REQUEST
        contentAsString(result) should include("Missing or invalid 'abv' parameter")
    }

    "'abv' parameter is missing in rate type request" in forAll {
      (
        ratePeriod: YearMonth,
        alcoholRegimes: Set[AlcoholRegime]
      ) =>
        val urlWithParams =
          s"/rates?ratePeriod=${Json.toJson(ratePeriod)(RatePeriod.yearMonthFormat).toString()}&alcoholRegimes=${Json.toJson(alcoholRegimes).toString()}"

        val requestWithMissingAbv  = FakeRequest("GET", urlWithParams)
        val result: Future[Result] = controller.rateType()(requestWithMissingAbv)

        status(result)        shouldBe BAD_REQUEST
        contentAsString(result) should include("Missing or invalid 'abv' parameter")
    }

    "'abv' parameter is invalid" in forAll {
      (
        ratePeriod: YearMonth,
        rateType: RateType,
        alcoholRegimes: Set[AlcoholRegime]
      ) =>
        val urlWithParams =
          s"/rates?ratePeriod=${Json.toJson(ratePeriod)(RatePeriod.yearMonthFormat).toString()}&rateType=${Json
            .toJson(rateType)
            .toString}&abv=abcd&alcoholRegimes=${Json.toJson(alcoholRegimes).toString()}"

        val requestWithMissingAbv  = FakeRequest("GET", urlWithParams)
        val result: Future[Result] = controller.rates()(requestWithMissingAbv)

        status(result)        shouldBe BAD_REQUEST
        contentAsString(result) should include("Invalid 'abv' parameter")
    }

    "'abv' parameter is invalid in rateType request" in forAll {
      (
        ratePeriod: YearMonth,
        alcoholRegimes: Set[AlcoholRegime]
      ) =>
        val urlWithParams =
          s"/rates?ratePeriod=${Json.toJson(ratePeriod)(RatePeriod.yearMonthFormat).toString()}&abv=abcd&alcoholRegimes=${Json.toJson(alcoholRegimes).toString()}"

        val requestWithMissingAbv  = FakeRequest("GET", urlWithParams)
        val result: Future[Result] = controller.rateType()(requestWithMissingAbv)

        status(result)        shouldBe BAD_REQUEST
        contentAsString(result) should include("Invalid 'abv' parameter")
    }

    "'alcoholRegimes' parameter is missing" in forAll {
      (
        ratePeriod: YearMonth,
        rateType: RateType,
        abv: AlcoholByVolume
      ) =>
        val urlWithParams =
          s"/rates?ratePeriod=${Json.toJson(ratePeriod)(RatePeriod.yearMonthFormat).toString()}&rateType=${Json
            .toJson(rateType)
            .toString}&abv=${Json.toJson(abv).toString}"

        val requestWithInvalidRegimes =
          FakeRequest("GET", urlWithParams)
        val result: Future[Result]    = controller.rates()(requestWithInvalidRegimes)

        status(result)        shouldBe BAD_REQUEST
        contentAsString(result) should include("Missing or invalid 'alcoholRegimes' parameter")
    }

    "'alcoholRegimes' parameter is missing in rateType request" in forAll {
      (
        ratePeriod: YearMonth,
        abv: AlcoholByVolume
      ) =>
        val urlWithParams =
          s"/rates?ratePeriod=${Json.toJson(ratePeriod)(RatePeriod.yearMonthFormat).toString()}&abv=${Json.toJson(abv).toString}"

        val requestWithInvalidRegimes =
          FakeRequest("GET", urlWithParams)
        val result: Future[Result]    = controller.rateType()(requestWithInvalidRegimes)

        status(result)        shouldBe BAD_REQUEST
        contentAsString(result) should include("Missing or invalid 'alcoholRegimes' parameter")
    }

    "'alcoholRegimes' parameter is invalid" in forAll {
      (
        ratePeriod: YearMonth,
        rateType: RateType,
        abv: AlcoholByVolume
      ) =>
        val urlWithParams =
          s"/rates?ratePeriod=${Json.toJson(ratePeriod)(RatePeriod.yearMonthFormat).toString()}&rateType=${Json
            .toJson(rateType)
            .toString}&abv=${Json.toJson(abv).toString}&alcoholRegimes=1234"

        val requestWithInvalidRegimes =
          FakeRequest("GET", urlWithParams)
        val result: Future[Result]    = controller.rates()(requestWithInvalidRegimes)

        status(result)        shouldBe BAD_REQUEST
        contentAsString(result) should include("Invalid 'alcoholRegimes' parameter")
    }

    "'alcoholRegimes' parameter is invalid in rateType request" in forAll {
      (
        ratePeriod: YearMonth,
        abv: AlcoholByVolume
      ) =>
        val urlWithParams =
          s"/rates?ratePeriod=${Json.toJson(ratePeriod)(RatePeriod.yearMonthFormat).toString()}&abv=${Json.toJson(abv).toString}&alcoholRegimes=1234"

        val requestWithInvalidRegimes =
          FakeRequest("GET", urlWithParams)
        val result: Future[Result]    = controller.rateType()(requestWithInvalidRegimes)

        status(result)        shouldBe BAD_REQUEST
        contentAsString(result) should include("Invalid 'alcoholRegimes' parameter")
    }
  }
}
