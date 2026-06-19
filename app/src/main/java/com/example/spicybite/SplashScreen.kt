package com.example.spicybite

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.spicybite.StartActivity
import com.google.firebase.auth.FirebaseAuth

@Suppress("DEPRECATION")
class SplashScreen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
        Handler(Looper.getMainLooper()).postDelayed({

            val user = FirebaseAuth.getInstance().currentUser

            if (user != null) {
                // already logged in
                startActivity(Intent(this, MainActivity::class.java))
            } else {
                startActivity(Intent(this, StartActivity::class.java))
            }

            finish()

        }, 1500)
    }
}