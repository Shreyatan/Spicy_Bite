package com.example.spicybite

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.spicybite.databinding.FragmentFeedbackBottomSheetBinding
import com.example.spicybite.model.FeedbackModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class FeedbackBottomSheet(private val orderId: String,  private val foodName: String) : BottomSheetDialogFragment() {
    private lateinit var binding: FragmentFeedbackBottomSheetBinding
    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFeedbackBottomSheetBinding.inflate(inflater, container, false)

        binding.btnSubmitFeedback.setOnClickListener {
            val rating = binding.ratingBar.rating
            val comment = binding.etFeedback.text.toString()

            if (rating == 0f) {
                Toast.makeText(requireContext(), "Please provide a rating", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            submitFeedback(rating, comment)
        }


        return binding.root
    }

    private fun submitFeedback(rating: Float, comment: String) {

        val userId = auth.currentUser?.uid ?: return
        val time = System.currentTimeMillis()

        database.reference.child("user").child(userId)
            .get()
            .addOnSuccessListener { snapshot ->

                val userName =
                    snapshot.child("name").getValue(String::class.java)
                        ?: "User"

                val feedback = FeedbackModel(
                    orderId = orderId,
                    userId = userId,
                    userName = userName,
                    rating = rating,
                    comment = comment,
                    currentTime = time,
                    foodName = foodName.trim()
                )

                database.reference.child("Feedback")
                    .push()
                    .setValue(feedback)
                    .addOnSuccessListener {

                        updateOrderFeedbackStatus()
                    }
                    .addOnFailureListener {

                        Toast.makeText(
                            requireContext(),
                            "Failed to submit feedback",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }
    }

    private fun updateOrderFeedbackStatus() {
        val userId = auth.currentUser?.uid ?: return

        val updates = hashMapOf<String, Any>(
            "feedbackSubmitted" to true
        )

        // Update in multiple places where order exists
        database.reference.child("Orders").child(orderId).updateChildren(updates)
        database.reference.child("user").child(userId).child("BuyHistory").child(orderId).updateChildren(updates)
        database.reference.child("CompletedOrder").child(orderId).updateChildren(updates)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Thank you for your feedback!", Toast.LENGTH_SHORT).show()
                dismiss()
            }
    }
}