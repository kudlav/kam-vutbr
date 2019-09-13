package com.kudlav.kam.adapters

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.kudlav.kam.AccountActivity
import com.kudlav.kam.R
import com.kudlav.kam.data.Transaction
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionParameters
import io.github.luizgrp.sectionedrecyclerviewadapter.StatelessSection
import kotlinx.android.synthetic.main.header_account.view.*
import kotlinx.android.synthetic.main.item_account.view.*
import java.text.DateFormat
import java.text.SimpleDateFormat

class AccountAdapter(private val data: AccountActivity.Result): StatelessSection(
    SectionParameters.builder()
        .itemResourceId(R.layout.item_account)
        .headerResourceId(R.layout.header_account)
        .build()
    )
{

    override fun getContentItemsTotal(): Int {
        return data.history.size
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

        private val df: DateFormat = SimpleDateFormat("d. M.")

        fun bind(position: Int) {
            val transaction: Transaction = data.history[position]
            view.tvTime.text =
                if (transaction.time != null) df.format(transaction.time)
                else "?. ?."
            view.tvDescription.text = transaction.description
            view.tvAmount.text =
                if (transaction.amount != null) "%,.2f Kč".format(transaction.amount)
                else "? Kč"
        }

    }

    inner class HeaderViewHolder(private val view: View): RecyclerView.ViewHolder(view) {

        fun bind() {
            view.tvBalance.text =
                if (data.balance != null) "%.2f Kč".format(data.balance)
                else view.context.getString(R.string.account_balance_unknown)
        }

    }

}
