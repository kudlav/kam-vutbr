package com.kudlav.kam.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kudlav.kam.R
import com.kudlav.kam.data.Transaction
import com.kudlav.kam.databinding.HeaderAccountBinding
import com.kudlav.kam.databinding.ItemAccountBinding
import java.text.SimpleDateFormat

class AccountAdapter(private val data: List<Transaction>, private var balance: Double?):
    RecyclerView.Adapter<RecyclerView.ViewHolder>()
{

    companion object {
        const val HEADER = 0
        const val ITEM = 1
    }

    override fun getItemViewType(position: Int): Int {
        return when (position) {
            0 -> HEADER
            else -> ITEM
        }
    }

    override fun getItemCount(): Int {
        return data.size + 1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            HEADER -> {
                val binding = HeaderAccountBinding.inflate(inflater, parent, false)
                HeaderViewHolder(binding)
            } else -> {
                val binding = ItemAccountBinding.inflate(inflater, parent, false)
                ItemViewHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (getItemViewType(position) == HEADER)
            (holder as HeaderViewHolder).bind()
        else
            (holder as ItemViewHolder).bind(position)
    }

    inner class ItemViewHolder(private val binding: ItemAccountBinding):
        RecyclerView.ViewHolder(binding.root)
    {
        fun bind(position: Int) {
            val currency = binding.root.context.getString(R.string.currency)
            val transaction: Transaction = data[position-1]
            binding.tvTime.text =
                if (transaction.time != null) SimpleDateFormat("d. M.").format(transaction.time)
                else "?. ?."
            binding.tvDescription.text = transaction.description
            binding.tvAmount.text =
                if (transaction.amount != null) {
                    if (transaction.amount % 1 == 0.0)
                        "%.0f %s".format(transaction.amount, currency)
                    else
                        "%.2f %s".format(transaction.amount, currency)
                }
                else "? $currency"
        }
    }

    inner class HeaderViewHolder(private val binding: HeaderAccountBinding):
        RecyclerView.ViewHolder(binding.root)
    {
        fun bind() {
            val currency = binding.root.context.getString(R.string.currency)
            val balance = balance
            binding.tvBalance.text =
                if (balance != null) {
                    if (balance % 1 == 0.0)
                        "%.0f %s".format(balance, currency)
                    else
                        "%.2f %s".format(balance, currency)
                }
                else binding.root.context.getString(R.string.account_balance_unknown)
        }
    }

}
