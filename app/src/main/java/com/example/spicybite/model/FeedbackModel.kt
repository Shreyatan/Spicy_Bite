package com.example.spicybite.model

data class FeedbackModel(

    var orderId: String? = null,
    var userId: String? = null,
    var userName: String? = null,
    var rating: Float? = null,
    var comment: String? = null,
    var currentTime: Long? = null,
    // ADD THIS
    var foodName: String? = null
)