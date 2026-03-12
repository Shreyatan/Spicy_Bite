package com.example.spicybite.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.spicybite.DetailsActivity
import com.example.spicybite.databinding.PopularItemBinding

class PopularAdapter (private val items:List<String>,private val price:List<String>,private val image:List<Int>, private val requireContext: Context) : RecyclerView.Adapter<PopularAdapter.PopularViewHolder>(){
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): PopularViewHolder {
        return PopularViewHolder(PopularItemBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun onBindViewHolder(
        holder: PopularViewHolder,
        position: Int
    ) {
       val item= items[position]
        val images=image[position]
        val price=price[position]
        holder.bind(item,price,images)
        holder.itemView.setOnClickListener {
            //setonclick listener detailed
            val intent= Intent(requireContext, DetailsActivity::class.java)
            intent.putExtra("menuFoodName",items.get(position))
            intent.putExtra("menuImage",image.get(position))
            requireContext.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    class PopularViewHolder (private  val binding: PopularItemBinding): RecyclerView.ViewHolder(binding.root){
      private val imageView = binding.imgFood
        fun bind(item:String,price:String,images:Int){
            binding.tvFoodName.text=item
            binding.tvPrice.text=price
            imageView.setImageResource(images)

        }
    }
}