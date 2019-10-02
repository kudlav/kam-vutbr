package com.kudlav.kam

import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
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
        setContentView(R.layout.activity_menu)

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
                        val regex = Regex("a\\[(\\d+)\\][^\"]*\".*?(?=<br\\/><br\\/>)<br\\/><br\\/>(.*?(?=<br\\/>\"))")
                        regex.findAll(selectFirst("#sa2 script").html()).forEach {match: MatchResult ->
                            val index: Int? = match.groupValues[1].toIntOrNull()
                            if (index != null) {
                                ingredientsList.add(match.groupValues[2].split("<br/>"))
                            }
                        }

                        menu.forEachIndexed { index: Int, tr: Element ->

                            // Food type (MAIN/SOUP/OTHER)
                            val td0: Element? = tr.selectFirst("td")
                            var tmpStr: String? = td0?.ownText()
                            val type: FoodType
                            if (tmpStr != null && tmpStr.isNotBlank()) {
                                type = when (tmpStr[0]) {
                                    'P' -> FoodType.SOUP
                                    'H' -> FoodType.MAIN
                                    else -> FoodType.OTHER
                                }
                            } else type = FoodType.OTHER

                            // Foot weight
                            tmpStr = td0?.selectFirst("small")?.text()
                            val weightParts: List<String>? = tmpStr?.split("/")
                            var weight: Int? = null
                            if (weightParts != null && weightParts.size > 1) {
                                try {
                                    weight = weightParts[1].trim().toInt()
                                } catch (e: NumberFormatException) {
                                }
                            }

                            // Czech name
                            tmpStr = tr.selectFirst(".jjjaz1jjj")?.ownText()
                            val nameCz: String = tmpStr ?: ""

                            // Allergens
                            tmpStr = tr.selectFirst(".jjjaz1jjj small")?.text()?.trim()
                            val allergensParts: List<String>? = tmpStr?.split(',')
                            val allergens = ArrayList<Int>()
                            try {
                                allergensParts?.forEach { allergen: String ->
                                    allergens.add(allergen.toInt())
                                }
                            } catch (e: Exception) { }

                            // English name
                            tmpStr = tr.selectFirst(".jjjaz2jjj")?.ownText()
                            val nameEn: String = tmpStr ?: ""

                            // Student price
                            tmpStr = tr.selectFirst(".slcen1")?.ownText()?.replaceFirst(",-", "")
                            var priceStudent: Int? = null
                            if (tmpStr != null && tmpStr.isNotBlank()) {
                                try {
                                    priceStudent = tmpStr.toInt()
                                } catch (e: NumberFormatException) {
                                }
                            }

                            // Employee price
                            tmpStr = tr.selectFirst(".slcen2")?.ownText()?.replaceFirst(",-", "")
                            var priceEmployee: Int? = null
                            if (tmpStr != null && tmpStr.isNotBlank()) {
                                try {
                                    priceEmployee = tmpStr.toInt()
                                } catch (e: NumberFormatException) {
                                }
                            }

                            // Other price
                            tmpStr = tr.selectFirst(".slcen3")?.ownText()?.replaceFirst(",-", "")
                            var priceOther: Int? = null
                            if (tmpStr != null && tmpStr.isNotBlank()) {
                                try {
                                    priceOther = tmpStr.toInt()
                                } catch (e: NumberFormatException) {
                                }
                            }

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
                result.error = e.message
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
