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

        val adapter = CartAdapter(requireContext(), cartList)

        binding.rvCart.layoutManager = LinearLayoutManager(requireContext())
        binding.rvCart.adapter = adapter

        // 🔹 Fetch cart data from Firebase
        database.addValueEventListener(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {

                cartList.clear()

                for (dataSnapshot in snapshot.children) {

                    val cartItem = dataSnapshot.getValue(CartItems::class.java)
                    cartItem?.itemKey = dataSnapshot.key
                    cartItem?.let {
                        cartList.add(it)
                    }
                }

                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })

        binding.proceedButton.setOnClickListener {
            val intent = Intent(requireContext(), PayOutActivity::class.java)
            startActivity(intent)
        }

        return binding.root
    }
}