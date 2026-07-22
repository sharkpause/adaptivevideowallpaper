package com.donzheng.adaptivevideowallpaper.data

import android.net.Uri
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class WallpaperPreferences(
    private val dataStore: DataStore<Preferences>
) {
    private companion object {
        val LIGHT_VIDEO_URI = stringPreferencesKey("light_video_uri")
        val DARK_VIDEO_URI = stringPreferencesKey("dark_video_uri")
    }

    suspend fun setLightVideo(uri: Uri) {
        dataStore.edit { preferences ->
            preferences[LIGHT_VIDEO_URI] = uri.toString()
        }
    }

    suspend fun setDarkVideo(uri: Uri) {
        dataStore.edit { preferences ->
            preferences[DARK_VIDEO_URI] = uri.toString()
        }
    }

    val lightVideo: Flow<Uri?> =
        dataStore.data.map { preferences ->
            preferences[LIGHT_VIDEO_URI]?.let(Uri::parse)
        }

    val darkVideo: Flow<Uri?> =
        dataStore.data.map { preferences ->
            preferences[DARK_VIDEO_URI]?.let(Uri::parse)
        }
}