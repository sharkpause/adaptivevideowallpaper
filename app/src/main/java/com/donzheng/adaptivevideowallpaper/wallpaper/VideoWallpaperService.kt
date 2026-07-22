package com.donzheng.adaptivevideowallpaper.wallpaper

import android.net.Uri
import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder
import androidx.annotation.OptIn
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.donzheng.adaptivevideowallpaper.R

class VideoWallpaperService : WallpaperService() {

    override fun onCreateEngine(): Engine {
        return VideoWallpaperEngine()
    }

    private inner class VideoWallpaperEngine : Engine() {
        private var player: ExoPlayer? = null

        @OptIn(UnstableApi::class)
        override fun onSurfaceCreated(holder: SurfaceHolder) {
            super.onSurfaceCreated(holder)

            player = ExoPlayer.Builder(this@VideoWallpaperService)
                .build()

            val mediaItem = MediaItem.fromUri(
                Uri.parse(
                    "android.resource://${packageName}/${R.raw.sample}"
                )
            )

            player?.setMediaItem(mediaItem)
            player?.prepare()
            player?.repeatMode = Player.REPEAT_MODE_ONE
            player?.videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING
            player?.play()

            player?.setVideoSurface(holder.surface)
        }

        override fun onSurfaceDestroyed(holder: SurfaceHolder) {
            player?.release()
            player = null

            super.onSurfaceDestroyed(holder)
        }
    }
}