import sbt.*

object AppDependencies {

  private val bootstrapVersion    = "10.5.0"

  val compile = Seq(
    "org.typelevel" %% "cats-core"                 % "2.13.0",
    "uk.gov.hmrc"   %% "bootstrap-backend-play-30" % bootstrapVersion,
    "com.beachape"  %% "enumeratum"                % "1.9.0",
    "com.beachape"  %% "enumeratum-play"           % "1.9.0"
  )

  val test = Seq(
    "uk.gov.hmrc"                %% "bootstrap-test-play-30" % bootstrapVersion % Test,
    "org.scalatestplus"          %% "scalacheck-1-17"        % "3.2.18.0"       % Test,
    "com.github.java-json-tools" %  "json-schema-validator"  % "2.2.14"
  )

  val itDependencies = Seq.empty
}
