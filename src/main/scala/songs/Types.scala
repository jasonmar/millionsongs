package songs

object Types {

  case class Artist(
    artist_7digitalid: Int	//ID from 7digital.com or -1
    ,artist_familiarity: Double	//algorithmic estimation
    ,artist_hotttnesss: Double	//algorithmic estimation
    ,artist_id: String	//Echo Nest ID
    ,artist_latitude: Double	//latitude
    ,artist_location: String	//location name
    ,artist_longitude: Double	//longitude
    ,artist_mbid: String	//ID from musicbrainz.org
    ,artist_mbtags: Vector[String]	//tags from musicbrainz.org
    ,artist_mbtags_count: Vector[Int]	//tag counts for musicbrainz tags
    ,artist_name: String	//artist name
    ,artist_playmeid: Int	//ID from playme.com, or -1
    ,artist_terms: Vector[String]	//Echo Nest tags
    ,artist_terms_freq: Vector[Double]	//Echo Nest tags freqs
    ,artist_terms_weight: Vector[Double]	//Echo Nest tags weight
    ,similar_artists: Vector[String]	//Echo Nest artist IDs (sim. algo. unpublished)
  )

  case class Segments(
    segments_confidence: Vector[Double]	//confidence measure
    ,segments_loudness_max: Vector[Double]	//max dB value
    ,segments_loudness_max_time: Vector[Double]	//time of max dB value, i.e. end of attack
    ,segments_loudness_max_start: Vector[Double]	//dB value at onset
    ,segments_pitches: Vector[Double]	//2D vector chroma feature, one value per note
    ,segments_start: Vector[Double]	//musical events, ~ note onsets
    ,segments_timbre: Vector[Double]	//2D  texture features (MFCC+PCA-like)
  )

  case class Audio(
    analysis_sample_rate: Double	//sample rate of the audio used
    ,audio_md5: String	//audio hash code
    ,duration: Double	//in seconds
    ,end_of_fade_in: Double	//seconds at the beginning of the song
    ,loudness: Double	//overall loudness in dB
    ,start_of_fade_out: Double	//time in sec
    ,tempo: Double	//estimated tempo in BPM
  )

  case class AudioEstimates(
    bars_confidence: Vector[Double]	//confidence measure
    ,bars_start: Vector[Double]	//beginning of bars, usually on a beat
    ,beats_confidence: Vector[Double]	//confidence measure
    ,beats_start: Vector[Double]	//result of beat tracking
    ,danceability: Double	//algorithmic estimation
    ,energy: Double	//energy from listener poInt of view
    ,key: Int	//key the song is in
    ,key_confidence: Double	//confidence measure
    ,mode: Int	//major or minor
    ,mode_confidence: Double	//confidence measure
    ,tatums_confidence: Vector[Double]	//confidence measure
    ,tatums_start: Vector[Double]	//smallest rythmic element
    ,time_signature: Int	//estimate of number of beats per bar, e.g. 4
    ,time_signature_confidence: Double	//confidence measure
    ,sections_confidence: Vector[Double]	// confidence measure
    ,sections_start: Vector[Double]	// largest grouping in a song, e.g. verse
  )

  case class Track(
    release: String	//album name
    ,release_7digitalid: Int	//ID from 7digital.com or -1
    ,song_hotttnesss: Double	//algorithmic estimation
    ,song_id: String	//Echo Nest song ID
    ,title: String	//song title
    ,track_id: String	//Echo Nest track ID
    ,track_7digitalid: Int	//ID from 7digital.com or -1
    ,year: Int	//song release year from MusicBrainz or 0
  )

  case class Song(
    artist: Artist
    ,audio: Audio
    ,audioEstimates: AudioEstimates
    ,track: Track
    ,segments: Segments
  )

  case class SongFeatures(
     artist_hotttnesss: Double
    ,song_hotttnesss: Double
    ,duration: Double
    ,loudnes: Double
    ,end_of_fade_in: Double
    ,start_of_fade_out: Double
    ,tempo: Double
    ,danceability: Double
    ,energy: Double
    ,key: Int
    ,mode: Int
    ,time_signature: Int
    ,pitchRange: Double
    ,timbreRange: Double
    ,year: Double
  )
}
