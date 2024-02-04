package com.barisgungorr.view.activity

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.barisgungorr.data.Connection.ConnectivityObserver
import com.barisgungorr.data.Connection.NetworkConnectivityObserver
import com.barisgungorr.notesapp.R
import com.barisgungorr.notesapp.databinding.ActivityMainBinding
import com.barisgungorr.view.utils.Constants
import com.barisgungorr.viewmodel.MyWorkerNotifications
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var connectivityObserver: ConnectivityObserver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.statusBarColor = ContextCompat.getColor(this, R.color.black)

        setupWorkManager()
        setupNetworkObserver()
        checkUser()
    }

    private fun setupWorkManager() {
        val call = PeriodicWorkRequestBuilder<MyWorkerNotifications>(1440, TimeUnit.MINUTES)
            .setInitialDelay(1440, TimeUnit.SECONDS)
            .build()

        WorkManager.getInstance(this).enqueue(call)

        WorkManager.getInstance(this).getWorkInfoByIdLiveData(call.id)
            .observe(this) {
                val current = it.state.name
                Log.e(R.string.activity_main_process_status.toString(), current)
            }
    }

    private fun setupNetworkObserver() {
        connectivityObserver = NetworkConnectivityObserver(applicationContext)

        connectivityObserver.observe().onEach { status ->
            when (status) {
                ConnectivityObserver.Status.Available -> {

                    binding.noInternet.visibility = View.GONE
                }

                else -> {
                    binding.noInternet.visibility = View.VISIBLE
                }
            }
        }.launchIn(lifecycleScope)
    }

    private fun checkUser() {
        val firebaseUser = FirebaseAuth.getInstance().currentUser

        if (firebaseUser != null && firebaseUser.isEmailVerified) {
            startActivity(Intent(this, NoteActivity::class.java))
            overridePendingTransition(0, 0)
            finish()
        } else {
            if (isNetworkAvailable(this)) {
                signOut()
            } else {
                Toast.makeText(this, R.string.activity_main_check_internet, Toast.LENGTH_SHORT)
                    .show()
            }
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
        } catch (_: Exception) {
        }
        FirebaseAuth.getInstance().signOut()
    }

    private fun isNetworkAvailable(context: Context?): Boolean {
        if (context == null) return false
        val connectivityManager =
            context.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val capabilities =
                connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
            capabilities?.run {
                hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                        hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                        hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
            } ?: false
        } else {
            val network = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        }
    }
}
