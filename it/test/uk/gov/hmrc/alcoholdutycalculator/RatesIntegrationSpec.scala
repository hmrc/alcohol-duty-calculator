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

package uk.gov.hmrc.alcoholdutycalculator

import com.fasterxml.jackson.databind.JsonNode
import com.github.fge.jackson.JsonLoader
import com.github.fge.jsonschema.core.report.ProcessingReport
import com.github.fge.jsonschema.main.{JsonSchema, JsonSchemaFactory}
import play.api.libs.json.Json
import play.api.test.FakeRequest
import uk.gov.hmrc.alcoholdutycalculator.base.ISpecBase
import uk.gov.hmrc.alcoholdutycalculator.controllers.routes
import uk.gov.hmrc.alcoholdutycalculator.models.{RateBand, RatePeriod, RateType}

import java.io.InputStream
import java.time.YearMonth

class RatesIntegrationSpec extends ISpecBase {

  "validates rate file against schema" in {
    val stream: InputStream = getClass.getResourceAsStream("/rates/alcohol-duty-rates.json")
    val lines               = scala.io.Source.fromInputStream(stream).getLines().mkString
    validateJsonAgainstSchema(lines) mustBe true
  }

  "for beer" - {
    "where tax type code 311, 312, 313, 314, 315" - {
      "service rates endpoint must" - {
        "show the correct rate" in {
          stubAuthorised()

          val expectedRate: Double = 9.27d

          val urlParams =
            s"?ratePeriod=${Json.toJson(YearMonth.of(2023, 5))(RatePeriod.yearMonthFormat).toString()}&alcoholRegimes=Beer"

          lazy val result =
            callRoute(FakeRequest("GET", routes.RatesController.rates().url + urlParams))

          status(result) mustBe OK
          val rateBandList = Json.parse(contentAsString(result)).as[Seq[RateBand]]

          rateBandList.find(x => Seq("311", "312", "313", "314", "315").contains(x.taxTypeCode)).get.rate mustBe Some(
            expectedRate
          )
        }
      }
    }
  }

  "service rates endpoint must" - {
    "respond with 200 status" in {
      stubAuthorised()

      val urlParams =
        s"?ratePeriod=${Json.toJson(YearMonth.of(2023, 5))(RatePeriod.yearMonthFormat).toString()}&alcoholRegimes=Beer,Wine,OtherFermentedProduct"

      lazy val result =
        callRoute(FakeRequest("GET", routes.RatesController.rates().url + urlParams))

      status(result) mustBe OK
      val rateBandList = Json.parse(contentAsString(result)).as[Seq[RateBand]]
      rateBandList must have size 30
    }
  }

  "service rate-band endpoint must" - {
    "respond with 200 status" in {
      stubAuthorised()
      val taxType     = "321"
      val urlParams   =
        s"?ratePeriod=${Json.toJson(YearMonth.of(2023, 5))(RatePeriod.yearMonthFormat).toString()}&taxTypeCode=$taxType"
      lazy val result =
        callRoute(FakeRequest("GET", routes.RatesController.rateBand().url + urlParams))
      status(result) mustBe OK
      val rateBand: RateBand = Json.parse(contentAsString(result)).as[RateBand]
      rateBand.rateType mustBe RateType.Core
    }
  }

  private lazy val jsonSchemaFactory: JsonSchemaFactory = JsonSchemaFactory.byDefault
  private lazy val jsonSchema: JsonSchema               =
    jsonSchemaFactory.getJsonSchema(s"resource:/rates/alcohol-duty-rates-schema.json")

  def validateJsonAgainstSchema(inputDoc: String): Boolean = {
    val inputJson: JsonNode      = JsonLoader.fromString(inputDoc)
    val report: ProcessingReport = jsonSchema.validate(inputJson)
    report.isSuccess
  }
}
