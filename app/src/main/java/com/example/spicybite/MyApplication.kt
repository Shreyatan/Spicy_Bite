package com.example.spicybite

import android.app.Application
import com.razorpay.Checkout

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        Checkout.preload(applicationContext)
    }
}