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
        holder.bind(position)
    }

    override fun getItemCount(): Int = menuItems.size

    inner class MenuViewHolder(private val binding: MenuItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        init{
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    openDetailsActivity(position)
                }


            }
        }

        private fun openDetailsActivity(position: Int) {
            val menuItem:MenuItem = menuItems[position]

            //intent to open details activity and pass data
            val intent = Intent(requireContext, DetailsActivity::class.java).apply{
                putExtra("MenuItemName",menuItem.foodName)
                putExtra("MenuItemPrice",menuItem.foodPrice)
                putExtra("MenuItemDescription",menuItem.foodDescription)
                putExtra("MenuItemIngredients",menuItem.foodIngrediant)
                putExtra("MenuItemImage",menuItem.foodImage)
            }

            //start the detail activity
            requireContext.startActivity(intent)
        }



    fun bind(position: Int) {

        val menuItem = menuItems[position]

        binding.apply {

            menuFoodName.text = menuItem.foodName
            menuPrice.text = menuItem.foodPrice

            val uri = Uri.parse(menuItem.foodImage)

            Glide.with(binding.root.context)
                .load(uri)
                .into(menuImage)
        }
    }
    }
}
