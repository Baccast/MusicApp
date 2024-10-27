package com.example.musicapp

import Track
import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import coil.compose.rememberAsyncImagePainter
import coil.compose.rememberImagePainter
import com.example.musicapp.ui.theme.MusicAppTheme

class TrackDetailsActivity : ComponentActivity() {

    private lateinit var audioManager: AudioManager
    private lateinit var trackList: List<Track>
    private lateinit var mediaPlayer: MediaPlayer
    private var currentIndex by mutableStateOf(0)
    private var isPlaying by mutableStateOf(false)
    private var volumeLevel by mutableStateOf(0.5f)
    private var trackPosition by mutableStateOf(0)
    private var trackDuration by mutableStateOf(0)

    private val playPauseReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                MediaPlayerService.ACTION_PLAY -> isPlaying = true
                MediaPlayerService.ACTION_PAUSE -> isPlaying = false
                MediaPlayerService.ACTION_UPDATE_PROGRESS -> {
                    trackPosition = intent.getIntExtra(MediaPlayerService.EXTRA_TRACK_POSITION, 0)
                    trackDuration = intent.getIntExtra(MediaPlayerService.EXTRA_TRACK_DURATION, 0)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        volumeLevel = fetchVolumeLevel()
        mediaPlayer = MediaPlayer()

        // Retrieve track list and current index from the intent
        trackList = intent.getParcelableArrayListExtra("track_list") ?: emptyList()
        currentIndex = intent.getIntExtra("track_index", 0)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 0
            )
        }

        // Register the play/pause and progress update receiver
        LocalBroadcastManager.getInstance(this).registerReceiver(
            playPauseReceiver,
            IntentFilter().apply {
                addAction(MediaPlayerService.ACTION_PLAY)
                addAction(MediaPlayerService.ACTION_PAUSE)
                addAction(MediaPlayerService.ACTION_UPDATE_PROGRESS)
            }
        )

        // Start MediaPlayerService with the initial track
        Intent(this, MediaPlayerService::class.java).also { intent ->
            intent.action = MediaPlayerService.ACTION_PLAY
            intent.putExtra(MediaPlayerService.EXTRA_AUDIO_FILE_RES_ID, trackList[currentIndex].audioFileResId)
            startService(intent)
        }

        LocalBroadcastManager.getInstance(this).registerReceiver(
            playPauseReceiver,
            IntentFilter(MediaPlayerService.ACTION_PLAY).apply {
                addAction(MediaPlayerService.ACTION_PAUSE)
            }
        )

        updateTrackDetails()  // Set initial track content
        // play the track
        togglePlayPause()
    }

    private fun adjustVolumeLevel(volumeLevel: Float) {
        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val newVolume = (volumeLevel * maxVolume).toInt()
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newVolume, 0)
        this.volumeLevel = volumeLevel
    }

    private fun fetchVolumeLevel(): Float {
        val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        return currentVolume / maxVolume.toFloat()
    }

    private fun togglePlayPause() {
        val action = if (isPlaying) MediaPlayerService.ACTION_PAUSE else MediaPlayerService.ACTION_PLAY
        val audioFileResId = trackList[currentIndex].audioFileResId  // Get audio resource ID of current track
        Intent(this, MediaPlayerService::class.java).also { intent ->
            intent.action = action
            intent.putExtra(MediaPlayerService.EXTRA_AUDIO_FILE_RES_ID, audioFileResId)
            startService(intent)
        }
        isPlaying = !isPlaying
    }

    private fun nextTrack() {
        if (currentIndex < trackList.size - 1) {
            currentIndex++
            updateTrackDetails()

            // Send new track info to MediaPlayerService
            val audioFileResId = trackList[currentIndex].audioFileResId
            Intent(this, MediaPlayerService::class.java).also { intent ->
                intent.action = MediaPlayerService.ACTION_PLAY
                intent.putExtra(MediaPlayerService.EXTRA_AUDIO_FILE_RES_ID, audioFileResId)
                startService(intent)
            }
            isPlaying = true
        }
    }

    private fun previousTrack() {
        if (currentIndex > 0) {
            currentIndex--
            updateTrackDetails()

            // Send new track info to MediaPlayerService
            val audioFileResId = trackList[currentIndex].audioFileResId
            Intent(this, MediaPlayerService::class.java).also { intent ->
                intent.action = MediaPlayerService.ACTION_PLAY
                intent.putExtra(MediaPlayerService.EXTRA_AUDIO_FILE_RES_ID, audioFileResId)
                startService(intent)
            }
            isPlaying = true
        }
    }

    private fun updateTrackDetails() {
        val currentTrack = trackList[currentIndex]
        setContent {
            MusicAppTheme {
                PlaybackScreen(
                    track = currentTrack,
                    isPlaying = isPlaying,
                    onPlayPause = { togglePlayPause() },
                    onNextTrack = { nextTrack() },
                    onPreviousTrack = { previousTrack() },
                    volumeLevel = volumeLevel,
                    onVolumeChange = { adjustVolumeLevel(it) },
                    trackPosition = trackPosition,
                    trackDuration = trackDuration,
                    onSeek = { position ->
                        Intent(this, MediaPlayerService::class.java).also { intent ->
                            intent.action = MediaPlayerService.ACTION_SEEK
                            intent.putExtra(MediaPlayerService.EXTRA_SEEK_POSITION, position)
                            startService(intent)
                        }
                    }
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(playPauseReceiver)
        if (isPlaying) {
            Intent(this, MediaPlayerService::class.java).also { intent ->
                intent.action = MediaPlayerService.ACTION_PAUSE
                startService(intent)
            }
        }
    }
}

@Composable
fun PlaybackScreen(
    track: Track,
    isPlaying: Boolean,
    onPlayPause: () -> Unit,
    onNextTrack: () -> Unit,
    onPreviousTrack: () -> Unit,
    volumeLevel: Float,
    onVolumeChange: (Float) -> Unit,
    trackPosition: Int,
    trackDuration: Int,
    onSeek: (Int) -> Unit
) {
    val elapsedTime = formatTime(trackPosition)
    val remainingTime = formatTime(trackDuration - trackPosition)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Track: ${track.songName}", fontSize = 24.sp)
        Text(text = "Artist: ${track.artistName}", fontSize = 20.sp)

        Spacer(modifier = Modifier.height(48.dp))

        Image(
            painter = rememberAsyncImagePainter(model = track.imageURL),
            contentDescription = "Track Image",
            modifier = Modifier
                .size(200.dp)
                .padding(16.dp),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = elapsedTime, fontSize = 16.sp)
            Text(text = remainingTime, fontSize = 16.sp)
        }

        Slider(
            value = trackPosition.toFloat(),
            onValueChange = { newValue ->
                onSeek(newValue.toInt())
            },
            valueRange = 0f..trackDuration.toFloat(),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "Volume: ${(volumeLevel * 100).toInt()}%", fontSize = 16.sp)
        Slider(
            value = volumeLevel,
            onValueChange = onVolumeChange,
            valueRange = 0f..1f,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            IconButton(onClick = onPreviousTrack) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_skip_previous),
                    contentDescription = "Previous Track",
                    modifier = Modifier.size(48.dp)
                )
            }

            IconButton(onClick = onPlayPause) {
                Icon(
                    painter = painterResource(id = if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play_arrow),
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    modifier = Modifier.size(48.dp)
                )
            }

            IconButton(onClick = onNextTrack) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_skip_next),
                    contentDescription = "Next Track",
                    modifier = Modifier.size(48.dp)
                )
            }
        }
    }
}

fun formatTime(milliseconds: Int): String {
    val minutes = (milliseconds / 1000) / 60
    val seconds = (milliseconds / 1000) % 60
    return String.format("%d:%02d", minutes, seconds)
}