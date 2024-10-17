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

import cats.implicits._
import play.api.Logging
import play.api.libs.json.Format.GenericFormat
import play.api.libs.json._
import play.api.mvc._
import uk.gov.hmrc.alcoholdutycalculator.controllers.actions.AuthorisedAction
import uk.gov.hmrc.alcoholdutycalculator.models.{AlcoholRegime, RateBand}
import uk.gov.hmrc.alcoholdutycalculator.models.RatePeriod._
import uk.gov.hmrc.alcoholdutycalculator.services.RatesService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import java.time.YearMonth
import javax.inject.Inject
import scala.util.Try

class RatesController @Inject() (
  authorise: AuthorisedAction,
  ratesService: RatesService,
  override val controllerComponents: ControllerComponents
) extends BackendController(controllerComponents)
    with Logging {

  def rates(): Action[AnyContent] =
    authorise { implicit request =>
      val queryParams = request.queryString

      val result: Either[String, Seq[RateBand]] = for {
        ratePeriod     <- extractParam[YearMonth]("ratePeriod", queryParams)
        alcoholRegimes <- extractQueryParam(
                            "alcoholRegimes",
                            queryParams
                          ).flatMap(_.split(',').toList.map(AlcoholRegime.fromString).sequence.map(_.toSet))
      } yield ratesService.rateBands(ratePeriod, alcoholRegimes)

      result.fold(
        error => {
          logger.warn(s"Unable to retrieve rate bands. Error: $error")
          BadRequest(error)
        },
        rateBands => Ok(Json.toJson(rateBands))
      )
    }

  def rateBand(): Action[AnyContent] =
    authorise { implicit request =>
      val queryParams = request.queryString

      val result = for {
        ratePeriod <- extractParam[YearMonth]("ratePeriod", queryParams)
        taxType    <- extractQueryParam("taxTypeCode", queryParams)
        _          <- if (isValidTaxCode(taxType)) {
                        Right(())
                      } else {
                        Left("Invalid 'taxTypeCode' parameter")
                      }
        rateBand   <- Right(ratesService.taxType(ratePeriod, taxType))
      } yield rateBand

      result match {
        case Right(Some(rateBand)) => Ok(Json.toJson(rateBand))
        case Right(None)           => NotFound("RateBand not found")
        case Left(error)           =>
          logger.warn(s"Unable to retrieve rate band. Error: $error")
          BadRequest(error)
      }
    }

  def rateBands(): Action[AnyContent] =
    authorise { implicit request =>
      val queryParams = request.queryString

      val result = for {

        ratePeriods <- extractParamCSV[YearMonth]("ratePeriods", queryParams)
        taxTypes    <- extractQueryParamCSV(
                         "taxTypeCodes",
                         queryParams
                       )
        _           <- if (taxTypes.forall(isValidTaxCode(_))) {
                         Right(())
                       } else {
                         Left("Invalid 'taxTypeCodes' parameter")
                       }
        _           <- if (ratePeriods.length == taxTypes.length) {
                         Right(())
                       } else {
                         Left("Expected the number of ratePeriods and taxTypeCodes to match")
                       }
        rateBands   <- Right(ratePeriods.zip(taxTypes).map { case (ratePeriod, taxType) =>
                         ratesService.taxType(ratePeriod, taxType)
                       })
      } yield {
        val foundRateBands = rateBands.flatten
        if (foundRateBands.length != taxTypes.length) None else Some(foundRateBands)
      }

      result match {
        case Right(Some(rateBand)) => Ok(Json.toJson(rateBand))
        case Right(None)           => NotFound("Some rateBands not found")
        case Left(error)           =>
          logger.warn(s"Unable to retrieve rate bands. Error: $error")
          BadRequest(error)
      }
    }

  private def extractParam[T](paramName: String, queryParams: Map[String, Seq[String]])(implicit
    reads: Reads[T]
  ): Either[String, T] =
    extractQueryParam(paramName, queryParams).flatMap(value => parseParam[T](paramName, value))

  private def extractParamCSV[T](paramName: String, queryParams: Map[String, Seq[String]])(implicit
    reads: Reads[T]
  ): Either[String, Seq[T]] =
    extractQueryParamCSV(paramName, queryParams).flatMap(_.map(parseParam[T](paramName, _)).sequence.map(_.toSeq))

  private def parseParam[T](
    paramName: String,
    value: String
  )(implicit reads: Reads[T]): Either[String, T] =
    Try(Json.parse(value).as[T]).toEither.leftMap(_ => s"Invalid '$paramName' parameter")

  private def extractQueryParam(paramName: String, queryParams: Map[String, Seq[String]]): Either[String, String] =
    queryParams.get(paramName).flatMap(_.headOption).toRight(s"Missing or invalid '$paramName' parameter")

  private def extractQueryParamCSV(
    paramName: String,
    queryParams: Map[String, Seq[String]]
  ): Either[String, Seq[String]] =
    extractQueryParam(paramName, queryParams).map(value => value.split(',').toSeq)

  private def isValidTaxCode(taxCode: String): Boolean =
    taxCode.matches("\\d\\d\\d")
}
