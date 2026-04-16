package com.example.spicybite.fragment

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.spicybite.LoginActivity
import com.example.spicybite.R
import com.example.spicybite.databinding.FragmentProfileBinding
import com.example.spicybite.model.UserModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class ProfileFragment : Fragment() {
    private lateinit var binding: FragmentProfileBinding


private val auth = FirebaseAuth.getInstance()
    private val database= FirebaseDatabase.getInstance()
//    private val databaseReference=database.reference.child("user")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        setUserData()
        binding.apply {

            name.isEnabled =false
            email.isEnabled =false
            address.isEnabled = false
            phone.isEnabled = false
        binding.editbutton.setOnClickListener {
            name.isEnabled =!name.isEnabled
            email.isEnabled =!email.isEnabled
            address.isEnabled =!address.isEnabled
            phone.isEnabled =!phone.isEnabled
        }

        }
        binding.savebutton.setOnClickListener {
            val name = binding.name.text.toString()
            val email = binding.email.text.toString()
            val address = binding.address.text.toString()
            val phone = binding.phone.text.toString()

            updateUserData(name,email,address,phone)



        }
        binding.logoutbutton.setOnClickListener {

            androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes") { _, _ ->

                    FirebaseAuth.getInstance().signOut()

                    val intent = Intent(requireContext(), LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
            // Inflate the layout for this fragment


            return binding.root
    }

    private fun updateUserData(name: String, email: String, address: String, phone: String) {
        val userId = auth.currentUser?.uid
        if(userId != null)
        {
            val userReference: DatabaseReference =
                database.reference.child("user").child(userId)
            val userData:HashMap<String,String> = hashMapOf(
                "name" to name,
                "email" to email,
                "address" to address,
                "phone" to phone
            )
            userReference.setValue(userData).addOnSuccessListener {
                Toast.makeText(requireContext(),"profile update successfully",Toast.LENGTH_SHORT).show()
            }.addOnFailureListener {
                Toast.makeText(requireContext() ,"Failed to update profile",Toast.LENGTH_SHORT).show()

            }


        }
    }

    private fun setUserData() {
        val userId = auth.currentUser?.uid
        if(userId != null){
            val userReference: DatabaseReference =
                database.getReference().child("user").child(userId)
            userReference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.exists()){
                        val userProfile = snapshot.getValue(UserModel::class.java)
                        if(userProfile != null){
                            binding.name.setText(userProfile.name)
                            binding.address.setText(userProfile.address)
                            binding.email.setText(userProfile.email)
                            binding.phone.setText(userProfile.phone)


                            }

                        }
                    }
                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(requireContext(),"Failed to fetch user data",Toast.LENGTH_SHORT).show()
                }
            })

        }

        }

    }


