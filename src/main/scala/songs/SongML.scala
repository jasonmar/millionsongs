package songs

import org.apache.spark.ml.{Pipeline, PipelineStage}
import org.apache.spark.ml.evaluation.RegressionEvaluator
import org.apache.spark.ml.feature.{OneHotEncoder, StandardScaler, VectorAssembler}
import org.apache.spark.ml.regression.{LinearRegression, LinearRegressionModel}
import org.apache.spark.ml.tuning.{CrossValidator, ParamGridBuilder}
import org.apache.spark.mllib.evaluation.RegressionMetrics
import org.apache.spark.sql.{DataFrame, SQLContext}
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
    ,"key"
    ,"mode"
    ,"pitchRange"
    ,"timbreRange"
    ,"year"
  )

  val featureColumnsWithEncoding = Array(
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


  // Used in CrossValidator and TrainValidationSplit for parameter selection
  val features2 = featureColumns.filterNot(_ == "start_of_fade_out")
  val features3 = features2.filterNot(_ == "tempo")
  val features4 = features3.filterNot(_ == "duration")

  // Used in hyperparameter grid to compare models with certain coefficients removed
  val featureSelection: Vector[Array[String]] = Vector(
    featureColumns,
    features2,
    features3,
    features4
  )

  // Used to lookup feature names by number of features
  val featureLists = featureSelection.map(a => (a.length,a)).toMap

  val labelColumn = "artist_hotttnesss"
  val predictionColumn = "hotness_hat"
  val featuresColumn = "features"
  val featuresColumnUnscaled = "features0"

  val allColumns = Vector(labelColumn) ++ initialColumns

  // Encodes categorical variables
  val encoder1 = new OneHotEncoder().setInputCol("key").setOutputCol("keyVec")
  val encoder2 = new OneHotEncoder().setInputCol("mode").setOutputCol("modeVec")

  // Combines columns into a feature vector
  val assembler = new VectorAssembler()
    .setInputCols(features4)
    .setOutputCol(featuresColumnUnscaled)

  // specify the model hyperparameters
  val linReg = new LinearRegression()
    .setFeaturesCol(featuresColumn)
    .setLabelCol(labelColumn)
    .setPredictionCol(predictionColumn)
    .setMaxIter(1000)
    .setTol(1e-6)
    .setRegParam(0.1)

  val scaler1 = new StandardScaler()
    .setInputCol(featuresColumnUnscaled)
    .setOutputCol(featuresColumn)

  // create a pipeline to run the encoding, feature assembly, and model training steps
  val transformStagesWithEncoding: Array[PipelineStage] = Array(encoder1, encoder2, assembler, scaler1)
  val transformStages: Array[PipelineStage] = Array(assembler, scaler1)
  val transformPipeline = new Pipeline().setStages(transformStages)

  val paramGrid = new ParamGridBuilder()
    .addGrid(linReg.regParam, Vector(0.1, 0.01))
    .addGrid(linReg.fitIntercept, Vector(true, false))
    .addGrid(linReg.elasticNetParam, Vector(0.0, 0.2, 1.0))
    .addGrid(assembler.inputCols, featureSelection)
    .build()

  val lrStages: Array[PipelineStage] = transformStages ++ Array[PipelineStage](linReg)
  val lrEstimator = new Pipeline().setStages(lrStages)

  val regressionEvaluator = new RegressionEvaluator()
    .setPredictionCol(predictionColumn)
    .setLabelCol(labelColumn)
    .setMetricName("r2")

  val trainingPipeline = new CrossValidator()
    .setEstimator(lrEstimator)
    .setEvaluator(regressionEvaluator)
    .setEstimatorParamMaps(paramGrid)
    .setNumFolds(3)

  def printStats(model: LinearRegressionModel, rm: RegressionMetrics, stage: String): String = {

    val coefs = model.coefficients.toArray.toVector

    val sb = new StringBuilder(4096)
    sb.append(System.lineSeparator())
    sb.append(System.lineSeparator())
    sb.append(System.lineSeparator())
    sb.append(System.lineSeparator())
    sb.append("Model params:")
    sb.append(model.explainParams())
    sb.append(System.lineSeparator())
    sb.append("Model coefficients:")
    sb.append(System.lineSeparator())
    sb.append(coefs.toString)
    sb.append(System.lineSeparator())
    if (model.hasSummary){
      sb.append("Model t-values:")
      sb.append(System.lineSeparator())
      sb.append(model.summary.tValues.toVector.toString())
      sb.append(System.lineSeparator())
    }

    SongML.featureLists.get(coefs.length).foreach{f =>
      f.zip(model.coefficients.toArray).foreach{t =>
        sb.append(s"${t._1}:\t${t._2}")
        sb.append(System.lineSeparator())
      }
    }
    sb.append(System.lineSeparator())
    sb.append(s"$stage Metrics")
    sb.append(System.lineSeparator())
    sb.append(s"Explained Variance:\t${rm.explainedVariance}")
    sb.append(System.lineSeparator())
    sb.append(s"R^2:\t\t\t${rm.r2}")
    sb.append(System.lineSeparator())
    sb.append(s"MSE:\t\t\t${rm.meanSquaredError}")
    sb.append(System.lineSeparator())
    sb.append(s"RMSE:\t\t\t${rm.rootMeanSquaredError}")
    sb.append(System.lineSeparator())
    sb.append(System.lineSeparator())
    sb.append(System.lineSeparator())
    sb.append(System.lineSeparator())
    sb.mkString
  }
}
