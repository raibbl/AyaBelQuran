import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.app.NotificationCompat
import androidx.media.app.NotificationCompat.MediaStyle
import androidx.wear.ongoing.OngoingActivity
import androidx.wear.ongoing.Status
import com.raibbl.ayabelquran.R
import com.raibbl.ayabelquran.presentation.MainActivity

class MediaPlayer {
    companion object {
        private var mediaPlayer: MediaPlayer? = null
        private var mediaSession: MediaSessionCompat? = null
        private const val CHANNEL_ID = "ongoing_channel"
        private const val NOTIFICATION_ID = 1

        // Initialize the MediaPlayer and prepare the audio
        fun initializeMediaPlayer(url: String, title: String, context: Context) {
            mediaPlayer?.release()

            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                setDataSource(url)
                prepareAsync()
                setOnPreparedListener {
                    mediaSession = MediaSessionCompat(context, "MediaSessionTag").apply {
                        setCallback(object : MediaSessionCompat.Callback() {
                            override fun onPlay() {
                                mediaPlayer?.start()
                                updatePlaybackState(PlaybackStateCompat.STATE_PLAYING)
                                showNotification(context, title, "Playing")
                            }

                            override fun onPause() {
                                mediaPlayer?.pause()
                                updatePlaybackState(PlaybackStateCompat.STATE_PAUSED)
                                showNotification(context, title, "Paused")
                            }
                        })
                        isActive = true

                        // Set media metadata
                        setMetadata(
                            MediaMetadataCompat.Builder()
                                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, title)
                                .build()
                        )
                    }
                    updatePlaybackState(PlaybackStateCompat.STATE_PAUSED) // Initial state
                    showNotification(context, title, "Ready to Play")
                }
            }
        }

        // Update playback state
        private fun updatePlaybackState(state: Int) {
            val playbackState = PlaybackStateCompat.Builder()
                .setActions(
                    PlaybackStateCompat.ACTION_PLAY or
                            PlaybackStateCompat.ACTION_PAUSE or
                            PlaybackStateCompat.ACTION_PLAY_PAUSE
                )
                .setState(state, PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 1.0f)
                .build()
            mediaSession?.setPlaybackState(playbackState)
        }

        // Show the notification with OngoingActivity API integration
        private fun showNotification(context: Context, title: String, description: String) {
            val intent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val channel = NotificationChannel(
                CHANNEL_ID,
                "Media Playback",
                NotificationManager.IMPORTANCE_LOW
            )
            notificationManager.createNotificationChannel(channel)

            val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_play)
                .setContentTitle(title)
                .setContentText(description)
                .setContentIntent(pendingIntent)
                .setCategory(NotificationCompat.CATEGORY_TRANSPORT)
                .setOngoing(true)
                .setStyle(
                    MediaStyle()
                        .setMediaSession(mediaSession?.sessionToken)
                        .setShowActionsInCompactView(0)
                )

            // Integrate OngoingActivity for Wear OS
            val ongoingActivityStatus = Status.Builder()
                .addTemplate(description)
                .addPart("title", Status.TextPart(title)) // Show title
                .build()

            val ongoingActivity = OngoingActivity.Builder(
                context, NOTIFICATION_ID, notificationBuilder
            )
                .setStaticIcon(R.drawable.ic_play)
                .setTouchIntent(pendingIntent)
                .setStatus(ongoingActivityStatus)
                .build()

            ongoingActivity.apply(context)

            notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
        }

        // Release MediaPlayer resources
        fun releaseMediaPlayer(context: Context) {
            mediaPlayer?.release()
            mediaPlayer = null
            mediaSession?.release()
            mediaSession = null

            // Cancel notification
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(NOTIFICATION_ID)
        }

        // Play or pause the audio
        fun playAudioFromUrl(context: Context) {
            val currentTitle =
                mediaSession?.controller?.metadata?.getString(MediaMetadataCompat.METADATA_KEY_TITLE)
                    ?: "Unknown Title"

            if (mediaPlayer?.isPlaying == false) {
                mediaPlayer?.start()
                updatePlaybackState(PlaybackStateCompat.STATE_PLAYING)
                showNotification(context, currentTitle, "Playing Audio")
            } else {
                mediaPlayer?.pause()
                updatePlaybackState(PlaybackStateCompat.STATE_PAUSED)
                showNotification(context, currentTitle, "Paused")
            }
        }
    }
}
