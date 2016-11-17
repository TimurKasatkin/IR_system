name := "IR_system"

version := "1.0"

scalaVersion := "2.11.6"

libraryDependencies ++= Seq(
	//Scalatra
	"org.scalatra" % "scalatra_2.10" % "2.4.1",
	"javax.servlet" % "javax.servlet-api" % "3.0.1" % "provided"

)

enablePlugins(JettyPlugin)


