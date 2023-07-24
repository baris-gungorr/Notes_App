package com.barisgungorr.notesapp.Activity

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.lifecycleScope
import com.airbnb.lottie.LottieAnimationView
import com.barisgungorr.notesapp.databinding.ActivityMainBinding
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

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

        mainLayout = binding.mainLayout
        noInternet = binding.noInternet
        connectivityObserver = NetworkConnectivityObserver(applicationContext)

        if (isNetworkAvailable(this)){

            mainLayout.visibility= View.VISIBLE

            noInternet.visibility= View.GONE

        }

        else{


            mainLayout.visibility= View.GONE

            noInternet.visibility= View.VISIBLE

        }


        connectivityObserver.observe().onEach {

            if (it==ConnectivityObserver.Status.Available){

                mainLayout.visibility= View.VISIBLE

                noInternet.visibility= View.GONE
            }

            else{

                mainLayout.visibility= View.GONE

                noInternet.visibility= View.VISIBLE

            }

        }.launchIn(lifecycleScope)


        checkUser()

    }

    private fun checkUser(){

        val firebaseUser=FirebaseAuth.getInstance().currentUser


        if (firebaseUser!=null && firebaseUser.isEmailVerified){

            startActivity(Intent(this, NoteActivity::class.java))

            overridePendingTransition(0,0)

            finish()
        }

        else{

            try {
                val googleSignInOptions= GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken("AIzaSyCoIZB0qQ3BbkYWw7KRL-A_kVboFKwciB4")
                    .requestEmail()
                    .build()

                val googleSignInClient: GoogleSignInClient = GoogleSignIn.getClient(this,googleSignInOptions)

                Auth.GoogleSignInApi.signOut(googleSignInClient.asGoogleApiClient())

            }
            catch (e:Exception){

            }
            FirebaseAuth.getInstance().signOut()
        }
    }
    fun isNetworkAvailable(context: Context?): Boolean {
        if (context == null) return false
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
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

        if (noInternet.isVisible){

            finishAffinity()

        }
        super.onBackPressed()
    }

}


