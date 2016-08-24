organization := "org.fgb"
name := "dsl-nlp-tools"
version := "0.0.1"
scalaVersion := "2.11.8"

val catsVersion = "0.6.0"
val circeVersion = "0.5.0-M1"

libraryDependencies ++= Seq(
  "edu.stanford.nlp" % "stanford-corenlp" % "3.6.0" withSources(),
  "edu.stanford.nlp" % "stanford-corenlp" % "3.6.0" classifier "models" withSources(),
  "com.google.protobuf" % "protobuf-java" % "2.6.1"
)

libraryDependencies ++= Seq(
  "org.typelevel" %% "cats" % catsVersion,
  "com.chuusai" %% "shapeless" % "2.3.1",
  "ch.qos.logback" % "logback-classic" % "1.1.3",
  "org.slf4j" % "log4j-over-slf4j" % "1.7.12"
)

libraryDependencies ++= Seq(
  "io.circe" %% "circe-core",
  "io.circe" %% "circe-generic",
  "io.circe" %% "circe-parser"
).map(_ % circeVersion)
