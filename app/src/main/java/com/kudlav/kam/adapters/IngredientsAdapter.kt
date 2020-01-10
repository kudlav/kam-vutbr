package com.kudlav.kam.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kudlav.kam.R
import kotlinx.android.synthetic.main.item_ingredients.view.*

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

    inner class ViewHolder(private val view: View): RecyclerView.ViewHolder(view) {
        fun bind() {
            view.tvIngredient.text = ingredients[adapterPosition]
        }
    }

}
