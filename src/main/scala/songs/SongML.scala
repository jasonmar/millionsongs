package songs

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
      ,s.track.song_hotttnesss
      ,s.audio.duration
      ,s.audio.loudness
      ,s.audio.end_of_fade_in
      ,s.audio.start_of_fade_out
      ,s.audio.tempo
      ,s.audioEstimates.danceability
      ,s.audioEstimates.energy
      ,s.audioEstimates.key
      ,s.audioEstimates.mode
      ,s.audioEstimates.time_signature
      ,s.segments.segments_pitches.max - s.segments.segments_pitches.min
      ,s.segments.segments_timbre.max - s.segments.segments_timbre.min
      ,s.track.year
    )
  }

}
