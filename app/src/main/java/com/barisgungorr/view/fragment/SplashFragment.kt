package com.barisgungorr.view.fragment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.widget.NestedScrollView
import androidx.navigation.Navigation
import com.airbnb.lottie.LottieAnimationView
import com.barisgungorr.data.InformationModel
import com.barisgungorr.notesapp.R
import com.barisgungorr.view.activity.NoteActivity
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

    private lateinit var googleSignIn: Button

    private lateinit var googleSignInClient: GoogleSignInClient

    private lateinit var firebaseAuth: FirebaseAuth

    private val RC_SIGN_IN = 100

    private lateinit var mainLayout: NestedScrollView

    private lateinit var noInternet: LottieAnimationView

    private lateinit var signIn: Button

    private lateinit var checkBox: CheckBox

    private lateinit var termsConditions: TextView


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_splash, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        googleSignIn = view.findViewById(R.id.google_sign_in)

        mainLayout = view.findViewById(R.id.main_layout)

        noInternet = view.findViewById(R.id.no_internet)

        signIn = view.findViewById(R.id.sign_in)

        checkBox = view.findViewById(R.id.checkbox)

        termsConditions = view.findViewById(R.id.terms_conditions)


        firebaseAuth = FirebaseAuth.getInstance()


        checkUser()

        val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("166513008091-0eeirmkqfk81hlkr2j590reid1sfjbff.apps.googleusercontent.com")
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(requireActivity(), googleSignInOptions)

        termsConditions.setOnClickListener {

            val openUrlIntent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://gungorbarisdevelop.blogspot.com/2023/07/uygulamasi-kosullar-ve-sartlar.html")
            )
            startActivity(openUrlIntent)


        }
        googleSignIn.setOnClickListener {

            if (checkBox.isChecked) {

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

        signIn.setOnClickListener {

            if (checkBox.isChecked) {

                Navigation.findNavController(view)
                    .navigate(R.id.action_splash_Fragment_to_sign_in_Fragment)
            } else {
                Toast.makeText(
                    requireContext(),
                    "Şartlar & Koşullar kabul edilmelidir!",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    @Deprecated("Deprecated in Java")
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

                    Toast.makeText(requireContext(), "Başarılı giriş", Toast.LENGTH_SHORT).show()
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
                val googleSignInOptions =
                    GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken("166513008091-0eeirmkqfk81hlkr2j590reid1sfjbff.apps.googleusercontent.com")
                        .requestEmail()
                        .build()


                val googleSignInClient: GoogleSignInClient =
                    GoogleSignIn.getClient(requireActivity(), googleSignInOptions)

                Auth.GoogleSignInApi.signOut(googleSignInClient.asGoogleApiClient())

            } catch (e: Exception) {
                println("Bir şeyler ters gitti!")

            }

            firebaseAuth.signOut()

        }
    }
}