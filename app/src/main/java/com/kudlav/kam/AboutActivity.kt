package com.kudlav.kam

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.kudlav.kam.databinding.ActivityAboutBinding

class AboutActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAboutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAboutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.tvVersion.text = getString(R.string.version, BuildConfig.VERSION_NAME)
    }

}
