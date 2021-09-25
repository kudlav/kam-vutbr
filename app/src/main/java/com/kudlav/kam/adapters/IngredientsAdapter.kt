package com.kudlav.kam.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kudlav.kam.R
import com.kudlav.kam.databinding.ItemIngredientsBinding

class IngredientsAdapter(private val ingredients: List<String>): RecyclerView.Adapter<IngredientsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater: LayoutInflater = LayoutInflater.from(parent.context)
        return ViewHolder(layoutInflater.inflate(R.layout.item_ingredients, parent, false))
    }

    override fun getItemCount(): Int {
        return ingredients.size
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind()
    }

    inner class ViewHolder(view: View): RecyclerView.ViewHolder(view) {

        private val binding = ItemIngredientsBinding.bind(view)

        fun bind() {
            binding.tvIngredient.text = ingredients[adapterPosition]
        }
    }

}
