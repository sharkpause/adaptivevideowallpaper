package com.donzheng.adaptivevideowallpaper.data

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore

val Context.dataStore by preferencesDataStore(
    name = "wallpaper_preferences"
)