package com.example.spicybite.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.spicybite.databinding.CartItemBinding
import com.example.spicybite.model.CartItems
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class CartAdapter(
    private val context: Context,
    private val cartItems: MutableList<CartItems>
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    inner class CartViewHolder(val binding: CartItemBinding) : RecyclerView.ViewHolder(binding.root)

    private val auth = FirebaseAuth.getInstance()
    private val userId = auth.currentUser?.uid ?: ""
    private val cartItemsReference: DatabaseReference =
        FirebaseDatabase.getInstance().reference
            .child("user")
            .child(userId ?: "")
            .child("cartItems")
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val binding = CartItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CartViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val item = cartItems[position]

        holder.binding.apply {
            tvFoodName.text = item.foodName
            tvPrice.text = "₹${item.foodPrice}"
            tvQty.text = item.foodQuantity.toString()

            // Load image with Glide
            Glide.with(context)
                .load(item.foodImage)
                .into(imgFood)

            // ➕ Plus
            btnPlus.setOnClickListener {

                item.foodQuantity?.let {

                    val newQty = it + 1
                    item.foodQuantity = newQty
                    tvQty.text = newQty.toString()

                    item.itemKey?.let { key ->
                        cartItemsReference.child(key).child("foodQuantity").setValue(newQty)
                    }
                }
            }

            // ➖ Minus
            btnMinus.setOnClickListener {

                item.foodQuantity?.let {

                    if (it > 1) {

                        val newQty = it - 1
                        item.foodQuantity = newQty
                        tvQty.text = newQty.toString()

                        item.itemKey?.let { key ->
                            cartItemsReference.child(key).child("foodQuantity").setValue(newQty)
                        }
                    }
                }
            }

            btnDelete.setOnClickListener {

                val key = item.itemKey

                // 🔥 STORE POSITION FIRST
                val position = holder.bindingAdapterPosition

                if (key != null && position != RecyclerView.NO_POSITION) {

                    cartItemsReference.child(key).removeValue()
                        .addOnSuccessListener {

                            // 🔥 REMOVE FROM LOCAL LIST
                            cartItems.removeAt(position)

                            // 🔥 UPDATE RECYCLERVIEW
                            notifyItemRemoved(position)
                            notifyItemRangeChanged(position, cartItems.size)

                            Toast.makeText(context, "Item Deleted", Toast.LENGTH_SHORT).show()
                        }

                        .addOnFailureListener {

                            Toast.makeText(context, "Delete Failed", Toast.LENGTH_SHORT).show()
                        }
                }
            }
        }
    }

    override fun getItemCount(): Int = cartItems.size

    // ===== Delete Functions =====
    private fun deleteItem(position: Int) {
        getUniqueKeyAtPosition(position) { uniqueKey ->
            if (uniqueKey != null) {
                removeItem(position, uniqueKey)
            } else {
                Toast.makeText(context, "Failed to delete item", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getUniqueKeyAtPosition(positionRetrieve: Int, onComplete: (String?) -> Unit) {
        cartItemsReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var uniqueKey: String? = null
                snapshot.children.forEachIndexed { index, dataSnapshot ->
                    if (index == positionRetrieve) {
                        uniqueKey = dataSnapshot.key
                        return@forEachIndexed
                    }
                }
                onComplete(uniqueKey)
            }

            override fun onCancelled(error: DatabaseError) {
                onComplete(null)
            }
        })
    }
    // Add this function in your CartAdapter
    fun getUpdatedItemsQuantities(): MutableList<Int> {
        val quantities = mutableListOf<Int>()
        for (item in cartItems) {
            quantities.add(item.foodQuantity ?: 1)
        }
        return quantities
    }
    private fun removeItem(position: Int, uniqueKey: String) {
        cartItemsReference.child(uniqueKey).removeValue()
            .addOnSuccessListener {
                cartItems.removeAt(position)
                notifyItemRemoved(position)
                notifyItemRangeChanged(position, cartItems.size)
                Toast.makeText(context, "Item Deleted", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed to Delete", Toast.LENGTH_SHORT).show()
            }
    }
}