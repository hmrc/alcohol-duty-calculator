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

import play.api.Logging
import play.api.libs.json.{JsError, JsSuccess, JsValue, Json}
import play.api.mvc.{Action, ControllerComponents}
import uk.gov.hmrc.alcoholdutycalculator.controllers.actions.AuthorisedAction
import uk.gov.hmrc.alcoholdutycalculator.models.{DutyCalculationRequest, DutyTotalCalculationRequest}
import uk.gov.hmrc.alcoholdutycalculator.models.{AdjustmentDutyCalculationRequest, AdjustmentTotalCalculationRequest}
import uk.gov.hmrc.alcoholdutycalculator.services.DutyService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.Inject
import uk.gov.hmrc.play.bootstrap.controller.WithJsonBody

import scala.concurrent.Future

class DutyCalculationController @Inject() (
  authorise: AuthorisedAction,
  dutyService: DutyService,
  override val controllerComponents: ControllerComponents
) extends BackendController(controllerComponents)
    with WithJsonBody
    with Logging {

  def calculateDuty(): Action[JsValue]           = authorise.async(parse.json) { implicit request =>
    request.body.validate[DutyCalculationRequest] match {
      case JsSuccess(value, _) => Future.successful(Ok(Json.toJson(dutyService.calculateDuty(value))))
      case JsError(e)          =>
        logger.error("Invalid JSON: " + e)
        Future.successful(BadRequest("Invalid JSON"))
    }
  }
  def calculateAdjustmentDuty(): Action[JsValue] = authorise.async(parse.json) { implicit request =>
    request.body.validate[AdjustmentDutyCalculationRequest] match {
      case JsSuccess(value, _) => Future.successful(Ok(Json.toJson(dutyService.calculateAdjustmentDuty(value))))
      case JsError(e)          =>
        logger.error("Invalid JSON: " + e)
        Future.successful(BadRequest("Invalid JSON"))
    }
  }

  def calculateAdjustmentTotal(): Action[JsValue] = authorise.async(parse.json) { implicit request =>
    request.body.validate[AdjustmentTotalCalculationRequest] match {
      case JsSuccess(value, _) => Future.successful(Ok(Json.toJson(dutyService.calculateAdjustmentTotal(value))))
      case JsError(e)          =>
        logger.error("Invalid JSON: " + e)
        Future.successful(BadRequest("Invalid JSON"))
    }
  }

  def calculateTotalDuty(): Action[JsValue] = authorise.async(parse.json) { implicit request =>
    request.body.validate[DutyTotalCalculationRequest] match {
      case JsSuccess(value, _) =>
        Future.successful(Ok(Json.toJson(dutyService.calculateTotalDuty(value.dutiesByTaxType))))
      case JsError(e)          =>
        logger.error("Invalid JSON: " + e)
        Future.successful(BadRequest("Invalid JSON"))
    }
  }
}
