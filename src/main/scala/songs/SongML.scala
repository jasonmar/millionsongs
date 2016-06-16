package songs

import org.apache.spark.ml.{Pipeline, PipelineStage}
import org.apache.spark.ml.evaluation.RegressionEvaluator
import org.apache.spark.ml.feature.{OneHotEncoder, StandardScaler, VectorAssembler}
import org.apache.spark.ml.regression.LinearRegression
import org.apache.spark.ml.tuning.{CrossValidator, ParamGridBuilder}
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
    ,"loudnessScaled"
    ,"end_of_fade_inScaled"
    ,"start_of_fade_out"
    ,"tempo"
    ,"keyVec"
    ,"modeVec"
    ,"pitchRangeScaled"
    ,"timbreRangeScaled"
    ,"yearScaled"
  )

  val labelColumn = "artist_hotttnesss"
  val predictionColumn = "hotness_hat"
  val featuresColumn = "features"

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
    .setMaxIter(1000)
    .setTol(1e-6)
    .setRegParam(0.1)

  val scaler1 = new StandardScaler().setWithMean(true).setWithStd(true).setInputCol("loudness").setOutputCol("loudnessScaled")
  val scaler2 = new StandardScaler().setWithMean(true).setWithStd(true).setInputCol("end_of_fade_in").setOutputCol("end_of_fade_inScaled")
  val scaler3 = new StandardScaler().setWithMean(true).setWithStd(true).setInputCol("pitchRange").setOutputCol("pitchRangeScaled")
  val scaler4 = new StandardScaler().setWithMean(true).setWithStd(true).setInputCol("timbreRange").setOutputCol("timbreRangeScaled")
  val scaler5 = new StandardScaler().setWithMean(true).setWithStd(true).setInputCol("year").setOutputCol("yearScaled")

  // create a pipeline to run the encoding, feature assembly, and model training steps
  val transformStages: Array[PipelineStage] = Array(encoder1, encoder2, scaler1, scaler2, scaler3, scaler4, scaler5, assembler)
  val transformPipeline = new Pipeline().setStages(transformStages)

  // Used in CrossValidator and TrainValidationSplit for hyperparameter optimization

  val features2 = featureColumns.filterNot(_ == "start_of_fade_out")
  val features3 = features2.filterNot(_ == "tempo")
  val features4 = features3.filterNot(_ == "duration")

  val featureSelection: Array[Array[String]] = Array(
    featureColumns,
    features2,
    features3,
    features4
  )

  val featureLists = featureSelection.map(a => (a.length,a)).toMap

  val paramGrid = new ParamGridBuilder()
    .addGrid(linReg.regParam, Array(0.1, 0.01))
    .addGrid(linReg.fitIntercept, Array(true,false))
    .addGrid(linReg.elasticNetParam, Array(0.0, 0.2, 0.6, 1.0))
    .addGrid(assembler.inputCols,featureSelection)
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

  // TrainValidationSplit was not used because it currently does not offer model save/load functionality

}
