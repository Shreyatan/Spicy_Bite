package com.example.spicybite.adapter

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.View
import androidx.fragment.app.FragmentActivity
import com.example.spicybite.FeedbackBottomSheet
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.spicybite.PayOutActivity
import com.example.spicybite.databinding.BuyAgainItemBinding


class BuyAgainAdapter(
    val buyAgainFoodName: MutableList<String>,
    val buyAgainFoodPrice: MutableList<String>,
    val buyAgainFoodImage: MutableList<String>,
    val feedbackStatus: MutableList<Boolean>,
    val orderStatus: MutableList<String>,
    val orderIdList: MutableList<String>,
    private val onItemClick: (Int) -> Unit
) : RecyclerView.Adapter<BuyAgainAdapter.BuyAgainViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BuyAgainViewHolder {
        val binding = BuyAgainItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return BuyAgainViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BuyAgainViewHolder, position: Int) {
        holder.bind(
            buyAgainFoodName[position],
            buyAgainFoodPrice[position],
            buyAgainFoodImage[position],
            feedbackStatus[position],
            orderStatus[position],
            orderIdList[position]
        )
    }

    override fun getItemCount(): Int = buyAgainFoodName.size


    inner class BuyAgainViewHolder(
        private val binding: BuyAgainItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(
            foodName: String,
            foodPrice: String,
            foodImage: String,
            feedbackDone: Boolean,
            status: String,
            orderId: String
        ){

            binding.recentFoodName.text = foodName
            binding.recentFoodPrice.text = foodPrice
            if (!foodImage.isNullOrEmpty()) {
                val uri = Uri.parse(foodImage)
                Glide.with(binding.root.context)
                    .load(uri)
                    .into(binding.recentImage)
            }


            val orderStatusLower = status.trim().lowercase()

            if (orderStatusLower == "delivered" && !feedbackDone) {
                binding.btnRateNow.visibility = View.VISIBLE
            } else {
                binding.btnRateNow.visibility = View.GONE
            }
            // ✅ 1. ITEM CLICK → OPEN DETAILS
            binding.root.setOnClickListener {
                onItemClick(adapterPosition)
            }

            // ✅ 2. BUTTON CLICK → DIRECT BUY AGAIN
            binding.btnBuyAgain.setOnClickListener {

                val context = binding.root.context

                val intent = Intent(context, PayOutActivity::class.java)

                intent.putStringArrayListExtra("foodItemName", arrayListOf(foodName))
                intent.putStringArrayListExtra("foodItemPrice", arrayListOf(foodPrice))
                intent.putStringArrayListExtra("foodItemImage", arrayListOf(foodImage))
                intent.putStringArrayListExtra("foodItemDescription", arrayListOf(""))
                intent.putStringArrayListExtra("foodItemIngredient", arrayListOf(""))
                intent.putIntegerArrayListExtra("foodItemQuantities", arrayListOf(1))

                context.startActivity(intent)
            }
            binding.btnRateNow.setOnClickListener {

                val context = binding.root.context

                if (context is FragmentActivity) {


                    val sheet = FeedbackBottomSheet(
                        orderId,
                        foodName
                    )

                    sheet.show(context.supportFragmentManager, "FeedbackSheet")


                }
            }
        }
    }
}
