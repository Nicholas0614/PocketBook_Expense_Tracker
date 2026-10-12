
package com.example.pocketbook_expensetracker.home

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
import com.example.pocketbook_expensetracker.databinding.FragmentRegisterBinding
import kotlinx.coroutines.launch

class RegisterFragment : Fragment() {

    private lateinit var binding: FragmentRegisterBinding
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnRegister.setOnClickListener {
            val name = binding.etName.text.toString()
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()
            viewModel.register(name, email, password)
        }

        binding.tvLogin.setOnClickListener { findNavController().popBackStack() }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.authState.collect { state ->
                    when (state) {
                        "Loading" -> binding.btnRegister.isEnabled = false
                        "Success" -> {
                            viewModel.clearState()
                            Toast.makeText(requireContext(), "Account created successfully", Toast.LENGTH_SHORT).show()
                            findNavController().popBackStack()
                        }
                        null -> binding.btnRegister.isEnabled = true
                        else -> {
                            binding.btnRegister.isEnabled = true
                            Toast.makeText(requireContext(), state, Toast.LENGTH_LONG).show()
                            viewModel.clearState()
                        }
                    }
                }
            }
        }
    }
}
