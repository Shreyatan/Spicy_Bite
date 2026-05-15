package com.example.spicybite.adapter
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import com.bumptech.glide.Glide
import com.example.spicybite.DetailsActivity
import com.example.spicybite.databinding.MenuItemBinding
import com.example.spicybite.model.MenuItem

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

            val intent = Intent(requireContext, DetailsActivity::class.java).apply {
                putExtra("MenuItemName", item.foodName)
                putExtra("MenuItemPrice", item.foodPrice)
                putExtra("MenuItemDescription", item.foodDescription ?: "") // ✅ FIX
                putExtra("MenuItemIngredients", item.foodIngrediant ?: "")
                putExtra("MenuItemImage", item.foodImage)
            }

            requireContext.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = menuItems.size

    class MenuViewHolder(private val binding: MenuItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(menuItem: MenuItem) {

            binding.menuFoodName.text = menuItem.foodName ?: ""
            binding.menuPrice.text = "₹${menuItem.foodPrice ?: "0"}"

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
}