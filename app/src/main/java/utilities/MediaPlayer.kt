import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.media3.session.MediaSession
import androidx.annotation.OptIn
import androidx.core.app.NotificationCompat
import androidx.media.app.NotificationCompat.MediaStyle
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.wear.ongoing.OngoingActivity
import androidx.wear.ongoing.Status
import com.raibbl.ayabelquran.R
import com.raibbl.ayabelquran.presentation.MainActivity

class MediaPlayer {
    companion object {
        private var exoPlayer: ExoPlayer? = null
        private var currentSource: String? = null
        private var mediaSession: MediaSession? = null
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

            // Step 1: Create ExoPlayer instance
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
            }

            // Step 2: Create a MediaSession
            mediaSession = MediaSession.Builder(context, exoPlayer!!).build()

            // Step 3: Attach a Player.Listener to update UI & state
            exoPlayer!!.addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    when (playbackState) {
                        Player.STATE_READY -> {
                            onReady?.invoke()
                            showNotification(context, title, "Playing")
                        }

                        Player.STATE_ENDED -> {
                            onCompletion?.invoke()
                            releasePlayer(context)
                        }

                        Player.STATE_BUFFERING -> {
                            showNotification(context, title, "Buffering ⏳")
                        }

                        Player.STATE_IDLE -> {
                            showNotification(context, title, "Paused ⏸️")
                        }
                    }
                }

                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    val state = if (isPlaying) "Playing" else "Paused"
                    showNotification(context, title, state)
                }
            })
        }


        // Show the notification with OngoingActivity API integration
        @OptIn(UnstableApi::class)
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
                        .setMediaSession(mediaSession?.sessionCompatToken)
                        .setShowActionsInCompactView(0)
                )


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


        fun playPause(context: Context) {
            println("playingfuinction")
            exoPlayer?.let {
                if (it.isPlaying) {
                    it.pause()
                    showNotification(context, "Quran Audio", "Paused")
                } else {
                    it.play()
                    showNotification(context, "Quran Audio", "Playing")
                }
            }
        }

        fun isInitializedWithSource(source: String): Boolean {
            return currentSource == source && exoPlayer != null
        }

        fun releasePlayer(context: Context) {
            exoPlayer?.release()
            exoPlayer = null
            mediaSession?.release()
            mediaSession = null
        }


    }
}
