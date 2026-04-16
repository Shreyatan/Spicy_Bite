package com.example.spicybite

import android.os.Bundle

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.spicybite.adapter.MenuAdapter
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.example.spicybite.databinding.FragmentMenuBottomSheetBinding
import com.example.spicybite.model.CartItem
import com.example.spicybite.model.MenuItem
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

//
class MenuBottomSheetFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentMenuBottomSheetBinding? = null
    private val binding get() = _binding!!
    private lateinit var database: FirebaseDatabase
    private  var menuItem: MutableList<MenuItem> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentMenuBottomSheetBinding.inflate(inflater, container, false)

        binding.buttonBack.setOnClickListener {
            dismiss()
        }

        retrieveMenuItems()

        return binding.root
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun retrieveMenuItems() {
        database=FirebaseDatabase.getInstance()
        val foodRef: DatabaseReference=database.reference.child("menu")
        menuItem = mutableListOf()
        foodRef.addListenerForSingleValueEvent(object:ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                for (foodSnapshot in snapshot.children) {

                    val item: MenuItem? = foodSnapshot.getValue(MenuItem::class.java)

                    item?.let { menuItem.add(it) }

                }

                // ✅ Safe check if fragment still attached
                if (isAdded && context != null) {
                    setAdapter()
                }

        }

            override fun onCancelled(error: DatabaseError) {
            }
            })
        }



    private fun setAdapter() {

        if (!isAdded || _binding == null) return

        binding.menuRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.menuRecyclerView.adapter = MenuAdapter(menuItem, requireContext())
    }
}
