import sbt.*

object AppDependencies {

  private val bootstrapVersion = "10.8.0"

  val compile: Seq[ModuleID] = Seq(
    "org.typelevel" %% "cats-core"                 % "2.13.0",
    "uk.gov.hmrc"   %% "bootstrap-backend-play-30" % bootstrapVersion,
    "com.beachape"  %% "enumeratum"                % "1.9.8",
    "com.beachape"  %% "enumeratum-play"           % "1.9.8"
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"       %% "bootstrap-test-play-30" % bootstrapVersion % Test,
    "org.scalatestplus" %% "scalacheck-1-17"        % "3.2.18.0"       % Test,
    ("com.networknt"     % "json-schema-validator"  % "2.0.4") exclude ("com.fasterxml.jackson.core", "jackson-databind"),
    "org.mozilla"        % "rhino"                  % "1.9.1"
  )

  val itDependencies: Seq[Nothing] = Seq.empty
}
