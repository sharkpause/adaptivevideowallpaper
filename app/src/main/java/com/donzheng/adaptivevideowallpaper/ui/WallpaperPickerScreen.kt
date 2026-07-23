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
import com.donzheng.adaptivevideowallpaper.ACTION_UPDATE_WALLPAPER
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
        selectedLightVideoUri?.let(lightPlayer::playVideo)
    }

    LaunchedEffect(Unit) {
        preferences.darkVideo.collect { uri ->
            selectedDarkVideoUri = uri
        }
    }
    LaunchedEffect(selectedDarkVideoUri) {
        selectedDarkVideoUri?.let(darkPlayer::playVideo)
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
            .padding(
                horizontal = 24.dp,
                vertical = 48.dp
            ),

        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Adaptive Video Wallpaper",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(Modifier.height(64.dp))

        VideoPickerSection(
            "Select Light Wallpaper",
            lightPlayer,
            onPickVideo = {
                lightVideoPicker.launch(arrayOf("video/*"))
            }
        )

        Spacer(Modifier.height(32.dp))

        VideoPickerSection(
            "Select Dark Wallpaper",
            darkPlayer,
            onPickVideo = {
                darkVideoPicker.launch(arrayOf("video/*"))
            }
        )

        Spacer(Modifier.height(32.dp))

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

private fun ExoPlayer.playVideo(uri: Uri) {
    setMediaItem(MediaItem.fromUri(uri))
    repeatMode = Player.REPEAT_MODE_ONE
    volume = 0f
    prepare()
    play()
}

@OptIn(UnstableApi::class)
@Composable
private fun VideoPreview(
    player: ExoPlayer
) {
    AndroidView(
        factory = { context ->
            PlayerView(context).apply {
                this.player = player
                useController = false
                resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
            }
        }
    )
}

@Composable
private fun VideoPickerSection(
    title: String,
    player: ExoPlayer,
    onPickVideo: () -> Unit
) {
    Button(
        onClick = onPickVideo
    ) {
        Text(title)
    }

    Spacer(Modifier.height(8.dp))

    VideoPreview(player)
}