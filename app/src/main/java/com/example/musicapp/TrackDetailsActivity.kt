package com.example.musicapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.musicapp.ui.theme.MusicAppTheme

class TrackDetailsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Receive the data passed through the Intent
        val trackName = intent.getStringExtra("track_name")
        val artistName = intent.getStringExtra("artist_name")

        setContent {
            MusicAppTheme {
                // Display track details
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Text(text = "Track: $trackName", fontSize = 24.sp)
                    Text(text = "Artist: $artistName", fontSize = 20.sp)
                }
            }
        }
    }
}
