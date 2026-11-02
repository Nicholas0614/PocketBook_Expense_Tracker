package com.example.pocketbook_expensetracker
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.pocketbook_expensetracker.data.repo.LocalTransactionRepo
import com.example.pocketbook_expensetracker.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LocalTransactionRepo.initialize(applicationContext)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}
