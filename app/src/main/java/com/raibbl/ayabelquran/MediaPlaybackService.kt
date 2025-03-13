package com.raibbl.ayabelquran

import android.app.*
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.IBinder
import androidx.annotation.OptIn
import androidx.core.app.NotificationCompat
import androidx.media.app.NotificationCompat.MediaStyle
import androidx.media3.common.*
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import com.raibbl.ayabelquran.presentation.MainActivity

class MediaPlaybackService : Service() {

    private var exoPlayer: ExoPlayer? = null
    private var mediaSession: MediaSession? = null
    private var currentSource: String? = null
    private var isPlaying = false //  Track play/pause state

    private val CHANNEL_ID = "ongoing_channel"
    private val NOTIFICATION_ID = 1

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        val title = intent?.getStringExtra("TITLE") ?: "Quran Audio"
        val ayahUrl = intent?.getStringExtra("URL") //  Single verse support
        val playlist = intent?.getStringArrayListExtra("PLAYLIST") //  Multi-verse support

        //  Ensure startForeground() is always called
        startForeground(NOTIFICATION_ID, buildNotification(this, title, "Loading..."))

        when (action) {
            "PLAY" -> {
                if (ayahUrl != null) {
                    if (currentSource == ayahUrl) {
                        //  If the same verse is playing, toggle play/pause instead of restarting
                        playPause()
                    } else {
                        currentSource = ayahUrl
                        initializeMediaPlayer(ayahUrl, title, this) //  Start new playback
                    }
                } else if (!playlist.isNullOrEmpty()) {
                    initializePlaylist(playlist, title, this) //  Full surah playback
                }
            }
            "TOGGLE_PLAY" -> playPause() //  Toggle play/pause
            "STOP" -> stopSelf()
        }

        return START_STICKY
    }

    private fun initializeMediaPlayer(url: String, title: String, context: Context) {
        releasePlayer() // Clear previous player instance

        currentSource = title //  Store the correct title for notifications

        val mediaMetadata = MediaMetadata.Builder().setTitle(title).build()

        exoPlayer = ExoPlayer.Builder(context).build().apply {
            val mediaItem = MediaItem.Builder()
                .setUri(Uri.parse(url))
                .setMediaMetadata(mediaMetadata)
                .build()

            val audioAttributes = AudioAttributes.Builder()
                .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                .setUsage(C.USAGE_MEDIA)
                .build()

            setMediaItem(mediaItem)
            setAudioAttributes(audioAttributes, true)
            prepare()
            play() //  Start playback automatically
            this@MediaPlaybackService.isPlaying = true
        }

        mediaSession = MediaSession.Builder(context, exoPlayer!!).build()
        attachPlayerListener()

        startForeground(NOTIFICATION_ID, buildNotification(context, title, "Playing"))
    }



    //  Initialize a Playlist (Full Surah Playback)
    private fun initializePlaylist(ayahUrls: List<String>, surahTitle: String, context: Context) {
        releasePlayer()

        if (ayahUrls.isEmpty()) {
            stopSelf()
            return
        }

        currentSource = surahTitle //  Store the Surah title

        val mediaItems = ayahUrls.map { url ->
            MediaItem.Builder().setUri(Uri.parse(url))
                .setMediaMetadata(MediaMetadata.Builder().setTitle(surahTitle).build()) //  Set Surah title
                .build()
        }

        exoPlayer = ExoPlayer.Builder(context).build().apply {
            setMediaItems(mediaItems)
            prepare()
            playWhenReady = true
            this@MediaPlaybackService.isPlaying = true
        }

        mediaSession = MediaSession.Builder(context, exoPlayer!!).build()
        attachPlayerListener()

        //  Start foreground with the Surah title
        startForeground(NOTIFICATION_ID, buildNotification(context, surahTitle, "Playing"))
    }



    private fun playPause() {
        exoPlayer?.let {
            if (it.isPlaying) {
                it.pause()
                updateNotification("Paused")
                isPlaying = false
            } else {
                it.play()
                updateNotification("Playing")
                isPlaying = true
            }
        }
    }

    @OptIn(UnstableApi::class)
    private fun buildNotification(context: Context, title: String, description: String): Notification {
        val intent = Intent(context, MainActivity::class.java).apply {
            action = Intent.ACTION_MAIN
            addCategory(Intent.CATEGORY_LAUNCHER)
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            CHANNEL_ID, "Media Playback", NotificationManager.IMPORTANCE_LOW
        )
        notificationManager.createNotificationChannel(channel)

        val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_play)
            .setContentTitle(title)
            .setContentText(description)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setStyle(
                MediaStyle().setMediaSession(mediaSession?.sessionCompatToken)
            )

        //  Add Wear OS Ongoing Activity
        val ongoingActivityStatus = androidx.wear.ongoing.Status.Builder()
            .addTemplate(description)
            .addPart("title", androidx.wear.ongoing.Status.TextPart(title))
            .build()

        val ongoingActivity = androidx.wear.ongoing.OngoingActivity.Builder(
            context, NOTIFICATION_ID, notificationBuilder
        )
            .setStaticIcon(R.drawable.ic_play)
            .setTouchIntent(pendingIntent)
            .setStatus(ongoingActivityStatus)
            .build()

        ongoingActivity.apply(context)

        return notificationBuilder.build()
    }

    private fun updateNotification(description: String) {
        val title = currentSource ?: "Quran Audio" //  Keep the currently playing title
        val notification = buildNotification(this, title, description)
        startForeground(NOTIFICATION_ID, notification)
    }


    private fun releasePlayer() {
        exoPlayer?.release()
        exoPlayer = null
        mediaSession?.release()
        mediaSession = null
        isPlaying = false
    }

    private fun attachPlayerListener() {
        exoPlayer?.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    Player.STATE_READY -> updateNotification("Playing")
                    Player.STATE_ENDED -> {
                        updateNotification("Finished")
                        stopSelf()
                    }
                    Player.STATE_BUFFERING -> updateNotification("Buffering ⏳")
                    Player.STATE_IDLE -> updateNotification("Paused ⏸️")
                }
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                val state = if (isPlaying) "Playing" else "Paused"
                updateNotification(state)
            }
        })
    }

    override fun onDestroy() {
        releasePlayer()
        super.onDestroy()
    }
}
