package com.example.spicybite.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.spicybite.databinding.RecentBuyItemBinding

class RecentOrderAdapter(
    private val names: ArrayList<String>,
    private val prices: ArrayList<String>,
    private val images: ArrayList<String>,
    private val quantities: ArrayList<Int>
) : RecyclerView.Adapter<RecentOrderAdapter.ViewHolder>() {

   class ViewHolder(val binding: RecentBuyItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = RecentBuyItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = names.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.binding.foodName.text = names[position]
        holder.binding.foodPrice.text = prices[position]

        val qty = if (position < quantities.size) quantities[position] else 1
        holder.binding.foodQuantity.text = qty.toString()

        Glide.with(holder.itemView.context)
            .load(images[position])
            .into(holder.binding.foodImage)
    }
}