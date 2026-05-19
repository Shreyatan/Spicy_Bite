package com.example.spicybite.adapter
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.google.firebase.database.FirebaseDatabase
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.widget.Toast
import com.bumptech.glide.Glide
import com.example.spicybite.DetailsActivity
import com.example.spicybite.databinding.MenuItemBinding
import com.example.spicybite.model.MenuItem
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

@Suppress("DEPRECATION")
class MenuAdapter(
    private val menuItems: List<MenuItem>,
    private val requireContext: Context,
) : RecyclerView.Adapter<MenuAdapter.MenuViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuViewHolder {

        val binding = MenuItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MenuViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MenuViewHolder, position: Int) {
        holder.bind(menuItems[position])

        holder.itemView.setOnClickListener {
            val item = menuItems[position]
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
                putExtra("MenuItemName", item.foodName)
                putExtra("MenuItemPrice", item.foodPrice)
                putExtra("MenuItemDescription", item.foodDescription ?: "") // ✅ FIX
                putExtra("MenuItemIngredients", item.foodIngrediant ?: "")
                putExtra("MenuItemImage", item.foodImage)
                putExtra("itemAvailable", item.itemAvailable)
            }

            requireContext.startActivity(intent)
        }
        loadRating(menuItems[position].foodName, holder)
    }
    override fun getItemCount(): Int = menuItems.size

        class MenuViewHolder(val binding: MenuItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(menuItem: MenuItem) {


            binding.menuFoodName.text = menuItem.foodName ?: ""
            binding.menuPrice.text = "₹${menuItem.foodPrice ?: "0"}"

            if (menuItem.itemAvailable == true) {
                binding.root.alpha = 1.0f
            } else {
                binding.root.alpha = 0.5f
            }
            binding.menuCategory.text = menuItem.foodCategory ?: "Food"

            binding.menuStatus.text =
                if (menuItem.itemAvailable == true)
                    "🟢 Available"
                else
                    "🔴 Out of stock"

            Glide.with(binding.root.context)
                .load(menuItem.foodImage)
                .into(binding.menuImage)
        }
    }
    private fun loadRating(
        foodName: String?,
        holder: MenuViewHolder
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