package com.myanmarrussian

import android.app.Application

class MyanmarRussianApp : Application() {
    override fun onCreate() {
        super.onCreate()
        AppState.init(this)
    }
}
