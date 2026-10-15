
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
import com.example.pocketbook_expensetracker.databinding.FragmentAddExpenseBinding
import com.example.pocketbook_expensetracker.databinding.ItemCategoryBinding
import com.example.pocketbook_expensetracker.util.CustomCategories
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class AddExpenseFragment : Fragment() {

    private lateinit var binding: FragmentAddExpenseBinding
    private val viewModel: AddExpenseViewModel by viewModels()
    private lateinit var categories: MutableList<Pair<String, ItemCategoryBinding>>
    private var selectedDate = LocalDate.now()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentAddExpenseBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        categories = listOf(
            "Food" to binding.categoryFood,
            "Groceries" to binding.categoryGroceries,
            "Transport" to binding.categoryTransport,
            "Bills" to binding.categoryBills,
            "Fun" to binding.categoryFun,
            "Health" to binding.categoryHealth,
            "Shopping" to binding.categoryShopping,
            "New" to binding.categoryNew
        ).toMutableList()

        val icons = listOf("🍜", "🛒", "🚌", "💡", "🎬", "🩺", "🛍", "+")

        categories.forEachIndexed { index, (name, tile) ->
            tile.tvCategoryName.text = name
            tile.tvCategoryIcon.text = icons[index]
            tile.root.setOnClickListener {
                if (name == "New") showAddCategoryDialog()
                else viewModel.setCategory(name)
            }
        }

        loadCustomCategories()

        binding.tvDate.text = selectedDate.format(DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.ENGLISH))
        binding.btnClose.setOnClickListener { findNavController().popBackStack() }
        binding.boxType.setOnClickListener { showTypeDialog() }
        binding.boxDate.setOnClickListener { showDatePicker() }
        binding.boxPayment.setOnClickListener { showPaymentDialog() }
        binding.btnSaveExpense.setOnClickListener { validateForm() }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.selectedCategory.collect { updateCategory(it) } }
                launch { viewModel.selectedType.collect { binding.tvType.text = "$it ⌄" } }
                launch { viewModel.selectedPayment.collect { binding.tvPayment.text = it } }
                launch {
                    viewModel.saveResult.collect { result ->
                        if (result != null) {
                            viewModel.clearSaveResult()
                            if (result == "Success") {
                                Toast.makeText(requireContext(), "Transaction saved", Toast.LENGTH_SHORT).show()
                                findNavController().popBackStack()
                            } else {
                                binding.btnSaveExpense.isEnabled = true
                                Toast.makeText(requireContext(), result, Toast.LENGTH_SHORT).show()
                            }
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
        val container = binding.categoryContainer

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
        tile.root.setOnClickListener { viewModel.setCategory(name) }

        row.addView(tile.root)
        categories.add(name to tile)
        updateCategory(viewModel.selectedCategory.value)
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
                    viewModel.setCategory(name)
                }
            }.show()
    }

    private fun updateCategory(selected: String) {
        categories.forEach { (name, tile) ->
            tile.root.setBackgroundResource(
                if (name == selected) R.drawable.bg_category_selected
                else R.drawable.bg_expense_field
            )
        }
    }

    private fun showTypeDialog() {
        val types = arrayOf("Expense", "Income")
        AlertDialog.Builder(requireContext()).setTitle("Select type").setItems(types) { _, which ->
            viewModel.setType(types[which])
        }.show()
    }

    private fun showPaymentDialog() {
        val payments = arrayOf("Cash", "Card", "Bank")
        AlertDialog.Builder(requireContext()).setTitle("Paid with").setItems(payments) { _, which ->
            viewModel.setPayment(payments[which])
        }.show()
    }

    private fun showDatePicker() {
        DatePickerDialog(requireContext(), { _, year, month, day ->
            selectedDate = LocalDate.of(year, month + 1, day)
            binding.tvDate.text = selectedDate.format(DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.ENGLISH))
        }, selectedDate.year, selectedDate.monthValue - 1, selectedDate.dayOfMonth).show()
    }

    private fun validateForm() {
        val name = binding.etNote.text.toString().trim()
        val amount = binding.etAmount.text.toString()
        val date = selectedDate.toString()

        if (name.isBlank()) {
            binding.etNote.error = "Please enter a note"
            return
        }

        val error = viewModel.validateAmount(amount)
        if (error != null) {
            binding.etAmount.error = error
            return
        }

        binding.btnSaveExpense.isEnabled = false
        viewModel.saveTransaction(name, amount, date)
    }
}
