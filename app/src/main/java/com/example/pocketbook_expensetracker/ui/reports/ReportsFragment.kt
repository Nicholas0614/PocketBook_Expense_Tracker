package com.example.pocketbook_expensetracker.ui.reports

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.pocketbook_expensetracker.R
import com.example.pocketbook_expensetracker.databinding.FragmentReportsBinding
import com.example.pocketbook_expensetracker.databinding.ItemCategoryReportBinding
import kotlinx.coroutines.launch
import java.util.Locale

class ReportsFragment : Fragment() {

    private lateinit var binding: FragmentReportsBinding
    private val viewModel: ReportsViewModel by viewModels()

    private val colors = listOf(
        Color.rgb(12, 144, 147),
        Color.rgb(243, 172, 70),
        Color.rgb(91, 119, 208),
        Color.rgb(233, 54, 85),
        Color.rgb(142, 111, 211),
        Color.rgb(70, 185, 150)
    )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentReportsBinding.inflate(inflater, container, false)
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


        binding.navHome.setOnClickListener { openTab(R.id.homeFragment) }
        binding.navActivity.setOnClickListener { openTab(R.id.transactionsFragment) }


        binding.tabWeek.setOnClickListener { viewModel.setPeriod("Week") }
        binding.tabMonth.setOnClickListener { viewModel.setPeriod("Month") }
        binding.tabYear.setOnClickListener { viewModel.setPeriod("Year") }


        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    updateTabs(state.period)
                    binding.tvTotalSpending.text = String.format(Locale.US, "$%,.2f", state.totalSpending)
                    binding.tvChartCenter.text = "${state.period}\nSpending"
                    updateCategories(state.categories)

                    binding.donutChart.setData(state.categories.map { it.amount })

                    binding.spendingTrendChart.setData(state.trendValues, state.trendLabels)


                }
            }
        }
    }

    private fun updateTabs(period: String) {
        val tabs = listOf("Week" to binding.tabWeek, "Month" to binding.tabMonth, "Year" to binding.tabYear)
        tabs.forEach { (name, tab) ->
            val selected = name == period
            tab.setBackgroundResource(if (selected) R.drawable.bg_teal_square else 0)
            tab.setTextColor(requireContext().getColor(if (selected) android.R.color.white else R.color.pocket_grey))
        }
    }

    private fun updateCategories(categories: List<CategoryReport>) {
        binding.categoryContainer.removeAllViews()

        if (categories.isEmpty()) {
            val emptyText = android.widget.TextView(requireContext())
            emptyText.text = "No expenses for this period"
            emptyText.gravity = android.view.Gravity.CENTER
            emptyText.setPadding(0, 20, 0, 20)
            binding.categoryContainer.addView(emptyText)
            return
        }

        categories.forEachIndexed { index, category ->
            val row = ItemCategoryReportBinding.inflate(layoutInflater, binding.categoryContainer, false)
            row.tvCategory.text = category.category
            row.tvPercentage.text = String.format(Locale.US, "%.1f%%", category.percentage)
            row.tvAmount.text = String.format(Locale.US, "$%,.2f", category.amount)
            row.viewColor.setBackgroundColor(colors[index % colors.size])
            binding.categoryContainer.addView(row.root)
        }
    }
}
