package com.example.pocketbook_expensetracker.ui.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pocketbook_expensetracker.data.model.Transaction
import com.example.pocketbook_expensetracker.data.repo.TransactionRepo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class TransactionsViewModel : ViewModel() {

    private val repo = TransactionRepo.getInstance()
    private val searchQuery = MutableStateFlow("")
    private val selectedFilter = MutableStateFlow("All")
    private val ascending = MutableStateFlow(false)

    val filteredTransactions: StateFlow<List<Transaction>> = combine(
        repo.getTransactions(), searchQuery, selectedFilter, ascending
    ) { items, query, filter, isAscending ->
        items.filter {
            (it.name.contains(query, true) || it.note.contains(query, true)) &&
                    (filter == "All" || (filter == "Income" && it.type == "Income") ||
                            (filter == "Expenses" && it.type == "Expense"))
        }.sortedBy { it.date }.let { if (isAscending) it else it.reversed() }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun search(query: String) {
        searchQuery.value = query
    }

    fun setFilter(filter: String) {
        selectedFilter.value = filter
    }

    fun toggleSort() {
        ascending.value = !ascending.value
    }
}
