package com.kudlav.kam

import android.app.Application
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceManager

class MainApplication: Application() {

    override fun onCreate() {
        super.onCreate()

        val sharedPreferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val theme: String? = sharedPreferences.getString("theme", null)

        AppCompatDelegate.setDefaultNightMode(
            when(theme) {
                "MODE_NIGHT_NO" -> AppCompatDelegate.MODE_NIGHT_NO
                "MODE_NIGHT_YES" -> AppCompatDelegate.MODE_NIGHT_YES
                "MODE_NIGHT_AUTO_BATTERY" -> AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY
                else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            }
        )
    }

}
