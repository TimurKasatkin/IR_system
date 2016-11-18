import sbt._
import Keys._

Keys.`package` := {
  (Keys.`package` in (crawler, Compile)).value
  (Keys.`package` in (core, Compile)).value
  (Keys.`package` in (web, Compile)).value
}

lazy val commonSettings = Seq(
  scalaVersion := "2.11.6",
  libraryDependencies ++= Seq(
    "org.slf4j" % "slf4j-log4j12" % "1.7.21",
    "org.scalatest" %% "scalatest" % "3.0.0" % "test"
  )
)

lazy val parent = project in file(".") aggregate(crawler, core, web) settings (commonSettings: _*) settings Seq(
  name := "IR-parent",
  version := "1.0"
)

lazy val crawler = project in file("./crawler") settings (commonSettings: _*) settings Seq(
  name := "IR-crawler",
  version := "1.0"
)

lazy val core = project in file("./core") dependsOn crawler settings (commonSettings: _*) settings Seq(
  name := "IR-core",
  version := "1.0"
) settings(libraryDependencies ++= Seq(
	"edu.stanford.nlp" % "stanford-corenlp" % "3.6.0",
	"edu.stanford.nlp" % "stanford-corenlp" % "3.6.0" classifier "models"
))

lazy val web = project in file("./web") settings (commonSettings: _*) settings Seq(
  name := "IR-web",
  version := "1.0"
) settings(libraryDependencies ++= Seq(
  "org.scalatra" % "scalatra_2.11" % "2.4.1" excludeAll ExclusionRule(organization = "org.slf4j"),
  //	"javax.servlet" % "javax.servlet-api" % "3.0.1" % "provided",
  "org.eclipse.jetty" % "jetty-webapp" % "9.4.0.M1",
  "org.scalatra" % "scalatra-scalate_2.11" % "2.4.1"
)) aggregate core dependsOn core enablePlugins JettyPlugin



