package com.example.spicybite

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.spicybite.adapter.RecentOrderAdapter
import com.example.spicybite.databinding.ActivityRecentOrderItemsBinding
import com.example.spicybite.model.OrderModel

class recentOrderItems : AppCompatActivity() {

    private lateinit var binding: ActivityRecentOrderItemsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRecentOrderItemsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 🔥 GET DATA SAFELY
        val orderDetails = intent.getSerializableExtra("RecentBuyOrderItem") as? OrderModel

        val foodNames = orderDetails?.foodNames ?: arrayListOf()
        val foodPrices = orderDetails?.foodPrices ?: arrayListOf()
        val foodImages = orderDetails?.foodImages ?: arrayListOf()
        val foodQuantities = orderDetails?.foodQuantities ?: arrayListOf()

        // ❗ EMPTY CHECK (IMPORTANT)
        if (foodNames.isEmpty()) {
            binding.recyclerViewRecent.visibility = android.view.View.GONE
            return
        }

        // 🔥 RecyclerView Setup
        binding.recyclerViewRecent.layoutManager =
            LinearLayoutManager(this)

        binding.recyclerViewRecent.adapter =
            RecentOrderAdapter(foodNames, foodPrices, foodImages, foodQuantities)

        // 🔙 Back button
        binding.backButton.setOnClickListener {
            finish()
        }
    }
}