package com.kudlav.kam

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import com.kudlav.kam.databinding.ActivityAboutBinding

class AboutActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAboutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAboutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.tvVersion.text = getString(R.string.version, BuildConfig.VERSION_NAME)

        binding.tvVersion.setOnClickListener {
            openWebpage("https://github.com/kudlav/kam-vutbr/releases")
        }

        binding.tvGithub.setOnClickListener {
            openWebpage("https://github.com/kudlav/kam-vutbr")
        }

        binding.tvTranslate.setOnClickListener {
            openWebpage("https://crowdin.com/project/kam-vut")
        }

        binding.tvAuthorKudlav.setOnClickListener {
            openWebpage("https://kudlav.github.io")
        }

        binding.tvPrivacy.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle(R.string.privacy_title)
                .setMessage(R.string.privacy_content)
                .setNegativeButton(R.string.label_ok) { _, _ -> }
                .show()
        }
    }

    private fun openWebpage(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }

}
