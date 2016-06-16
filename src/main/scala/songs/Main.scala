package songs

import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.mllib.regression.LinearRegressionModel
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SQLContext
import songs.Types._

object Main {

  def main(args: Array[String]): Unit = {

    val conf = new SparkConf().setAppName(Config.appName)
    val sc = new SparkContext(conf)
    val sqlContext = new SQLContext(sc)

    // a list of paths to HDF5 files
    val files: Vector[String] = Files.getPaths(Config.inputDir)

    // send list of files to the cluster
    val h5PathRDD = sc.parallelize(files, Config.nWorkers)

    // read song features from the files
    val songsRDD: RDD[SongFeatures] = h5PathRDD.map(HDF5.open).flatMap(_.toOption)
      .map(ReadSong.readSongs)
      .flatMap(_.toOption)
      .map(SongML.extractFeatures)

    val songsDataFrame = sqlContext.createDataFrame(songsRDD).toDF(SongML.allColumns:_*)

    // split into training and test
    val modelData = SongML.splitDataFrame(songsDataFrame)

    // persist to parquet format
    modelData.persist()

    val loaded = SongML.loadModelData(sqlContext = sqlContext)

    // show summary stats for our training data
    val summary = loaded.training.describe(SongML.allColumns:_*)
    summary.show(1000)

    // Train the model
    val startTime = System.nanoTime()
    val lirModel = SongML.pipeline.fit(loaded.training)
    val elapsedTime = (System.nanoTime() - startTime) / 1e9
    println(s"Training time: $elapsedTime seconds")

    // Save the trained model
    lirModel.write.overwrite().save(Config.modelOut)
    val savedModel = LinearRegressionModel.load(sc,Config.modelOut)

    // Print the weights and intercept for linear regression.
    val colWeights = SongML.featureColumns.zip(savedModel.weights.toArray)
    println(s"Weights: $colWeights")
    println(s"Intercept: ${savedModel.intercept}")

    // print training results
    val trainingResults = lirModel.transform(loaded.test)
    val trainingMSE = trainingResults.select(SongML.labelColumn,SongML.predictionColumn).map(r => math.pow(r.getAs[Double](SongML.labelColumn) - r.getAs[Double](SongML.predictionColumn),2)).mean()
    println("Training data results:")
    println(s"MSE: $trainingMSE")
    trainingResults.show(10)

    // print test results
    val testResults = lirModel.transform(loaded.test)
    val testMSE = testResults.select(SongML.labelColumn,SongML.predictionColumn).map(r => math.pow(r.getAs[Double](SongML.labelColumn) - r.getAs[Double](SongML.predictionColumn),2)).mean()
    println("Test data results:")
    println(s"MSE: $testMSE")
    testResults.show(10)

    sc.stop()

  }
}
