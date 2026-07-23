package com.donzheng.adaptivevideowallpaper.wallpaper

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.net.Uri
import android.service.wallpaper.WallpaperService
import android.util.Log
import android.view.SurfaceHolder
import androidx.annotation.OptIn
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.donzheng.adaptivevideowallpaper.ACTION_UPDATE_WALLPAPER
import com.donzheng.adaptivevideowallpaper.R
import com.donzheng.adaptivevideowallpaper.data.WallpaperPreferences
import com.donzheng.adaptivevideowallpaper.data.dataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class VideoWallpaperService : WallpaperService() {

    private var currentEngine: VideoWallpaperEngine? = null

    override fun onCreateEngine(): Engine {
        return VideoWallpaperEngine().also {
            currentEngine = it
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        currentEngine?.updateWallpaper()
    }

    private inner class VideoWallpaperEngine : Engine() {
        private var player: ExoPlayer? = null
        val scope = CoroutineScope(Dispatchers.Main)
        val preferences = WallpaperPreferences(this@VideoWallpaperService.dataStore)

        @OptIn(UnstableApi::class)
        override fun onSurfaceCreated(holder: SurfaceHolder) {
            super.onSurfaceCreated(holder)

            player = ExoPlayer.Builder(this@VideoWallpaperService)
                .build()
            player?.setVideoSurface(holder.surface)

            updateWallpaper()
        }

        @OptIn(UnstableApi::class)
        fun updateWallpaper() {
            scope.launch {
                val isDark =
                    (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
                            Configuration.UI_MODE_NIGHT_YES

                val uri = if (isDark) {
                    preferences.darkVideo.first()
                } else {
                    preferences.lightVideo.first()
                }

                uri?.let {
                    player?.setMediaItem(MediaItem.fromUri(it))
                    player?.repeatMode = Player.REPEAT_MODE_ONE
                    player?.videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING
                    player?.prepare()
                    player?.play()
                }
            }
        }

        override fun onVisibilityChanged(visible: Boolean) {
            super.onVisibilityChanged(visible)

            if (visible) {
                player?.play()
            } else {
                player?.pause()
            }
        }

        override fun onSurfaceDestroyed(holder: SurfaceHolder) {
            if (currentEngine === this) {
                currentEngine = null
            }

            scope.cancel()
            player?.release()
            player = null

            super.onSurfaceDestroyed(holder)
        }
    }
}