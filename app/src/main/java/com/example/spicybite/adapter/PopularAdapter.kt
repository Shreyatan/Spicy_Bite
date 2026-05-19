package com.example.spicybite.adapter

import android.content.Context
import android.content.Intent
import com.google.firebase.database.FirebaseDatabase
import android.view.LayoutInflater
import android.view.ViewGroup
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import android.widget.Toast
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
            // ✅ CHECK STOCK
            if (item.itemAvailable != true) {

                Toast.makeText(
                    requireContext,
                    "Item is currently out of stock",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            val intent = Intent(requireContext, DetailsActivity::class.java).apply {
                putExtra("menuFoodName", item.foodName)
                putExtra("menuImage", item.foodImage)
                putExtra("menuPrice", item.foodPrice)
                putExtra("menuCategory", item.foodCategory)
                putExtra("menuDescription", item.foodDescription)
                putExtra("itemAvailable", item.itemAvailable)
            }

            requireContext.startActivity(intent)
        }
        loadRating(items[position].foodName, holder)
    }

    override fun getItemCount(): Int = items.size

    class PopularViewHolder(
        val binding: PopularItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: MenuItem) {

            binding.tvFoodName.text = item.foodName
            if (item.itemAvailable == true) {
                binding.root.alpha = 1.0f
            } else {
                binding.root.alpha = 0.5f
            }
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
    private fun loadRating(
        foodName: String?,
        holder: PopularViewHolder
    ) {

        if (foodName.isNullOrEmpty()) return

        FirebaseDatabase.getInstance()
            .reference
            .child("Feedback")
            .addValueEventListener(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {

                    var totalRating = 0f
                    var count = 0

                    for (feedbackSnap in snapshot.children) {

                        val dbFoodName = feedbackSnap
                            .child("foodName")
                            .getValue(String::class.java)
                            ?.trim()
                            ?.lowercase()

                        val currentFoodName = foodName
                            ?.trim()
                            ?.lowercase()

                        if (dbFoodName == currentFoodName) {

                            val ratingValue =
                                feedbackSnap.child("rating").value

                            val rating = when (ratingValue) {
                                is Long -> ratingValue.toFloat()
                                is Double -> ratingValue.toFloat()
                                is Float -> ratingValue
                                else -> null
                            }

                            if (rating != null) {
                                totalRating += rating
                                count++
                            }
                        }
                    }

                    if (count > 0) {

                        val avg = totalRating / count

                        holder.binding.tvRating.text =
                            "⭐ %.1f ($count)".format(avg)

                    } else {

                        holder.binding.tvRating.text = "⭐ New"
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })


    }
    }