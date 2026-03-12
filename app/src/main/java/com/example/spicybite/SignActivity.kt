package com.example.spicybite

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.spicybite.databinding.ActivitySignBinding
import com.example.spicybite.model.UserModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth

import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.database.DatabaseReference

import com.google.firebase.database.database

class SignActivity : AppCompatActivity() {
    private lateinit var email: String
    private lateinit var password: String
    private lateinit var username: String
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var googleSignInClient: GoogleSignInClient

    private lateinit var binding: ActivitySignBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val googleSignInOptions=GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        //initialise firenbase database
        auth = Firebase.auth
        database = Firebase.database.reference
        googleSignInClient= GoogleSignIn.getClient(this, googleSignInOptions)


        binding.createaccountbutton.setOnClickListener {
            username = binding.userName.text.toString()
            email = binding.emailAddress.text.toString().trim()
            password = binding.password.text.toString().trim()

            if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all the fields", Toast.LENGTH_SHORT).show()
            } else if (!isValidEmail(email)) {
                binding.emailAddress.error = "Enter valid email"
            } else if (!isValidPassword(password)) {
                binding.password.error =
                    "Password must contain uppercase, lowercase, number and special character"
            } else {
                CreateAccount(email, password)
            }
        }
        binding.alreadyhavebutton.setOnClickListener {
            val intent=Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
        binding.googlebtn.setOnClickListener {
            val signIntent=googleSignInClient.signInIntent
            launcher.launch(signIntent)

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
    private fun CreateAccount(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Account created successfully", Toast.LENGTH_SHORT).show()
                    saveUserData()
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this, task.exception?.message, Toast.LENGTH_LONG).show()
                    Log.e("FirebaseError", task.exception.toString())
                }
            }
    }

    private fun saveUserData() {
        username=binding.userName.text.toString()
        email=binding.emailAddress.text.toString().trim()
        password=binding.password.text.toString().trim()
        val user = UserModel(username, email, password)
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            database.child("user").child(userId).setValue(user)
        }

    }
        private fun isValidEmail(email: String): Boolean {
            return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
        }

        private fun isValidPassword(password: String): Boolean {
            val passwordPattern =
                "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#\$%^&+=!]).{8,}$"

            return Regex(passwordPattern).matches(password)
        }

}