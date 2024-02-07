package com.barisgungorr.view.fragment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.barisgungorr.data.InformationModel
import com.barisgungorr.notesapp.R
import com.barisgungorr.notesapp.databinding.FragmentSplashBinding
import com.barisgungorr.view.activity.NoteActivity
import com.barisgungorr.view.utils.Constants
import com.barisgungorr.view.utils.Constants.RC_SIGN_IN
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.FirebaseDatabase

class SplashFragment : Fragment() {

    private var _binding: FragmentSplashBinding? = null
    private val binding get() = _binding!!

    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var firebaseAuth: FirebaseAuth


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSplashBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firebaseAuth = FirebaseAuth.getInstance()

        checkUser()

        val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(Constants.CLIENT_ID_SP)
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(requireActivity(), googleSignInOptions)

        binding.termsConditions.setOnClickListener {
            val openUrlIntent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://gungorbarisdevelop.blogspot.com/2023/07/uygulamasi-kosullar-ve-sartlar.html")
            )
            startActivity(openUrlIntent)
        }

        binding.googleSignIn.setOnClickListener {
            if (binding.checkbox.isChecked) {
                val intent = googleSignInClient.signInIntent
                startActivityForResult(intent, RC_SIGN_IN)
            } else {
                Toast.makeText(
                    requireContext(),
                    "Şartlar & Koşullar kabul edilmelidir!",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        binding.signIn.setOnClickListener {
            if (binding.checkbox.isChecked) {
                findNavController().navigate(R.id.action_splash_Fragment_to_sign_in_Fragment)
            } else {
                Toast.makeText(
                    requireContext(),
                    R.string.splash_fragment_term_conditions,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {

            RC_SIGN_IN -> {
                val accountTask = GoogleSignIn.getSignedInAccountFromIntent(data)
                try {
                    val account = accountTask.getResult(ApiException::class.java)
                    firebaseAuthWithGoogleAccount(account)
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "Başarısız kayıt", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun firebaseAuthWithGoogleAccount(account: GoogleSignInAccount?) {
        val credential = GoogleAuthProvider.getCredential(account!!.idToken, null)
        firebaseAuth.signInWithCredential(credential)
            .addOnSuccessListener {

                val firebaseUser = firebaseAuth.currentUser

                if (it.additionalUserInfo!!.isNewUser) {
                    val user = InformationModel(
                        firebaseUser?.displayName.toString(),
                        firebaseUser?.email.toString(),
                        "",
                        firebaseUser?.photoUrl.toString(),
                        firebaseUser?.uid.toString()
                    )

                    if (firebaseUser != null) {
                        FirebaseDatabase.getInstance().reference.child("Users")
                            .child(firebaseUser.uid)
                            .setValue(user)

                        Toast.makeText(
                            requireContext(),
                            "Başarılı kayıt",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Kullanıcı bulunamadı!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Toast.makeText(requireContext(), "Succes", Toast.LENGTH_SHORT).show()
                }

                requireActivity().startActivity(Intent(requireContext(), NoteActivity::class.java))

                requireActivity().overridePendingTransition(0, 0)

                requireActivity().finish()

            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
            }
    }

    private fun checkUser() {
        val firebaseUser = firebaseAuth.currentUser

        if (firebaseUser != null && firebaseUser.isEmailVerified) {
            startActivity(Intent(requireActivity(), NoteActivity::class.java))
            activity?.overridePendingTransition(0, 0)
            activity?.finish()
        } else {
            try {
                Auth.GoogleSignInApi.signOut(googleSignInClient.asGoogleApiClient())
            } catch (e: Exception) {
                println(e.printStackTrace())
            }
            firebaseAuth.signOut()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
