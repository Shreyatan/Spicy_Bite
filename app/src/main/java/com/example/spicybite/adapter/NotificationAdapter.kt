package com.example.spicybite.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.spicybite.databinding.NotificationItemBinding
import java.util.ArrayList

class NotificationAdapter(
    private val notifications: List<String>,
    private val notificationImages: List<Int>
) : RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val binding = NotificationItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return NotificationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        val message = notifications[position]
        val imageRes = notificationImages[position]
        holder.bind(notifications[position], notificationImages[position])
    }

    override fun getItemCount(): Int = notifications.size

    inner class NotificationViewHolder(private val binding: NotificationItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(message: String, imageRes: Int) {   binding.notificationtextview.text = message
            binding.notificationimageview.setImageResource(imageRes)
            }
        }
}