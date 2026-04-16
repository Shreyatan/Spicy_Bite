package com.example.spicybite.model

import android.location.Address

data class UserModel(
    val name: String?=null,
    val email: String?=null,
    val password: String?=null,
    val address: String?=null,
    val phone: String?=null,
    var role: String? = null,

)
