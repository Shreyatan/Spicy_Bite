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
import android.text.Editable
import android.text.TextWatcher
import androidx.core.content.ContextCompat
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.database.DatabaseReference

import com.google.firebase.database.database

class SignActivity : AppCompatActivity() {
    private var isPasswordVisible = false
    private lateinit var email: String
    private lateinit var password: String
    private lateinit var username: String
    private lateinit var middleName: String
    private lateinit var lastName: String
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
            middleName = binding.middleName.text.toString().trim()
            lastName = binding.lastName.text.toString().trim()
            email = binding.emailAddress.text.toString().trim()
            password = binding.password.text.toString().trim()

            if (username.isEmpty()) {
                binding.userName.error = "First name required"
            }
            else if (!username.matches(Regex("^[A-Za-z ]+$"))) {
                binding.userName.error = "Only alphabets allowed"
            }
            // ✅ Middle Name Optional
            else if (middleName.isNotEmpty() &&
                !middleName.matches(Regex("^[A-Za-z ]+$"))) {

                binding.middleName.error = "Only alphabets allowed"
            }

// ✅ Last Name Optional
            else if (lastName.isNotEmpty() &&
                !lastName.matches(Regex("^[A-Za-z ]+$"))) {

                binding.lastName.error = "Only alphabets allowed"
            }
            else if (email.isEmpty()) {
                binding.emailAddress.error = "Email required"
            }
            else if (!isValidEmail(email)) {
                binding.emailAddress.error = "Enter valid email"
            }
            else if (password.isEmpty()) {
                binding.password.error = "Password required"
            }
            else if (!isValidPassword(password)) {
                binding.password.error =
                    "Password must contain uppercase, lowercase, number & special character"
            }
            else {
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

        binding.eyeIcon.setOnClickListener {

            if (isPasswordVisible) {
                // 🔒 HIDE PASSWORD
                binding.password.inputType =
                    android.text.InputType.TYPE_CLASS_TEXT or
                            android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD

                binding.eyeIcon.setImageResource(R.drawable.eye_hide)

            } else {
                // 👁 SHOW PASSWORD
                binding.password.inputType =
                    android.text.InputType.TYPE_CLASS_TEXT or
                            android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD

                binding.eyeIcon.setImageResource(R.drawable.eye)

            }

            // cursor last position
            binding.password.setSelection(binding.password.text.length)

            isPasswordVisible = !isPasswordVisible
        }
        binding.password.addTextChangedListener(object : android.text.TextWatcher {

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                val password = s.toString()

                val pattern = Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#\$%^&+=!]).{8,}$")

                if (password.isEmpty()) {
                    // 👇 kuch nahi dikhega initially
                    binding.passwordStatus.text = ""

                } else if (password.matches(pattern)) {
                    // ✅ Strong
                    binding.passwordStatus.text = "Strong Password ✅"
                    binding.passwordStatus.setTextColor(ContextCompat.getColor(this@SignActivity, android.R.color.holo_green_dark))

                } else {
                    // ❌ Weak
                    binding.passwordStatus.text = "Weak Password ❌"
                    binding.passwordStatus.setTextColor(resources.getColor(android.R.color.holo_red_dark))
                }
            }

            override fun afterTextChanged(s: android.text.Editable?) {}
        })
        binding.emailAddress.addTextChangedListener(object : TextWatcher {

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                val email = s.toString().trim()

                if (email.isNotEmpty() &&
                    !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {

                    binding.emailAddress.error = "Invalid Email"
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })
        binding.userName.addTextChangedListener(object : TextWatcher {

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                val name = s.toString()

                if (name.isNotEmpty() &&
                    !name.matches(Regex("^[A-Za-z ]+$"))) {

                    binding.userName.error = "Only alphabets allowed"
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })
        binding.lastName.addTextChangedListener(object : TextWatcher {

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                val lastName = s.toString()

                if (lastName.isNotEmpty() &&
                    !lastName.matches(Regex("^[A-Za-z ]+$"))) {

                    binding.lastName.error = "Only alphabets allowed"
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })
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
                            firstName = user?.displayName ?: "",
                            email = user?.email ?: "",
                            role = "user"
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
        middleName = binding.middleName.text.toString().trim()
        lastName = binding.lastName.text.toString().trim()
        email=binding.emailAddress.text.toString().trim()
        password=binding.password.text.toString().trim()
        val fullName = "$username $middleName $lastName".trim()
        val user = UserModel( name=fullName,firstName = username,
            middleName = middleName,
            lastName = lastName,
            email = email,
            password = password,
            role = "user")
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