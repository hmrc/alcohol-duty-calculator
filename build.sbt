import uk.gov.hmrc.DefaultBuildSettings.integrationTestSettings

lazy val microservice = Project("alcohol-duty-calculator", file("."))
  .enablePlugins(play.sbt.PlayScala, SbtDistributablesPlugin)
  .settings(
    majorVersion := 0,
    scalaVersion := "2.13.8",
    libraryDependencies ++= AppDependencies.compile ++ AppDependencies.test,
    // https://www.scala-lang.org/2021/01/12/configuring-and-suppressing-warnings.html
    // suppress warnings in generated routes files
    scalacOptions += "-Wconf:src=routes/.*:s",
    scalafmtOnCompile := true
  )
  .settings(inConfig(Test)(testSettings): _*)
  .configs(IntegrationTest)
  .settings(inConfig(IntegrationTest)(itSettings): _*)
  .settings(headerSettings(IntegrationTest): _*)
  .settings(automateHeaderSettings(IntegrationTest))
  .settings(resolvers += Resolver.jcenterRepo)
  .settings(CodeCoverageSettings.settings: _*)
  .settings(PlayKeys.playDefaultPort := 16003)

lazy val testSettings: Seq[Def.Setting[_]] = Seq(
  unmanagedSourceDirectories := Seq(
    baseDirectory.value / "test",
    baseDirectory.value / "test-common"
  ),
  fork := true,
  scalafmtOnCompile := true
)

lazy val itSettings: Seq[Def.Setting[_]] = Defaults.itSettings ++ Seq(
  unmanagedSourceDirectories := Seq(
    baseDirectory.value / "it",
    baseDirectory.value / "test-common"
  ),
  parallelExecution := false,
  fork := true,
  scalafmtOnCompile := true
)

addCommandAlias("runAllChecks", ";clean;compile;scalafmtCheckAll;coverage;test;it:test;scalastyle;coverageReport")
