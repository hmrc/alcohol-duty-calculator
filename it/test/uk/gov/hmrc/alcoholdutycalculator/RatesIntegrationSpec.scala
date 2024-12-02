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

  private lazy val jsonSchemaFactory: JsonSchemaFactory = JsonSchemaFactory.byDefault
  private lazy val jsonSchema: JsonSchema               =
    jsonSchemaFactory.getJsonSchema(s"resource:/rates/alcohol-duty-rates-schema.json")

  private val month = 5

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

  "validate rate file against schema" in {
    val stream: InputStream = getClass.getResourceAsStream("/rates/alcohol-duty-rates.json")
    val lines               = scala.io.Source.fromInputStream(stream).getLines().mkString
    validateJsonAgainstSchema(lines) mustBe true
  }

  "validate the rates within the file" - {
    // For every tax year we need a section! Seq(years and loop) up to a max of cy-5
    s"For cy" - {
      lazy val cy = 2023

      "For band A" - {
        val rate: Option[Double]  = Some(9.27d)
        val taxTypes: Seq[String] = Seq("311", "312", "313", "314", "315")
        val count                 = 5

        "service rates endpoint must" - {
          "show the correct rate" in {
            verifyRate(cy, taxTypes, count, rate)
          }
        }
      }

      "For band B" - {
        val rate: Option[Double]  = Some(21.01d)
        val taxTypes: Seq[String] = Seq("321")
        val count                 = 1

        "service rates endpoint must" - {
          "show the correct rate" in {
            verifyRate(cy, taxTypes, count, rate)
          }
        }
      }

      "For band C" - {
        val rate: Option[Double]  = Some(9.67d)
        val taxTypes: Seq[String] = Seq("322")
        val count                 = 1

        "service rates endpoint must" - {
          "show the correct rate" in {
            verifyRate(cy, taxTypes, count, rate)
          }
        }
      }

      "For band D" - {
        val rate: Option[Double]  = Some(24.77d)
        val taxTypes: Seq[String] = Seq("323", "324", "325")
        val count                 = 3

        "service rates endpoint must" - {
          "show the correct rate" in {
            verifyRate(cy, taxTypes, count, rate)
          }
        }
      }

      "For band E" - {
        val rate: Option[Double]  = Some(28.50d)
        val taxTypes: Seq[String] = Seq("331", "333", "334", "335")
        val count                 = 4

        "service rates endpoint must" - {
          "show the correct rate" in {
            verifyRate(cy, taxTypes, count, rate)
          }
        }
      }

      "For band F" - {
        val rate: Option[Double]  = Some(31.64d)
        val taxTypes: Seq[String] = Seq("341", "343", "344", "345")
        val count                 = 4

        "service rates endpoint must" - {
          "show the correct rate" in {
            verifyRate(cy, taxTypes, count, rate)
          }
        }
      }

      "For band G" - {
        val rate: Option[Double]  = Some(8.42d)
        val taxTypes: Seq[String] = Seq("351", "352", "353", "354", "355")
        val count                 = 5

        "service rates endpoint must" - {
          "show the correct rate" in {
            verifyRate(cy, taxTypes, count, rate)
          }
        }
      }

      "For band H" - {
        val rate: Option[Double]  = Some(19.08d)
        val taxTypes: Seq[String] = Seq("356")
        val count                 = 1

        "service rates endpoint must" - {
          "show the correct rate" in {
            verifyRate(cy, taxTypes, count, rate)
          }
        }
      }

      "For band I" - {
        val rate: Option[Double]  = Some(8.78d)
        val taxTypes: Seq[String] = Seq("357")
        val count                 = 1

        "service rates endpoint must" - {
          "show the correct rate" in {
            verifyRate(cy, taxTypes, count, rate)
          }
        }
      }

      "For band J" - {
        val rate: Option[Double]  = Some(19.08d)
        val taxTypes: Seq[String] = Seq("358", "359", "360")
        val count                 = 3

        "service rates endpoint must" - {
          "show the correct rate" in {
            verifyRate(cy, taxTypes, count, rate)
          }
        }
      }

      "For band K" - {
        val taxTypes: Seq[String] = Seq("371", "372", "373", "374", "375", "376", "377", "378", "379", "380")
        val count                 = 10

        "service rates endpoint must" - {
          "show the correct rate" in {
            verifyRate(cy, taxTypes, count, None)
          }
        }
      }

    }

    // TODO when we hit the next tax year current cy tests become cy-1 and cy can be updated with new values (up to cy-5)
    s"For cy-1" - {
      "For band A" - {}
      "For band B" - {}
      "For band C" - {}
      // etc...
    }
  }

  private def verifyRate(taxYear: Int, taxTypes: Seq[String], count: Int, rate: Option[Double]): Unit = {
    val rateBandList = getRatesFromJson(taxYear)
    val rates        = rateBandList.filter(rateBand => taxTypes.contains(rateBand.taxTypeCode))

    rates must have size count
    rates.foreach(band => band.rate mustBe rate)
  }

  private def getRatesFromJson(taxYear: Int): Seq[RateBand] = {
    stubAuthorised()

    val urlParams =
      s"?ratePeriod=${Json
        .toJson(
          YearMonth.of(taxYear, month)
        )(RatePeriod.yearMonthFormat)
        .toString()}&alcoholRegimes=Beer,Cider,Wine,Spirits,OtherFermentedProduct"

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
