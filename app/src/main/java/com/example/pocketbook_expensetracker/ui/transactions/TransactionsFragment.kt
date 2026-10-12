package com.example.pocketbook_expensetracker.ui.transactions

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pocketbook_expensetracker.R
import com.example.pocketbook_expensetracker.databinding.FragmentTransactionsBinding
import kotlinx.coroutines.launch

class TransactionsFragment : Fragment() {

    private lateinit var binding: FragmentTransactionsBinding
    private val viewModel: TransactionsViewModel by viewModels()
    private lateinit var adapter: TransactionAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentTransactionsBinding.inflate(inflater, container, false)
        return binding.root
    }


    private fun openTab(destination: Int) {
        val navController = findNavController()
        if (navController.currentDestination?.id == destination) return

        navController.navigate(destination, null, androidx.navigation.navOptions {
            popUpTo(R.id.homeFragment) { inclusive = false }
            launchSingleTop = true
        })
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



        adapter = TransactionAdapter(emptyList()) { transaction ->
            val bundle = Bundle().apply { putString("transactionId", transaction.id) }
            findNavController().navigate(R.id.action_transactionsFragment_to_editExpenseFragment, bundle)
        }


        binding.rvTransactions.layoutManager = LinearLayoutManager(requireContext())
        binding.rvTransactions.adapter = adapter

        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.search(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.chipAll.setOnClickListener {
            viewModel.setFilter("All")
            updateChips("All")
        }

        binding.chipExpenses.setOnClickListener {
            viewModel.setFilter("Expenses")
            updateChips("Expenses")
        }

        binding.chipIncome.setOnClickListener {
            viewModel.setFilter("Income")
            updateChips("Income")
        }

        binding.btnSort.setOnClickListener { viewModel.toggleSort() }

        binding.navHome.setOnClickListener {
            findNavController().navigate(R.id.action_transactionsFragment_to_homeFragment)
        }



        binding.navHome.setOnClickListener { openTab(R.id.homeFragment) }
        binding.navReports.setOnClickListener { openTab(R.id.reportsFragment) }





        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.filteredTransactions.collect { transactions ->
                    adapter.updateList(transactions)
                }
            }
        }
    }

    private fun updateChips(filter: String) {
        binding.chipAll.isChecked = filter == "All"
        binding.chipExpenses.isChecked = filter == "Expenses"
        binding.chipIncome.isChecked = filter == "Income"
    }
}
