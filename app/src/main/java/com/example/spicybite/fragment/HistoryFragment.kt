package com.example.spicybite.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.spicybite.R
import com.example.spicybite.adapter.BuyAgainAdapter
import com.example.spicybite.databinding.FragmentHistoryBinding


class HistoryFragment : Fragment() {

    private lateinit var binding: FragmentHistoryBinding
    private lateinit var buyAgainAdapter: BuyAgainAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentHistoryBinding.inflate(inflater, container, false)

        setupRecyclerView()

        return binding.root
    }

    private fun setupRecyclerView() {

        val buyAgainFoodName = arrayListOf("Burger", "Pizza", "Sandwich")

        val buyAgainFoodPrice = arrayListOf("$10", "$20", "$30")

        val buyAgainFoodImage = arrayListOf(
            R.drawable.menu1,
            R.drawable.menu2,
            R.drawable.menu3
        )

        buyAgainAdapter = BuyAgainAdapter(
            buyAgainFoodName,
            buyAgainFoodPrice,
            buyAgainFoodImage
        )

        binding.buyAgainRecyclerView.layoutManager =
            LinearLayoutManager(requireContext())

        binding.buyAgainRecyclerView.adapter = buyAgainAdapter
    }
}