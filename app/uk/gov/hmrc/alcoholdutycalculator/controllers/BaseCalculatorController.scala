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

import play.api.libs.json.{Format, Json}

import scala.util.Try

trait BaseCalculatorController {
  def extractParam[T](
    paramName: String,
    queryParams: Map[String, Seq[String]],
    jsonFormat: Format[T]
  ): Either[String, T] =
    extractQueryParam(paramName, queryParams).flatMap(value =>
      Try(Json.parse(value).as[T](jsonFormat)).toEither.left.map(_ => s"Invalid '$paramName' parameter")
    )

  def extractQueryParam(paramName: String, queryParams: Map[String, Seq[String]]): Either[String, String] =
    queryParams.get(paramName).flatMap(_.headOption).toRight(s"Missing or invalid '$paramName' parameter")

}
