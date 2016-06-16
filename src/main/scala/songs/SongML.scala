package songs

import org.apache.hadoop.fs.Path
import org.apache.spark.SparkContext
import org.apache.spark.ml.Pipeline
import org.apache.spark.ml.evaluation.RegressionEvaluator
import org.apache.spark.ml.feature.{OneHotEncoder, VectorAssembler}
import org.apache.spark.ml.regression.LinearRegression
import org.apache.spark.ml.tuning.{CrossValidator, ParamGridBuilder}
import org.apache.spark.mllib.linalg.Vector
import org.apache.spark.mllib.regression.LinearRegressionModel
import org.apache.spark.sql.{DataFrame, Row, SQLContext}
import songs.Types.{Song, SongFeatures}

object SongML {

  case class ModelData(training: DataFrame, test: DataFrame, eval: DataFrame){
    def saveAsParquet(): Unit = {
      training.write.parquet("songs.training.parquet")
      test.write.parquet("songs.test.parquet")
      eval.write.parquet("songs.eval.parquet")
    }
  }

  def loadModelData(trainingPath: String = "songs.training.parquet", testPath: String = "songs.test.parquet", evalPath: String = "songs.eval.parquet", sqlContext: SQLContext): ModelData = {
    val trainingDF = sqlContext.read.parquet(trainingPath)
    val testDF = sqlContext.read.parquet(testPath)
    val evalDF = sqlContext.read.parquet(evalPath)
    ModelData(trainingDF,testDF,evalDF)
  }

  def splitDataFrame(df: DataFrame): ModelData = {
    val dataFrames = df.randomSplit(Array(0.8,0.1,0.1), seed=1999)
    val training = dataFrames.head.cache()
    val eval = dataFrames(1).cache()
    val test = dataFrames.last.cache()

    ModelData(training,test,eval)
  }

  def extractFeatures(s: Song): SongFeatures = {
    SongFeatures(
      s.artist.artist_hotttnesss
      ,s.audio.duration
      ,s.audio.loudness
      ,s.audio.end_of_fade_in
      ,s.audio.start_of_fade_out
      ,s.audio.tempo
      ,s.audioEstimates.key
      ,s.audioEstimates.mode
      ,s.segments.segments_pitches.max - s.segments.segments_pitches.min
      ,s.segments.segments_timbre.max - s.segments.segments_timbre.min
      ,s.track.year
    )
  }

  val initialColumns = Array(
    "duration"
    ,"loudness"
    ,"end_of_fade_in"
    ,"start_of_fade_out"
    ,"tempo"
    ,"key"
    ,"mode"
    ,"pitchRange"
    ,"timbreRange"
    ,"year"
  )

  // specify columns to be used in features vector
  val featureColumns = Array(
    "duration"
    ,"loudness"
    ,"end_of_fade_in"
    ,"start_of_fade_out"
    ,"tempo"
    ,"keyVec"
    ,"modeVec"
    ,"pitchRange"
    ,"timbreRange"
    ,"year"
  )

  val labelColumn = "artist_hotttnesss"
  val predictionColumn = "hotness_hat"
  val featuresColumn = "features"
  val weightColumn = "weight"

  val allColumns = Vector(labelColumn) ++ initialColumns

  // Encodes categorical variables
  val encoder1 = new OneHotEncoder().setInputCol("key").setOutputCol("keyVec")
  val encoder2 = new OneHotEncoder().setInputCol("mode").setOutputCol("modeVec")

  // Combines columns into a feature vector
  val assembler = new VectorAssembler()
    .setInputCols(featureColumns)
    .setOutputCol(featuresColumn)

  // specify the model hyperparameters
  val linReg = new LinearRegression()
    .setFeaturesCol(featuresColumn)
    .setLabelCol(labelColumn)
    .setPredictionCol(predictionColumn)
    .setWeightCol(weightColumn)
    .setMaxIter(1000)
    .setTol(1e-6)
    .setRegParam(0.1)

  // create a pipeline to run the encoding, feature assembly, and model training steps
  val transformPipeline = new Pipeline().setStages(Array(encoder1, encoder2, assembler))

  // Used in CrossValidator and TrainValidationSplit for hyperparameter optimization
  val paramGrid = new ParamGridBuilder()
    .addGrid(linReg.regParam, Array(0.1, 0.01))
    .addGrid(linReg.fitIntercept)
    .addGrid(linReg.elasticNetParam, Array(0.0, 0.25, 0.5, 0.75, 1.0))
    .build()

  val lrEstimator = new Pipeline().setStages(Array(encoder1, encoder2, assembler, linReg))

  // CrossValidator was not used because it currently does not offer easy display of regression metrics
  val trainingPipeline = new CrossValidator()
    .setEstimator(lrEstimator)
    .setEvaluator(new RegressionEvaluator)
    .setEstimatorParamMaps(paramGrid)

  // TrainValidationSplit was not used because it currently does not offer model save/load functionality

  def loadLinearRegressionModel(sc: SparkContext, path: String): LinearRegressionModel = {
    val datapath = new Path(path, "data").toUri.toString
    val sqlContext = SQLContext.getOrCreate(sc)
    val dataRDD = sqlContext.read.parquet(datapath)
    val dataArray = dataRDD.select("weights", "intercept").take(1)
    val modelClass = "org.apache.spark.ml.regression.LinearRegressionModel"
    assert(dataArray.length == 1, s"Unable to load $modelClass data from: $datapath")
    val data = dataArray(0)
    assert(data.length == 2, s"Unable to load $modelClass data from: $datapath")
    data match {
      case Row(weights: Vector, intercept: Double) =>
        new LinearRegressionModel(weights, intercept)
      case _ =>
        throw new Exception(s"LinearRegressionModel.load failed to load model from $path")
    }
  }

}
