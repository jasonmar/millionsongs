package songs

import org.apache.spark.ml.Pipeline
import org.apache.spark.ml.feature.{OneHotEncoder, VectorAssembler}
import org.apache.spark.ml.regression.LinearRegression
import org.apache.spark.sql.DataFrame
import songs.Types.{Song, SongFeatures}

object SongML {

  case class ModelData(training: DataFrame, test: DataFrame, eval: DataFrame)

  def splitDataFrame(df: DataFrame): ModelData = {
    val dataFrames = df.randomSplit(Array(0.33,0.33,0.33), seed=1999)
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
  val ignoredColumn = "song_hotttnesss"
  val featuresColumn = "features"

  val allColumns = Vector(labelColumn,ignoredColumn) ++ initialColumns

  // encode categorical variables
  val encoder1 = new OneHotEncoder().setInputCol("key").setOutputCol("keyVec")
  val encoder2 = new OneHotEncoder().setInputCol("mode").setOutputCol("modeVec")

  // combine columns into a feature vector
  val assembler = new VectorAssembler()
    .setInputCols(featureColumns)
    .setOutputCol(featuresColumn)

  // specify the model hyperparameters
  val lir = new LinearRegression()
    .setFeaturesCol(featuresColumn)
    .setLabelCol(labelColumn)
    .setRegParam(0.0)
    .setElasticNetParam(0.0)
    .setMaxIter(1000)
    .setTol(1e-6)
    .setPredictionCol(predictionColumn)

  // create a pipeline to run the encoding, feature assembly, and model training steps
  val pipeline = new Pipeline().setStages(Array(encoder1, encoder2, assembler, lir))

}
