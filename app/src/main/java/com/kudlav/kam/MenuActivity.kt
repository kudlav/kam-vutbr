package com.kudlav.kam

import android.content.SharedPreferences
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import com.kudlav.kam.data.Restaurant
import com.kudlav.kam.data.RestaurantDatabase
import org.jsoup.Jsoup
import kotlinx.android.synthetic.main.activity_menu.*
import org.jsoup.nodes.Element
import androidx.recyclerview.widget.LinearLayoutManager
import com.kudlav.kam.adapters.MenuAdapter
import com.kudlav.kam.data.Food
import com.kudlav.kam.data.FoodType
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter
import kotlinx.android.synthetic.main.activity_menu.swipeRefreshLayout
import org.jsoup.select.Elements

class MenuActivity : AppCompatActivity() {

    private var restaurantId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        setContentView(R.layout.activity_menu)
        setResult(RESULT_CANCELED, null)

        // load data
        restaurantId = intent.getIntExtra("id", -1)
        val restaurant: Restaurant? = RestaurantDatabase.getRestaurant(applicationContext, restaurantId)
        title = restaurant?.name

        menuView.layoutManager = LinearLayoutManager(this)

        DownloadMenu().execute(restaurantId)

        swipeRefreshLayout.setOnRefreshListener {
            llError.visibility = View.GONE
            DownloadMenu().execute(restaurantId)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.restaurant_navigation, menu)

        val sharedPreferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val favorites: String = sharedPreferences.getString("favorite", "") ?: ""
        if (favorites.split(',').contains(restaurantId.toString())) {
            menu.findItem(R.id.favorite).icon = ContextCompat.getDrawable(this, R.drawable.ic_favorite_full_white)
        }
        else {
            menu.findItem(R.id.favorite).icon = ContextCompat.getDrawable(this, R.drawable.ic_favorite_empty_white)
        }

        return true
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        return when (item.itemId) {
            R.id.favorite -> {
                val sharedPreferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
                val favorites: String = sharedPreferences.getString("favorite", "") ?: ""
                val favoritesList = ArrayList<String>(favorites.split(','))

                if (favoritesList.contains(restaurantId.toString())) {
                    favoritesList.remove(restaurantId.toString())
                    item.icon = ContextCompat.getDrawable(this, R.drawable.ic_favorite_empty_white)
                }
                else {
                    favoritesList.add(restaurantId.toString())
                    item.icon = ContextCompat.getDrawable(this, R.drawable.ic_favorite_full_white)
                }

                sharedPreferences.edit()
                    .putString("favorite", favoritesList.joinToString(","))
                    .apply()

                setResult(RESULT_OK, null)

                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    inner class DownloadMenu: AsyncTask<Int, Int, Result>() {

        override fun onPreExecute() {
            super.onPreExecute()
            swipeRefreshLayout.isRefreshing = true
        }

        override fun doInBackground(vararg params: Int?): Result {
            val id: Int? = params[0]

            val result = Result(ArrayList(), ArrayList(), ArrayList(), null)

            try {
                Jsoup.connect(getString(R.string.url_menu) + id).get().run {
                    val menu: Elements = select("#m$id tr")

                    if (menu.isEmpty()) {
                        result.error = selectFirst("#sa2 > p")?.ownText()
                    }
                    else {

                        val ingredientsList = ArrayList<List<String>>()
                        val regex = Regex("a\\[(\\d+)][^\"]*\"([^\"]*)")
                        regex.findAll(selectFirst("#sa2 script").html()).forEach {match: MatchResult ->
                            if (match.groupValues.size == 3) {
                                val ingredients: List<String> = match.groupValues[2].split("<br/>")
                                ingredientsList.add(ingredients.subList(2, ingredients.size - 1))
                            }
                        }

                        menu.forEachIndexed { index: Int, tr: Element ->

                            // Food type (MAIN/SOUP/OTHER)
                            var tmpStr: String? = tr.selectFirst("td")?.ownText()
                            val type: FoodType = if (tmpStr != null && tmpStr.isNotBlank()) {
                                when (tmpStr[0]) {
                                    'P' -> FoodType.SOUP
                                    'H' -> FoodType.MAIN
                                    else -> FoodType.OTHER
                                }
                            } else FoodType.OTHER

                            // Foot weight
                            tmpStr = tr.getElementsByClass("gram")?.first()?.text()
                            val weightParts: List<String>? = tmpStr?.split("/")
                            val weight: Int? = if (weightParts != null && weightParts.isNotEmpty()) {
                                weightParts.last().trim().toIntOrNull()
                            } else null

                            // Czech name
                            tmpStr = tr.selectFirst(".levyjid")?.ownText()
                            val nameCz: String = tmpStr ?: ""

                            // Allergens
                            tmpStr = tr.selectFirst(".levyjid small")?.text()?.trim()
                            val allergensParts: List<String>? = tmpStr?.split(',')
                            val allergens = ArrayList<Int>()
                            try {
                                allergensParts?.forEach { allergen: String ->
                                    allergens.add(allergen.toInt())
                                }
                            } catch (e: Exception) { }

                            // English name
                            tmpStr = tr.select(".levyjid").getOrNull(1)?.ownText()
                            val nameEn: String = tmpStr ?: ""

                            // Student price
                            tmpStr = tr.selectFirst(".slcen1")?.ownText()?.replaceFirst(",-", "")
                            val priceStudent: Int? = if (tmpStr != null && tmpStr.isNotBlank()) {
                                tmpStr.toIntOrNull()
                            } else null

                            // Employee price
                            tmpStr = tr.selectFirst(".slcen2")?.ownText()?.replaceFirst(",-", "")
                            val priceEmployee: Int? = if (tmpStr != null && tmpStr.isNotBlank()) {
                                tmpStr.toIntOrNull()
                            } else null

                            // Other price
                            tmpStr = tr.selectFirst(".slcen3")?.ownText()?.replaceFirst(",-", "")
                            val priceOther: Int? = if (tmpStr != null && tmpStr.isNotBlank()) {
                                tmpStr.toIntOrNull()
                            } else null

                            // Create Food object
                            val food = Food(
                                type,
                                weight,
                                nameCz,
                                nameEn,
                                allergens,
                                ingredientsList.getOrElse(index) { listOf() },
                                priceStudent,
                                priceEmployee,
                                priceOther
                            )

                            // Save into array
                            when (type) {
                                FoodType.SOUP -> result.soup.add(food)
                                FoodType.MAIN -> result.main.add(food)
                                FoodType.OTHER -> result.other.add(food)
                            }
                        }
                    }
                }
            }
            catch (e: Exception) {
                result.error = e.localizedMessage
                cancel(false)
            }

            return result
        }

        override fun onPostExecute(result: Result) {
            super.onPostExecute(result)
            swipeRefreshLayout.isRefreshing = false

            val adapter = SectionedRecyclerViewAdapter()
            if (result.soup.isNotEmpty()) adapter.addSection(MenuAdapter(FoodType.SOUP, result.soup))
            if (result.main.isNotEmpty()) adapter.addSection(MenuAdapter(FoodType.MAIN, result.main))
            if (result.other.isNotEmpty()) adapter.addSection(MenuAdapter(FoodType.OTHER, result.other))

            if (result.error != null) {
                llError.visibility = View.VISIBLE
                tvError.text = result.error
            }
            else if (result.soup.isEmpty() && result.main.isEmpty() && result.other.isEmpty()) {
                llError.visibility = View.VISIBLE
                tvError.text = getString(R.string.menu_err_nothing)
            }
            else {
                menuView.visibility = View.VISIBLE
            }

            menuView.adapter = adapter
        }

        override fun onCancelled(result: Result) {
            super.onCancelled(result)
            swipeRefreshLayout.isRefreshing = false

            llError.visibility = View.VISIBLE
            tvError.text = result.error
            menuView.visibility = View.GONE
        }

    }

    data class Result(
        val soup: ArrayList<Food>,
        val main: ArrayList<Food>,
        val other: ArrayList<Food>,
        var error: String?
    )

}
