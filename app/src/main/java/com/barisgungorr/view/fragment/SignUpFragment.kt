package com.barisgungorr.view.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import androidx.navigation.Navigation.findNavController
import com.barisgungorr.data.InformationModel
import com.barisgungorr.notesapp.R
import com.barisgungorr.notesapp.databinding.FragmentSignUpBinding
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


class SignUpFragment : Fragment() {

    private lateinit var binding: FragmentSignUpBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseDatabase: FirebaseDatabase


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSignUpBinding.inflate(inflater, container, false)
        firebaseAuth = FirebaseAuth.getInstance()
        firebaseDatabase = FirebaseDatabase.getInstance()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        setupNavigationListeners()
        setupSignUpButton()
    }

    private fun setupNavigationListeners() {
        binding.tvSignIn.setOnClickListener {
            findNavController(it).navigate(R.id.action_sign_Up_Fragment_to_sign_in_Fragment)
        }
    }

    private fun setupSignUpButton() {
        binding.signup.setOnClickListener {
            val mail = binding.signUpEmail.text.toString().trim()
            val password = binding.signUpPassword.text.toString().trim()
            val rePassword = binding.signUpRePassword.text.toString().trim()


            if (mail.isEmpty() || password.isEmpty() || rePassword.isEmpty()) {
                Toast.makeText(
                    requireContext(),
                    R.string.fragment_sign_up_fill_in_fields,
                    Toast.LENGTH_SHORT
                ).show()
            } else if (password.length < 6) {
                Toast.makeText(
                    requireContext(),
                    R.string.fragment_sign_up_password_length,
                    Toast.LENGTH_SHORT
                ).show()
            } else if (password != rePassword) {
                Toast.makeText(
                    requireContext(),
                    R.string.fragment_sign_up_password_not_match,
                    Toast.LENGTH_SHORT
                ).show()
            } else if (!isEmailValid(mail)) {
                Toast.makeText(
                    requireContext(),
                    R.string.fragment_sign_up_invalid_email,
                    Toast.LENGTH_LONG
                ).show()
            } else {
                binding.progressBar.visibility = View.VISIBLE
                performSignUp(mail, password)
            }
        }
    }

    private fun isEmailValid(email: String): Boolean {
        val pattern = Regex("^\\w+([\\.-]?\\w+)*@\\w+([\\.-]?\\w+)*(\\.\\w{2,3})+\$")
        return pattern.matches(email)
    }

    private fun performSignUp(mail: String, password: String) {
        lifecycleScope.launch {
            try {
                firebaseAuth.createUserWithEmailAndPassword(mail, password).await()
                findNavController(requireView())
                    .navigate(R.id.action_sign_Up_Fragment_to_sign_in_Fragment)
            } catch (exception: Exception) {
                val message = when (exception) {
                    is FirebaseAuthWeakPasswordException -> R.string.fragment_sign_up_password_length
                    is FirebaseAuthInvalidCredentialsException -> R.string.fragment_sign_up_incorrect_email
                    is FirebaseAuthUserCollisionException -> R.string.fragment_sign_up_invalid_email
                    is FirebaseNetworkException -> R.string.fragment_sign_up_network_error
                    else -> R.string.fragment_sign_up_registration_error
                }
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            }
        }
    }
}
