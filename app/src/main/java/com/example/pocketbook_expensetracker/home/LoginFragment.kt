
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
import com.example.pocketbook_expensetracker.R
import com.example.pocketbook_expensetracker.databinding.FragmentLoginBinding
import kotlinx.coroutines.launch

class LoginFragment : Fragment() {

    private lateinit var binding: FragmentLoginBinding
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (viewModel.isLoggedIn()) {
            findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
            return
        }



        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()
            viewModel.login(email, password)
        }

        binding.tvRegister.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.authState.collect { state ->
                    when (state) {
                        "Loading" -> binding.btnLogin.isEnabled = false
                        "Success" -> {
                            viewModel.clearState()
                            findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
                        }
                        null -> binding.btnLogin.isEnabled = true
                        else -> {
                            binding.btnLogin.isEnabled = true
                            Toast.makeText(requireContext(), state, Toast.LENGTH_LONG).show()
                            viewModel.clearState()
                        }
                    }
                }
            }
        }
    }
}
