package songs

import com.typesafe.config.ConfigFactory

object Config {
  val config = ConfigFactory.load()
  val inputDir = config.getString("songs.inputdir")
  val appName = config.getString("songs.appname")
  val master = config.getString("songs.master")
  val nWorkers = config.getInt("songs.nworkers")
  val modelOut = config.getString("songs.modelout")
}
