import sbt.*

object AppDependencies {

  private val bootstrapVersion    = "10.1.0"
  private val mockitoScalaVersion = "1.17.37"

  val compile = Seq(
    "org.typelevel" %% "cats-core"                 % "2.13.0",
    "uk.gov.hmrc"   %% "bootstrap-backend-play-30" % bootstrapVersion,
    "com.beachape"  %% "enumeratum"                % "1.9.0",
    "com.beachape"  %% "enumeratum-play"           % "1.9.0"
  )

  val test = Seq(
    "uk.gov.hmrc"                %% "bootstrap-test-play-30"   % bootstrapVersion    % Test,
    "org.mockito"                %% "mockito-scala"            % mockitoScalaVersion % Test,
    "org.mockito"                %% "mockito-scala-scalatest"  % mockitoScalaVersion % Test,
    "org.scalatestplus"          %% "scalatestplus-scalacheck" % "3.1.0.0-RC2"       % Test,
    "com.github.java-json-tools" %  "json-schema-validator"    % "2.2.14"
  )

  val itDependencies = Seq.empty
}
