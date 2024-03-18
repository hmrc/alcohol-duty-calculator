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

import play.api.libs.json._
import play.api.mvc._
import uk.gov.hmrc.alcoholdutycalculator.controllers.actions.AuthorisedAction
import uk.gov.hmrc.alcoholdutycalculator.models.{AlcoholByVolume, AlcoholRegime, RateBand, RatePeriod, RateType}
import uk.gov.hmrc.alcoholdutycalculator.services.RatesService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import java.time.YearMonth
import javax.inject.Inject
import scala.util.Try

class RatesController @Inject() (
  authorise: AuthorisedAction,
  ratesService: RatesService,
  override val controllerComponents: ControllerComponents
) extends BackendController(controllerComponents) {

  def rates(): Action[AnyContent] =
    authorise { implicit request =>
      val queryParams = request.queryString

      val result: Either[String, Seq[RateBand]] = for {
        ratePeriod     <- extractParam[YearMonth]("ratePeriod", queryParams, RatePeriod.yearMonthFormat)
        rateType       <- extractParam[RateType]("rateType", queryParams, RateType.format)
        abv            <- extractParam[AlcoholByVolume]("abv", queryParams, AlcoholByVolume.format)
        alcoholRegimes <- extractParam[Set[AlcoholRegime]](
                            "alcoholRegimes",
                            queryParams,
                            Format(Reads.set[AlcoholRegime], Writes.set[AlcoholRegime])
                          )
      } yield ratesService.rateBands(ratePeriod, rateType, abv, alcoholRegimes)

      result.fold(
        error => BadRequest(error),
        rateBands => Ok(Json.toJson(rateBands))
      )
    }
  def validateTaxType(): Action[AnyContent] =
    authorise { implicit request =>
      val queryParams = request.queryString

      val result: Either[String, Boolean] = for { //is either fine?
        ratePeriod     <- extractParam[YearMonth]("ratePeriod", queryParams, RatePeriod.yearMonthFormat)
        taxCode        <- extractParam("taxType", queryParams, Json.format[String]) //is it fine to have extractparam without [] type and Strig format
        abv            <- extractParam[AlcoholByVolume]("abv", queryParams, AlcoholByVolume.format) //need to check abv and regime?
        alcoholRegimes <- extractParam[Set[AlcoholRegime]](
          "alcoholRegimes",
          queryParams,
          Format(Reads.set[AlcoholRegime], Writes.set[AlcoholRegime])
        )
      } yield ratesService.taxType(ratePeriod, taxCode, abv, alcoholRegimes)

      result.fold(
        error => BadRequest(error),
        validTaxCode => Ok(Json.toJson(validTaxCode))
      )
    }

  private def extractParam[T](
    paramName: String,
    queryParams: Map[String, Seq[String]],
    jsonFormat: Format[T]
  ): Either[String, T] =
    extractQueryParam(paramName, queryParams).flatMap(value =>
      Try(Json.parse(value).as[T](jsonFormat)).toEither.left.map(_ => s"Invalid '$paramName' parameter")
    )

  private def extractQueryParam(paramName: String, queryParams: Map[String, Seq[String]]): Either[String, String] =
    queryParams.get(paramName).flatMap(_.headOption).toRight(s"Missing or invalid '$paramName' parameter")

}
