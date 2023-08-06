package com.barisgungorr.view.Fragment

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
import com.barisgungorr.view.Activity.NoteActivity
import com.google.firebase.auth.FirebaseAuth


class Sign_in_Fragment : Fragment() {
    private lateinit var binding: FragmentSignInBinding
    private lateinit var firebaseAuth: FirebaseAuth


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        return inflater.inflate(R.layout.fragment_sign_in_, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentSignInBinding.bind(view)
        firebaseAuth = FirebaseAuth.getInstance()

        binding.goToSignUp.setOnClickListener {

            Navigation.findNavController(view).navigate(R.id.action_sign_in_Fragment_to_sign_Up_Fragment) // kodla
        }

        binding.goToForgotPassword.setOnClickListener {

            Navigation.findNavController(view).navigate(R.id.action_sign_in_Fragment_to_forget_Fragment)  // kodla
        }

        binding.login.setOnClickListener {
            val mail: String = binding.loginemail.text.toString().trim()
            val password: String = binding.loginpassword.text.toString().trim()

            if (mail.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), "Alanları doldurunuz!", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            binding.progressBar.visibility = View.VISIBLE

            firebaseAuth.signInWithEmailAndPassword(mail, password).addOnCompleteListener { task ->
                if (task.isSuccessful) {

                    checkMailVerification()
                } else {
                    Toast.makeText(requireContext(), "Hesap Mevcut Değil!", Toast.LENGTH_LONG)
                        .show()
                    binding.progressBar.visibility = View.INVISIBLE
                }
            }
        }

    }

    private fun checkMailVerification () {

        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser!!.isEmailVerified) {
            Toast.makeText(requireContext(),"Başarılı Giriş",Toast.LENGTH_SHORT).show()
            binding.progressBar.visibility = View.INVISIBLE

            val intent =Intent(activity, NoteActivity::class.java)
            startActivity(intent)

            activity?.finish()

        } else {
            Toast.makeText(requireContext(),"E-mail adresini doğrulayın",Toast.LENGTH_SHORT).show()
            binding.progressBar.visibility = View.INVISIBLE
            firebaseAuth.signOut()
        }

    }
}
