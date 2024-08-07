import sbt.*

object AppDependencies {

  private val bootstrapVersion = "8.5.0"

  val compile = Seq(
    "uk.gov.hmrc" %% "bootstrap-backend-play-30" % bootstrapVersion,
    "com.beachape" %% "enumeratum" % "1.7.3",
    "com.beachape" %% "enumeratum-play" % "1.8.0"
  )

  val test = Seq(
    "uk.gov.hmrc"       %% "bootstrap-test-play-30"   % bootstrapVersion % Test,
    "org.mockito"       %% "mockito-scala"            % "1.17.30"        % Test,
    "org.scalatestplus" %% "scalatestplus-scalacheck" % "3.1.0.0-RC2"    % Test,
    "org.mockito"       %% "mockito-scala-scalatest"  % "1.17.30"        % Test
  )

  val itDependencies = Seq.empty
}
