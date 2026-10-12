
package com.example.pocketbook_expensetracker.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pocketbook_expensetracker.data.model.Transaction
import com.example.pocketbook_expensetracker.data.repo.TransactionRepo
import com.example.pocketbook_expensetracker.util.TransactionValidator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AddExpenseViewModel : ViewModel() {

    private val repo = TransactionRepo.getInstance()

    private val _selectedCategory = MutableStateFlow("Food")
    val selectedCategory: StateFlow<String> = _selectedCategory

    private val _selectedType = MutableStateFlow("Expense")
    val selectedType: StateFlow<String> = _selectedType

    private val _selectedPayment = MutableStateFlow("Cash")
    val selectedPayment: StateFlow<String> = _selectedPayment

    private val _saveResult = MutableStateFlow<String?>(null)
    val saveResult: StateFlow<String?> = _saveResult

    fun setCategory(category: String) {
        _selectedCategory.value = category
    }

    fun setType(type: String) {
        _selectedType.value = type
    }

    fun setPayment(payment: String) {
        _selectedPayment.value = payment
    }

    fun validateAmount(amount: String): String? = TransactionValidator.validateAmount(amount)

    fun saveTransaction(name: String, amount: String, date: String, note: String) {
        if (name.isBlank()) {
            _saveResult.value = "Please enter a transaction name"
            return
        }

        val error = validateAmount(amount)
        if (error != null) {
            _saveResult.value = error
            return
        }

        val category = _selectedCategory.value
        val icon = when (category) {
            "Food" -> "🍜"
            "Groceries" -> "🛒"
            "Transport" -> "🚌"
            "Bills" -> "💡"
            "Fun" -> "🎬"
            "Health" -> "🩺"
            "Shopping" -> "🛍"
            else -> "💰"
        }

        val transaction = Transaction(
            name = name.trim(),
            amount = amount.toDouble(),
            category = category,
            type = _selectedType.value,
            paymentMethod = _selectedPayment.value,
            date = date,
            note = note.trim(),
            icon = icon
        )

        viewModelScope.launch {
            try {
                repo.addTransaction(transaction)
                _saveResult.value = "Success"
            } catch (e: Exception) {
                _saveResult.value = e.message ?: "Failed to save transaction"
            }
        }
    }

    fun clearSaveResult() {
        _saveResult.value = null
    }
}
