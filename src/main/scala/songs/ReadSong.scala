package songs

import songs.Types._
import HDF5._
import ch.systemsx.cisd.hdf5._

import scala.util._

object ReadSong {

  def buildPath(group: String, s: String): String = {
    val sb = new StringBuilder(512)
    sb.append('/')
    sb.append(group)
    sb.append('/')
    sb.append(s)
    sb.mkString
  }

  def meta(s: String): String = buildPath("metadata",s)
  def an(s: String): String = buildPath("analysis",s)
  def mbPath(s: String): String = buildPath("musicbrainz",s)

  def readSongs(f: IHDF5Reader): Try[Song] = Try{
    val metadata = getCompoundDS(f,"/metadata/songs")
    val analysis = getCompoundDS(f,"/analysis/songs")
    val mb = getCompoundDS(f,"/musicbrainz/songs")

    val idx_similar_artists = metadata.get[Int](Fields.idx_similar_artists)
    val similar_artists = Try{f.readStringArray(meta(Fields.similar_artists))}.getOrElse(Array.empty[String]).toVector

    val idx_artist_terms = metadata.get[Int](Fields.idx_artist_terms)
    val artist_terms = Try{f.readStringArray(meta(Fields.artist_terms))}.getOrElse(Array.empty[String]).toVector
    val artist_terms_freq = f.readDoubleArray(meta(Fields.artist_terms_freq)).toVector
    val artist_terms_weight = f.readDoubleArray(meta(Fields.artist_terms_weight)).toVector

    val idx_segments_start = analysis.get[Int](Fields.idx_segments_start)
    val segments_start = f.readDoubleArray(an(Fields.segments_start)).toVector

    val idx_segments_confidence = analysis.get[Int](Fields.idx_segments_confidence)
    val segments_confidence = f.readDoubleArray(an(Fields.segments_confidence)).toVector

    val idx_segments_pitches = analysis.get[Int](Fields.idx_segments_pitches) // 2D Vector
    val segments_pitches = f.readDoubleArray(an(Fields.segments_pitches)).toVector

    val idx_segments_timbre = analysis.get[Int](Fields.idx_segments_timbre) // 2D Vector
    val segments_timbre = f.readDoubleArray(an(Fields.segments_timbre)).toVector

    val idx_segments_loudness_max = analysis.get[Int](Fields.idx_segments_loudness_max)
    val segments_loudness_max = f.readDoubleArray(an(Fields.segments_loudness_max)).toVector

    val idx_segments_loudness_max_time = analysis.get[Int](Fields.idx_segments_loudness_max_time)
    val segments_loudness_max_time = f.readDoubleArray(an(Fields.segments_loudness_max_time)).toVector

    val idx_segments_loudness_start = analysis.get[Int](Fields.idx_segments_loudness_start)
    val segments_loudness_start = f.readDoubleArray(an(Fields.segments_loudness_start)).toVector

    val idx_sections_start = analysis.get[Int](Fields.idx_sections_start)
    val sections_start = f.readDoubleArray(an(Fields.sections_start)).toVector

    val idx_sections_confidence = analysis.get[Int](Fields.idx_sections_confidence)
    val sections_confidence = f.readDoubleArray(an(Fields.sections_confidence)).toVector

    val idx_beats_start = analysis.get[Int](Fields.idx_beats_start)
    val beats_start = f.readDoubleArray(an(Fields.beats_start)).toVector

    val idx_beats_confidence = analysis.get[Int](Fields.idx_beats_confidence)
    val beats_confidence = f.readDoubleArray(an(Fields.beats_confidence)).toVector

    val idx_bars_start = analysis.get[Int](Fields.idx_bars_start)
    val bars_start = f.readDoubleArray(an(Fields.bars_start)).toVector

    val idx_bars_confidence = analysis.get[Int](Fields.idx_bars_confidence)
    val bars_confidence = f.readDoubleArray(an(Fields.bars_confidence)).toVector

    val idx_tatums_start = analysis.get[Int](Fields.idx_tatums_start)
    val tatums_start = f.readDoubleArray(an(Fields.tatums_start)).toVector

    val idx_tatums_confidence = analysis.get[Int](Fields.idx_tatums_confidence)
    val tatums_confidence = f.readDoubleArray(an(Fields.tatums_confidence)).toVector

    val idx_artist_mbtags = mb.get[Int](Fields.idx_artist_mbtags)
    val artist_mbtags = Try{f.readStringArray(mbPath(Fields.artist_mbtags))}.getOrElse(Array.empty[String]).toVector
    val artist_mbtags_count = f.readIntArray(mbPath(Fields.artist_mbtags_count)).toVector
    
    val analysis_sample_rate = analysis.get[Double](Fields.analysis_sample_rate)
    val danceability = analysis.get[Double](Fields.danceability)
    val duration = analysis.get[Double](Fields.duration)
    val end_of_fade_in = analysis.get[Double](Fields.end_of_fade_in)
    val energy = analysis.get[Double](Fields.energy)
    val key_confidence = analysis.get[Double](Fields.key_confidence)
    val loudness = analysis.get[Double](Fields.loudness)
    val mode_confidence = analysis.get[Double](Fields.mode_confidence)
    val start_of_fade_out = analysis.get[Double](Fields.start_of_fade_out)
    val tempo = analysis.get[Double](Fields.tempo)
    val time_signature_confidence = analysis.get[Double](Fields.time_signature_confidence)
    val artist_familiarity = metadata.get[Double](Fields.artist_familiarity)
    val artist_hotttnesss = metadata.get[Double](Fields.artist_hotttnesss)
    val artist_latitude = metadata.get[Double](Fields.artist_latitude)
    val artist_longitude = metadata.get[Double](Fields.artist_longitude)
    val song_hotttnesss = metadata.get[Double](Fields.song_hotttnesss)
    val key = analysis.get[Int](Fields.key)
    val mode = analysis.get[Int](Fields.mode)
    val time_signature = analysis.get[Int](Fields.time_signature)
    val artist_7digitalid = metadata.get[Int](Fields.artist_7digitalid)
    val artist_playmeid = metadata.get[Int](Fields.artist_playmeid)
    val release_7digitalid = metadata.get[Int](Fields.release_7digitalid)
    val track_7digitalid = metadata.get[Int](Fields.track_7digitalid)
    val year = mb.get[Int](Fields.year)
    val audio_md5 = analysis.get[String](Fields.audio_md5)
    val track_id = analysis.get[String](Fields.track_id)
    val artist_id = metadata.get[String](Fields.artist_id)
    val artist_location = metadata.get[String](Fields.artist_location)
    val artist_mbid = metadata.get[String](Fields.artist_mbid)
    val artist_name = metadata.get[String](Fields.artist_name)
    val release = metadata.get[String](Fields.release)
    val song_id = metadata.get[String](Fields.song_id)
    val title = metadata.get[String](Fields.title)

    val artist = Artist(
      artist_7digitalid: Int //ID from 7digital.com or -1
      ,artist_familiarity: Double //algorithmic estimation
      ,artist_hotttnesss: Double //algorithmic estimation
      ,artist_id: String //Echo Nest ID
      ,artist_latitude: Double //latitude
      ,artist_location: String //location name
      ,artist_longitude: Double //longitude
      ,artist_mbid: String //ID from musicbrainz.org
      ,artist_mbtags: Vector[String] //tags from musicbrainz.org
      ,artist_mbtags_count: Vector[Int] //tag counts for musicbrainz tags
      ,artist_name: String //artist name
      ,artist_playmeid: Int //ID from playme.com,or -1
      ,artist_terms: Vector[String] //Echo Nest tags
      ,artist_terms_freq: Vector[Double] //Echo Nest tags freqs
      ,artist_terms_weight: Vector[Double] //Echo Nest tags weight
      ,similar_artists: Vector[String] //Echo Nest artist IDs (sim. algo. unpublished)
    )

    val segments = Segments(
      segments_confidence: Vector[Double] //confidence measure
      ,segments_loudness_max: Vector[Double] //max dB value
      ,segments_loudness_max_time: Vector[Double] //time of max dB value,i.e. end of attack
      ,segments_loudness_start: Vector[Double] //dB value at onset
      ,segments_pitches: Vector[Double] //2D vector chroma feature,one value per note
      ,segments_start: Vector[Double] //musical events,~ note onsets
      ,segments_timbre: Vector[Double] //2D  texture features (MFCC+PCA-like)
    )

    val audio = Audio(
      analysis_sample_rate: Double //sample rate of the audio used
      ,audio_md5: String //audio hash code
      ,duration: Double //in seconds
      ,end_of_fade_in: Double //seconds at the beginning of the song
      ,loudness: Double //overall loudness in dB
      ,start_of_fade_out: Double //time in sec
      ,tempo: Double //estimated tempo in BPM
    )

    val audioEstimates = AudioEstimates(
      bars_confidence: Vector[Double] //confidence measure
      ,bars_start: Vector[Double] //beginning of bars,usually on a beat
      ,beats_confidence: Vector[Double] //confidence measure
      ,beats_start: Vector[Double] //result of beat tracking
      ,danceability: Double //algorithmic estimation
      ,energy: Double //energy from listener poInt of view
      ,key: Int //key the song is in
      ,key_confidence: Double //confidence measure
      ,mode: Int //major or minor
      ,mode_confidence: Double //confidence measure
      ,tatums_confidence: Vector[Double] //confidence measure
      ,tatums_start: Vector[Double] //smallest rythmic element
      ,time_signature: Int //estimate of number of beats per bar,e.g. 4
      ,time_signature_confidence: Double //confidence measure
      ,sections_confidence: Vector[Double] // confidence measure
      ,sections_start: Vector[Double] // largest grouping in a song,e.g. verse
    )

    val track = Track(
      release: String //album name
      ,release_7digitalid: Int //ID from 7digital.com or -1
      ,song_hotttnesss: Double //algorithmic estimation
      ,song_id: String //Echo Nest song ID
      ,title: String //song title
      ,track_id: String //Echo Nest track ID
      ,track_7digitalid: Int //ID from 7digital.com or -1
      ,year: Int //song release year from MusicBrainz or 0
    )

    Song(artist,audio,audioEstimates,track,segments)
  }
}
