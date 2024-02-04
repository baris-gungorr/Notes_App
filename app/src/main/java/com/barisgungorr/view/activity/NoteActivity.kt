package com.barisgungorr.view.activity

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.airbnb.lottie.LottieAnimationView
import com.barisgungorr.data.Connection.ConnectivityObserver
import com.barisgungorr.data.Connection.NetworkConnectivityObserver
import com.barisgungorr.notesapp.R
import com.barisgungorr.notesapp.databinding.ActivityMainBinding
import com.barisgungorr.notesapp.databinding.ActivityNoteBinding
import com.barisgungorr.view.utils.Constants
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class NoteActivity : AppCompatActivity() {
    private lateinit var binding: ActivityNoteBinding
    private lateinit var connectivityObserver: ConnectivityObserver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNoteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        connectivityObserver = NetworkConnectivityObserver(applicationContext)

        updateUI(isNetworkAvailable(this))

        connectivityObserver.observe().onEach {
            updateUI(it == ConnectivityObserver.Status.Available)
        }.launchIn(lifecycleScope)

        checkUser()
    }

    private fun updateUI(isNetworkAvailable: Boolean) {
        binding.mainLayout.visibility = if (isNetworkAvailable) View.VISIBLE else View.GONE
        binding.noInternet.visibility = if (isNetworkAvailable) View.GONE else View.VISIBLE
    }

    private fun checkUser() {
        val firebaseUser = FirebaseAuth.getInstance().currentUser

        if (firebaseUser == null || !firebaseUser.isEmailVerified) {
            signOut()
        }
    }

    private fun signOut() {
        try {
            val googleSignInOptions =
                GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(Constants.CLIENT_ID)
                    .requestEmail()
                    .build()

            val googleSignInClient: GoogleSignInClient =
                GoogleSignIn.getClient(this, googleSignInOptions)

            Auth.GoogleSignInApi.signOut(googleSignInClient.asGoogleApiClient())
        } catch (e: Exception) {
            Log.e("ERROR", "Error signing out: ${e.message}")
        }
        FirebaseAuth.getInstance().signOut()
    }

    private fun isNetworkAvailable(context: Context?): Boolean {
        if (context == null) return false
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val capabilities =
                connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
            if (capabilities != null) {
                when {
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                        return true
                    }
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                        return true
                    }
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> {
                        return true
                    }
                }
            }
        } else {
            val activeNetworkInfo = connectivityManager.activeNetworkInfo
            if (activeNetworkInfo != null && activeNetworkInfo.isConnected) {
                return true
            }
        }
        return false
    }

    override fun onSupportNavigateUp(): Boolean {
        if (binding.noInternet.isVisible) {
            finishAffinity()
        }
        return super.onSupportNavigateUp()
    }
}
