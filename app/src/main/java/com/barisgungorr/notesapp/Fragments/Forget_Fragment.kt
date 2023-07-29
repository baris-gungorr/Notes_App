package com.barisgungorr.notesapp.Fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.Navigation
import com.barisgungorr.notesapp.R
import com.barisgungorr.notesapp.databinding.FragmentForgetBinding
import com.google.firebase.auth.FirebaseAuth


class Forget_Fragment : Fragment() {

    private lateinit var binding: FragmentForgetBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_forget_, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentForgetBinding.bind(view)

        binding.gobacktologin.setOnClickListener {

        Navigation.findNavController(view).navigate(R.id.action_forget_Fragment_to_sign_in_Fragment)

        }

        binding.passwordRecover.setOnClickListener {

            binding.progressBar.visibility = View.VISIBLE

            val mail: String = binding.forgotPasswordText.toString().trim()
            if (mail.isEmpty()) {
                Toast.makeText(requireContext(),"Mail adresinizi girin",Toast.LENGTH_LONG).show()

                binding.progressBar.visibility = View.INVISIBLE



            }else   {
                FirebaseAuth.getInstance().sendPasswordResetEmail(mail).addOnCompleteListener {task ->

                    if (task.isSuccessful) {
                        Toast.makeText(requireContext(),
                        "Sıfırlama maili gönderildi !",
                            Toast.LENGTH_LONG
                            ).show()

                        Navigation.findNavController(view).navigate(R.id.action_forget_Fragment_to_sign_in_Fragment)

                        binding.progressBar.visibility=View.INVISIBLE

                    }

                    else {
                        Toast.makeText(requireContext(),
                        "Hatalı E-mail adresi ",
                            Toast.LENGTH_LONG
                            ).show()

                        binding.progressBar.visibility = View.INVISIBLE
                    }
                }
            }
        }
    }
    }


