package com.example.spicybite.fragment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.graphics.Color
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.spicybite.R
import com.example.spicybite.adapter.BuyAgainAdapter
import com.example.spicybite.databinding.FragmentHistoryBinding
import com.example.spicybite.model.OrderModel
import com.example.spicybite.recentOrderItems
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class HistoryFragment : Fragment() {

    private lateinit var binding: FragmentHistoryBinding
    private lateinit var buyAgainAdapter: BuyAgainAdapter
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var userId: String
    private var listOfOrderItem: MutableList<OrderModel> = mutableListOf()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentHistoryBinding.inflate(inflater, container, false)
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

// ✅ Initialize adapter FIRST
        buyAgainAdapter = BuyAgainAdapter(
            mutableListOf(),
            mutableListOf(),
            mutableListOf()
        ) { position ->

            // 🔥 IMPORTANT: +1 lagana hai
            val selectedOrder = listOfOrderItem[position + 1]

            val intent = Intent(requireContext(), recentOrderItems::class.java)
            intent.putExtra("RecentBuyOrderItem", selectedOrder)
            startActivity(intent)
        }

// ✅ Setup RecyclerView
        binding.buyAgainRecyclerView.layoutManager =
            LinearLayoutManager(requireContext())

        binding.buyAgainRecyclerView.adapter = buyAgainAdapter
        // Optional performance fix
        binding.buyAgainRecyclerView.isNestedScrollingEnabled = false
        //retrieve and display the user order history
        retrieveBuyHistory()
        binding.recentbuyitem.setOnClickListener {
            seeItemRecentBuy()
        }


        return binding.root
    }



    private fun seeItemRecentBuy() {
        listOfOrderItem.firstOrNull()?.let { recentBuy ->
            val intent = Intent(requireContext(), recentOrderItems::class.java)
            intent.putExtra("RecentBuyOrderItem", recentBuy)
            startActivity(intent)
        }
    }

    private fun retrieveBuyHistory() {
        binding.recentbuyitem.visibility = View.INVISIBLE
        userId = auth.currentUser?.uid ?: ""
        val buyItemReference = database.reference.child("user").child(userId).child("BuyHistory")
        val shortingQuery = buyItemReference.orderByChild("currentTime")

        shortingQuery.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listOfOrderItem.clear()
                for (buySnapShot in snapshot.children) {
                    val buyHistoryItem = buySnapShot.getValue(OrderModel::class.java)

                    buyHistoryItem?.let {
                        listOfOrderItem.add(it)
                    }
                }
                listOfOrderItem.reverse()
                if (listOfOrderItem.isNotEmpty()) {
                    setDataInRecentBuyItem()

                    setPreviousBuyItemsRecyclerView()
                }

            }

            override fun onCancelled(error: DatabaseError) {
            }
        })

    }


    private fun setDataInRecentBuyItem() {

        if (listOfOrderItem.isEmpty()) return

        val recentItem = listOfOrderItem.first()

        Log.d("DEBUG", "Accepted: ${recentItem.orderAccepted}")
        Log.d("DEBUG", "Payment: ${recentItem.paymentReceived}")

        binding.recentbuyitem.visibility = View.VISIBLE

        binding.recentFoodName.text =
            recentItem.foodNames?.firstOrNull() ?: ""

        binding.recentFoodPrice.text =
            recentItem.foodPrices?.firstOrNull() ?: ""

        val image = recentItem.foodImages?.firstOrNull()

        if (!image.isNullOrEmpty()) {
            val uri = Uri.parse(image)
            Glide.with(requireContext())
                .load(uri)
                .into(binding.recentImage)
        }

        val status = recentItem.status?.trim()?.lowercase()
        val isPaymentReceived = recentItem.paymentReceived ?: false

        when (status) {

            "delivered" -> {
                if (recentItem.paymentReceived == true) {
                    binding.statusText.text = "Delivered ✅"
                    binding.orderestatus.setCardBackgroundColor(Color.parseColor("#4CAF50")) // Green
                } else {
                    binding.statusText.text = "Payment Pending 💰"
                    binding.orderestatus.setCardBackgroundColor(Color.parseColor("#F44336")) // Red
                }
            }

            "on the way" -> {
                binding.statusText.text = "On The Way 🚚"
                binding.orderestatus.setCardBackgroundColor(Color.parseColor("#2196F3")) // Blue
            }

            "picked up" -> {
                binding.statusText.text = "Picked Up 📦"
                binding.orderestatus.setCardBackgroundColor(Color.parseColor("#FFC107")) // Amber
            }

            "arrived" -> {   // 🔥 ADD THIS
                binding.statusText.text = "Arrived 📍"
                binding.orderestatus.setCardBackgroundColor(Color.parseColor("#9C27B0")) // Purple
            }

            else -> {
                binding.statusText.text = "Pending ⏳"
                binding.orderestatus.setCardBackgroundColor(Color.parseColor("#9E9E9E")) // Grey
            }
        }
    }

    private fun setPreviousBuyItemsRecyclerView() {

        val buyAgainFoodName = mutableListOf<String>()
        val buyAgainFoodPrice = mutableListOf<String>()
        val buyAgainFoodImage = mutableListOf<String>()

        for (i in 1 until listOfOrderItem.size) {
            listOfOrderItem[i].foodNames?.firstOrNull()?.let { name ->
                buyAgainFoodName.add(name)

                listOfOrderItem[i].foodPrices?.firstOrNull()?.let { price ->
                    buyAgainFoodPrice.add(price)

                    listOfOrderItem[i].foodImages?.firstOrNull()?.let { image ->
                        buyAgainFoodImage.add(image)
                    }
                }
            }
        }

        // ✅ Update existing adapter data
        buyAgainAdapter.apply {
            this.buyAgainFoodName.clear()
            this.buyAgainFoodPrice.clear()
            this.buyAgainFoodImage.clear()

            this.buyAgainFoodName.addAll(buyAgainFoodName)
            this.buyAgainFoodPrice.addAll(buyAgainFoodPrice)
            this.buyAgainFoodImage.addAll(buyAgainFoodImage)

            notifyDataSetChanged()
        }
    }
}
