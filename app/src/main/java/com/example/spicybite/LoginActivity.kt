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
    private var isPasswordVisible = false
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
        binding.eyeIcon.setOnClickListener {

            if (isPasswordVisible) {
                // 🔒 HIDE PASSWORD
                binding.passwordlogin.inputType =
                    android.text.InputType.TYPE_CLASS_TEXT or
                            android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD

                binding.eyeIcon.setImageResource(R.drawable.eye_hide)

            } else {
                // 👁 SHOW PASSWORD
                binding.passwordlogin.inputType =
                    android.text.InputType.TYPE_CLASS_TEXT or
                            android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD

                binding.eyeIcon.setImageResource(R.drawable.eye)

            }

            // cursor last position
            binding.passwordlogin.setSelection(binding.passwordlogin.text.length)

            isPasswordVisible = !isPasswordVisible
        }
        binding.forgotpassword.setOnClickListener {

            val email = binding.emaillogin.text.toString().trim()

            if (email.isEmpty()) {
                Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Enter valid email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            sendPasswordResetLink(email)
        }

    }
    private fun sendPasswordResetLink(email: String) {

        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->

                if (task.isSuccessful) {
                    Toast.makeText(
                        this,
                        "Reset link sent to your email",
                        Toast.LENGTH_LONG
                    ).show()

                } else {
                    Toast.makeText(this, "Check your email for reset link 📩", Toast.LENGTH_LONG).show()
                }
            }

    }

    private val launcher=registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            if (task.isSuccessful) {
                val account: GoogleSignInAccount = task.result
                val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                auth.signInWithCredential(credential).addOnCompleteListener { task ->
                    if (task.isSuccessful) {

                        val user = auth.currentUser
                        val uid = user?.uid ?: return@addOnCompleteListener

                        database.child("user").child(uid).get()
                            .addOnSuccessListener { snapshot ->

                                if (snapshot.exists()) {
                                    // ✅ Already User
                                    Toast.makeText(this, "User Login Success", Toast.LENGTH_SHORT).show()
                                    startActivity(Intent(this, MainActivity::class.java))
                                    finish()

                                } else {
                                    // ❌ First time → create user
                                    val userData = UserModel(
                                        user?.displayName ?: "",
                                        user?.email ?: "",
                                        ""
                                    )

                                    database.child("user").child(uid).setValue(userData)

                                    Toast.makeText(this, "New User Created", Toast.LENGTH_SHORT).show()
                                    startActivity(Intent(this, MainActivity::class.java))
                                    finish()
                                }
                            }

                    } else {
                        Toast.makeText(this, "Google Sign-In Failed", Toast.LENGTH_SHORT).show()


                    }
                }
            }
        }
    }
    private fun createUser() {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {

                    val uid = auth.currentUser?.uid ?: return@addOnCompleteListener

                    // 🔥 CHECK USER NODE
                    database.child("user").child(uid).get()
                        .addOnSuccessListener { snapshot ->

                            if (snapshot.exists()) {
                                // ✅ USER LOGIN SUCCESS
                                Toast.makeText(this,"User Login Successful",Toast.LENGTH_SHORT).show()
                                startActivity(Intent(this, MainActivity::class.java))
                                finish()

                            } else {
                                // ❌ ADMIN TRYING USER APP
                                Toast.makeText(this,"Access Denied! Not a User",Toast.LENGTH_SHORT).show()
                                auth.signOut()
                            }
                        }

                } else {
                    Toast.makeText(this,"Login Failed",Toast.LENGTH_SHORT).show()
                }
            }
    }
    override fun onStart() {
        super.onStart()
        val currentUser: FirebaseUser? = auth.currentUser

        if (currentUser != null) {
            auth.signOut()   // 🔥 force login again
        }
    }

    fun updateUi(user: FirebaseUser?) {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}

