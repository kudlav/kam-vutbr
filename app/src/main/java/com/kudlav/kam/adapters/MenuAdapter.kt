package com.kudlav.kam.adapters

import android.content.SharedPreferences
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
import com.kudlav.kam.data.Food
import com.kudlav.kam.data.FoodType
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionParameters
import io.github.luizgrp.sectionedrecyclerviewadapter.StatelessSection
import kotlinx.android.synthetic.main.item_menu.view.*
import kotlinx.android.synthetic.main.header_menu.view.*
import android.view.LayoutInflater
import com.kudlav.kam.R
import kotlinx.android.synthetic.main.dialog_food.view.*
import java.lang.Exception

class MenuAdapter(private val section: FoodType, private val itemList: ArrayList<Food>): StatelessSection(
    SectionParameters.builder()
        .itemResourceId(R.layout.item_menu)
        .headerResourceId(R.layout.header_menu)
        .footerResourceId(R.layout.footer_menu)
        .build()
    )
{

    override fun getContentItemsTotal(): Int {
        return itemList.size
    }

    override fun getItemViewHolder(parent: View): RecyclerView.ViewHolder {
        return ItemViewHolder(parent)
    }

    override fun onBindItemViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val itemViewHolder: ItemViewHolder = holder as ItemViewHolder
        itemViewHolder.bind(position)
    }

    override fun getHeaderViewHolder(view: View): RecyclerView.ViewHolder {
        return HeaderViewHolder(view)
    }

    override fun onBindHeaderViewHolder(holder: RecyclerView.ViewHolder?) {
        val headerHolder = holder as HeaderViewHolder
        headerHolder.bind()
    }

    inner class ItemViewHolder(private val view: View): RecyclerView.ViewHolder(view) {

        private val preferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(view.context)
        private val lang: String? = preferences.getString("food_lang", "cs")
        private val priceCategory: String? = preferences.getString("price_category", "cs")
        private val allergenChosen: Set<String>? = preferences.getStringSet("allergens", null)
        private val allergenNames = view.context.resources.getStringArray(R.array.allergen_entries)

        fun bind(position: Int) {

            val data = itemList[position]
            val price: Int? = when(priceCategory) {
                "student" -> data.priceStudent
                "employee" -> data.priceEmployee
                else -> data.priceOther
            }

            view.tvName.text = if (lang == "en") data.nameEn else data.nameCz
            view.tvWeight.text =
                if (data.weight != null) "%d %s".format(data.weight, view.context.getString(R.string.unit_weight))
                else ""
            val allergens: ArrayList<String> = ArrayList()
            try {
                allergenChosen?.forEach { item: String ->
                    if (data.allergens.contains(item.toInt())) {
                        allergens.add(allergenNames[item.toInt()-1])
                    }
                }
            } catch (e: Exception) {
            }
            view.tvAllergens.text = allergens.joinToString()

            view.tvPrice.text =
                if (price != null) "%d %s".format(price, view.context.getString(R.string.currency))
                else "? ${view.context.getString(R.string.currency)}"

            view.setOnClickListener{

                val dialogBuilder = AlertDialog.Builder(view.context)
                    .setPositiveButton(view.context.getString(R.string.btn_close)) {
                            dialog, _ -> dialog.dismiss()
                    }

                val inflater = LayoutInflater.from(this.view.context)
                val dialogView = inflater.inflate(R.layout.dialog_food, null)
                dialogView.lvIngredients.adapter = IngredientsAdapter(view.context, data.ingredients)
                dialogBuilder.setView(dialogView)

                val alert = dialogBuilder.create()
                alert.setTitle(view.tvName.text)
                alert.show()
            }
        }

    }

    inner class HeaderViewHolder(private val view: View): RecyclerView.ViewHolder(view) {

        fun bind() {
            view.tvCategory.text = when(section) {
                FoodType.SOUP -> view.context.getString(R.string.foodtype_soup)
                FoodType.MAIN -> view.context.getString(R.string.foodtype_main)
                else -> view.context.getString(R.string.foodtype_other)
            }
        }

    }

}
