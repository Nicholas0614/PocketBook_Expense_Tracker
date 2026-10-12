package com.example.pocketbook_expensetracker.home

import android.app.DatePickerDialog
import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class AddExpenseFragment : Fragment() {

    private lateinit var binding: FragmentAddExpenseBinding
    private val viewModel: AddExpenseViewModel by viewModels()
    private lateinit var categories: List<Pair<String, ItemCategoryBinding>>
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
        )

        val icons = listOf("🍜", "🛒", "🚌", "💡", "🎬", "🩺", "🛍", "+")

        categories.forEachIndexed { index, (name, tile) ->
            tile.tvCategoryName.text = name
            tile.tvCategoryIcon.text = icons[index]
            tile.root.setOnClickListener { viewModel.setCategory(name) }
        }

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

    private fun updateCategory(selected: String) {
        categories.forEach { (name, tile) ->
            tile.root.setBackgroundResource(if (name == selected) R.drawable.bg_category_selected else R.drawable.bg_expense_field)
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
        val name = binding.etName.text.toString()
        val amount = binding.etAmount.text.toString()
        val note = binding.etNote.text.toString()
        val date = selectedDate.toString()

        if (name.isBlank()) {
            binding.etName.error = "Please enter a transaction name"
            return
        }

        val error = viewModel.validateAmount(amount)
        if (error != null) {
            binding.etAmount.error = error
            return
        }

        binding.btnSaveExpense.isEnabled = false
        viewModel.saveTransaction(name, amount, date, note)
    }

}
