/*
 * Copyright 2025 HM Revenue & Customs
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
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, ControllerComponents}
import uk.gov.hmrc.alcoholdutycalculator.controllers.actions.AuthorisedAction
import uk.gov.hmrc.alcoholdutycalculator.models._
import uk.gov.hmrc.alcoholdutycalculator.services.DutySuspendedVolumesService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import uk.gov.hmrc.play.bootstrap.controller.WithJsonBody

import javax.inject.Inject
import scala.concurrent.Future

class DutySuspendedVolumesController @Inject() (
  authorise: AuthorisedAction,
  dutySuspendedVolumesService: DutySuspendedVolumesService,
  override val controllerComponents: ControllerComponents
) extends BackendController(controllerComponents)
    with WithJsonBody
    with Logging {

  def calculateDutySuspendedVolumes(): Action[JsValue] = authorise.async(parse.json) { implicit request =>
    withJsonBody[DutySuspendedQuantities] { quantities =>
      Future.successful(Ok(Json.toJson(dutySuspendedVolumesService.calculateDutySuspendedVolumes(quantities))))
    }
  }
}
