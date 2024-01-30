package com.barisgungorr.view.activity

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.lifecycleScope
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.barisgungorr.viewmodel.MyWorkerNotifications
import com.airbnb.lottie.LottieAnimationView
import com.barisgungorr.data.Connection.ConnectivityObserver
import com.barisgungorr.data.Connection.NetworkConnectivityObserver
import com.barisgungorr.notesapp.R
import com.barisgungorr.notesapp.databinding.ActivityMainBinding
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
    private lateinit var mainLayout: NestedScrollView
    private lateinit var noInternet: LottieAnimationView
    private lateinit var connectivityObserver: ConnectivityObserver


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        window.statusBarColor = ContextCompat.getColor(this, R.color.black)


        val call = PeriodicWorkRequestBuilder<MyWorkerNotifications>(1440, TimeUnit.MINUTES)
            .setInitialDelay(1440, TimeUnit.SECONDS)
            .build()

        WorkManager.getInstance(this).enqueue(call)

        WorkManager.getInstance(this).getWorkInfoByIdLiveData(call.id)
            .observe(this) {
                val current = it.state.name
                Log.e("Arkaplan işlem durumu", current)
            }

        mainLayout = findViewById(R.id.main_layout)

        noInternet = findViewById(R.id.no_internet)
        connectivityObserver = NetworkConnectivityObserver(applicationContext)

        if (isNetworkAvailable(this)) {

            mainLayout.visibility = View.VISIBLE

            noInternet.visibility = View.GONE

        } else {

            mainLayout.visibility = View.GONE

            noInternet.visibility = View.VISIBLE

        }

        connectivityObserver.observe().onEach {

            if (it == ConnectivityObserver.Status.Available) {

                mainLayout.visibility = View.VISIBLE

                noInternet.visibility = View.GONE
            } else {

                mainLayout.visibility = View.GONE

                noInternet.visibility = View.VISIBLE

            }

        }.launchIn(lifecycleScope)


        checkUser()

    }

    private fun checkUser() {

        val firebaseUser = FirebaseAuth.getInstance().currentUser


        if (firebaseUser != null && firebaseUser.isEmailVerified) {

            startActivity(Intent(this, NoteActivity::class.java))

            overridePendingTransition(0, 0)

            finish()
        } else {

            try {
                val googleSignInOptions =
                    GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken("AIzaSyCoIZB0qQ3BbkYWw7KRL-A_kVboFKwciB4")
                        .requestEmail()
                        .build()

                val googleSignInClient: GoogleSignInClient =
                    GoogleSignIn.getClient(this, googleSignInOptions)

                Auth.GoogleSignInApi.signOut(googleSignInClient.asGoogleApiClient())

            } catch (_: Exception) {

            }
            FirebaseAuth.getInstance().signOut()
        }
    }

    private fun isNetworkAvailable(context: Context?): Boolean {
        if (context == null) return false
        val connectivityManager =
            context.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
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

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {

        if (noInternet.isVisible) {

            finishAffinity()

        }
        super.onBackPressed()
    }

}



