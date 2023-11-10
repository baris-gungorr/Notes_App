package com.barisgungorr.view.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.navigation.Navigation
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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_sign__up_, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        binding = FragmentSignUpBinding.bind(view)
        firebaseAuth = FirebaseAuth.getInstance()
        firebaseDatabase = FirebaseDatabase.getInstance()

        binding.gotologin.setOnClickListener {
            Navigation.findNavController(view)
                .navigate(R.id.action_sign_Up_Fragment_to_sign_in_Fragment)
        }

        binding.signup.setOnClickListener {

            val mail = binding.signUpEmaail.text.toString().trim()
            val password = binding.signUpPassword.text.toString().trim()
            val repassword = binding.signUpRePassword.text.toString().trim()

            if (mail.isEmpty() || password.isEmpty() || repassword.isEmpty()) {
                Toast.makeText(requireContext(), "Tüm alanları doldurun", Toast.LENGTH_SHORT).show()
            } else if (password.length < 6) {
                Toast.makeText(
                    requireContext(),
                    "Parolanız en az 6 haneli olmalıdır",
                    Toast.LENGTH_SHORT
                ).show()

            } else if (password != repassword) {
                Toast.makeText(requireContext(), "Parolalar uyuşmuyor", Toast.LENGTH_SHORT).show()

            } else {
                binding.progressBar.visibility = View.VISIBLE

                firebaseAuth.createUserWithEmailAndPassword(mail, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {

                            val firebaseUser: FirebaseUser? = firebaseAuth.currentUser
                            val user = InformationModel(
                                binding.signUpName.text.toString(),
                                binding.signUpEmaail.text.toString(),
                                binding.signUpPassword.text.toString(),
                                "https://res.cloudinary.com/dmioqpqrb/image/upload/v1690542261/profile_bfndcy.jpg",
                                firebaseUser?.uid.toString()
                            )

                            if (firebaseUser != null) {

                                firebaseDatabase.reference.child("Users").child(firebaseUser.uid)
                                    .setValue(user)
                            }
                            Toast.makeText(
                                requireContext(),
                                "Başarılı Kayıt",
                                Toast.LENGTH_SHORT
                            ).show()

                            sendEmailVerification()


                        } else {

                            Toast.makeText(
                                requireContext(),
                                "Kayıt 'Başarısız'",
                                Toast.LENGTH_SHORT
                            ).show()
                            binding.progressBar.visibility = View.INVISIBLE
                            performSignUp(mail, password)

                        }
                    }
            }
        }
    }

    private fun sendEmailVerification() {
        val firebaseUser: FirebaseUser? = firebaseAuth.currentUser

        binding.progressBar.visibility = View.INVISIBLE

        firebaseUser?.sendEmailVerification()?.addOnSuccessListener {

            Toast.makeText(
                requireContext(),
                "Doğrulama postanız 'Gönderildi'  \n Doğrulayın ve giriş yapın",
                Toast.LENGTH_SHORT
            ).show()

            Navigation.findNavController(requireView())
                .navigate(R.id.action_sign_Up_Fragment_to_sign_in_Fragment)

        }
    }

    private fun isEmailValid(email: String): Boolean {
        val pattern = Regex("^\\w+([\\.-]?\\w+)*@\\w+([\\.-]?\\w+)*(\\.\\w{2,3})+\$")
        return pattern.matches(email)
    }

    private fun performSignUp(mail: String, password: String) {
        if (!isEmailValid(mail)) {
            Toast.makeText(requireContext(), "Geçersiz E-mail formatı", Toast.LENGTH_LONG).show()
            return
        }
        CoroutineScope(Dispatchers.Main).launch {
            try {
                firebaseAuth.createUserWithEmailAndPassword(mail, password).await()
                Navigation.findNavController(requireView())
                    .navigate(R.id.action_sign_Up_Fragment_to_sign_in_Fragment)

            } catch (exception: Exception) {
                val message = when (exception) {
                    is FirebaseAuthWeakPasswordException -> "Parolanız güçlü değil!"
                    is FirebaseAuthInvalidCredentialsException -> "E-mail biçimi hatalı!"
                    is FirebaseAuthUserCollisionException -> "Bu E-mail daha önce kayıt edilmiş!"
                    is FirebaseNetworkException -> "Ağ hatası!"
                    else -> "Kullanıcı kaydı hatası!"
                }
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            }
        }
    }
}
