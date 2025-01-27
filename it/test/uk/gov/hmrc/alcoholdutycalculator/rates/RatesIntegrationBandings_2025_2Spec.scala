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

class RatesIntegrationBandings_2025_2Spec extends ISpecBase {

  val schemaUri: String                                 = getClass.getResource("/alcohol-duty-rates-schema.json").toURI.toString
  private lazy val jsonSchemaFactory: JsonSchemaFactory = JsonSchemaFactory.byDefault
  private lazy val jsonSchema: JsonSchema               = jsonSchemaFactory.getJsonSchema(schemaUri)
  private lazy val year: Int                            = 2025
  private lazy val startMonthInclusive: Int             = 2 // Feb 25
  private lazy val endMonthExclusive: Int               = 13
  private lazy val month: Int                           = Random.between(startMonthInclusive, endMonthExclusive)
  private lazy val totalNoRateBands: Int                = 48
  private lazy val totalNumberRangeDetails: Int         = 52
  private lazy val totalNumberRanges: Int               = 56

  private lazy val ratePeriod: String = Json
    .toJson(
      YearMonth.of(year, month)
    )(RatePeriod.yearMonthFormat)
    .toString()

  s"rates file for period $ratePeriod must" - {
    "validate against schema" in {
      val stream: InputStream = getClass.getResourceAsStream("/rates/alcohol-duty-rates.json")
      val lines: String       = scala.io.Source.fromInputStream(stream).getLines().mkString
      validateJsonAgainstSchema(lines) mustBe true
    }

    "hold valid data" - {
      lazy val rateBandList: Seq[RateBand] = getRatesFromJson(ratePeriod)

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
        val rate: Option[Double]   = Some(9.61d)
        val taxTypes: Seq[String]  = Seq("311", "312", "313", "314", "315")
        val rateCount: Int         = 5
        val rangeDetailsCount: Int = 5
        val rangesCount: Int       = 5

        "service rates endpoint must" - {
          "show the correct rate" in {
            verify(rateBandList, taxTypes, rateCount, rangeDetailsCount, rangesCount, rate)
          }
        }
      }

      "For band B" - {
        val rate: Option[Double]   = Some(21.78d)
        val taxTypes: Seq[String]  = Seq("321")
        val rateCount: Int         = 1
        val rangeDetailsCount: Int = 1
        val rangesCount: Int       = 1

        "service rates endpoint must" - {
          "show the correct rate" in {
            verify(rateBandList, taxTypes, rateCount, rangeDetailsCount, rangesCount, rate)
          }
        }
      }

      "For band C" - {
        val rate: Option[Double]   = Some(10.02d)
        val taxTypes: Seq[String]  = Seq("322")
        val rateCount: Int         = 1
        val rangeDetailsCount: Int = 1
        val rangesCount: Int       = 2

        "service rates endpoint must" - {
          "show the correct rate" in {
            verify(rateBandList, taxTypes, rateCount, rangeDetailsCount, rangesCount, rate)
          }
        }
      }

      "For band D" - {
        val rate: Option[Double]   = Some(25.67d)
        val taxTypes: Seq[String]  = Seq("323", "324", "325")
        val rateCount: Int         = 3
        val rangeDetailsCount: Int = 4
        val rangesCount: Int       = 4

        "service rates endpoint must" - {
          "show the correct rate" in {
            verify(rateBandList, taxTypes, rateCount, rangeDetailsCount, rangesCount, rate)
          }
        }
      }

      "For band E" - {
        val rate: Option[Double]   = Some(29.54d)
        val taxTypes: Seq[String]  = Seq("331", "333", "334", "335")
        val rateCount: Int         = 4
        val rangeDetailsCount: Int = 4
        val rangesCount: Int       = 4

        "service rates endpoint must" - {
          "show the correct rate" in {
            verify(rateBandList, taxTypes, rateCount, rangeDetailsCount, rangesCount, rate)
          }
        }
      }

      "For band F" - {
        val rate: Option[Double]   = Some(32.79d)
        val taxTypes: Seq[String]  = Seq("341", "343", "344", "345")
        val rateCount: Int         = 4
        val rangeDetailsCount: Int = 4
        val rangesCount: Int       = 4

        "service rates endpoint must" - {
          "show the correct rate" in {
            verify(rateBandList, taxTypes, rateCount, rangeDetailsCount, rangesCount, rate)
          }
        }
      }

      "For band G" - {
        val rate: Option[Double]   = Some(8.28d)
        val taxTypes: Seq[String]  = Seq("351", "352", "353", "354", "355")
        val rateCount: Int         = 5
        val rangeDetailsCount: Int = 5
        val rangesCount: Int       = 5

        "service rates endpoint must" - {
          "show the correct rate" in {
            verify(rateBandList, taxTypes, rateCount, rangeDetailsCount, rangesCount, rate)
          }
        }
      }

      "For band H" - {
        val rate: Option[Double]   = Some(18.76d)
        val taxTypes: Seq[String]  = Seq("356")
        val rateCount: Int         = 1
        val rangeDetailsCount: Int = 1
        val rangesCount: Int       = 1

        "service rates endpoint must" - {
          "show the correct rate" in {
            verify(rateBandList, taxTypes, rateCount, rangeDetailsCount, rangesCount, rate)
          }
        }
      }

      "For band I" - {
        val rate: Option[Double]   = Some(8.63d)
        val taxTypes: Seq[String]  = Seq("357")
        val rateCount: Int         = 1
        val rangeDetailsCount: Int = 1
        val rangesCount: Int       = 2

        "service rates endpoint must" - {
          "show the correct rate" in {
            verify(rateBandList, taxTypes, rateCount, rangeDetailsCount, rangesCount, rate)
          }
        }
      }

      "For band J" - {
        val rate: Option[Double]   = Some(18.76d)
        val taxTypes: Seq[String]  = Seq("358", "359", "360")
        val rateCount: Int         = 3
        val rangeDetailsCount: Int = 4
        val rangesCount: Int       = 4

        "service rates endpoint must" - {
          "show the correct rate" in {
            verify(rateBandList, taxTypes, rateCount, rangeDetailsCount, rangesCount, rate)
          }
        }
      }

      "For band K" - {
        val taxTypes: Seq[String]  = Seq("371", "372", "373", "374", "375", "376", "377", "378", "379", "380")
        val rateCount: Int         = 10
        val rangeDetailsCount: Int = 11
        val rangesCount: Int       = 12

        "service rates endpoint must" - {
          "show the correct rate" in {
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
