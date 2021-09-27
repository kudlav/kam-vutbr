package com.kudlav.kam

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.kudlav.kam.adapters.AccountAdapter
import com.kudlav.kam.databinding.ActivityAccountBinding
import com.kudlav.kam.models.AccountViewModel
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.launch

@FlowPreview
class AccountActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAccountBinding
    private val viewModel: AccountViewModel by viewModels()
    private val settingsLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            viewModel.updateData()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAccountBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.accountView.layoutManager = LinearLayoutManager(this)

        viewModel.loading.observe(this, { loading ->
            binding.swipeRefreshLayout.isRefreshing = loading
        })

        viewModel.history.observe(this, { list ->
            binding.accountView.visibility = View.VISIBLE
            val adapter = AccountAdapter(list, viewModel.balance.value)
            binding.accountView.adapter = adapter
        })

        lifecycleScope.launch {
            for (msg in viewModel.error) {
                if (msg == "no-card") enterCard()
                else {
                    binding.swipeRefreshLayout.isRefreshing = false
                    Toast.makeText(
                        applicationContext,
                        "${getString(R.string.err_network)}: $msg",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }

        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.updateData()
        }
    }

    private fun enterCard() {
        val missingCardDialog = AlertDialog.Builder(this)
        missingCardDialog.setMessage(getString(R.string.err_missing_card))
            .setPositiveButton(R.string.label_ok) { _, _ ->
                val intent = Intent(this, SettingsActivity::class.java)
                settingsLauncher.launch(intent)
            }
            .show()
    }

}
