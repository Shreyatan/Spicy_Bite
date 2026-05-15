package com.example.spicybite.model

data class MenuItem(
    var foodName: String? = null,
    var foodPrice: String? = null,
    var foodImage: String? = null,
    var foodCategory: String? = null,
    var foodDescription: String? = null,   // ✅ ADD THIS
    var foodIngrediant: String? = null,
    var itemAvailable: Boolean? = true
)