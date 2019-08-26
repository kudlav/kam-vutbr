package com.kudlav.kam.data

import android.content.Context
import android.util.JsonReader
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object RestaurantDatabase {

    fun getAllRestaurants(context: Context):ArrayList<Restaurant> {
        context.assets.open("restaurants.json").use { inputStream ->
            JsonReader(inputStream.reader()).use {
                val restaurantType = object : TypeToken<ArrayList<Restaurant>>() {}.type
                return Gson().fromJson(inputStream.reader(), restaurantType)
            }
        }
    }

    fun getRestaurant(context: Context, id: Int): Restaurant? {
        val restaurants: ArrayList<Restaurant> = getAllRestaurants(context)
        return restaurants.find { it.id == id }
    }

}
