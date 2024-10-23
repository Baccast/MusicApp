package com.example.musicapp

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.musicapp.ui.theme.MusicAppTheme
import kotlin.random.Random

val sampleTracks = listOf(
    Track(
        imageURL = "https://www.nationalgeographic.com/animals/mammals/facts/domestic-cat",
        artistName = "Artist 1",
        songName = "Song 1"
    ),
    Track(
        imageURL = "https://via.placeholder.com/150",
        artistName = "Artist 2",
        songName = "Song 2"
    ),
    Track(
        imageURL = "https://via.placeholder.com/150",
        artistName = "Artist 3",
        songName = "Song 3"
    ),
    Track(
        imageURL = "https://via.placeholder.com/150",
        artistName = "Artist 4",
        songName = "Song 4"
    ),
    Track(
        imageURL = "https://via.placeholder.com/150",
        artistName = "Artist 5",
        songName = "Song 5"
    ),
    Track(
        imageURL = "https://via.placeholder.com/150",
        artistName = "Artist 6",
        songName = "Song 6"
    ),
    Track(
        imageURL = "https://via.placeholder.com/150",
        artistName = "Artist 7",
        songName = "Song 7"
    ),
    Track(
        imageURL = "https://via.placeholder.com/150",
        artistName = "Artist 8",
        songName = "Song 8"
    ),
    Track(
        imageURL = "https://via.placeholder.com/150",
        artistName = "Artist 9",
        songName = "Song 9"
    ),
    Track(
        imageURL = "https://via.placeholder.com/150",
        artistName = "Artist 10",
        songName = "Song 10"
    ),
    Track(
        imageURL = "https://via.placeholder.com/150",
        artistName = "Artist 11",
        songName = "Song 11"
    ),
    Track(
        imageURL = "https://via.placeholder.com/150",
        artistName = "Artist 12",
        songName = "Song 12"
    ),
    Track(
        imageURL = "https://via.placeholder.com/150",
        artistName = "Artist 13",
        songName = "Song 13"
    ),
    Track(
        imageURL = "https://via.placeholder.com/150",
        artistName = "Artist 14",
        songName = "Song 14"
    ),
    Track(
        imageURL = "https://via.placeholder.com/150",
        artistName = "Artist 15",
        songName = "Song 15"
    ),
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MusicAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    TrackList(tracks = sampleTracks, modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun TrackList(tracks: List<Track>, modifier: Modifier = Modifier) {

    var tracks by remember {
        mutableStateOf(sampleTracks.toMutableList())
    }
    var trackToDelete by remember {
        mutableStateOf<Track?>(null)
    }
    var showDialog by remember {
        mutableStateOf(false)
    }

    Column(modifier = modifier.fillMaxSize()) {
        Text(
            text = "My Music",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(16.dp)
        )

        LazyColumn(modifier = modifier) {
            items(tracks) { track ->
                TrackItem(
                    track = track,
                    onLongPress = {
                        trackToDelete = track
                        showDialog = true
                    }
                )
            }
        }

        if (showDialog && trackToDelete != null) {
            DeleteTrackDialog(
                track = trackToDelete!!,
                onConfirm = {
                    tracks = tracks.toMutableStateList().apply { remove(trackToDelete) }
                    showDialog = false
                },
                onDismiss = {
                    showDialog = false
                }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TrackItem(track: Track, onLongPress: () -> Unit) {
    val context = LocalContext.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .combinedClickable(
                onClick = {
                    val intent = Intent(context, TrackDetailsActivity::class.java).apply {
                        putExtra("track_name", track.songName)
                        putExtra("artist_name", track.artistName)
                    }
                    context.startActivity(intent)
                },
                onLongClick = onLongPress
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Letter square with artist's first letter and random background color
        LetterSquare(artistName = track.artistName)

        // Spacer to add some space between the letter square and text
        Spacer(modifier = Modifier.width(8.dp))

        // Text displaying the song name and artist name
        Column(modifier = Modifier.weight(1f)) {
            Text(text = track.songName, fontWeight = FontWeight.Bold)
            Text(text = track.artistName)
        }
    }
}


@Composable
fun LetterSquare(artistName: String) {
    // Get the first letter of the artist's name
    val firstLetter = artistName.first().toString()

    // Generate a random background color
    val randomColor = Color(
        red = Random.nextInt(256),
        green = Random.nextInt(256),
        blue = Random.nextInt(256)
    )

    // Box to display the letter with a background color
    Box(
        modifier = Modifier
            .size(40.dp)  // Size of the square
            .background(randomColor, RoundedCornerShape(4.dp)),  // Random background color
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = firstLetter,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White  // You can customize text color
        )
    }
}

@Composable
fun DeleteTrackDialog(track: Track, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text(text = "Delete Track") },
        text = { Text("Are you sure you want to delete ${track.songName} by ${track.artistName}?") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Delete")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Preview
@Composable
fun TrackListPreview() {
    MusicAppTheme {
        TrackList(tracks = sampleTracks)
    }
}