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
import com.kudlav.kam.databinding.ItemRestaurantBinding

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

        private val binding = ItemRestaurantBinding.bind(view)

        fun bind() {
            val data = itemList[adapterPosition]
            binding.tvName.text = data.name
            when (data.state) {
                'z' -> {
                    binding.tvOpen.text = view.context.getString(R.string.restaurant_closed)
                    binding.tvOpen.setTextColor(Color.parseColor("#B40020"))
                    binding.tvOpen.setTypeface(null, Typeface.NORMAL)
                    binding.ivOpen.setColorFilter(Color.parseColor("#B40020"))
                }
                'x' -> {
                    binding.tvOpen.text = view.context.getString(R.string.restaurant_interrupt)
                    binding.tvOpen.setTextColor(Color.GRAY)
                    binding.tvOpen.setTypeface(null, Typeface.NORMAL)
                    binding.ivOpen.setColorFilter(Color.GRAY)
                }
                'n' -> {
                    binding.tvOpen.text = view.context.getString(R.string.restaurant_unavailable)
                    binding.tvOpen.setTextColor(Color.parseColor("#f9a825"))
                    binding.tvOpen.setTypeface(null, Typeface.NORMAL)
                    binding.ivOpen.setColorFilter(Color.parseColor("#f9a825"))
                }
                'm' -> {
                    binding.tvOpen.text = view.context.getString(R.string.restaurant_available)
                    binding.tvOpen.setTextColor(Color.parseColor("#4caf50"))
                    binding.tvOpen.setTypeface(null, Typeface.BOLD)
                    binding.ivOpen.setColorFilter(Color.parseColor("#4caf50"))
                }
                else -> {
                    binding.tvOpen.text = data.state.toString()
                    binding.tvOpen.setTypeface(null, Typeface.NORMAL)
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
                    .into(binding.ivRestaurantImg)
            }
            else {
                Glide.with(view)
                    .load(data.pictureUrl)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.mipmap.ic_launcher_round)
                    .into(binding.ivRestaurantImg)
            }
        }
    }
}
