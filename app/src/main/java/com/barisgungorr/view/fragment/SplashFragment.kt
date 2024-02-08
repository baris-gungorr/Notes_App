package com.barisgungorr.view.fragment

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.barisgungorr.data.InformationModel
import com.barisgungorr.notesapp.R
import com.barisgungorr.notesapp.databinding.FragmentSplashBinding
import com.barisgungorr.view.activity.NoteActivity
import com.barisgungorr.view.utils.Constants
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.AuthResult
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
        setupGoogleSignIn()
        setupOnClickListeners()

    }

    private val signInResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                try {
                    val account = task.getResult(ApiException::class.java)
                    firebaseAuthWithGoogleAccount(account)
                } catch (e: ApiException) {
                    Toast.makeText(
                        requireContext(),
                        R.string.splash_fragment_sign_in_failed,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }


    private fun setupGoogleSignIn() {
        val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(Constants.CLIENT_ID_SP)
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(requireActivity(), googleSignInOptions)
    }

    private fun setupOnClickListeners() {
        val isCheckboxChecked = binding.checkbox.isChecked

        binding.termsConditions.setOnClickListener { openTermsAndConditionsUrl() }

        binding.googleSignIn.setOnClickListener {
            if (isCheckboxChecked) {
                val signInIntent = googleSignInClient.signInIntent
                signInResultLauncher.launch(signInIntent)
            } else {
                showTermsAndConditionsToast()
            }
        }

        binding.signIn.setOnClickListener {
            if (isCheckboxChecked) {
                findNavController().navigate(R.id.action_splash_Fragment_to_sign_in_Fragment)
            } else {
                showTermsAndConditionsToast()
            }
        }
    }

    private fun firebaseAuthWithGoogleAccount(account: GoogleSignInAccount?) {
        if (account != null) {
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            firebaseAuth.signInWithCredential(credential)
                .addOnSuccessListener { handleSignInSuccess(it) }
                .addOnFailureListener { handleSignInFailure(it) }
        } else {
            Toast.makeText(
                requireContext(),
                R.string.splash_fragment_such_account,
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun handleSignInSuccess(result: AuthResult) {
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser != null) {
            if (result.additionalUserInfo!!.isNewUser) {
                val user = InformationModel(
                    firebaseUser.displayName.toString(),
                    firebaseUser.email.toString(),
                    "",
                    firebaseUser.photoUrl.toString(),
                    firebaseUser.uid.toString()
                )

                FirebaseDatabase.getInstance().reference.child("Users")
                    .child(firebaseUser.uid)
                    .setValue(user)

                Toast.makeText(
                    requireContext(),
                    R.string.splash_fragment_Successful_registration,
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(
                    requireContext(),
                    R.string.splash_fragment_Successful_registration,
                    Toast.LENGTH_SHORT
                ).show()
            }

            startNoteActivity()
        } else {
            Toast.makeText(
                requireContext(),
                R.string.splash_fragment_user_notFound,
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun handleSignInFailure(exception: Exception) {
        Toast.makeText(requireContext(), exception.message, Toast.LENGTH_SHORT).show()
    }

    private fun checkUser() {
        val firebaseUser = firebaseAuth.currentUser

        if (firebaseUser != null && firebaseUser.isEmailVerified) {
            startNoteActivity()
        } else {
            signOut()
        }
    }

    private fun signOut() {
        try {
            Auth.GoogleSignInApi.signOut(googleSignInClient.asGoogleApiClient())
        } catch (e: Exception) {
            println(e.printStackTrace())
        }
        firebaseAuth.signOut()
    }

    private fun startNoteActivity() {
        startActivity(Intent(requireActivity(), NoteActivity::class.java))
        activity?.overridePendingTransition(0, 0)
        activity?.finish()
    }

    private fun openTermsAndConditionsUrl() {
        val openUrlIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse(Constants.TERMS_CONDITION)
        )
        startActivity(openUrlIntent)
    }

    private fun showTermsAndConditionsToast() {
        Toast.makeText(
            requireContext(),
            R.string.splash_fragment_term_conditions,
            Toast.LENGTH_SHORT
        ).show()
    }

}