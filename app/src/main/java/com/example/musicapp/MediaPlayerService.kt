package com.example.musicapp

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat

class MediaPlayerService : Service() {

    companion object {
        const val CHANNEL_ID = "running_channel"
        const val ACTION_PLAY = "action_play"
        const val ACTION_PAUSE = "action_pause"
        const val ACTION_NEXT = "action_next"
        const val ACTION_PREVIOUS = "action_previous"
    }

    private var isPlaying = false  // To track play/pause state

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            Actions.START.toString() -> {
                Log.d(TAG, "Service started")
                startForegroundService()  // Start foreground with media controls
            }
            Actions.STOP.toString() -> {
                Log.d(TAG, "Service stopped")
                stopSelf()  // Stop the service when STOP action is received
            }
            ACTION_PLAY -> {
                Log.d(TAG, "Play action received")
                isPlaying = true
                // Handle play logic here
                updateNotification()  // Update notification to show Pause button
            }
            ACTION_PAUSE -> {
                Log.d(TAG, "Pause action received")
                isPlaying = false
                // Handle pause logic here
                updateNotification()  // Update notification to show Play button
            }
            ACTION_NEXT -> {
                Log.d(TAG, "Next action received")
                // Handle next track logic here
            }
            ACTION_PREVIOUS -> {
                Log.d(TAG, "Previous action received")
                // Handle previous track logic here
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun startForegroundService() {
        createNotificationChannel()
        updateNotification()  // Create initial notification with play/pause button
    }

    private fun updateNotification() {
        // Create pending intents for next, previous, and play/pause toggle
        val nextIntent = Intent(this, MediaPlayerService::class.java).apply {
            action = ACTION_NEXT
        }
        val nextPendingIntent = PendingIntent.getService(
            this,
            0,
            nextIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val previousIntent = Intent(this, MediaPlayerService::class.java).apply {
            action = ACTION_PREVIOUS
        }
        val previousPendingIntent = PendingIntent.getService(
            this,
            0,
            previousIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Toggle between play and pause based on the isPlaying state
        val playPauseIntent = Intent(this, MediaPlayerService::class.java).apply {
            action = if (isPlaying) ACTION_PAUSE else ACTION_PLAY
        }
        val playPausePendingIntent = PendingIntent.getService(
            this,
            0,
            playPauseIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val playPauseIcon = if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play_arrow
        val playPauseText = if (isPlaying) "Pause" else "Play"

        // Build the notification with a single play/pause button
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Music Player")
            .setContentText("Now Playing")
            .setSmallIcon(R.drawable.ic_music_note)
            .addAction(R.drawable.ic_skip_previous, "Previous", previousPendingIntent)
            .addAction(playPauseIcon, playPauseText, playPausePendingIntent)  // Single button toggles play/pause
            .addAction(R.drawable.ic_skip_next, "Next", nextPendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        // Update the foreground service notification
        startForeground(1, notification)
        Log.d(TAG, "Notification updated with $playPauseText button")
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Music Player Service Channel",
                NotificationManager.IMPORTANCE_LOW
            )

            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(serviceChannel)
        }
    }

    enum class Actions {
        START,
        STOP
    }
}
