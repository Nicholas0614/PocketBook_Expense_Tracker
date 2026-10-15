
package com.example.pocketbook_expensetracker.home

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.pocketbook_expensetracker.R
import com.example.pocketbook_expensetracker.data.model.Transaction
import com.example.pocketbook_expensetracker.databinding.FragmentEditExpenseBinding
import com.example.pocketbook_expensetracker.databinding.ItemCategoryBinding
import com.example.pocketbook_expensetracker.util.CustomCategories
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class EditExpenseFragment : Fragment() {

    private lateinit var binding: FragmentEditExpenseBinding
    private val viewModel: EditExpenseViewModel by viewModels()
    private lateinit var categories: MutableList<Pair<String, ItemCategoryBinding>>
    private var currentTransaction: Transaction? = null
    private var selectedCategory = "Food"
    private var selectedType = "Expense"
    private var selectedPayment = "Cash"
    private var selectedDate = LocalDate.now()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentEditExpenseBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val form = binding.editForm

        form.tvTitle.text = "Edit expense"
        form.tvType.text = selectedType
        form.tvPayment.text = selectedPayment
        form.btnSaveExpense.visibility = View.GONE
        updateDate()

        categories = listOf(
            "Food" to form.categoryFood,
            "Groceries" to form.categoryGroceries,
            "Transport" to form.categoryTransport,
            "Bills" to form.categoryBills,
            "Fun" to form.categoryFun,
            "Health" to form.categoryHealth,
            "Shopping" to form.categoryShopping,
            "New" to form.categoryNew
        ).toMutableList()

        val icons = listOf("🍜", "🛒", "🚌", "💡", "🎬", "🩺", "🛍", "+")

        categories.forEachIndexed { index, (name, tile) ->
            tile.tvCategoryName.text = name
            tile.tvCategoryIcon.text = icons[index]
            tile.root.setOnClickListener {
                if (name == "New") showAddCategoryDialog()
                else {
                    selectedCategory = name
                    updateCategory()
                }
            }
        }

        loadCustomCategories()
        updateCategory()

        form.btnClose.setOnClickListener { findNavController().popBackStack() }
        form.boxType.setOnClickListener { showTypeDialog() }
        form.boxDate.setOnClickListener { showDatePicker() }
        form.boxPayment.setOnClickListener { showPaymentDialog() }
        binding.btnUpdateExpense.setOnClickListener { updateExpense() }
        binding.btnDeleteExpense.setOnClickListener { showDeleteDialog() }

        val id = requireArguments().getString("transactionId").orEmpty()
        viewModel.loadTransaction(id)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.transaction.collect { transaction ->
                        if (transaction != null && currentTransaction?.id != transaction.id) {
                            populateForm(transaction)
                        }
                    }
                }
                launch {
                    viewModel.error.collect { error ->
                        if (error != null) {
                            Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show()
                            viewModel.clearError()
                        }
                    }
                }
            }
        }
    }

    private fun loadCustomCategories() {
        CustomCategories.get(requireContext()).forEach { addCategoryTile(it) }
    }

    private fun addCategoryTile(name: String) {
        if (categories.any { it.first == name }) return

        val customCount = categories.size - 8
        val container = binding.editForm.categoryContainer

        val row = if (customCount % 4 == 0) LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            val margin = (8 * resources.displayMetrics.density).toInt()
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = margin }
            container.addView(this)
        } else container.getChildAt(container.childCount - 1) as LinearLayout

        val tile = ItemCategoryBinding.inflate(layoutInflater, row, false)
        tile.root.layoutParams = LinearLayout.LayoutParams(
            0,
            (64 * resources.displayMetrics.density).toInt(),
            1f
        ).apply {
            if (customCount % 4 != 0) marginStart = (7 * resources.displayMetrics.density).toInt()
        }

        tile.tvCategoryName.text = name
        tile.tvCategoryIcon.text = "🏷"
        tile.root.setOnClickListener {
            selectedCategory = name
            updateCategory()
        }

        row.addView(tile.root)
        categories.add(name to tile)
        updateCategory()
    }

    private fun showAddCategoryDialog() {
        val input = EditText(requireContext()).apply {
            hint = "Category name"
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
            maxLines = 1
        }

        val wrapper = LinearLayout(requireContext()).apply {
            setPadding(48, 8, 48, 0)
            addView(input)
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Add category")
            .setView(wrapper)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Add") { _, _ ->
                val name = CustomCategories.add(requireContext(), input.text.toString())
                if (name == null) {
                    Toast.makeText(requireContext(), "Enter a unique category name (1-20 characters)", Toast.LENGTH_LONG).show()
                } else {
                    addCategoryTile(name)
                    selectedCategory = name
                    updateCategory()
                }
            }.show()
    }

    private fun updateCategory() {
        categories.forEach { (name, tile) ->
            tile.root.setBackgroundResource(
                if (name == selectedCategory) R.drawable.bg_category_selected
                else R.drawable.bg_expense_field
            )
        }
    }

    private fun updateDate() {
        binding.editForm.tvDate.text = selectedDate.format(DateTimeFormatter.ofPattern("MMM d, yyyy"))
    }

    private fun showTypeDialog() {
        val types = arrayOf("Expense", "Income")
        AlertDialog.Builder(requireContext()).setTitle("Select type").setItems(types) { _, index ->
            selectedType = types[index]
            binding.editForm.tvType.text = selectedType
        }.show()
    }

    private fun showPaymentDialog() {
        val payments = arrayOf("Cash", "Card", "Bank")
        AlertDialog.Builder(requireContext()).setTitle("Paid with").setItems(payments) { _, index ->
            selectedPayment = payments[index]
            binding.editForm.tvPayment.text = selectedPayment
        }.show()
    }

    private fun showDatePicker() {
        DatePickerDialog(requireContext(), { _, year, month, day ->
            selectedDate = LocalDate.of(year, month + 1, day)
            updateDate()
        }, selectedDate.year, selectedDate.monthValue - 1, selectedDate.dayOfMonth).show()
    }

    private fun updateExpense() {
        val transaction = currentTransaction ?: return
        val name = binding.editForm.etNote.text.toString().trim()

        if (name.isBlank()) {
            binding.editForm.etNote.error = "Please enter a note"
            return
        }

        val amountText = binding.editForm.etAmount.text.toString()
        val error = viewModel.validateAmount(amountText)

        if (error != null) {
            binding.editForm.etAmount.error = error
            return
        }

        val icon = when (selectedCategory) {
            "Food" -> "🍜"
            "Groceries" -> "🛒"
            "Transport" -> "🚌"
            "Bills" -> "💡"
            "Fun" -> "🎬"
            "Health" -> "🩺"
            "Shopping" -> "🛍"
            else -> "🏷"
        }

        val updated = transaction.copy(
            name = name,
            amount = amountText.toDouble(),
            category = selectedCategory,
            type = selectedType,
            paymentMethod = selectedPayment,
            date = selectedDate.toString(),
            note = name,
            icon = icon
        )

        viewModel.updateTransaction(updated) {
            if (!isAdded) return@updateTransaction
            Toast.makeText(requireContext(), "Transaction updated", Toast.LENGTH_SHORT).show()
            findNavController().popBackStack()
        }
    }

    private fun showDeleteDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete expense")
            .setMessage("Are you sure you want to delete this transaction?")
            .setPositiveButton("Delete") { _, _ ->
                val id = currentTransaction?.id ?: return@setPositiveButton
                viewModel.deleteTransaction(id) {
                    if (!isAdded) return@deleteTransaction
                    Toast.makeText(requireContext(), "Transaction deleted", Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun populateForm(transaction: Transaction) {
        currentTransaction = transaction
        selectedCategory = transaction.category

        if (categories.none { it.first == selectedCategory }) {
            addCategoryTile(selectedCategory)
        }

        selectedType = transaction.type
        selectedPayment = transaction.paymentMethod
        selectedDate = runCatching { LocalDate.parse(transaction.date) }.getOrDefault(LocalDate.now())

        binding.editForm.tvTitle.text = "Edit expense"
        binding.editForm.etAmount.setText(transaction.amount.toString())
        binding.editForm.etNote.setText(transaction.name.ifBlank { transaction.note })
        binding.editForm.tvType.text = selectedType
        binding.editForm.tvPayment.text = selectedPayment
        binding.editForm.btnSaveExpense.visibility = View.GONE

        updateDate()
        updateCategory()
    }
}
