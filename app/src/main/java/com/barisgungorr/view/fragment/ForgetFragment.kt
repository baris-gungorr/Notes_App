package com.barisgungorr.view.fragment

import android.os.Bundle
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.barisgungorr.notesapp.R
import com.barisgungorr.notesapp.databinding.FragmentForgetBinding
import com.google.firebase.auth.FirebaseAuth


class ForgetFragment : Fragment() {

    private lateinit var binding: FragmentForgetBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var navController: NavController

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentForgetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initVariables()
        initViews()
    }

    private fun initVariables() {
        auth = FirebaseAuth.getInstance()
        navController = Navigation.findNavController(requireView())
    }

    private fun initViews() {
        binding.tvGoBack.setOnClickListener {
            navController.navigate(R.id.action_forget_Fragment_to_sign_in_Fragment)
        }

        binding.btnResetPassword.setOnClickListener {
            binding.progressBar.visibility = View.VISIBLE
            val mail: String = binding.emailText.text.toString().trim()
            if (mail.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(mail).matches()) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.fragment_forgot_enter_email_address),
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                auth.sendPasswordResetEmail(mail)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(
                                requireContext(),
                                getString(R.string.fragment_forgot_reset_mail_sent),
                                Toast.LENGTH_SHORT
                            ).show()
                            navController.navigate(R.id.action_forget_Fragment_to_sign_in_Fragment)
                        } else {
                            Toast.makeText(
                                requireContext(),
                                getString(R.string.fragment_forgot_invalid_email_address),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        binding.progressBar.visibility = View.INVISIBLE
                    }
            }
        }
    }
}

