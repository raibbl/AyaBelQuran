import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.app.NotificationCompat
import androidx.media.app.NotificationCompat.MediaStyle
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.wear.ongoing.OngoingActivity
import androidx.wear.ongoing.Status
import com.raibbl.ayabelquran.R
import com.raibbl.ayabelquran.presentation.MainActivity

class MediaPlayer {
    companion object {
        private var exoPlayer: ExoPlayer? = null
        private var currentSource: String? = null
        private var mediaSession: MediaSessionCompat? = null
        private const val CHANNEL_ID = "ongoing_channel"
        private const val NOTIFICATION_ID = 1


        fun initializeMediaPlayer(
            url: String,
            title: String,
            context: Context,
            onReady: (() -> Unit)? = null,
            onCompletion: (() -> Unit)? = null
        ) {
            releasePlayer(context)
            exoPlayer = ExoPlayer.Builder(context).build().apply {
                val mediaItem = MediaItem.fromUri(Uri.parse(url))
                val audioAttributes = AudioAttributes.Builder()
                    .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                    .setUsage(C.USAGE_MEDIA)
                    .build()
                currentSource = url
                setMediaItem(mediaItem)
                setAudioAttributes(audioAttributes, true)
                prepare()

                mediaSession = MediaSessionCompat(context, "MediaSessionTag").apply {
                    setCallback(object : MediaSessionCompat.Callback() {
                        override fun onPlay() {
                            exoPlayer?.play()
                            updatePlaybackState(PlaybackStateCompat.STATE_PLAYING)
                            showNotification(context, title, "Audio Playing")
                        }

                        override fun onPause() {
                            exoPlayer?.pause()
                            onCompletion?.invoke()
                            updatePlaybackState(PlaybackStateCompat.STATE_PAUSED)
                            showNotification(context, title, "Paused")
                        }
                    })
                    isActive = true

                    setMetadata(
                        MediaMetadataCompat.Builder()
                            .putString(MediaMetadataCompat.METADATA_KEY_TITLE, title)
                            .build()
                    )

                    addListener(object : Player.Listener {
                        override fun onPlaybackStateChanged(state: Int) {
                            when (state) {
                                Player.STATE_READY -> {
                                    onReady?.invoke()
                                    updatePlaybackState(PlaybackStateCompat.STATE_PLAYING)
                                    showNotification(context, title, "Playing")
                                }

                                Player.STATE_ENDED -> {
                                    onCompletion?.invoke()
                                    releasePlayer(context)
                                }

                                Player.STATE_BUFFERING -> {
                                    updatePlaybackState(PlaybackStateCompat.STATE_BUFFERING)
                                    showNotification(context, title, "Buffering ⏳")
                                }

                                Player.STATE_IDLE -> {
                                    updatePlaybackState(PlaybackStateCompat.STATE_PAUSED)
                                    showNotification(context, title, "Paused ⏸️")
                                }
                            }
                        }
                    })
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
            val intent = Intent(context, MainActivity::class.java).apply {
                action = Intent.ACTION_MAIN
                addCategory(Intent.CATEGORY_LAUNCHER)
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
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




        // Play or pause the audio
        fun playPause(context: Context) {
            val currentTitle = mediaSession?.controller?.metadata?.getString(MediaMetadataCompat.METADATA_KEY_TITLE)
                ?: "Unknown Title"

            exoPlayer?.let {
                if (it.isPlaying) {
                    it.pause()
                    updatePlaybackState(PlaybackStateCompat.STATE_PAUSED)
                    showNotification(context, currentTitle, "Audio Paused")
                } else {
                    it.play()
                    updatePlaybackState(PlaybackStateCompat.STATE_PLAYING)
                    showNotification(context, currentTitle, "Audio Playing")
                }
            }
        }

        fun isInitializedWithSource(source: String): Boolean {
          return  currentSource == source && exoPlayer != null
        }

        fun releasePlayer(context: Context) {
            exoPlayer?.release()
            exoPlayer = null
            mediaSession?.release()
            mediaSession = null
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(NOTIFICATION_ID)
        }


    }
}
