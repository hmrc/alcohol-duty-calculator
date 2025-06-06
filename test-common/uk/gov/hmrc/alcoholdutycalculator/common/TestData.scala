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

package uk.gov.hmrc.alcoholdutycalculator.common

import org.scalacheck.Gen.Choose
import org.scalacheck.{Arbitrary, Gen}
import uk.gov.hmrc.alcoholdutycalculator.models.AdjustmentType.{Drawback, Overdeclaration, RepackagedDraughtProducts, Spoilt, Underdeclaration}
import uk.gov.hmrc.alcoholdutycalculator.models._

import java.time.YearMonth

trait TestData {

  def alphaNumericString: String = Gen.alphaNumStr.retryUntil(_.nonEmpty).sample.get
  val testInternalId: String     = alphaNumericString

  implicit val arbitraryYearMonth: Arbitrary[YearMonth] = Arbitrary {
    for {
      year  <- Gen.choose(1900, 2200)
      month <- Gen.choose(1, 12)
    } yield YearMonth.of(year, month)
  }

  implicit val arbitraryRateType: Arbitrary[RateType] = Arbitrary {
    Gen.oneOf(
      RateType.Core,
      RateType.DraughtRelief,
      RateType.SmallProducerRelief,
      RateType.DraughtAndSmallProducerRelief
    )
  }

  implicit val arbitraryAlcoholRegimeName: Arbitrary[AlcoholRegime] = Arbitrary {
    Gen.oneOf(
      AlcoholRegime.Beer,
      AlcoholRegime.Cider,
      AlcoholRegime.Wine,
      AlcoholRegime.Spirits
    )
  }

  val genAlcoholByVolumeValue: Gen[BigDecimal] =
    for {
      value <- Gen.choose(0.001, 100.00)
      scale <- Gen.oneOf(0, 1)
    } yield BigDecimal(value).setScale(scale, BigDecimal.RoundingMode.UP)

  val genAlcoholByVolumeValueTooBigScale: Gen[BigDecimal] =
    for {
      value <- Gen.choose(0.001, 100.00)
      scale <- Gen.choose(2, 10)
    } yield BigDecimal(value).setScale(scale, BigDecimal.RoundingMode.UP)

  val genAlcoholByVolumeValueNegative: Gen[BigDecimal] =
    Gen
      .chooseNum(Double.MinValue, -0.1)
      .map(d => BigDecimal(d).setScale(1, BigDecimal.RoundingMode.HALF_UP))

  val genAlcoholByVolumeValueMoreThan100: Gen[BigDecimal] =
    Gen
      .chooseNum(100.01, Double.MaxValue)
      .map(d => BigDecimal(d).setScale(1, BigDecimal.RoundingMode.UP))

  val genAlcoholByVolumeValueOutOfRange: Gen[BigDecimal] =
    Gen.oneOf(genAlcoholByVolumeValueNegative, genAlcoholByVolumeValueMoreThan100)

  implicit val arbitraryAlcoholByVolume: Arbitrary[AlcoholByVolume] = Arbitrary {
    genAlcoholByVolumeValue.map(AlcoholByVolume.apply)
  }

  implicit val chooseBigDecimal: Choose[BigDecimal] =
    Choose.xmap[Double, BigDecimal](d => BigDecimal(d), bd => bd.toDouble)(implicitly[Choose[Double]])

  implicit val arbitraryABVRangeName: Arbitrary[AlcoholType] = Arbitrary {
    Gen.oneOf(
      AlcoholType.Beer,
      AlcoholType.Cider,
      AlcoholType.SparklingCider,
      AlcoholType.Wine,
      AlcoholType.Spirits,
      AlcoholType.OtherFermentedProduct
    )
  }

  implicit val arbitraryRateBand: Arbitrary[RateBand] = Arbitrary {
    for {
      taxType       <- Gen.alphaStr
      description   <- Gen.alphaStr
      rateType      <- arbitraryRateType.arbitrary
      name          <- arbitraryAlcoholRegimeName.arbitrary
      alcoholRegime <- Gen
                         .nonEmptyListOf(
                           for {
                             name   <- arbitraryABVRangeName.arbitrary
                             minABV <- arbitraryAlcoholByVolume.arbitrary
                             maxABV <- arbitraryAlcoholByVolume.arbitrary
                           } yield ABVRange(name, minABV, maxABV)
                         )
                         .map(ranges => RangeDetailsByRegime(name, ranges))
      rate          <- Gen.option(Gen.chooseNum(-99999.99, 99999.99).map(BigDecimal(_)))
    } yield RateBand(taxType, description, rateType, Set(alcoholRegime), rate)
  }

  implicit val arbitraryListRateBand: Arbitrary[Seq[RateBand]] = Arbitrary {
    Gen.nonEmptyListOf(arbitraryRateBand.arbitrary)
  }

  implicit val arbitraryRatePeriod: Arbitrary[RatePeriod] = Arbitrary {
    for {
      name              <- Gen.alphaStr
      validityStartDate <- Arbitrary.arbitrary[YearMonth]
      validityEndDate   <- Gen.option(Arbitrary.arbitrary[YearMonth])
      rateBands         <- Gen.nonEmptyListOf(arbitraryRateBand.arbitrary)
    } yield RatePeriod(name, validityStartDate, validityEndDate, rateBands)
  }

  implicit val arbitraryListRatePeriod: Arbitrary[Seq[RatePeriod]]            = Arbitrary {
    Gen.nonEmptyListOf(arbitraryRatePeriod.arbitrary)
  }
  implicit val arbitraryNegativeDutyAdjustmentType: Arbitrary[AdjustmentType] = Arbitrary {
    Gen.oneOf(Spoilt, Overdeclaration, Drawback)
  }
  implicit val arbitraryPositiveDutyAdjustmentType: Arbitrary[AdjustmentType] = Arbitrary {
    Gen.oneOf(RepackagedDraughtProducts, Underdeclaration)
  }

  val adjustmentDutyCalculationRequest: AdjustmentDutyCalculationRequest   = AdjustmentDutyCalculationRequest(
    Spoilt,
    pureAlcoholVolume = BigDecimal(1.0),
    rate = BigDecimal(1.0)
  )
  val duty: AdjustmentDuty                                                 = AdjustmentDuty(BigDecimal(1.00))
  val repackagedDutyChangeRequest: RepackagedDutyChangeRequest             =
    RepackagedDutyChangeRequest(BigDecimal(5), BigDecimal(4))
  val adjustmentTotalCalculationRequest: AdjustmentTotalCalculationRequest =
    AdjustmentTotalCalculationRequest(Seq(BigDecimal(10), BigDecimal(9), BigDecimal(-18)))
  val totalCalculationRequest: DutyTotalCalculationRequest                 = DutyTotalCalculationRequest(
    Seq(
      DutyByTaxType(
        taxType = "taxType",
        totalLitres = BigDecimal(1.0),
        pureAlcohol = BigDecimal(1.0),
        dutyRate = BigDecimal(1.0)
      ),
      DutyByTaxType(
        taxType = "taxType2",
        totalLitres = BigDecimal(2.0),
        pureAlcohol = BigDecimal(2.0),
        dutyRate = BigDecimal(2.0)
      )
    )
  )

  val totalCalculationResponse: DutyTotalCalculationResponse = DutyTotalCalculationResponse(
    totalDuty = BigDecimal(3.0),
    dutiesByTaxType = Seq(
      DutyCalculationByTaxTypeResponse(
        taxType = "taxType",
        totalLitres = BigDecimal(1.0),
        pureAlcohol = BigDecimal(1.0),
        dutyRate = BigDecimal(1.0),
        dutyDue = BigDecimal(1.0)
      ),
      DutyCalculationByTaxTypeResponse(
        taxType = "taxType2",
        totalLitres = BigDecimal(2.0),
        pureAlcohol = BigDecimal(2.0),
        dutyRate = BigDecimal(2.0),
        dutyDue = BigDecimal(2.0)
      )
    )
  )

  val calculateDutyDueByTaxTypeRequest: CalculateDutyDueByTaxTypeRequest =
    CalculateDutyDueByTaxTypeRequest(
      declarationOrAdjustmentItems = Seq(
        CalculateDutyDueByTaxTypeRequestItem(
          taxType = "331",
          dutyDue = BigDecimal("115.11")
        ),
        CalculateDutyDueByTaxTypeRequestItem(
          taxType = "332",
          dutyDue = BigDecimal("321.88")
        ),
        CalculateDutyDueByTaxTypeRequestItem(
          taxType = "332",
          dutyDue = BigDecimal("245.79")
        ),
        CalculateDutyDueByTaxTypeRequestItem(
          taxType = "331",
          dutyDue = BigDecimal("8.12")
        )
      )
    )

  val calculatedDutyDueByTaxType: CalculatedDutyDueByTaxType =
    CalculatedDutyDueByTaxType(
      totalDutyDueByTaxType = Seq(
        CalculatedDutyDueByTaxTypeItem(
          taxType = "332",
          totalDutyDue = BigDecimal("567.67")
        ),
        CalculatedDutyDueByTaxTypeItem(
          taxType = "331",
          totalDutyDue = BigDecimal("123.23")
        )
      )
    )

  val dutySuspendedQuantities = DutySuspendedQuantities(
    totalLitresDeliveredInsideUK = BigDecimal(3),
    pureAlcoholDeliveredInsideUK = BigDecimal(1.0000),
    totalLitresDeliveredOutsideUK = BigDecimal(7.26),
    pureAlcoholDeliveredOutsideUK = BigDecimal(2.8),
    totalLitresReceived = BigDecimal(10.1),
    pureAlcoholReceived = BigDecimal(3.9999)
  )

  val dutySuspendedFinalVolumes = DutySuspendedFinalVolumes(
    totalLitresDelivered = BigDecimal(10.26),
    totalLitres = BigDecimal(0.16),
    pureAlcoholDelivered = BigDecimal(3.8),
    pureAlcohol = BigDecimal(-0.1999)
  )
}
