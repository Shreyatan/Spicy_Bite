package com.example.spicybite.fragment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.graphics.Color
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.spicybite.FeedbackBottomSheet
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
            mutableListOf(),
            mutableListOf(),
            mutableListOf(),
            mutableListOf()
        ) { position ->
            // 🔥 IMPORTANT: +1 lagana hai
            val selectedOrder = listOfOrderItem.getOrNull(position + 1)
                ?: return@BuyAgainAdapter

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
        binding.recentCard.setOnClickListener {
            seeItemRecentBuy()
        }


        return binding.root
    }



    private fun seeItemRecentBuy() {
        if (listOfOrderItem.isEmpty()) {
            Toast.makeText(requireContext(), "No orders found", Toast.LENGTH_SHORT).show()
            return
        }

        val recentBuy = listOfOrderItem.getOrNull(0)

        val intent = Intent(requireContext(), recentOrderItems::class.java)
        intent.putExtra("RecentBuyOrderItem", recentBuy)
        startActivity(intent)
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

        val status = recentItem.status?.trim()?.lowercase()
        val feedbackDone = recentItem.feedbackSubmitted == true
        if (status == "delivered" && recentItem.paymentReceived == true) {

            if (feedbackDone) {
                binding.btnRate.visibility = View.GONE
            } else {
                binding.btnRate.visibility = View.VISIBLE
            }

        } else {
            binding.btnRate.visibility = View.GONE
        }
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


        val otp = recentItem.deliveryOTP ?: ""

        when (status) {

            "delivered" -> {
                if (recentItem.paymentReceived == true) {
                    binding.statusText.text = "Delivered ✅"
                    binding.orderestatus.setCardBackgroundColor(Color.parseColor("#2E7D32")) // deep green
                } else {
                    binding.statusText.text = "Payment Pending 💰"
                    binding.orderestatus.setCardBackgroundColor(Color.parseColor("#D32F2F") )// soft red)
                }

                binding.otpText.visibility = View.GONE   // ❌ hide after delivery
            }

            "on the way", "arrived" -> {

                binding.statusText.text = if (status == "on the way")
                    "On The Way 🚚" else "Arrived 📍"

                binding.orderestatus.setCardBackgroundColor(
                    if (status == "on the way")
                        Color.parseColor("#1976D2") // calm blue)
                    else
                        Color.parseColor("#7B1FA2") // elegant purple)
                )

                // 🔥 SHOW OTP HERE
                binding.otpText.visibility = View.VISIBLE
                binding.otpText.text =
                    if (otp.isNotEmpty()) "Delivery OTP: $otp"
                    else "Waiting for OTP..."
            }

            "picked up" -> {
                binding.statusText.text = "Picked Up 📦"
                binding.orderestatus.setCardBackgroundColor(Color.parseColor("#F9A825") // warm amber
                )
                binding.otpText.visibility = View.GONE
            }

            else -> {

                binding.statusText.text = "Order Placed 🕐"
                binding.orderestatus.setCardBackgroundColor(
                    Color.parseColor("#757575") // neutral gray
                )
                binding.otpText.visibility = View.GONE
            }
        }
        binding.btnRate.setOnClickListener {
            val orderId = recentItem.itemPushKey ?: return@setOnClickListener
            val foodName = recentItem.foodNames?.firstOrNull() ?: ""

            val sheet = FeedbackBottomSheet(
                orderId,
                foodName
            )
            sheet.show(parentFragmentManager, "FeedbackSheet")
        }
    }
    private fun setPreviousBuyItemsRecyclerView() {
        val orderIdList = mutableListOf<String>()
        val buyAgainFoodName = mutableListOf<String>()
        val buyAgainFoodPrice = mutableListOf<String>()
        val buyAgainFoodImage = mutableListOf<String>()
        val feedbackStatusList = mutableListOf<Boolean>()
        val orderStatusList = mutableListOf<String>()

        for (i in 1 until listOfOrderItem.size) {

            val item = listOfOrderItem[i]

            val name = item.foodNames?.firstOrNull()
            val price = item.foodPrices?.firstOrNull()
            val image = item.foodImages?.firstOrNull()

            feedbackStatusList.add(item.feedbackSubmitted == true)
            orderStatusList.add(item.status ?: "")

            orderIdList.add(item.itemPushKey ?: "")

            if (name != null) buyAgainFoodName.add(name)
            if (price != null) buyAgainFoodPrice.add(price)
            if (image != null) buyAgainFoodImage.add(image)
        }

        buyAgainAdapter.buyAgainFoodName.clear()
        buyAgainAdapter.buyAgainFoodPrice.clear()
        buyAgainAdapter.buyAgainFoodImage.clear()
        buyAgainAdapter.feedbackStatus.clear()
        buyAgainAdapter.orderStatus.clear()
        buyAgainAdapter.orderIdList.clear()

        buyAgainAdapter.buyAgainFoodName.addAll(buyAgainFoodName)
        buyAgainAdapter.buyAgainFoodPrice.addAll(buyAgainFoodPrice)
        buyAgainAdapter.buyAgainFoodImage.addAll(buyAgainFoodImage)
        buyAgainAdapter.feedbackStatus.addAll(feedbackStatusList)
        buyAgainAdapter.orderStatus.addAll(orderStatusList)
        buyAgainAdapter.orderIdList.addAll(orderIdList)
        buyAgainAdapter.notifyDataSetChanged()
    }
}
