package com.myanmarrussian

import android.content.Context
import android.content.SharedPreferences

/**
 * AppState - Equivalent to iOS AppState ObservableObject
 * Manages shared app state including API key and backend URL
 */
object AppState {

    private const val PREFS_NAME = "myanmar_russian_prefs"
    private const val KEY_API_KEY = "apiKey"
    private const val KEY_BACKEND_URL = "backendUrl"
    private const val DEFAULT_BACKEND_URL = "https://ai-agent-app-6dmy.onrender.com"

    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    var apiKey: String
        get() = prefs.getString(KEY_API_KEY, "") ?: ""
        set(value) {
            prefs.edit().putString(KEY_API_KEY, value).apply()
        }

    var backendUrl: String
        get() = prefs.getString(KEY_BACKEND_URL, DEFAULT_BACKEND_URL) ?: DEFAULT_BACKEND_URL
        set(value) {
            prefs.edit().putString(KEY_BACKEND_URL, value).apply()
        }

    val isLoggedIn: Boolean
        get() = apiKey.isNotEmpty()
}
