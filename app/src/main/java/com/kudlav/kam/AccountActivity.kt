package com.kudlav.kam

import android.app.AlertDialog
import android.content.Intent
import android.content.SharedPreferences
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.kudlav.kam.adapters.AccountAdapter
import com.kudlav.kam.data.Transaction
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter
import kotlinx.android.synthetic.main.activity_account.*
import org.jsoup.Jsoup
import org.jsoup.select.Elements
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class AccountActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account)

        accountView.layoutManager = LinearLayoutManager(this)

        val sharedPreferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val cardNo: String? = sharedPreferences.getString("card_number", null)

        if (cardNo != null) {
            DownloadInfo().execute(cardNo)

            swipeRefreshLayout.setOnRefreshListener {
                DownloadInfo().execute(cardNo)
            }
        }
        else {
            val missingCardDialog = AlertDialog.Builder(this)
            missingCardDialog.setMessage(getString(R.string.err_missing_card))
                .setPositiveButton(R.string.label_ok) { dialog, id ->
                    val intent = Intent(this, SettingsActivity::class.java)
                    startActivity(intent)
                }
                .show()
        }
    }

    inner class DownloadInfo: AsyncTask<String, Int?, Result>() {

        override fun onPreExecute() {
            super.onPreExecute()
            swipeRefreshLayout.isRefreshing = true
        }

        override fun doInBackground(vararg params: String): Result {

            var error: String? = null
            var balance: Double? = null
            val history: ArrayList<Transaction> = ArrayList()

            try {
                Jsoup.connect(getString(R.string.url_account))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .requestBody("aktu=1&submit=Hledej&tisky=tisky&cis=${params[0]}")
                    .post()
                    .run {

                        val tables: Elements = select("#sa2 form table")

                        if (tables.size > 0) {

                            // Account balance
                            balance = tables[0].selectFirst("tr:nth-child(4) td:nth-child(2)")
                                ?.ownText()
                                ?.replaceFirst(" Kč", "")
                                ?.replaceFirst(",", ".")
                                ?.toDoubleOrNull()

                            if (tables.size == 2) {
                                // Transaction history
                                val df = SimpleDateFormat("d. M. yyy kk:mm:ss")
                                val tr: Elements = tables[1].getElementsByTag("tr")

                                for (i: Int in 1 until tr.size) {
                                    val td: Elements = tr[i].getElementsByTag("td")
                                    if (td.size != 3) continue

                                    var time: Date? = null
                                    try {
                                        time = df.parse(td[0].ownText())
                                    } catch (e: ParseException) {
                                    }

                                    val desc: String = td[1].ownText()

                                    val amount: Double? = td[2].ownText()
                                        .replaceFirst(",", ".")
                                        .toDoubleOrNull()

                                    history.add(Transaction(time, desc, amount))
                                }
                            }
                        } else { // Missing data

                        }
                    }
            }
            catch (e: Exception) {
                cancel(false)
            }
            finally {
                return Result(error, balance, history.reversed())
            }
        }

        override fun onPostExecute(result: Result) {
            super.onPostExecute(result)
            accountView.visibility = View.VISIBLE
            swipeRefreshLayout.isRefreshing = false

            val adapter = SectionedRecyclerViewAdapter()
            adapter.addSection(AccountAdapter(result))

            accountView.adapter = adapter
        }

        override fun onCancelled(result: Result) {
            super.onCancelled(result)
            swipeRefreshLayout.isRefreshing = false
            Toast.makeText(applicationContext, getString(R.string.account_err_network), Toast.LENGTH_LONG).show()
        }

    }

    data class Result(
        val error: String?,
        val balance: Double?,
        val history: List<Transaction>
    )
}
