package songs

import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SQLContext
import org.slf4j.LoggerFactory
import songs.Types._

object Main {

  def main(args: Array[String]): Unit = {

    val conf = new SparkConf().setAppName(Config.appName)
    val sc = new SparkContext(conf)
    val sqlContext = new SQLContext(sc)
    val logger = LoggerFactory.getLogger(getClass.getName)

    logger.info("Listing HDF5 input files")
    val files: Vector[String] = Files.getPaths(Config.inputDir)

    logger.info("Sending file list to cluster")
    val h5PathRDD = sc.parallelize(files, Config.nWorkers)

    logger.info("Reading song features from input files")
    val songsRDD: RDD[SongFeatures] = h5PathRDD.map(HDF5.open).flatMap(_.toOption)
      .map(ReadSong.readSongs)
      .flatMap(_.toOption)
      .map(SongML.extractFeatures)

    logger.info("Creating DataFrame")
    val songsDataFrame = sqlContext.createDataFrame(songsRDD).toDF(SongML.allColumns:_*)

    logger.info("Splitting data into training, test, and evaluation datasets")
    val modelData = SongML.splitDataFrame(songsDataFrame)

    logger.info("Persist to parquet format")
    modelData.saveAsParquet()

    logger.info("Exiting")
    sc.stop()
  }
}
