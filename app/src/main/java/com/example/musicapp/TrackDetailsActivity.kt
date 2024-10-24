package com.example.musicapp

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
import coil.compose.rememberAsyncImagePainter
import coil.compose.rememberImagePainter
import com.example.musicapp.ui.theme.MusicAppTheme

class TrackDetailsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Receive the data passed through the Intent
        val trackName = intent.getStringExtra("track_name")
        val artistName = intent.getStringExtra("artist_name")
        val trackImageURL = intent.getStringExtra("track_image_url")

        setContent {
            MusicAppTheme {
                // Display track details and playback controls
                PlaybackScreen(trackName = trackName, artistName = artistName, trackImageURL = trackImageURL)
            }
        }
    }
}

@Composable
fun PlaybackScreen(trackName: String?, artistName: String?, trackImageURL: String?) {
    var isPlaying by remember { mutableStateOf(false) }
    var trackPosition by remember { mutableStateOf(0f) }
    var volumeLevel by remember { mutableStateOf(0.5f) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ){
        Text(text = "Track: $trackName", fontSize = 24.sp)
        Text(text = "Artist: $artistName", fontSize = 20.sp)

        Spacer(modifier = Modifier.height(48.dp))
        
        Image(
            painter = rememberAsyncImagePainter(model = trackImageURL),
            contentDescription = "Track Image",
            modifier = Modifier
                .size(200.dp)
                .padding(16.dp),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Simulate seek bar for track position
        Text(text = "Track Position: ${trackPosition.toInt()}%", fontSize = 16.sp)
        Slider(
            value = trackPosition,
            onValueChange = { trackPosition = it },
            valueRange = 0f..100f,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Simulate volume control
        Text(text = "Volume: ${(volumeLevel * 100).toInt()}%", fontSize = 16.sp)
        Slider(
            value = volumeLevel,
            onValueChange = { volumeLevel = it },
            valueRange = 0f..1f,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            IconButton(onClick = { /* Simulate previous track action */ }) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_skip_previous),  // Previous icon
                    contentDescription = "Previous Track",
                    modifier = Modifier.size(48.dp)
                )
            }

            IconButton(onClick = { isPlaying = !isPlaying }) {
                Icon(
                    painter = painterResource(id = if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play_arrow),
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    modifier = Modifier.size(48.dp)
                )
            }

            IconButton(onClick = { /* Simulate next track action */ }) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_skip_next),  // Next icon
                    contentDescription = "Next Track",
                    modifier = Modifier.size(48.dp)
                )
            }
        }
    }

    Text(text = if (isPlaying) "Playing" else "Paused", fontSize = 18.sp)
}