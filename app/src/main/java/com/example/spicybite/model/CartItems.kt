package com.example.spicybite.model

data class CartItems(
    val foodName: String?=null,
    val foodPrice: String?=null,
    val foodDescription: String?=null,
   val foodIngrediant: String?=null,
    var foodQuantity: Int?=null,
    val foodImage: String?=null,
    var itemKey: String? = null

)

