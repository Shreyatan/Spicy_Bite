package com.example.spicybite.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.spicybite.R
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.spicybite.adapter.NotificationAdapter
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.example.spicybite.databinding.FragmentNotificationBottomBinding



class Notification_Bottom_Fragment: BottomSheetDialogFragment() {

    private lateinit var binding: FragmentNotificationBottomBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout using ViewBinding
        binding = FragmentNotificationBottomBinding.inflate(inflater, container, false)
        val notifications = listOf(
            "Your order has been canceled successfully",
            "Order has been taken by the driver",
            "Congratulations! Your order is on the way"
        )
        val notificationImages: List<Int> = listOf(
            R.drawable.sademoji,
            R.drawable.truck,
            R.drawable.congrats
        )
        val adapter = NotificationAdapter(notifications, notificationImages)
    binding.notificationRecyclerView.layoutManager= LinearLayoutManager(requireContext())
        binding.notificationRecyclerView.adapter = adapter
        return binding.root
    }

    companion object {
    }
}