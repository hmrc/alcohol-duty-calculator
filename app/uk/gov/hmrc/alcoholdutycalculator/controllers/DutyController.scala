/*
 * Copyright 2024 HM Revenue & Customs
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

import play.api.libs.json.{Format, Json, Reads, Writes}
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.alcoholdutycalculator.controllers.actions.AuthorisedAction
import uk.gov.hmrc.alcoholdutycalculator.models.TaxDuty
import uk.gov.hmrc.alcoholdutycalculator.services.DutyService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.Inject

class DutyController @Inject() (
  authorise: AuthorisedAction,
  dutyService: DutyService,
  override val controllerComponents: ControllerComponents
) extends BackendController(controllerComponents)
    with BaseCalculatorController {
  def calculateDuty(): Action[AnyContent] =
    authorise { implicit request =>
      val queryParams = request.queryString

      val result: Either[String, TaxDuty] = for {
        abv    <- extractParam[BigDecimal]("abv", queryParams, Format(Reads.bigDecReads, Writes.BigDecimalWrites))
        volume <- extractParam[BigDecimal]("volume", queryParams, Format(Reads.bigDecReads, Writes.BigDecimalWrites))
        rate   <- extractParam[BigDecimal]("rate", queryParams, Format(Reads.bigDecReads, Writes.BigDecimalWrites))
      } yield dutyService.calculateDuty(abv, volume, rate)

      result.fold(
        error => BadRequest(error),
        rateBands => Ok(Json.toJson(rateBands))
      )
    }
}
