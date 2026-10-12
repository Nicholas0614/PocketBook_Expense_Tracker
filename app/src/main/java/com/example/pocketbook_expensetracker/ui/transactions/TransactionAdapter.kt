
package com.example.pocketbook_expensetracker.ui.transactions

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.pocketbook_expensetracker.R
import com.example.pocketbook_expensetracker.data.model.Transaction
import com.example.pocketbook_expensetracker.databinding.ItemDateHeaderBinding
import com.example.pocketbook_expensetracker.databinding.ItemTransactionBinding
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class TransactionAdapter(private var items: List<Transaction>,
                         private val onItemClick: (Transaction) -> Unit = {}
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_TRANSACTION = 1
    }

    inner class HeaderViewHolder(val binding: ItemDateHeaderBinding) : RecyclerView.ViewHolder(binding.root)
    inner class TransactionViewHolder(val binding: ItemTransactionBinding) : RecyclerView.ViewHolder(binding.root)

    private var displayItems = buildList(items)

    override fun getItemViewType(position: Int): Int {
        return if (displayItems[position] is TransactionListItem.Header) TYPE_HEADER else TYPE_TRANSACTION
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == TYPE_HEADER) {
            HeaderViewHolder(ItemDateHeaderBinding.inflate(inflater, parent, false))
        } else {
            TransactionViewHolder(ItemTransactionBinding.inflate(inflater, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = displayItems[position]) {
            is TransactionListItem.Header -> {
                val binding = (holder as HeaderViewHolder).binding
                binding.tvDate.text = formatDate(item.date)
                binding.tvDailyTotal.text = String.format(Locale.US, "%s$%,.2f", if (item.total >= 0) "+" else "-", kotlin.math.abs(item.total))
            }
            is TransactionListItem.Entry -> {
                val binding = (holder as TransactionViewHolder).binding
                val transaction = item.transaction

                holder.itemView.setOnClickListener {
                    onItemClick(transaction)
                }

                val isIncome = transaction.type == "Income"
                binding.tvIcon.text = transaction.icon
                binding.tvName.text = transaction.name
                binding.tvDetails.text = "${transaction.category} · ${transaction.paymentMethod}"
                binding.tvAmount.text = String.format(Locale.US, "%s$%,.2f", if (isIncome) "+" else "-", transaction.amount)
                binding.tvAmount.setTextColor(holder.itemView.context.getColor(if (isIncome) R.color.pocket_green else R.color.pocket_red))
            }
        }
    }

    override fun getItemCount(): Int = displayItems.size

    fun updateList(newItems: List<Transaction>) {
        items = newItems
        displayItems = buildList(items)
        notifyDataSetChanged()
    }

    private fun buildList(transactions: List<Transaction>): List<TransactionListItem> {
        val result = mutableListOf<TransactionListItem>()
        val groups = linkedMapOf<String, MutableList<Transaction>>()
        transactions.forEach { groups.getOrPut(it.date) { mutableListOf() }.add(it) }

        groups.forEach { (date, group) ->
            val total = group.sumOf { if (it.type == "Income") it.amount else -it.amount }
            result.add(TransactionListItem.Header(date, total))
            group.forEach { result.add(TransactionListItem.Entry(it)) }
        }
        return result
    }

    @SuppressLint("NewApi")
    private fun formatDate(date: String): String {
        return try {
            val parsed = LocalDate.parse(date)
            when (parsed) {
                LocalDate.now() -> "Today"
                LocalDate.now().minusDays(1) -> "Yesterday"
                else -> parsed.format(DateTimeFormatter.ofPattern("MMM d", Locale.ENGLISH))
            }
        } catch (e: Exception) {
            date
        }
    }
}
