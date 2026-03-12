package com.example.spicybite.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.spicybite.databinding.BuyAgainItemBinding


class BuyAgainAdapter(
    private val buyAgainFoodName: ArrayList<String>,
    private val buyAgainFoodPrice: ArrayList<String>,
    private val buyAgainFoodImage: ArrayList<Int>
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
            buyAgainFoodImage[position]
        )
    }

    override fun getItemCount(): Int = buyAgainFoodName.size


    inner class BuyAgainViewHolder(
        private val binding: BuyAgainItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(foodName: String, foodPrice: String, foodImage: Int) {

            binding.recentFoodName.text = foodName
            binding.recentFoodPrice.text = foodPrice
            binding.recentImage.setImageResource(foodImage)

            // Buy Again Button Click
            binding.root.setOnClickListener {
                // Yaha future me click logic daal sakti ho
            }
        }
    }
}