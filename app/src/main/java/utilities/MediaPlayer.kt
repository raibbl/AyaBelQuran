package utilities

import android.media.AudioAttributes
import android.media.MediaPlayer

class MediaPlayer {

    companion object {
        var mediaPlayer: MediaPlayer? = null
        fun releaseMediaPlayer() {
            mediaPlayer?.release()
            mediaPlayer = null
        }

        fun playAudioFromUrl(url: String) {
            if (mediaPlayer == null || mediaPlayer?.isPlaying == false) {
                mediaPlayer?.release() // Release any previous MediaPlayer
                mediaPlayer = MediaPlayer().apply {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .build()
                    )
                    setDataSource(url)
                    prepareAsync() // Prepare the MediaPlayer asynchronously
                    setOnPreparedListener { start() }
                }
            } else {
                mediaPlayer?.pause()
            }
        }
    }

}