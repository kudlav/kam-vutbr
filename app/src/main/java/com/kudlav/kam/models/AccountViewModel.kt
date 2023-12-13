package com.kudlav.kam.models

import android.app.Application
import android.content.SharedPreferences
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.preference.PreferenceManager
import com.kudlav.kam.R
import com.kudlav.kam.data.Transaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.sendBlocking
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.select.Elements
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date

class AccountViewModel(application: Application) : AndroidViewModel(application) {

    val loading: MutableLiveData<Boolean> = MutableLiveData(false)
    val balance: MutableLiveData<Double?> = MutableLiveData()
    val history: MutableLiveData<List<Transaction>> = MutableLiveData()
    val error: Channel<String> = Channel(Channel.CONFLATED)

    init {
        updateData()
    }

    // todo MULTIPLE UPDATE DATA CALL
    fun updateData() {
        loading.value = true
        val pref: SharedPreferences =
            PreferenceManager.getDefaultSharedPreferences(getApplication())
        val cardNo: String? = pref.getString("card_number", null)

        if (!cardNo.isNullOrEmpty()) {
            viewModelScope.launch {
                val transactions = fetchData(cardNo, true)
                history.postValue(transactions)
                transactions.addAll(fetchData(cardNo, false))
                history.postValue(transactions)
                loading.postValue(false)
            }
        } else {
            error.sendBlocking("no-card")
            loading.value = false
        }
    }

    private suspend fun fetchData(cardNo: String, currentMonth: Boolean): MutableList<Transaction> {
        val transactions = mutableListOf<Transaction>()
        withContext(Dispatchers.IO) {
            val aktu = if (currentMonth) 1 else 0
            try {
                Jsoup.connect(getApplication<Application>().getString(R.string.url_account))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .requestBody("aktu=$aktu&submit=Hledej&tisky=tisky&cis=$cardNo")
                    .post()
                    .run {

                        val tables: Elements = select("#sa2 form table")

                        if (tables.size > 0) {

                            // Account balance
                            balance.postValue(
                                tables[0].selectFirst("tr:nth-child(4) td:nth-child(2)")
                                    ?.ownText()
                                    ?.replaceFirst(" Kč", "")
                                    ?.replaceFirst(",", ".")
                                    ?.toDoubleOrNull()
                            )

                            if (tables.size > 1) {

                                // Transaction history
                                val df = SimpleDateFormat("dd.MM.yy kk:mm")
                                val transactionRows: Elements = tables[1].getElementsByTag("tr")

                                for (i: Int in 1 until transactionRows.size) {
                                    val td: Elements = transactionRows[i].getElementsByTag("td")
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

                                    transactions.add(Transaction(time, desc, amount))
                                }
                            }
                        } else { // Missing data
                            error.send(getApplication<Application>().getString(R.string.err_nodata))
                        }
                    }
            } catch (e: Exception) {
                error.send(e.localizedMessage ?: "unknown exception")
            }
        }
        transactions.reverse()
        return transactions
    }
}
