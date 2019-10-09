package com.kudlav.kam.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.kudlav.kam.R
import kotlinx.android.synthetic.main.item_ingredients.view.*

class IngredientsAdapter(private val context: Context, private val ingredients: List<String>): BaseAdapter() {

    override fun getView(position: Int, view: View?, parent: ViewGroup): View {
        val layoutInflater: LayoutInflater = LayoutInflater.from(parent.context)
        val rowView: View =  layoutInflater.inflate(R.layout.item_ingredients, parent, false)

        rowView.tvIngredient.text = getItem(position)

        return rowView
    }

    override fun getItem(position: Int): String {
        return ingredients[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return ingredients.size
    }

}
