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

class SearchFragment : Fragment() {

    private lateinit var binding: FragmentSearchBinding
    private lateinit var adapter: MenuAdapter
    private val originalMenuItems = listOf(
        MenuItem("Burger","$15","","",""),
        MenuItem("Sandwich","$58","","",""),
        MenuItem("Noodles","$19","","",""),
        MenuItem("Pizza","$118","","",""),
        MenuItem("Pasta","$110","","",""),
        MenuItem("Momos","$25","","","")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }
    private val filteredMenuItems = mutableListOf<MenuItem>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding=FragmentSearchBinding.inflate(inflater,container,false)
        adapter = MenuAdapter(filteredMenuItems, requireContext())
        LinearLayoutManager(requireContext()).also { binding.menuRecyclerView.layoutManager = it }
        binding.menuRecyclerView.adapter=adapter


        //setup  for search view
        setupSearchView()

        // set up for show all menu items
        showAllMenu()
        return binding.root
    }

    private fun showAllMenu() {

        filteredMenuItems.clear()
        filteredMenuItems.addAll(originalMenuItems)

        adapter.notifyDataSetChanged()
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

        filteredMenuItems.clear()

        originalMenuItems.forEach { item ->

            if (item.foodName?.contains(query ?: "", ignoreCase = true) == true) {
                filteredMenuItems.add(item)
            }
        }

        adapter.notifyDataSetChanged()
    }



    companion object {
    }
}