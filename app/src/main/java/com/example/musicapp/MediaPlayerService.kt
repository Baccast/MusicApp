package com.example.musicapp

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager

class MediaPlayerService : Service() {

    companion object {
        const val CHANNEL_ID = "running_channel"
        const val ACTION_PLAY = "action_play"
        const val ACTION_PAUSE = "action_pause"
        const val ACTION_NEXT = "action_next"
        const val ACTION_PREVIOUS = "action_previous"
        const val EXTRA_AUDIO_FILE_RES_ID = "audio_file_res_id"  // New extra for audio file ID
        const val ACTION_UPDATE_PROGRESS = "action_update_progress"
        const val EXTRA_TRACK_POSITION = "track_position"
        const val EXTRA_TRACK_DURATION = "track_duration"
    }

    private var mediaPlayer: MediaPlayer? = null
    private var isPlaying = false
    private var currentAudioResId: Int = -1

    override fun onBind(p0: Intent?): IBinder? = null

    private val handler = android.os.Handler()
    private val updateProgressTask = object : Runnable {
        override fun run() {
            mediaPlayer?.let {
                if (isPlaying) {
                    val currentPosition = it.currentPosition
                    val duration = it.duration
                    broadcastProgressUpdate(currentPosition, duration)
                }
            }
            handler.postDelayed(this, 1000)
        }
    }

    private fun broadcastPlayPauseState() {
        val intent = Intent(if (isPlaying) ACTION_PLAY else ACTION_PAUSE)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PLAY -> {
                val audioResId = intent.getIntExtra(EXTRA_AUDIO_FILE_RES_ID, -1)
                if (audioResId != -1 && audioResId != currentAudioResId) {
                    // Play a new track if the audio resource ID is different
                    playAudio(audioResId)
                } else if (!isPlaying) {
                    resumeAudio()
                }
            }
            ACTION_PAUSE -> pauseAudio()
            ACTION_NEXT -> nextTrack()  // Handles skipping to next track
            ACTION_PREVIOUS -> previousTrack()  // Handles going to previous track
        }
        return START_STICKY
    }

    private fun startForegroundService() {
        createNotificationChannel()
        updateNotification()
    }

    private fun playAudio(audioResId: Int) {
        // Release existing MediaPlayer if a new track is selected
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer.create(this, audioResId).apply {
            setOnCompletionListener {
                this@MediaPlayerService.isPlaying = false
                updateNotification()  // Update to show play button
                broadcastPlayPauseState()
            }
        }

        // Start playback
        mediaPlayer?.start()
        isPlaying = true
        handler.post(updateProgressTask)
        currentAudioResId = audioResId
        startForegroundService()
        updateNotification()
        broadcastPlayPauseState()
    }

    private fun pauseAudio() {
        mediaPlayer?.pause()
        isPlaying = false
        handler.removeCallbacks(updateProgressTask)
        updateNotification()
        broadcastPlayPauseState()
    }

    private fun broadcastProgressUpdate(currentPosition: Int, duration: Int) {
        val intent = Intent(ACTION_UPDATE_PROGRESS).apply {
            putExtra(EXTRA_TRACK_POSITION, currentPosition)
            putExtra(EXTRA_TRACK_DURATION, duration)
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    private fun resumeAudio() {
        mediaPlayer?.start()
        isPlaying = true
        updateNotification()
        broadcastPlayPauseState()
    }

    private fun nextTrack() {
        // Play the next track in the list
        val nextAudioResId = currentAudioResId + 1
        if (nextAudioResId < sampleTracks.size) {
            playAudio(nextAudioResId)
        }
    }

    private fun previousTrack() {
        // Play the previous track in the list
        val previousAudioResId = currentAudioResId - 1
        if (previousAudioResId >= 0) {
            playAudio(previousAudioResId)
        }
    }


    private fun updateNotification() {
        // PendingIntent for play/pause toggle
        val playPauseIntent = Intent(this, MediaPlayerService::class.java).apply {
            action = if (isPlaying) ACTION_PAUSE else ACTION_PLAY
        }
        val playPausePendingIntent = PendingIntent.getService(
            this, 0, playPauseIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val playPauseIcon = if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play_arrow
        val playPauseText = if (isPlaying) "Pause" else "Play"

        // Build and start the notification
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Music Player")
            .setContentText("Now Playing: Test Track")
            .setSmallIcon(R.drawable.ic_music_note)
            .addAction(playPauseIcon, playPauseText, playPausePendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        startForeground(1, notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID, "Music Player Service Channel", NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(serviceChannel)
        }
    }

    override fun onDestroy() {
        handler.removeCallbacks(updateProgressTask)
        mediaPlayer?.release()
        mediaPlayer = null
        super.onDestroy()
    }
}