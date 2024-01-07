import sbt.*

object AppDependencies {

  private val bootstrapVersion = "7.22.0"

  val compile = Seq(
    "uk.gov.hmrc" %% "bootstrap-backend-play-28" % bootstrapVersion
  )

  val test = Seq(
    "uk.gov.hmrc"       %% "bootstrap-test-play-28"   % bootstrapVersion % "test, it",
    "org.mockito"       %% "mockito-scala"            % "1.17.27"        % "test, it",
    "org.scalatestplus" %% "scalatestplus-scalacheck" % "3.1.0.0-RC2"    % "test, it",
    "org.mockito"       %% "mockito-scala-scalatest"  % "1.17.14"        % "test, it"
  )
}
