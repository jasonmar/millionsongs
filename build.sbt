name := "songs"

version := "1.0"

scalaVersion := "2.10.5"

libraryDependencies += "org.apache.spark" % "spark-core_2.10" % "1.6.1"

libraryDependencies += "org.apache.spark" % "spark-mllib_2.10" % "1.6.1"

libraryDependencies += "org.apache.spark" % "spark-hive_2.10" % "1.6.1"

libraryDependencies += "org.apache.spark" % "spark-repl_2.10" % "1.6.1"

libraryDependencies += "org.scala-saddle" % "saddle-hdf5_2.10" % "1.3.4"

//libraryDependencies += "org.scalatest" % "scalatest_2.10" % "2.2.4" % "test"

mainClass in (Compile, run) := Some("songs.Main")
