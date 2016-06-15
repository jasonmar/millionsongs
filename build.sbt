name := "songs"

version := "1.0"

scalaVersion := "2.10.5"

libraryDependencies += "org.apache.spark" % "spark-core_2.10" % "1.6.1"

libraryDependencies += "org.apache.spark" % "spark-mllib_2.10" % "1.6.1"

libraryDependencies += "org.apache.spark" % "spark-hive_2.10" % "1.6.1"

libraryDependencies += "org.apache.spark" % "spark-repl_2.10" % "1.6.1"

libraryDependencies += "ch.ethz" % "sis-jhdf5-core" % "1.0.0"

libraryDependencies += "ch.ethz" % "sis-base" % "1.0.0"

//libraryDependencies += "org.scalatest" % "scalatest_2.10" % "2.2.4" % "test"

mainClass in (Compile, run) := Some("songs.Main")

mainClass in assembly := Some("songs.Main")

assemblyJarName in assembly := "songs.jar"

test in assembly := {}



assemblyMergeStrategy in assembly := {
  case PathList("javax", "servlet", xs @ _*)         => MergeStrategy.first
  case PathList(ps @ _*) if ps.last endsWith ".html" => MergeStrategy.first
  case PathList(ps @ _*) if ps.last endsWith ".class" => MergeStrategy.last

  case PathList(ps @ _*) if ps.last endsWith ".so" => MergeStrategy.last
  case PathList(ps @ _*) if ps.last endsWith ".dll" => MergeStrategy.last
  case PathList(ps @ _*) if ps.last endsWith ".jnilib" => MergeStrategy.last
  case PathList(ps @ _*) if ps.last endsWith ".xml" => MergeStrategy.last
  case PathList(ps @ _*) if ps.last endsWith ".thrift" => MergeStrategy.last


  case "application.conf"                            => MergeStrategy.concat
  case "unwanted.txt"                                => MergeStrategy.discard
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}

