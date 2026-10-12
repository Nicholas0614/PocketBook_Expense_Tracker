
package com.example.pocketbook_expensetracker.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.pocketbook_expensetracker.R
import com.example.pocketbook_expensetracker.data.model.Transaction
import com.example.pocketbook_expensetracker.databinding.FragmentHomeBinding
import com.example.pocketbook_expensetracker.databinding.ItemTransactionBinding
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private val viewModel: HomeViewModel by viewModels()

    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    private fun openTab(destination: Int) {
        findNavController().navigate(destination, null, androidx.navigation.navOptions {
            popUpTo(R.id.homeFragment) { inclusive = false }
            launchSingleTop = true
        })
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        viewModel.loadUserName()


        binding.navActivity.setOnClickListener { openTab(R.id.transactionsFragment) }
        binding.navReports.setOnClickListener { openTab(R.id.reportsFragment) }
        binding.tvSeeAll.setOnClickListener { openTab(R.id.transactionsFragment) }
        binding.btnAdd.setOnClickListener { findNavController().navigate(R.id.action_homeFragment_to_addExpenseFragment) }

        binding.tvAvatar.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Logout") { _, _ ->

                    findNavController().navigate(R.id.loginFragment, null, androidx.navigation.navOptions {
                        popUpTo(R.id.nav_graph) { inclusive = false }
                        launchSingleTop = true
                    })
                    authViewModel.logout()

                }
                .setNegativeButton("Cancel", null)
                .show()
        }




        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.collect { state -> updateDashboard(state) }
                }
                launch {
                    viewModel.userName.collect { name ->
                        binding.tvUsername.text = name.ifBlank { "User" }
                        binding.tvAvatar.text = name.firstOrNull()?.uppercaseChar()?.toString() ?: "U"
                    }
                }
            }
        }

    }

    private fun updateDashboard(state: HomeUiState) {
        binding.tvBalance.text = formatMoney(state.balance)
        binding.tvIncome.text = "+${formatMoney(state.income)}"
        binding.tvExpenses.text = "-${formatMoney(state.expenses)}"
        binding.tvSpentDescription.text = "${formatMoney(state.expenses)} spent"
        binding.tvIncomeDescription.text = "of ${formatMoney(state.income)} income"
        binding.tvBalanceMonth.text = "Balance in ${LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM", Locale.ENGLISH))}"



        val progress = if (state.income > 0) (state.expenses / state.income * 100).toInt().coerceIn(0, 100) else 0
        binding.progressSpending.max = 100
        binding.progressSpending.progress = progress

        val rows = listOf(binding.rowNoodle, binding.rowBus, binding.rowMarket, binding.rowSalary)
        rows.forEachIndexed { index, row ->
            val transaction = state.recentTransactions.getOrNull(index)
            row.root.visibility = if (transaction == null) View.GONE else View.VISIBLE
            if (transaction != null) updateTransactionRow(row, transaction)
        }
    }

    private fun updateTransactionRow(row: ItemTransactionBinding, transaction: Transaction) {
        val isIncome = transaction.type == "Income"
        row.tvIcon.text = transaction.icon
        row.tvName.text = transaction.name
        row.tvDetails.text = "${transaction.category} · ${formatDate(transaction.date)}"
        row.tvAmount.text = "${if (isIncome) "+" else "-"}${formatMoney(transaction.amount)}"
        row.tvAmount.setTextColor(requireContext().getColor(if (isIncome) R.color.pocket_green else R.color.pocket_red))
    }

    private fun formatMoney(amount: Double): String = String.format(Locale.US, "$%,.2f", amount)

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
