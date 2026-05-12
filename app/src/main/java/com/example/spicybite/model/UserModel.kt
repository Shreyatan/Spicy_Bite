package com.example.spicybite.model

import android.location.Address

data class UserModel(
    val name: String? = null,
    var firstName: String? = "",
    var middleName: String? = "",
    var lastName: String? = "",
    val email: String?=null,
    val password: String?=null,
    var role: String? = null,
    val address: String? = null,
    val houseNo: String? = null,
    val street: String? = null,
    val city: String? = null,
    val state: String? = null,
    val pincode: String? = null,
    val phone: String?=null,



)
