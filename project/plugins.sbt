addSbtPlugin("org.scala-js"     % "sbt-scalajs"      % "1.16.0")
addSbtPlugin("org.scala-native" % "sbt-scala-native" % "0.5.5")
addSbtPlugin("org.scoverage"    % "sbt-scoverage"    % "2.1.0")
addSbtPlugin("org.typelevel"    % "sbt-typelevel"    % "0.7.2-27-e384990-SNAPSHOT")

resolvers ++= Resolver.sonatypeOssRepos("snapshots")
