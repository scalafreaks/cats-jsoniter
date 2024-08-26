ThisBuild / tlBaseVersion := "0.1"

ThisBuild / organization     := "dev.scalafreaks"
ThisBuild / organizationName := "ScalaFreaks"
ThisBuild / startYear        := Some(2024)
ThisBuild / licenses         := Seq(License.Apache2)
ThisBuild / developers       := List(tlGitHubDev("aartigao", "Alan Artigao"))

ThisBuild / tlFatalWarnings := true
ThisBuild / tlJdkRelease    := Some(11)

val Scala3 = "3.3.3"
ThisBuild / scalaVersion       := Scala3
ThisBuild / crossScalaVersions := Seq(Scala3)

ThisBuild / githubWorkflowJavaVersions := Seq(JavaSpec.temurin("11"))

ThisBuild / githubWorkflowAddedJobs += {
  val jobSetup = (ThisBuild / githubWorkflowJobSetup).value.toList
  val coverageAggregate =
    WorkflowStep.Sbt(
      List("coverage", "test", "coverageAggregate"),
      name = Some("Coverage Aggregate")
    )
  val codecovPublish =
    WorkflowStep.Use(
      name = Some("Publish Aggregated Coverage"),
      ref = UseRef.Public("codecov", "codecov-action", "v4"),
      params = Map(
        "token"            -> "${{ secrets.CODECOV_TOKEN }}",
        "file"             -> s"./target/scala-$Scala3/scoverage-report/scoverage.xml",
        "flags"            -> "unittests",
        "codecov_yml_path" -> "./.codecov.yml"
      )
    )
  WorkflowJob(
    "codecov",
    "Codecov Publish",
    jobSetup :+ coverageAggregate :+ codecovPublish,
    cond = Some("github.event_name != 'push'"),
    scalas = List(Scala3)
  )
}

ThisBuild / githubWorkflowTargetBranches :=
  (ThisBuild / githubWorkflowTargetBranches).value.filterNot(_.contains("update/"))

lazy val versions = new {

  val cats = "2.12.0"

  val jsoniter = "2.30.8"

  val scalaTest = "3.2.19"

}

lazy val `cats-jsoniter-root` = tlCrossRootProject.aggregate(`cats-jsoniter`.jvm)

lazy val `cats-jsoniter` = crossProject(JVMPlatform)
  .in(file("."))
  .settings(
    name           := "cats-jsoniter",
    description    := "Jsoniter typeclasses for Cats datatypes",
    scalacOptions ++= Seq("-no-indent"),
    libraryDependencies ++= Seq(
      "com.github.plokhotnyuk.jsoniter-scala" %%% "jsoniter-scala-core"      % versions.jsoniter,
      "com.github.plokhotnyuk.jsoniter-scala" %%% "jsoniter-scala-macros"    % versions.jsoniter,
      "org.typelevel"                         %%% "cats-core"                % versions.cats,
      "org.scalatest"                         %%% "scalatest-funsuite"       % versions.scalaTest % Test,
      "org.scalatest"                         %%% "scalatest-shouldmatchers" % versions.scalaTest % Test
    )
  )
