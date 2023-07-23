package com.barisgungorr.notesapp.Activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import com.barisgungorr.notesapp.NoteActivity
import com.barisgungorr.notesapp.R
import com.barisgungorr.notesapp.databinding.ActivityMainBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInOptionsExtension
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var googleSignIn:Button
    private lateinit var firebaseAuth: FirebaseAuth
    private val RC_SIGN_IN = 100
    private lateinit var googleSignInClient: GoogleSignInClient


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

   //     googleSignIn = findViewById(R.id.google_sign_in)

        firebaseAuth = FirebaseAuth.getInstance()
        checkUser()

        val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("AIzaSyCoIZB0qQ3BbkYWw7KRL-A_kVboFKwciB4")
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this,googleSignInOptions)
        googleSignIn.setOnClickListener {
            val intent = googleSignInClient.signInIntent
            startActivityForResult(intent,RC_SIGN_IN)
        }


    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode) {
            RC_SIGN_IN-> {
                val accountTask = GoogleSignIn.getSignedInAccountFromIntent(data)
                try {
                    val account = accountTask.getResult(ApiException::class.java)

                    firebaseAuthwithGoogleAccount(account)
                }
                catch (e: Exception) {
                    Toast.makeText(this,"Giriş başarılı",Toast.LENGTH_LONG).show()
                }

            }
        }
    }

    private fun firebaseAuthwithGoogleAccount(account: GoogleSignInAccount?) {

        val credential = GoogleAuthProvider.getCredential(account!!.idToken,null)

        firebaseAuth.signInWithCredential(credential)
            .addOnSuccessListener {
                startActivity(Intent(this,NoteActivity::class.java))

                overridePendingTransition(0,0)
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this,it.message,Toast.LENGTH_SHORT).show()
            }

    }


    private fun checkUser () {

        val firebaseUser = firebaseAuth.currentUser

        if (firebaseUser!= null) {
            startActivity(Intent(this,NoteActivity::class.java))
            overridePendingTransition(0,0)
            finish()

        }
        else{
            firebaseAuth.signOut()
        }

    }
}