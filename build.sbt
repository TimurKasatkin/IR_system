name := "IR_system"

version := "1.0"

scalaVersion := "2.12.0"

resolvers += "OSS Sonatype" at "https://repo1.maven.org/maven2/"

libraryDependencies ++= Seq(
	//Testing
	"org.scalatest" %% "scalatest" % "3.0.0" % "test",

	"edu.stanford.nlp" % "stanford-parser" % "3.6.0",
	"edu.stanford.nlp" % "stanford-corenlp" % "3.6.0",

	"org.scalanlp" % "chalk" % "1.3.0",

	//Breeze
	"org.scalanlp" % "breeze_2.10" % "0.12",
	"org.scalanlp" % "breeze-natives_2.10" % "0.12"
//	"org.scalanlp" % "breeze-viz_2.10" % "0.12"
)

