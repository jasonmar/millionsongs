package songs

import org.apache.spark.ml.Pipeline
import org.apache.spark.ml.feature.{OneHotEncoder, VectorAssembler}
import org.apache.spark.ml.regression.LinearRegression
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.mllib.regression.LinearRegressionModel
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SQLContext
import songs.Types._

object Main {

  def main(args: Array[String]): Unit = {

    // a list of paths to HDF5 files
    val files: Vector[String] = Files.getPaths(Config.inputDir)

    val conf = new SparkConf().setAppName(Config.appName)
    val sc = new SparkContext(conf)
    val sqlContext = new SQLContext(sc)

    // send list of files to the cluster
    val h5PathRDD = sc.parallelize(files, Config.nWorkers)

    // read song features from the files
    val songsRDD: RDD[SongFeatures] = h5PathRDD.map(HDF5.open).flatMap(_.toOption)
      .map(ReadSong.readSongs)
      .flatMap(_.toOption)
      .map(SongML.extractFeatures)

    // convert RDD to DataFrame
    val songsDataFrame = sqlContext.createDataFrame(songsRDD).toDF(
      "artist_hotttnesss"
      ,"song_hotttnesss"
      ,"duration"
      ,"loudness"
      ,"end_of_fade_in"
      ,"start_of_fade_out"
      ,"tempo"
      ,"danceability"
      ,"energy"
      ,"key"
      ,"mode"
      ,"time_signature"
      ,"pitchRange"
      ,"timbreRange"
      ,"year"
    )

    // encode categorical variables
    val encoder1 = new OneHotEncoder().setInputCol("key").setOutputCol("keyVec")
    val encoder2 = new OneHotEncoder().setInputCol("mode").setOutputCol("modeVec")

    // specify columns to be used in features vector
    val featureColumns = Array(
      "artist_hotttnesss"
      ,"duration"
      ,"loudness"
      ,"end_of_fade_in"
      ,"start_of_fade_out"
      ,"tempo"
      ,"danceability"
      ,"energy"
      ,"keyVec"
      ,"modeVec"
      ,"time_signature"
      ,"pitchRange"
      ,"timbreRange"
      ,"year"
    )

    // combine columns into a feature vector
    val assembler = new VectorAssembler()
      .setInputCols(featureColumns)
      .setOutputCol("features")

    // specify the model hyperparameters
    val lir = new LinearRegression()
      .setFeaturesCol("features")
      .setLabelCol("artist_hotttnesss")
      .setRegParam(0.0)
      .setElasticNetParam(0.0)
      .setMaxIter(1000)
      .setTol(1e-6)
      .setPredictionCol("prediction")

    // create a pipeline to run the encoding, feature assembly, and model training steps
    val pipeline = new Pipeline().setStages(Array(encoder1, encoder2, assembler, lir))

    // split into training and test
    val modelData = SongML.splitDataFrame(songsDataFrame)

    // Train the model
    val startTime = System.nanoTime()
    val lirModel = pipeline.fit(modelData.training)
    val elapsedTime = (System.nanoTime() - startTime) / 1e9
    println(s"Training time: $elapsedTime seconds")

    // Save the trained model
    lirModel.save(Config.modelOut)
    val savedModel = LinearRegressionModel.load(sc,Config.modelOut)

    // Print the weights and intercept for linear regression.
    val colWeights = featureColumns.zip(savedModel.weights.toArray)
    println(s"Weights: $colWeights")
    println(s"Intercept: ${savedModel.intercept}")

    // print training results
    val trainingResults = lirModel.transform(modelData.test)
    val trainingMSE = trainingResults.select("artist_hotttnesss","prediction").map(r => math.pow(r.getAs[Double]("artist_hotttnesss") - r.getAs[Double]("prediction"),2)).mean()
    println("Training data results:")
    println(s"MSE: $trainingMSE")

    // print test results
    val testResults = lirModel.transform(modelData.test)
    val testMSE = testResults.select("artist_hotttnesss","prediction").map(r => math.pow(r.getAs[Double]("artist_hotttnesss") - r.getAs[Double]("prediction"),2)).mean()
    println("Test data results:")
    println(s"MSE: $testMSE")

    sc.stop()

  }
}
