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

import org.apache.pekko.stream.Materializer
import org.apache.pekko.stream.testkit.NoMaterializer
import org.mockito.ArgumentMatchers.any
import play.api.libs.json.Json
import uk.gov.hmrc.alcoholdutycalculator.base.SpecBase
import uk.gov.hmrc.alcoholdutycalculator.models._
import uk.gov.hmrc.alcoholdutycalculator.services.DutySuspendedVolumesService

class DutySuspendedVolumesControllerSpec extends SpecBase {
  "DutySuspendedVolumesController when" - {
    "calculating duty suspended final volumes must" - {
      "return 200 OK with the DutySuspendedFinalVolumes" in new SetUp {
        when(mockDutySuspendedVolumesService.calculateDutySuspendedVolumes(any())).thenReturn(dutySuspendedFinalVolumes)

        val result =
          controller.calculateDutySuspendedVolumes()(fakeRequestWithJsonBody(Json.toJson(dutySuspendedQuantities)))

        status(result)                                      mustBe OK
        contentAsJson(result).as[DutySuspendedFinalVolumes] mustBe dutySuspendedFinalVolumes
      }

      "return 400 BAD REQUEST when the DutySuspendedQuantities in the request is not valid" in new SetUp {
        val result = controller.calculateDutySuspendedVolumes()(
          fakeRequestWithJsonBody(
            Json.parse("""{"totalLitresDeliveredInsideUK": 1.1, "pureAlcoholDeliveredInsideUK": 0.123}""")
          )
        )

        status(result) mustBe BAD_REQUEST
      }
    }
  }

  class SetUp {
    implicit lazy val materializer: Materializer = NoMaterializer

    val mockDutySuspendedVolumesService = mock[DutySuspendedVolumesService]

    val controller = new DutySuspendedVolumesController(
      fakeAuthorisedAction,
      mockDutySuspendedVolumesService,
      cc
    )
  }
}
