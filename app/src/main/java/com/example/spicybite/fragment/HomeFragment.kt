package com.example.spicybite.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.denzcoskun.imageslider.constants.ScaleTypes
import com.denzcoskun.imageslider.interfaces.ItemClickListener
import com.denzcoskun.imageslider.models.SlideModel
import com.example.spicybite.R
import com.example.spicybite.MenuBottomSheetFragment
import com.example.spicybite.adapter.MenuAdapter
import com.example.spicybite.databinding.FragmentHomeBinding
import com.example.spicybite.model.MenuItem
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var database: FirebaseDatabase
    private lateinit var menuItem: MutableList<MenuItem>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        binding.btnViewMenu.setOnClickListener {
            val bottomSheetDialog = MenuBottomSheetFragment()
            bottomSheetDialog.show(parentFragmentManager, "menu")
        }

        retrieveAndDisplayPopularItems()

        return binding.root
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun retrieveAndDisplayPopularItems() {
        database = FirebaseDatabase.getInstance()
        val foodRef = database.reference.child("menu")
        menuItem = mutableListOf()

        //retreive menu item from the database
        foodRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (foodSnapshot in snapshot.children) {
                    val item: MenuItem? = foodSnapshot.getValue(MenuItem::class.java)
                    item?.let { menuItem.add(it) }
                }

                //display popular item in recyclerview
                randomPopularItems()
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private fun randomPopularItems() {

        if (menuItem.isEmpty()) return

        val index = menuItem.indices.toList().shuffled()
        val numItemToShow = minOf(6, menuItem.size)

        val subsetMenuItems = index.take(numItemToShow).map {
            menuItem[it]
        }

        setPopularAdapter(subsetMenuItems)
    }
    private fun setPopularAdapter(subsetMenuItems: List<MenuItem>) {

        if (!isAdded) return

        val adapter = MenuAdapter(subsetMenuItems, requireContext())
        binding.rvPopular.layoutManager = LinearLayoutManager(requireContext())
        binding.rvPopular.adapter = adapter
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val imageList = ArrayList<SlideModel>()
        imageList.add(SlideModel(R.drawable.banner1, ScaleTypes.FIT))
        imageList.add(SlideModel(R.drawable.banner2, ScaleTypes.FIT))
        imageList.add(SlideModel(R.drawable.banner3, ScaleTypes.FIT))

        binding.imageSlider.setImageList(imageList, ScaleTypes.FIT)
        binding.imageSlider.setItemClickListener(object : ItemClickListener {
            override fun onItemSelected(position: Int) {
                val itemMessage = "Selected Image $position"
                Toast.makeText(requireContext(), itemMessage, Toast.LENGTH_SHORT).show()
            }

            override fun doubleClick(position: Int) {

            }
        })
    }
}