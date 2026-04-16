package com.example.spicybite.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.spicybite.PayOutActivity
import com.example.spicybite.adapter.CartAdapter
import com.example.spicybite.databinding.FragmentCartBinding
import com.example.spicybite.model.CartItems
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class CartFragment : Fragment() {

    private lateinit var binding: FragmentCartBinding
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var cartAdapter: CartAdapter
    private val cartList = mutableListOf<CartItems>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentCartBinding.inflate(inflater, container, false)

        auth = FirebaseAuth.getInstance()
        val userId = auth.currentUser?.uid ?: return binding.root

        database = FirebaseDatabase.getInstance()
            .reference.child("user").child(userId).child("cartItems")

        cartAdapter = CartAdapter(requireContext(), cartList)

        binding.rvCart.layoutManager = LinearLayoutManager(requireContext())
        binding.rvCart.adapter = cartAdapter
        // 🔹 Fetch cart data from Firebase
        database.addListenerForSingleValueEvent(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {

                cartList.clear()

                for (dataSnapshot in snapshot.children) {

                    val cartItem = dataSnapshot.getValue(CartItems::class.java)
                    cartItem?.itemKey = dataSnapshot.key
                    cartItem?.let {
                        cartList.add(it)
                    }
                }

                cartAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })

        binding.proceedButton.setOnClickListener {
            //get order item details before proceed to checkout
            getOrderItemDetails()
//            val intent = Intent(requireContext(), PayOutActivity::class.java)
//            startActivity(intent)
        }

        return binding.root
    }
    private fun getOrderItemDetails() {

        val foodName = mutableListOf<String>()
        val foodPrice = mutableListOf<String>()
        val foodDescription = mutableListOf<String>()
        val foodImage = mutableListOf<String>()
        val foodIngredient = mutableListOf<String>()

        val foodQuantities = cartAdapter.getUpdatedItemsQuantities()

        for (item in cartList) {

            item.foodName?.let { foodName.add(it) }
            item.foodPrice?.let { foodPrice.add(it) }
            item.foodDescription?.let { foodDescription.add(it) }
            item.foodImage?.let { foodImage.add(it) }
            item.foodIngrediant?.let { foodIngredient.add(it) }

        }

        orderNow(
            foodName,
            foodPrice,
            foodDescription,
            foodImage,
            foodIngredient,
            foodQuantities
        )
    }

    private fun orderNow(
        foodName: MutableList<String>,
        foodPrice: MutableList<String>,
        foodDescription: MutableList<String>,
        foodImage: MutableList<String>,
        foodIngredient: MutableList<String>,
        foodQuantities: MutableList<Int>
    ) {

        val intent = Intent(requireContext(), PayOutActivity::class.java)

        intent.putStringArrayListExtra("foodItemName", ArrayList(foodName))
        intent.putStringArrayListExtra("foodItemPrice", ArrayList(foodPrice))
        intent.putStringArrayListExtra("foodItemDescription", ArrayList(foodDescription))
        intent.putStringArrayListExtra("foodItemImage", ArrayList(foodImage))
        intent.putStringArrayListExtra("foodItemIngredient", ArrayList(foodIngredient))
        // ⭐ IMPORTANT LINE
        intent.putIntegerArrayListExtra(
            "foodItemQuantities",
            ArrayList(cartAdapter.getUpdatedItemsQuantities())
        )

        startActivity(intent)
    }
}