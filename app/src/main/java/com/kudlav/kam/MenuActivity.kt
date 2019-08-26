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
            tvEmpty.visibility = View.GONE
            DownloadMenu().execute(restaurantId)
        }
    }

    inner class DownloadMenu: AsyncTask<Int, Int, List<ArrayList<Food>>>() {

        override fun onPreExecute() {
            super.onPreExecute()
            swipeRefreshLayout.isRefreshing = true
        }

        override fun doInBackground(vararg params: Int?): List<ArrayList<Food>> {
            val id: Int? = params[0]

            val result = listOf(
                ArrayList<Food>(), // soup
                ArrayList<Food>(), // main
                ArrayList<Food>() // other
            )

            try {
                Jsoup.connect(getString(R.string.url_menu) + id).get().run {
                    select("#m$id tr").forEach { tr: Element ->

                        // Food type (MAIN/SOUP/OTHER)
                        val td0: Element? = tr.selectFirst("td")
                        var tmpStr: String? = td0?.ownText()
                        val type: FoodType
                        if (tmpStr != null && tmpStr.isNotBlank()) {
                            type = when(tmpStr[0]) {
                                'P' -> FoodType.SOUP
                                'H' -> FoodType.MAIN
                                else -> FoodType.OTHER
                            }
                        }
                        else type = FoodType.OTHER

                        // Foot weight
                        tmpStr = td0?.selectFirst("small")?.text()
                        val weightParts: List<String>? = tmpStr?.split("/")
                        var weight: Int? = null
                        if (weightParts != null && weightParts.size > 1) {
                            try {
                                weight = weightParts[1].trim().toInt()
                            }
                            catch (e: NumberFormatException) {}
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
                        }
                        catch (e: Exception) {}

                        // English name
                        tmpStr = tr.selectFirst(".jjjaz2jjj")?.ownText()
                        val nameEn: String = tmpStr ?: ""

                        // Student price
                        tmpStr = tr.selectFirst(".slcen1")?.ownText()?.replaceFirst(",-","")
                        var priceStudent: Int? = null
                        if (tmpStr != null && tmpStr.isNotBlank()) {
                            try {
                                priceStudent = tmpStr.toInt()
                            }
                            catch (e: NumberFormatException) {}
                        }

                        // Employee price
                        tmpStr = tr.selectFirst(".slcen2")?.ownText()?.replaceFirst(",-","")
                        var priceEmployee: Int? = null
                        if (tmpStr != null && tmpStr.isNotBlank()) {
                            try {
                                priceEmployee = tmpStr.toInt()
                            }
                            catch (e: NumberFormatException) {}
                        }

                        // Other price
                        tmpStr = tr.selectFirst(".slcen3")?.ownText()?.replaceFirst(",-","")
                        var priceOther: Int? = null
                        if (tmpStr != null && tmpStr.isNotBlank()) {
                            try {
                                priceOther = tmpStr.toInt()
                            }
                            catch (e: NumberFormatException) {}
                        }

                        // Create Food object
                        val food = Food(
                            type,
                            weight,
                            nameCz,
                            nameEn,
                            allergens,
                            priceStudent,
                            priceEmployee,
                            priceOther
                        )

                        // Save into array
                        when(type) {
                            FoodType.SOUP -> result[0].add(food)
                            FoodType.MAIN -> result[1].add(food)
                            FoodType.OTHER -> result[2].add(food)
                        }
                    }
                }
            }
            catch (e: Exception) {
                cancel(false)
                return result
            }

            return result
        }

        override fun onPostExecute(result: List<ArrayList<Food>>) {
            super.onPostExecute(result)
            swipeRefreshLayout.isRefreshing = false

            val adapter = SectionedRecyclerViewAdapter()
            if (result[0].isNotEmpty()) adapter.addSection(MenuAdapter(FoodType.SOUP, result[0]))
            if (result[1].isNotEmpty()) adapter.addSection(MenuAdapter(FoodType.MAIN, result[1]))
            if (result[2].isNotEmpty()) adapter.addSection(MenuAdapter(FoodType.OTHER, result[2]))

            if (result[0].isEmpty() && result[1].isEmpty() && result[2].isEmpty()) {
                tvEmpty.visibility = View.VISIBLE
            }
            else {
                menuView.visibility = View.VISIBLE
            }

            menuView.adapter = adapter
        }

        override fun onCancelled(result: List<ArrayList<Food>>) {
            super.onCancelled(result)
            swipeRefreshLayout.isRefreshing = false

            tvEmpty.visibility = View.VISIBLE
            menuView.visibility = View.GONE
        }

    }

}
