package com.kudlav.kam

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.EditTextPreference
import androidx.preference.ListPreference
import androidx.preference.Preference.OnPreferenceChangeListener
import androidx.preference.PreferenceFragmentCompat

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settings, SettingsFragment())
            .commit()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)

            // Validate card number
            val cardNumber: EditTextPreference? = preferenceScreen.findPreference("card_number")

            cardNumber?.onPreferenceChangeListener =  OnPreferenceChangeListener { _, value ->
                val newValue: String = Regex("\\D").replace(value.toString(), "")
                cardNumber?.text = newValue
                false
            }

            val theme: ListPreference? = preferenceScreen.findPreference("theme")

            theme?.onPreferenceChangeListener = OnPreferenceChangeListener { _, value ->
                AppCompatDelegate.setDefaultNightMode(
                    when(value) {
                        "MODE_NIGHT_NO" -> AppCompatDelegate.MODE_NIGHT_NO
                        "MODE_NIGHT_YES" -> AppCompatDelegate.MODE_NIGHT_YES
                        "MODE_NIGHT_AUTO_BATTERY" -> AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY
                        else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                    }
                )
                true
            }
        }
    }
}
