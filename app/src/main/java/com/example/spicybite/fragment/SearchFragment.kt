package com.example.spicybite.fragment

import com.example.spicybite.model.MenuItem
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.spicybite.R
import com.example.spicybite.databinding.FragmentSearchBinding
import android.widget.SearchView
import com.example.spicybite.adapter.MenuAdapter
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class SearchFragment : Fragment() {

    private lateinit var binding: FragmentSearchBinding
    private lateinit var adapter: MenuAdapter
    private lateinit var database: FirebaseDatabase
    private val originalMenuItems = mutableListOf<MenuItem>()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentSearchBinding.inflate(inflater, container, false)

        //retrieve the menu item from database
        retrieveMenuItems()


        //setup  for search view
        setupSearchView()


        return binding.root
    }

    private fun retrieveMenuItems() {
        //get dabase reference
        database = FirebaseDatabase.getInstance()
        //reference to the Menu node
        val foodReference = database.reference.child("menu")
        foodReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (foodSnapshot in snapshot.children) {
                    val menuItem = foodSnapshot.getValue(MenuItem::class.java)
                    menuItem?.let {
                        originalMenuItems.add(it)
                    }

                }
                showAllMenu()
            }

            override fun onCancelled(error: DatabaseError) {

            }
        }
        )
    }

    private fun showAllMenu() {
        val filteredMenuItem = ArrayList(originalMenuItems)
        setAdapter(filteredMenuItem)
    }

    private fun setAdapter(filteredMenuItems: ArrayList<MenuItem>) {
        adapter = MenuAdapter(filteredMenuItems, requireContext())
        binding.menuRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.menuRecyclerView.adapter = adapter
    }

    private fun setupSearchView() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                filterMenuItems(query)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterMenuItems(newText)
                return true
            }
        })
    }

    private fun filterMenuItems(query: String?) {

        val searchText = query ?: ""

        // agar search empty hai → sab items dikhao
        if (searchText.isEmpty()) {
            setAdapter(ArrayList(originalMenuItems))
            return
        }

        val filteredMenuItems = originalMenuItems.filter { item ->
            item.foodName?.contains(searchText, ignoreCase = true) == true
        }

        setAdapter(ArrayList(filteredMenuItems))
    }
}
