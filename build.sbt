ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.8"

lazy val root = (project in file("."))
  .settings(
    name := "nolol",
    idePackagePrefix := Some("pw.byakuren.nolol")
  )

resolvers += "jcenter-bintray" at "https://jcenter.bintray.com"

libraryDependencies ++= Seq("net.dv8tion" % "JDA" % "5.0.0-alpha.10",
  "org.scalikejdbc" %% "scalikejdbc" % "3.5.0",
  "com.h2database"  %  "h2" % "2.1.210",
  "ch.qos.logback"  %  "logback-classic" % "1.2.11",
  "org.xerial" % "sqlite-jdbc" % "3.7.2")
