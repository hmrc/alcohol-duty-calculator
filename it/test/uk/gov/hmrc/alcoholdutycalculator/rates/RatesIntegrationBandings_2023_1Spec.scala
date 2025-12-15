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

package uk.gov.hmrc.alcoholdutycalculator.rates

import com.fasterxml.jackson.databind.JsonNode
import com.github.fge.jackson.JsonLoader
import com.github.fge.jsonschema.core.report.ProcessingReport
import com.github.fge.jsonschema.main.{JsonSchema, JsonSchemaFactory}
import play.api.libs.json.Json
import play.api.test.FakeRequest
import uk.gov.hmrc.alcoholdutycalculator.base.ISpecBase
import uk.gov.hmrc.alcoholdutycalculator.controllers.routes
import uk.gov.hmrc.alcoholdutycalculator.models._

import java.io.InputStream
import java.time.YearMonth
import scala.util.Random

class RatesIntegrationBandings_2023_1Spec extends ISpecBase {

  val schemaUri: String                                 = getClass.getResource("/alcohol-duty-rates-schema.json").toURI.toString
  private lazy val jsonSchemaFactory: JsonSchemaFactory = JsonSchemaFactory.byDefault
  private lazy val jsonSchema: JsonSchema               = jsonSchemaFactory.getJsonSchema(schemaUri)
  private lazy val totalNoRateBands: Int                = 48
  private lazy val totalNumberRangeDetails: Int         = 52
  private lazy val totalNumberRanges: Int               = 56
  private lazy val periodYear: Int                      = 2023
  private lazy val periodStartMonthInclusive: Int       = 1
  private lazy val periodEndMonthExclusive: Int         = 13
  private lazy val periodMonth: Int                     = Random.between(periodStartMonthInclusive, periodEndMonthExclusive)

  private lazy val ratePeriod: String = Json
    .toJson(
      YearMonth.of(periodYear, periodMonth)
    )(RatePeriod.yearMonthFormat)
    .toString()

  // Boundary test for last valid month in period where this period has been end dated and a new period created
  // For "validityEndDate": "2025-02" where validityEndDate is 'Exclusive'
  private lazy val periodEndYear: Int        = 2025
  private lazy val periodEndMonth: Int       = 1
  private lazy val closingRatePeriod: String = Json
    .toJson(
      YearMonth.of(periodEndYear, periodEndMonth)
    )(RatePeriod.yearMonthFormat)
    .toString()

  val periods: List[String] = List(ratePeriod, closingRatePeriod)

  periods.foreach { validRatePeriod =>
    s"rates file for period $validRatePeriod must" - {
      "validate against schema" in {
        val stream: InputStream = getClass.getResourceAsStream("/rates/alcohol-duty-rates.json")
        val lines: String       = scala.io.Source.fromInputStream(stream).getLines().mkString
        validateJsonAgainstSchema(lines) mustBe true
      }

      "hold valid data" - {
        lazy val rateBandList: Seq[RateBand] = getRatesFromJson(validRatePeriod)

        "for all entries" - {
          "have the correct count of rate bands" in {
            rateBandList.length mustBe totalNoRateBands
          }

          "have the correct count of range details" in {
            val rangeDetails: Seq[RangeDetailsByRegime] = rateBandList.flatMap(x => x.rangeDetails)
            rangeDetails.length mustBe totalNumberRangeDetails
          }

          "have the correct count of ranges" in {
            val rangeDetails: Seq[RangeDetailsByRegime] = rateBandList.flatMap(x => x.rangeDetails)
            val ranges: Seq[ABVRange]                   = rangeDetails.flatMap(y => y.abvRanges)
            ranges.length mustBe totalNumberRanges
          }
        }

        "For band A" - {
          val rate: Option[Double]   = Some(9.27d)
          val taxTypes: Seq[String]  = Seq("311", "312", "313", "314", "315")
          val rateCount: Int         = 5
          val rangeDetailsCount: Int = 5
          val rangesCount: Int       = 5

          "service rates endpoint must" - {
            "show the correct rate" in
              verify(rateBandList, taxTypes, rateCount, rangeDetailsCount, rangesCount, rate)
          }
        }

        "For band B" - {
          val rate: Option[Double]   = Some(21.01d)
          val taxTypes: Seq[String]  = Seq("321")
          val rateCount: Int         = 1
          val rangeDetailsCount: Int = 1
          val rangesCount: Int       = 1

          "service rates endpoint must" - {
            "show the correct rate" in
              verify(rateBandList, taxTypes, rateCount, rangeDetailsCount, rangesCount, rate)
          }
        }

        "For band C" - {
          val rate: Option[Double]   = Some(9.67d)
          val taxTypes: Seq[String]  = Seq("322")
          val rateCount: Int         = 1
          val rangeDetailsCount: Int = 1
          val rangesCount: Int       = 2

          "service rates endpoint must" - {
            "show the correct rate" in
              verify(rateBandList, taxTypes, rateCount, rangeDetailsCount, rangesCount, rate)
          }
        }

        "For band D" - {
          val rate: Option[Double]   = Some(24.77d)
          val taxTypes: Seq[String]  = Seq("323", "324", "325")
          val rateCount: Int         = 3
          val rangeDetailsCount: Int = 4
          val rangesCount: Int       = 4

          "service rates endpoint must" - {
            "show the correct rate" in
              verify(rateBandList, taxTypes, rateCount, rangeDetailsCount, rangesCount, rate)
          }
        }

        "For band E" - {
          val rate: Option[Double]   = Some(28.50d)
          val taxTypes: Seq[String]  = Seq("331", "333", "334", "335")
          val rateCount: Int         = 4
          val rangeDetailsCount: Int = 4
          val rangesCount: Int       = 4

          "service rates endpoint must" - {
            "show the correct rate" in
              verify(rateBandList, taxTypes, rateCount, rangeDetailsCount, rangesCount, rate)
          }
        }

        "For band F" - {
          val rate: Option[Double]   = Some(31.64d)
          val taxTypes: Seq[String]  = Seq("341", "343", "344", "345")
          val rateCount: Int         = 4
          val rangeDetailsCount: Int = 4
          val rangesCount: Int       = 4

          "service rates endpoint must" - {
            "show the correct rate" in
              verify(rateBandList, taxTypes, rateCount, rangeDetailsCount, rangesCount, rate)
          }
        }

        "For band G" - {
          val rate: Option[Double]   = Some(8.42d)
          val taxTypes: Seq[String]  = Seq("351", "352", "353", "354", "355")
          val rateCount: Int         = 5
          val rangeDetailsCount: Int = 5
          val rangesCount: Int       = 5

          "service rates endpoint must" - {
            "show the correct rate" in
              verify(rateBandList, taxTypes, rateCount, rangeDetailsCount, rangesCount, rate)
          }
        }

        "For band H" - {
          val rate: Option[Double]   = Some(19.08d)
          val taxTypes: Seq[String]  = Seq("356")
          val rateCount: Int         = 1
          val rangeDetailsCount: Int = 1
          val rangesCount: Int       = 1

          "service rates endpoint must" - {
            "show the correct rate" in
              verify(rateBandList, taxTypes, rateCount, rangeDetailsCount, rangesCount, rate)
          }
        }

        "For band I" - {
          val rate: Option[Double]   = Some(8.78d)
          val taxTypes: Seq[String]  = Seq("357")
          val rateCount: Int         = 1
          val rangeDetailsCount: Int = 1
          val rangesCount: Int       = 2

          "service rates endpoint must" - {
            "show the correct rate" in
              verify(rateBandList, taxTypes, rateCount, rangeDetailsCount, rangesCount, rate)
          }
        }

        "For band J" - {
          val rate: Option[Double]   = Some(19.08d)
          val taxTypes: Seq[String]  = Seq("358", "359", "360")
          val rateCount: Int         = 3
          val rangeDetailsCount: Int = 4
          val rangesCount: Int       = 4

          "service rates endpoint must" - {
            "show the correct rate" in
              verify(rateBandList, taxTypes, rateCount, rangeDetailsCount, rangesCount, rate)
          }
        }

        "For band K" - {
          val taxTypes: Seq[String]  = Seq("371", "372", "373", "374", "375", "376", "377", "378", "379", "380")
          val rateCount: Int         = 10
          val rangeDetailsCount: Int = 11
          val rangesCount: Int       = 12

          "service rates endpoint must" - {
            "show the correct rate" in
              verify(rateBandList, taxTypes, rateCount, rangeDetailsCount, rangesCount, None)
          }
        }
      }
    }
  }

  private def verify(
    rateBandList: Seq[RateBand],
    taxTypes: Seq[String],
    rateCount: Int,
    rangeDetailsCount: Int,
    rangesCount: Int,
    rate: Option[Double]
  ): Unit = {

    val rates: Seq[RateBand]                    = rateBandList.filter(rateBand => taxTypes.contains(rateBand.taxTypeCode))
    val rangeDetails: Seq[RangeDetailsByRegime] = rates.flatMap(x => x.rangeDetails)
    val ranges: Seq[ABVRange]                   = rangeDetails.flatMap(y => y.abvRanges)

    rates        must have size rateCount
    rangeDetails must have size rangeDetailsCount
    ranges       must have size rangesCount
    rates.foreach(band => band.rate mustBe rate)
  }

  private def getRatesFromJson(ratePeriod: String): Seq[RateBand] = {
    stubAuthorised()

    val urlParams =
      s"?ratePeriod=$ratePeriod&alcoholRegimes=Beer,Cider,Wine,Spirits,OtherFermentedProduct"

    lazy val result =
      callRoute(FakeRequest("GET", routes.RatesController.rates().url + urlParams))

    status(result) mustBe OK
    Json.parse(contentAsString(result)).as[Seq[RateBand]]
  }

  private def validateJsonAgainstSchema(inputDoc: String): Boolean = {
    val inputJson: JsonNode      = JsonLoader.fromString(inputDoc)
    val report: ProcessingReport = jsonSchema.validate(inputJson)
    report.isSuccess
  }
}
