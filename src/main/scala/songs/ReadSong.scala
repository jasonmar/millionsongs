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

  def readSongs(f: IHDF5Reader): Try[Vector[Song]] = Try{
    val metadata = getCompoundDS(f,"/metadata/songs")
    val analysis = getCompoundDS(f,"/analysis/songs")
    val mb = getCompoundDS(f,"/musicbrainz/songs")

    val idx_similar_artists = metadata.vector[Int](Fields.idx_similar_artists)
    val similar_artists = f.readStringArray(meta(Fields.similar_artists)).toVector

    val idx_artist_terms = metadata.vector[Int](Fields.idx_artist_terms)
    val artist_terms = f.readStringArray(meta(Fields.artist_terms)).toVector
    val artist_terms_freq = f.readDoubleArray(meta(Fields.artist_terms_freq)).toVector
    val artist_terms_weight = f.readDoubleArray(meta(Fields.artist_terms_weight)).toVector

    val idx_segments_start = analysis.vector[Int](Fields.idx_segments_start)
    val segments_start = f.readDoubleArray(an(Fields.segments_start)).toVector

    val idx_segments_confidence = analysis.vector[Int](Fields.idx_segments_confidence)
    val segments_confidence = f.readDoubleArray(an(Fields.segments_confidence)).toVector

    val idx_segments_pitches = analysis.vector[Int](Fields.idx_segments_pitches) // 2D Vector
    val segments_pitches = f.readDoubleArray(an(Fields.segments_pitches)).toVector

    val idx_segments_timbre = analysis.vector[Int](Fields.idx_segments_timbre) // 2D Vector
    val segments_timbre = f.readDoubleArray(an(Fields.segments_timbre)).toVector

    val idx_segments_loudness_max = analysis.vector[Int](Fields.idx_segments_loudness_max)
    val segments_loudness_max = f.readDoubleArray(an(Fields.segments_loudness_max)).toVector

    val idx_segments_loudness_max_time = analysis.vector[Int](Fields.idx_segments_loudness_max_time)
    val segments_loudness_max_time = f.readDoubleArray(an(Fields.segments_loudness_max_time)).toVector

    val idx_segments_loudness_start = analysis.vector[Int](Fields.idx_segments_loudness_start)
    val segments_loudness_start = f.readDoubleArray(an(Fields.segments_loudness_start)).toVector

    val idx_sections_start = analysis.vector[Int](Fields.idx_sections_start)
    val sections_start = f.readDoubleArray(an(Fields.sections_start)).toVector

    val idx_sections_confidence = analysis.vector[Int](Fields.idx_sections_confidence)
    val sections_confidence = f.readDoubleArray(an(Fields.sections_confidence)).toVector

    val idx_beats_start = analysis.vector[Int](Fields.idx_beats_start)
    val beats_start = f.readDoubleArray(an(Fields.beats_start)).toVector

    val idx_beats_confidence = analysis.vector[Int](Fields.idx_beats_confidence)
    val beats_confidence = f.readDoubleArray(an(Fields.beats_confidence)).toVector

    val idx_bars_start = analysis.vector[Int](Fields.idx_bars_start)
    val bars_start = f.readDoubleArray(an(Fields.bars_start)).toVector

    val idx_bars_confidence = analysis.vector[Int](Fields.idx_bars_confidence)
    val bars_confidence = f.readDoubleArray(an(Fields.bars_confidence)).toVector

    val idx_tatums_start = analysis.vector[Int](Fields.idx_tatums_start)
    val tatums_start = f.readDoubleArray(an(Fields.tatums_start)).toVector

    val idx_tatums_confidence = analysis.vector[Int](Fields.tatums_confidence)
    val tatums_confidence = f.readDoubleArray(an(Fields.tatums_confidence)).toVector

    val idx_artist_mbtags = mb.vector[Int](Fields.idx_artist_mbtags)
    val artist_mbtags = f.readStringArray(mbPath(Fields.artist_mbtags)).toVector
    val artist_mbtags_count = f.readIntArray(mbPath(Fields.artist_mbtags_count)).toVector
    
    val analysis_sample_rate = analysis.vector[Double](Fields.analysis_sample_rate)
    val danceability = analysis.vector[Double](Fields.danceability)
    val duration = analysis.vector[Double](Fields.duration)
    val end_of_fade_in = analysis.vector[Double](Fields.end_of_fade_in)
    val energy = analysis.vector[Double](Fields.energy)
    val key_confidence = analysis.vector[Double](Fields.key_confidence)
    val loudness = analysis.vector[Double](Fields.loudness)
    val mode_confidence = analysis.vector[Double](Fields.mode_confidence)
    val start_of_fade_out = analysis.vector[Double](Fields.start_of_fade_out)
    val tempo = analysis.vector[Double](Fields.tempo)
    val time_signature_confidence = analysis.vector[Double](Fields.time_signature_confidence)
    val artist_familiarity = metadata.vector[Double](Fields.artist_familiarity)
    val artist_hotttnesss = metadata.vector[Double](Fields.artist_hotttnesss)
    val artist_latitude = metadata.vector[Double](Fields.artist_latitude)
    val artist_longitude = metadata.vector[Double](Fields.artist_longitude)
    val song_hotttnesss = metadata.vector[Double](Fields.song_hotttnesss)
    val key = analysis.vector[Int](Fields.key)
    val mode = analysis.vector[Int](Fields.mode)
    val time_signature = analysis.vector[Int](Fields.time_signature)
    val artist_7digitalid = metadata.vector[Int](Fields.artist_7digitalid)
    val artist_playmeid = metadata.vector[Int](Fields.artist_playmeid)
    val release_7digitalid = metadata.vector[Int](Fields.release_7digitalid)
    val track_7digitalid = metadata.vector[Int](Fields.track_7digitalid)
    val year = mb.vector[Int](Fields.year)
    val audio_md5 = analysis.vector[String](Fields.audio_md5)
    val track_id = analysis.vector[String](Fields.track_id)
    val artist_id = metadata.vector[String](Fields.artist_id)
    val artist_location = metadata.vector[String](Fields.artist_location)
    val artist_mbid = metadata.vector[String](Fields.artist_mbid)
    val artist_name = metadata.vector[String](Fields.artist_name)
    val release = metadata.vector[String](Fields.release)
    val song_id = metadata.vector[String](Fields.song_id)
    val title = metadata.vector[String](Fields.title)

    song_id.indices.map{i =>

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
