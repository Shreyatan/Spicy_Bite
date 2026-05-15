package com.example.spicybite.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.spicybite.DetailsActivity
import com.example.spicybite.databinding.PopularItemBinding
import com.example.spicybite.model.MenuItem

class PopularAdapter(
    private val items: List<MenuItem>,
    private val requireContext: Context
) : RecyclerView.Adapter<PopularAdapter.PopularViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PopularViewHolder {
        val binding = PopularItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PopularViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PopularViewHolder, position: Int) {
        holder.bind(items[position])

        holder.itemView.setOnClickListener {
            val item = items[position]

            val intent = Intent(requireContext, DetailsActivity::class.java).apply {
                putExtra("menuFoodName", item.foodName)
                putExtra("menuImage", item.foodImage)
                putExtra("menuPrice", item.foodPrice)
                putExtra("menuCategory", item.foodCategory)
                putExtra("menuDescription", item.foodDescription)
            }

            requireContext.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = items.size

    class PopularViewHolder(
        private val binding: PopularItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: MenuItem) {

            binding.tvFoodName.text = item.foodName
            binding.tvPrice.text = "₹${item.foodPrice}"

            // Category
            binding.menuCategory.text = item.foodCategory ?: "Category"

            // Status
            binding.menuStatus.text = if (item.itemAvailable == true) {
                "🟢 Available"
            } else {
                "🔴 Out of Stock"
            }

            // Image
            Glide.with(binding.root.context)
                .load(item.foodImage)
                .into(binding.imgFood)

            // Add to cart text (optional UI)
            binding.menuAddToCart.text = "Add To Cart"
        }
    }
}