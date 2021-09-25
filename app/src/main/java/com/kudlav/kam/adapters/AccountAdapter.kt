package com.kudlav.kam.adapters

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.kudlav.kam.AccountActivity
import com.kudlav.kam.R
import com.kudlav.kam.data.Transaction
import com.kudlav.kam.databinding.HeaderAccountBinding
import com.kudlav.kam.databinding.ItemAccountBinding
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionParameters
import io.github.luizgrp.sectionedrecyclerviewadapter.Section
import java.text.DateFormat
import java.text.SimpleDateFormat

class AccountAdapter(private val data: AccountActivity.Result): Section(
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
        private val binding = ItemAccountBinding.bind(view)

        fun bind(position: Int) {

            val transaction: Transaction = data.history[position]
            binding.tvTime.text =
                if (transaction.time != null) df.format(transaction.time)
                else "?. ?."
            binding.tvDescription.text = transaction.description
            binding.tvAmount.text =
                if (transaction.amount != null) {
                    if (transaction.amount % 1 == 0.0) "%.0f %s".format(transaction.amount, view.context.getString(R.string.currency))
                    else "%.2f %s".format(transaction.amount, view.context.getString(R.string.currency))
                }
                else "? ${view.context.getString(R.string.currency)}"
        }

    }

    inner class HeaderViewHolder(private val view: View): RecyclerView.ViewHolder(view) {

        private val binding = HeaderAccountBinding.bind(view)

        fun bind() {
            val balance: Double? = data.balance
            binding.tvBalance.text =
                if (balance != null) {
                    if (balance % 1 == 0.0) "%.0f %s".format(balance, view.context.getString(R.string.currency))
                    else "%.2f %s".format(balance, view.context.getString(R.string.currency))
                }
                else view.context.getString(R.string.account_balance_unknown)
        }

    }

}
