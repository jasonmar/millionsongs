package songs

import ncsa.hdf.`object`.h5._
import songs.Types._
import HDF5._
import scala.util._

object ReadSong {

  def meta(s: String): String = "/metadata/" + s
  def an(s: String): String = "/analysis/" + s
  def mbPath(s: String): String = "/musicbrainz/" + s

  def readSongs(f: H5File): Try[Vector[Song]] = Try{
    val metadata = getCompoundDS(f,meta("songs"))
    val analysis = getCompoundDS(f,an("songs"))
    val mb = getCompoundDS(f,mbPath("songs"))

    val idx_similar_artists = metadata.toVector[Int](Fields.idx_similar_artists)
    val similar_artists = getScalarDS(f,meta(Fields.similar_artists)).toVector[String]

    val idx_artist_terms = metadata.toVector[Int](Fields.idx_artist_terms)
    val artist_terms = getScalarDS(f,meta(Fields.artist_terms)).toVector[String]
    val artist_terms_freq = getScalarDS(f,meta(Fields.artist_terms_freq)).toVector[Double]
    val artist_terms_weight = getScalarDS(f,meta(Fields.artist_terms_weight)).toVector[Double]

    val idx_segments_start = analysis.toVector[Int](Fields.idx_segments_start)
    val segments_start = getScalarDS(f,an(Fields.segments_start)).toVector[Double]

    val idx_segments_confidence = analysis.toVector[Int](Fields.idx_segments_confidence)
    val segments_confidence = getScalarDS(f,an(Fields.segments_confidence)).toVector[Double]

    val idx_segments_pitches = analysis.toVector[Int](Fields.idx_segments_pitches) // 2D Vector
    val segments_pitches = getScalarDS(f,an(Fields.segments_pitches)).toVector[Double]

    val idx_segments_timbre = analysis.toVector[Int](Fields.idx_segments_timbre) // 2D Vector
    val segments_timbre = getScalarDS(f,an(Fields.segments_timbre)).toVector[Double]

    val idx_segments_loudness_max = analysis.toVector[Int](Fields.idx_segments_loudness_max)
    val segments_loudness_max = getScalarDS(f,an(Fields.segments_loudness_max)).toVector[Double]

    val idx_segments_loudness_max_time = analysis.toVector[Int](Fields.idx_segments_loudness_max_time)
    val segments_loudness_max_time = getScalarDS(f,an(Fields.segments_loudness_max_time)).toVector[Double]

    val idx_segments_loudness_start = analysis.toVector[Int](Fields.idx_segments_loudness_start)
    val segments_loudness_start = getScalarDS(f,an(Fields.segments_loudness_start)).toVector[Double]

    val idx_sections_start = analysis.toVector[Int](Fields.idx_sections_start)
    val sections_start = getScalarDS(f,an(Fields.sections_start)).toVector[Double]

    val idx_sections_confidence = analysis.toVector[Int](Fields.idx_sections_confidence)
    val sections_confidence = getScalarDS(f,an(Fields.sections_confidence)).toVector[Double]

    val idx_beats_start = analysis.toVector[Int](Fields.idx_beats_start)
    val beats_start = getScalarDS(f,an(Fields.beats_start)).toVector[Double]

    val idx_beats_confidence = analysis.toVector[Int](Fields.idx_beats_confidence)
    val beats_confidence = getScalarDS(f,an(Fields.beats_confidence)).toVector[Double]

    val idx_bars_start = analysis.toVector[Int](Fields.idx_bars_start)
    val bars_start = getScalarDS(f,an(Fields.bars_start)).toVector[Double]

    val idx_bars_confidence = analysis.toVector[Int](Fields.idx_bars_confidence)
    val bars_confidence = getScalarDS(f,an(Fields.bars_confidence)).toVector[Double]

    val idx_tatums_start = analysis.toVector[Int](Fields.idx_tatums_start)
    val tatums_start = getScalarDS(f,an(Fields.tatums_start)).toVector[Double]

    val idx_tatums_confidence = analysis.toVector[Int](Fields.tatums_confidence)
    val tatums_confidence = getScalarDS(f,an(Fields.tatums_confidence)).toVector[Double]

    val idx_artist_mbtags = mb.toVector[Int](Fields.idx_artist_mbtags)
    val artist_mbtags = getScalarDS(f,mbPath(Fields.artist_mbtags)).toVector[String]
    val artist_mbtags_count = getScalarDS(f,mbPath(Fields.artist_mbtags_count)).toVector[Int]
    
    val analysis_sample_rate = analysis.toVector[Double](Fields.analysis_sample_rate)
    val danceability = analysis.toVector[Double](Fields.danceability)
    val duration = analysis.toVector[Double](Fields.duration)
    val end_of_fade_in = analysis.toVector[Double](Fields.end_of_fade_in)
    val energy = analysis.toVector[Double](Fields.energy)
    val key_confidence = analysis.toVector[Double](Fields.key_confidence)
    val loudness = analysis.toVector[Double](Fields.loudness)
    val mode_confidence = analysis.toVector[Double](Fields.mode_confidence)
    val start_of_fade_out = analysis.toVector[Double](Fields.start_of_fade_out)
    val tempo = analysis.toVector[Double](Fields.tempo)
    val time_signature_confidence = analysis.toVector[Double](Fields.time_signature_confidence)
    val artist_familiarity = metadata.toVector[Double](Fields.artist_familiarity)
    val artist_hotttnesss = metadata.toVector[Double](Fields.artist_hotttnesss)
    val artist_latitude = metadata.toVector[Double](Fields.artist_latitude)
    val artist_longitude = metadata.toVector[Double](Fields.artist_longitude)
    val song_hotttnesss = metadata.toVector[Double](Fields.song_hotttnesss)
    val key = analysis.toVector[Int](Fields.key)
    val mode = analysis.toVector[Int](Fields.mode)
    val time_signature = analysis.toVector[Int](Fields.time_signature)
    val artist_7digitalid = metadata.toVector[Int](Fields.artist_7digitalid)
    val artist_playmeid = metadata.toVector[Int](Fields.artist_playmeid)
    val release_7digitalid = metadata.toVector[Int](Fields.release_7digitalid)
    val track_7digitalid = metadata.toVector[Int](Fields.track_7digitalid)
    val year = mb.toVector[Int](Fields.year)
    val audio_md5 = analysis.toVector[String](Fields.audio_md5)
    val track_id = analysis.toVector[String](Fields.track_id)
    val artist_id = metadata.toVector[String](Fields.artist_id)
    val artist_location = metadata.toVector[String](Fields.artist_location)
    val artist_mbid = metadata.toVector[String](Fields.artist_mbid)
    val artist_name = metadata.toVector[String](Fields.artist_name)
    val release = metadata.toVector[String](Fields.release)
    val song_id = metadata.toVector[String](Fields.song_id)
    val title = metadata.toVector[String](Fields.title)
    
    metadata.indices.map{i =>

      val artist = Artist(
        artist_7digitalid(i): Int //ID from 7digital.com or -1
        ,artist_familiarity(i): Double //algorithmic estimation
        ,artist_hotttnesss(i): Double //algorithmic estimation
        ,artist_id(i): String //Echo Nest ID
        ,artist_latitude(i): Double //latitude
        ,artist_location(i): String //location name
        ,artist_longitude(i): Double //longitude
        ,artist_mbid(i): String //ID from musicbrainz.org
        ,HDF5.readArray[String](artist_mbtags,idx_artist_mbtags,i): Vector[String] //tags from musicbrainz.org
        ,HDF5.readArray[Int](artist_mbtags_count,idx_artist_mbtags,i): Vector[Int] //tag counts for musicbrainz tags
        ,artist_name(i): String //artist name
        ,artist_playmeid(i): Int //ID from playme.com,or -1
        ,HDF5.readArray[String](artist_terms,idx_artist_terms,i): Vector[String] //Echo Nest tags
        ,HDF5.readArray[Double](artist_terms_freq,idx_artist_terms,i): Vector[Double] //Echo Nest tags freqs
        ,HDF5.readArray[Double](artist_terms_weight,idx_artist_terms,i): Vector[Double] //Echo Nest tags weight
        ,HDF5.readArray[String](similar_artists,idx_similar_artists,i): Vector[String] //Echo Nest artist IDs (sim. algo. unpublished)
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
        analysis_sample_rate(i): Double //sample rate of the audio used
        ,audio_md5(i): String //audio hash code
        ,duration(i): Double //in seconds
        ,end_of_fade_in(i): Double //seconds at the beginning of the song
        ,loudness(i): Double //overall loudness in dB
        ,start_of_fade_out(i): Double //time in sec
        ,tempo(i): Double //estimated tempo in BPM
      )

      val audioEstimates = AudioEstimates(
        HDF5.readArray[Double](bars_confidence,idx_bars_confidence,i): Vector[Double] //confidence measure
        ,HDF5.readArray[Double](bars_start,idx_bars_start,i): Vector[Double] //beginning of bars,usually on a beat
        ,HDF5.readArray[Double](beats_confidence,idx_beats_confidence,i): Vector[Double] //confidence measure
        ,HDF5.readArray[Double](beats_start,idx_beats_start,i): Vector[Double] //result of beat tracking
        ,danceability(i): Double //algorithmic estimation
        ,energy(i): Double //energy from listener poInt of view
        ,key(i): Int //key the song is in
        ,key_confidence(i): Double //confidence measure
        ,mode(i): Int //major or minor
        ,mode_confidence(i): Double //confidence measure
        ,HDF5.readArray[Double](tatums_confidence,idx_tatums_confidence,i): Vector[Double] //confidence measure
        ,HDF5.readArray[Double](tatums_start,idx_tatums_start,i): Vector[Double] //smallest rythmic element
        ,time_signature(i): Int //estimate of number of beats per bar,e.g. 4
        ,time_signature_confidence(i): Double //confidence measure
        ,HDF5.readArray[Double](sections_confidence,idx_sections_confidence,i): Vector[Double] // confidence measure
        ,HDF5.readArray[Double](sections_start,idx_sections_start,i): Vector[Double] // largest grouping in a song,e.g. verse
      )

      val track = Track(
        release(i): String //album name
        ,release_7digitalid(i): Int //ID from 7digital.com or -1
        ,song_hotttnesss(i): Double //algorithmic estimation
        ,song_id(i): String //Echo Nest song ID
        ,title(i): String //song title
        ,track_id(i): String //Echo Nest track ID
        ,track_7digitalid(i): Int //ID from 7digital.com or -1
        ,year(i): Int //song release year from MusicBrainz or 0
      )

      Song(artist,audio,audioEstimates,track,segments)
    }.toVector
  }
}
