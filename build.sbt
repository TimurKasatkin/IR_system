import sbt.Keys._
import sbt._

Keys.`package` := {
  (Keys.`package` in(crawler, Compile)).value
  (Keys.`package` in(core, Compile)).value
  (Keys.`package` in(web, Compile)).value
}

lazy val commonSettings = Seq(
  scalaVersion := "2.11.8",
  libraryDependencies ++= Seq(
    "org.scalatest" %% "scalatest" % "3.0.0" % "test",

    //Logging
    "ch.qos.logback" % "logback-classic" % "1.1.7",
    "com.typesafe.scala-logging" %% "scala-logging" % "3.5.0"
  ),
  dependencyOverrides := Set(
    "org.scala-lang" % "scala-library" % Keys.scalaVersion.value,
    "org.scala-lang" % "scala-reflect" % Keys.scalaVersion.value,
    "org.scala-lang" % "scala-compiler" % Keys.scalaVersion.value
  )
)

lazy val parent = project in file(".") aggregate(crawler, core, web) settings (commonSettings: _*) settings Seq(
  name := "IR-parent",
  version := "1.0"
)

lazy val crawler = project in file("./crawler") settings (commonSettings: _*) settings Seq(
  name := "IR-crawler",
  version := "1.0"
) settings (libraryDependencies ++= Seq(
  "net.ruippeixotog" %% "scala-scraper" % "1.1.0",
  "com.github.scopt" %% "scopt" % "3.5.0"
))

lazy val core = project in file("./core") dependsOn crawler settings (commonSettings: _*) settings Seq(
  name := "IR-core",
  version := "1.0"
) settings(
  mainClass in Compile := Some("ru.innopolis.ir.project.core.rest.IRSystemCoreRestService"),
  libraryDependencies ++= {
    val stanfordCoreNLPVersion = "3.6.0"
    val scalatraVersion = "2.4.1"
    val jettyVersion = "9.3.14.v20161028"
    Seq(
      //Documents preprocessing
      "edu.stanford.nlp" % "stanford-corenlp" % stanfordCoreNLPVersion,
      "edu.stanford.nlp" % "stanford-corenlp" % stanfordCoreNLPVersion classifier "models",

      //Comand line options
      "com.github.scopt" %% "scopt" % "3.5.0",

      //REST service
      "org.scalatra" % "scalatra_2.11" % scalatraVersion excludeAll ExclusionRule(organization = "org.slf4j"),
      "org.scalatra" % "scalatra-json_2.11" % scalatraVersion,
      "org.json4s" % "json4s-jackson_2.11" % "3.5.0",
      "org.eclipse.jetty" % "jetty-server" % jettyVersion,
      "org.eclipse.jetty" % "jetty-webapp" % jettyVersion
    )
  })

lazy val web = project in file("./web") settings (commonSettings: _*) settings Seq(
  name := "IR-web",
  version := "1.0"
) settings (libraryDependencies ++= Seq(
  "org.scalatra" % "scalatra_2.11" % "2.4.1" excludeAll ExclusionRule(organization = "org.slf4j"),
  "org.eclipse.jetty" % "jetty-webapp" % "9.4.0.M1",
  "org.scalatra" % "scalatra-scalate_2.11" % "2.4.1",
  "org.json4s" % "json4s-jackson_2.11" % "3.5.0"
)) enablePlugins JettyPlugin



