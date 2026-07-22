package com.donzheng.adaptivevideowallpaper.ui

import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import androidx.annotation.OptIn
import com.donzheng.adaptivevideowallpaper.wallpaper.VideoWallpaperService
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.rememberCoroutineScope
import com.donzheng.adaptivevideowallpaper.data.WallpaperPreferences
import com.donzheng.adaptivevideowallpaper.data.dataStore
import kotlinx.coroutines.launch

@OptIn(UnstableApi::class)
@Composable
fun WallpaperPickerScreen() {
    val context = LocalContext.current
    val preferences = WallpaperPreferences(context.dataStore)
    val scope = rememberCoroutineScope()

    var selectedLightVideoUri by remember {
        mutableStateOf<Uri?>(null)
    }

    var selectedDarkVideoUri by remember {
        mutableStateOf<Uri?>(null)
    }

    val lightPlayer = remember {
        ExoPlayer.Builder(context).build()
    }
    val darkPlayer = remember {
        ExoPlayer.Builder(context).build()
    }

    val scrollState = rememberScrollState()

    DisposableEffect(Unit) {
        onDispose {
            lightPlayer.release()
        }
    }

    LaunchedEffect(Unit) {
        preferences.lightVideo.collect { uri ->
            selectedLightVideoUri = uri
        }
    }
    LaunchedEffect(selectedLightVideoUri) {
        selectedLightVideoUri?.let { uri ->
            lightPlayer.setMediaItem(
                MediaItem.fromUri(uri)
            )
            lightPlayer.repeatMode = Player.REPEAT_MODE_ONE
            lightPlayer.volume = 0f

            lightPlayer.prepare()
            lightPlayer.play()
        }

    }

    LaunchedEffect(Unit) {
        preferences.darkVideo.collect { uri ->
            selectedDarkVideoUri = uri
        }
    }
    LaunchedEffect(selectedDarkVideoUri) {
        selectedDarkVideoUri?.let { uri ->
            darkPlayer.setMediaItem(
                MediaItem.fromUri(uri)
            )
            darkPlayer.repeatMode = Player.REPEAT_MODE_ONE
            darkPlayer.volume = 0f

            darkPlayer.prepare()
            darkPlayer.play()
        }

    }

    val lightVideoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            context.contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            scope.launch {
                preferences.setLightVideo(uri)
            }
            selectedLightVideoUri = uri
        }
    }
    val darkVideoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            context.contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            scope.launch {
                preferences.setDarkVideo(uri)
            }
            selectedDarkVideoUri = uri
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(24.dp),

        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            "Adaptive Video Wallpaper",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(Modifier.height(32.dp))

        Button(
            onClick = {
                lightVideoPicker.launch(
                    arrayOf("video/*")
                )
            }
        ) {
            Text("Select Light Video")
        }

        Spacer(Modifier.height(16.dp))

        AndroidView(
            factory = { context ->
                val lightPlayerView = PlayerView(context)

                lightPlayerView.player = lightPlayer
                lightPlayerView.useController = false
                lightPlayerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM

                lightPlayerView
            }
        )

        Button(
            onClick = {
                darkVideoPicker.launch(
                    arrayOf("video/*")
                )
            }
        ) {
            Text("Select Dark Video")
        }

        Spacer(Modifier.height(16.dp))

        AndroidView(
            factory = { context ->
                val darkPlayerView = PlayerView(context)

                darkPlayerView.player = darkPlayer
                darkPlayerView.useController = false
                darkPlayerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM

                darkPlayerView
            }
        )

        Button(
            onClick = {
                val intent = Intent(
                    WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER
                ).apply {
                    putExtra(
                        WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                        ComponentName(
                            context,
                            VideoWallpaperService::class.java
                        )
                    )
                }

                context.startActivity(intent)
            }
        ) {
            Text("Preview & Apply")
        }
    }
}