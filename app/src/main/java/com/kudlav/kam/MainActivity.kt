package com.kudlav.kam

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.kudlav.kam.adapters.RestaurantAdapter
import com.kudlav.kam.data.Restaurant
import com.kudlav.kam.data.RestaurantDatabase
import kotlinx.android.synthetic.main.activity_main.*
import org.jsoup.Jsoup
import org.jsoup.select.Elements

class MainActivity : AppCompatActivity() {

    private var restaurantList = ArrayList<Restaurant>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        loadData()

        restaurantView.layoutManager = LinearLayoutManager(this)
        restaurantView.adapter = RestaurantAdapter(restaurantList)

        swipeRefreshLayout.setOnRefreshListener {
            DownloadOpeningHours().execute(restaurantList)
        }
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            loadData()
            restaurantView.adapter?.notifyDataSetChanged()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.app_navigation, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        return when (item.itemId) {
            R.id.account -> {
                startActivity(Intent(this, AccountActivity::class.java))
                true
            }
            R.id.settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                true
            }
            R.id.about -> {
                startActivity(Intent(this, AboutActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun loadData() {
        val restaurantsUnsorted = RestaurantDatabase.getAllRestaurants(applicationContext)

        val sharedPreferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val favorites: String = sharedPreferences.getString("favorite", "") ?: ""
        val favoritesList: List<String> = favorites.split(',')
        restaurantList.clear()

        restaurantsUnsorted.forEach {restaurant: Restaurant ->
            if (favoritesList.contains(restaurant.id.toString())) {
                restaurant.favorite = true
                restaurantList.add(0, restaurant) // Push on the top
            }
            else {
                restaurant.favorite = false
                restaurantList.add(restaurant) // Push to the end
            }
        }

        DownloadOpeningHours().execute(restaurantList)
    }


    inner class DownloadOpeningHours: AsyncTask<ArrayList<Restaurant>, Int, Result>() {

        override fun onPreExecute() {
            super.onPreExecute()
            swipeRefreshLayout.isRefreshing = true
        }

        override fun doInBackground(vararg params: ArrayList<Restaurant>): Result {
            var restaurantList: ArrayList<Restaurant> = params[0]

            try {
                Jsoup.connect(getString(R.string.url_restaurants)).get().run {
                    val tr: Elements = select("#sa2 > .ntab > tbody > tr")
                    for (i: Int in 1 until tr.size) {
                        val td: Elements = tr[i].getElementsByTag("td")
                        if (td.size < 5) continue

                        val id: Int? = td[0].getElementsByTag("a")
                            .firstOrNull()
                            ?.attr("href")
                            ?.replaceFirst("?p=menu&provoz=","")
                            ?.toIntOrNull()

                        val state: Char? = td[1].text()
                            .toCharArray()
                            .firstOrNull()

                        // Update restaurant
                        val restaurant: Restaurant? = restaurantList.find {
                                restaurant -> restaurant.id == id
                        }
                        if (restaurant != null) {
                            restaurant.state = state ?: '?'
                        }
                    }
                }
            }
            catch (e: Exception) {
                cancel(false)
                return Result(ArrayList(), "Communication error")
            }

            return Result(restaurantList, null)
        }

        override fun onPostExecute(result: Result) {
            super.onPostExecute(result)
            swipeRefreshLayout.isRefreshing = false

            if (result.error == null) {
                restaurantView.adapter = RestaurantAdapter(restaurantList)
            }
            else {
                Toast.makeText(applicationContext, result.error, Toast.LENGTH_LONG).show()
            }
        }

        override fun onCancelled(result: Result) {
            super.onCancelled(result)
            swipeRefreshLayout.isRefreshing = false
            Toast.makeText(applicationContext, result.error, Toast.LENGTH_LONG).show()
        }

    }


    data class Result(
        val restaurants: ArrayList<Restaurant>,
        val error: String?
    )

}
