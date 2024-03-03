package com.barisgungorr.view.fragment

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.navigation.Navigation
import com.barisgungorr.notesapp.R
import com.barisgungorr.notesapp.databinding.FragmentSignInBinding
import com.barisgungorr.view.activity.NoteActivity
import com.google.firebase.auth.FirebaseAuth

class SignInFragment : Fragment() {
    private var _binding: FragmentSignInBinding? = null
    private val binding get() = _binding!!
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        _binding = FragmentSignInBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupFirebaseAuth()
        setupNavigationListeners(view)
        setupLoginButton()
    }

    private fun setupFirebaseAuth() {
        firebaseAuth = FirebaseAuth.getInstance()
    }

    private fun setupNavigationListeners(view: View) {
        binding.goToSignUp.setOnClickListener {
            Navigation.findNavController(view)
                .navigate(R.id.action_sign_in_Fragment_to_sign_Up_Fragment)
        }
        binding.goToForgotPassword.setOnClickListener {
            Navigation.findNavController(view)
                .navigate(R.id.action_sign_in_Fragment_to_forget_Fragment)
        }
    }
    private fun setupLoginButton() {
        binding.btnSignIn.setOnClickListener {
            performLogin()
        }
    }
    private fun performLogin() {
        val mail: String = binding.loginEmail.text.toString().trim()
        val password: String = binding.loginPassword.text.toString().trim()

        if (mail.isEmpty() || password.isEmpty()) {
            Toast.makeText(requireContext(), R.string.fragment_sign_in_fill_in_fields, Toast.LENGTH_LONG).show()
            return
        }

        binding.progressBar.visibility = View.VISIBLE

        firebaseAuth.signInWithEmailAndPassword(mail, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                //checkMailVerification() // Bu satırı silin veya yorum satırı yapın
                binding.progressBar.visibility = View.INVISIBLE

                val intent = Intent(activity, NoteActivity::class.java)
                startActivity(intent)

                activity?.finish()
            } else {
                Toast.makeText(requireContext(), R.string.fragment_sign_in_noSuchAccount, Toast.LENGTH_LONG).show()
                binding.progressBar.visibility = View.INVISIBLE
            }
        }
    }
    // Bu fonksiyonu silin veya yorum satırı yapın
    /*private fun checkMailVerification() {
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser!!.isEmailVerified) {
            binding.progressBar.visibility = View.INVISIBLE

            val intent = Intent(activity, NoteActivity::class.java)
            startActivity(intent)

            activity?.finish()

        } else {
            Toast.makeText(requireContext(), R.string.fragment_sign_in_verify_email, Toast.LENGTH_SHORT)
                .show()
            binding.progressBar.visibility = View.INVISIBLE
            firebaseAuth.signOut()
        }
    }*/
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
