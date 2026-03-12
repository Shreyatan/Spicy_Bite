package com.example.spicybite

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.spicybite.databinding.ActivityLoginBinding
import com.example.spicybite.model.UserModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.database

class LoginActivity : AppCompatActivity() {
    private lateinit var email: String
    private lateinit var password: String
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var googleSignInClient: GoogleSignInClient
    private val binding : ActivityLoginBinding by lazy{
        ActivityLoginBinding.inflate(layoutInflater)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val googleSignInOptions=GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        //initialization of firebase auth

        auth = Firebase.auth
        database = Firebase.database.reference
        googleSignInClient= GoogleSignIn.getClient(this, googleSignInOptions)

        //login with email and paaswword

        binding.loginbutton.setOnClickListener{
            email=binding.emaillogin.text.toString().trim()
            password=binding.passwordlogin.text.toString().trim()
            if(email.isBlank()||password.isBlank())
            {
                Toast.makeText(this,"Please fill all the fields",Toast.LENGTH_SHORT).show()
            }
            else{
                createUser()

            }


        }
        binding.donthavebutton.setOnClickListener{
            startActivity(Intent(this,SignActivity::class.java))
        }
        binding.googlelogin.setOnClickListener {
            val signInIntent=googleSignInClient.signInIntent
            launcher.launch(signInIntent)
        }
    }

    private val launcher=registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            if (task.isSuccessful) {
                val account: GoogleSignInAccount = task.result
                val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                auth.signInWithCredential(credential).addOnCompleteListener { it ->
                    if (it.isSuccessful) {

                        val user = auth.currentUser

                        val userModel = UserModel(
                            user?.displayName ?: "",
                            user?.email ?: "",
                            ""
                        )

                        val userId = user?.uid
                        if (userId != null) {
                            database.child("user").child(userId).setValue(userModel)
                        }
                        Toast.makeText(this, "Sign in with Google successfully", Toast.LENGTH_SHORT)
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(this, "Sign in with Google failed", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }
        }
    }
    private fun createUser() {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null) {
                        updateUi(user)
                    }
                    Toast.makeText(this,"Login Successful",Toast.LENGTH_SHORT).show()
                }
                else{
                    Toast.makeText(this,"Login Failed",Toast.LENGTH_SHORT).show()
                }
            }

    }
    override fun onStart() {
        super.onStart()
        val currentUser:FirebaseUser?=auth.currentUser
        if(currentUser!=null)
        {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    fun updateUi(user: FirebaseUser?) {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}

