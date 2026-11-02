package com.example.pocketbook_expensetracker.data.repo

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import com.example.pocketbook_expensetracker.data.local.PocketBookDatabase
import com.example.pocketbook_expensetracker.data.model.Transaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.util.UUID

/** Local, offline repository that preserves the existing ViewModel contract. */
class LocalTransactionRepo private constructor(context: Context) : TransactionRepo {
    private val helper = PocketBookDatabase(context)
    private val updates = MutableStateFlow(readAll())

    override fun getTransactions(): Flow<List<Transaction>> = updates.asStateFlow()

    override suspend fun getTransactionById(id: String): Transaction? = withContext(Dispatchers.IO) {
        helper.readableDatabase.query("transactions", null, "id=?", arrayOf(id), null, null, null)
            .use { cursor -> if (cursor.moveToFirst()) cursor.toTransaction() else null }
    }

    override suspend fun addTransaction(transaction: Transaction) = withContext(Dispatchers.IO) {
        validate(transaction)
        synchronized(helper) {
            val item = transaction.copy(id = UUID.randomUUID().toString())
            helper.writableDatabase.insertOrThrow("transactions", null, values(item))
            updates.value = readAll()
        }
        Unit
    }

    override suspend fun updateTransaction(transaction: Transaction) = withContext(Dispatchers.IO) {
        validate(transaction)
        require(transaction.id.isNotBlank()) { "Missing transaction ID" }
        synchronized(helper) {
            val changed = helper.writableDatabase.update("transactions", values(transaction), "id=?", arrayOf(transaction.id))
            require(changed == 1) { "Transaction not found" }
            updates.value = readAll()
        }
        Unit
    }

    override suspend fun deleteTransaction(id: String) = withContext(Dispatchers.IO) {
        synchronized(helper) {
            helper.writableDatabase.delete("transactions", "id=?", arrayOf(id))
            updates.value = readAll()
        }
        Unit
    }

    private fun validate(item: Transaction) {
        require(item.amount.isFinite() && item.amount > 0) { "Amount must be greater than zero" }
        require(item.type == "Income" || item.type == "Expense") { "Invalid transaction type" }
        require(item.name.isNotBlank()) { "Please enter a description" }
        require(item.date.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))) { "Invalid date" }
    }

    private fun values(item: Transaction) = ContentValues().apply {
        put("id", item.id)
        put("name", item.name)
        put("category", item.category)
        put("amount", item.amount)
        put("type", item.type)
        put("payment_method", item.paymentMethod)
        put("date", item.date)
        put("icon", item.icon)
        put("note", item.note)
    }

    private fun readAll(): List<Transaction> =
        helper.readableDatabase.query("transactions", null, null, null, null, null, "date DESC")
            .use { cursor -> buildList { while (cursor.moveToNext()) add(cursor.toTransaction()) } }

    private fun Cursor.toTransaction() = Transaction(
        id = getString(getColumnIndexOrThrow("id")),
        name = getString(getColumnIndexOrThrow("name")),
        category = getString(getColumnIndexOrThrow("category")),
        amount = getDouble(getColumnIndexOrThrow("amount")),
        type = getString(getColumnIndexOrThrow("type")),
        paymentMethod = getString(getColumnIndexOrThrow("payment_method")),
        date = getString(getColumnIndexOrThrow("date")),
        icon = getString(getColumnIndexOrThrow("icon")),
        note = getString(getColumnIndexOrThrow("note"))
    )

    companion object {
        @Volatile private var instance: LocalTransactionRepo? = null
        fun initialize(context: Context) {
            if (instance == null) synchronized(this) {
                if (instance == null) instance = LocalTransactionRepo(context.applicationContext)
            }
        }
        fun getInstance(): LocalTransactionRepo =
            checkNotNull(instance) { "Initialize LocalTransactionRepo in MainActivity" }
    }
}
