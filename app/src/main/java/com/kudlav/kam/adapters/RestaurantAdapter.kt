package com.kudlav.kam.adapters

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.kudlav.kam.MenuActivity
import com.kudlav.kam.R
import com.kudlav.kam.data.Restaurant
import kotlinx.android.synthetic.main.item_restaurant.view.*

class RestaurantAdapter(private val itemList: List<Restaurant>): RecyclerView.Adapter<RestaurantAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater: LayoutInflater = LayoutInflater.from(parent.context)
        return ViewHolder(layoutInflater.inflate(R.layout.item_restaurant, parent, false))
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind()
    }

    inner class ViewHolder(private val view: View): RecyclerView.ViewHolder(view) {

        fun bind() {
            val data = itemList[adapterPosition]
            view.tvName.text = data.name
            when (data.state) {
                'z' -> {
                    view.tvOpen.text = view.context.getString(R.string.restaurant_closed)
                    view.tvOpen.setTextColor(Color.parseColor("#B40020"))
                    view.tvOpen.setTypeface(null, Typeface.NORMAL)
                    view.ivOpen.setColorFilter(Color.parseColor("#B40020"))
                }
                'x' -> {
                    view.tvOpen.text = view.context.getString(R.string.restaurant_interrupt)
                    view.tvOpen.setTextColor(Color.GRAY)
                    view.tvOpen.setTypeface(null, Typeface.NORMAL)
                    view.ivOpen.setColorFilter(Color.GRAY)
                }
                'n' -> {
                    view.tvOpen.text = view.context.getString(R.string.restaurant_unavailable)
                    view.tvOpen.setTextColor(Color.parseColor("#f9a825"))
                    view.tvOpen.setTypeface(null, Typeface.NORMAL)
                    view.ivOpen.setColorFilter(Color.parseColor("#f9a825"))
                }
                'm' -> {
                    view.tvOpen.text = view.context.getString(R.string.restaurant_available)
                    view.tvOpen.setTextColor(Color.parseColor("#4caf50"))
                    view.tvOpen.setTypeface(null, Typeface.BOLD)
                    view.ivOpen.setColorFilter(Color.parseColor("#4caf50"))
                }
                else -> {
                    view.tvOpen.text = data.state.toString()
                    view.tvOpen.setTypeface(null, Typeface.NORMAL)
                }
            }

            view.setOnClickListener {
                val intent = Intent(view.context, MenuActivity::class.java).apply {
                    putExtra("id", data.id)
                }
                (view.context as Activity).startActivityForResult(intent, 0)
            }

            if (data.favorite) {
                Glide.with(view)
                    .load("")
                    .placeholder(R.drawable.favorite)
                    .into(view.ivRestaurantImg)
            }
            else {
                Glide.with(view)
                    .load(data.pictureUrl)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.mipmap.ic_launcher_round)
                    .into(view.ivRestaurantImg)
            }
        }
    }
}
