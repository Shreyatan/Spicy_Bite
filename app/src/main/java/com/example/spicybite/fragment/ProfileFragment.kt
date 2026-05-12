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
            houseNo.isEnabled = false
            street.isEnabled = false
            city.isEnabled = false
            state.isEnabled = false
            pincode.isEnabled = false
            phone.isEnabled = false
            binding.editbutton.setOnClickListener {

                val enable = !binding.name.isEnabled

                binding.name.isEnabled = enable
                binding.email.isEnabled = enable
                binding.houseNo.isEnabled = enable
                binding.street.isEnabled = enable
                binding.city.isEnabled = enable
                binding.state.isEnabled = enable
                binding.pincode.isEnabled = enable
                binding.phone.isEnabled = enable

                binding.houseNo.requestFocus()
            }
        }
        binding.savebutton.setOnClickListener {

            val fullName = binding.name.text.toString().trim()
            val nameParts = fullName.trim().split("\\s+".toRegex())

            val firstName = nameParts.getOrNull(0) ?: ""

            val middleName =
                if (nameParts.size > 2)
                    nameParts.subList(1, nameParts.size - 1).joinToString(" ")
                else
                    ""

            val lastName =
                if (nameParts.size >= 2)
                    nameParts.last()
                else
                    ""

            val email = binding.email.text.toString()
            val houseNo = binding.houseNo.text.toString()
            val street = binding.street.text.toString()
            val city = binding.city.text.toString()
            val state = binding.state.text.toString()
            val pincode = binding.pincode.text.toString()
            val phone = binding.phone.text.toString()
            if (firstName.isEmpty()) {
                binding.name.error = "Enter first name"
            }
            else if (!firstName.matches(Regex("^[A-Za-z ]+$"))) {
                binding.name.error = "Only alphabets allowed"
            }
            else if (
                middleName.isNotEmpty() &&
                !middleName.matches(Regex("^[A-Za-z ]+$"))
            ) {
                binding.name.error = "Middle name should contain only alphabets"
            }

            else if (
                lastName.isNotEmpty() &&
                !lastName.matches(Regex("^[A-Za-z ]+$"))
            ) {
                binding.name.error = "Last name should contain only alphabets"
            }
            else if (email.isEmpty()) {
                binding.email.error = "Enter email"
            }
            else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.email.error = "Invalid email"
            }
            else if (houseNo.isEmpty()) {
                binding.houseNo.error = "Enter house number"
            }
            else if (street.isEmpty()) {
                binding.street.error = "Enter street"
            }
            else if (city.isEmpty()) {
                binding.city.error = "Enter city"
            }
            else if (!city.matches(Regex("^[A-Za-z\\s-]+$"))) {
                binding.city.error = "Only alphabets allowed"
            }
            else if (state.isEmpty()) {
                binding.state.error = "Enter state"
            }
            else if (!state.matches(Regex("^[A-Za-z\\s-]+$"))) {
                binding.state.error = "Only alphabets allowed"
            }
            else if (pincode.isEmpty()) {
                binding.pincode.error = "Enter pincode"
            }
            else if (!pincode.matches(Regex("^[0-9]{6}$"))) {
                binding.pincode.error = "Enter valid 6-digit pincode"
            }
            else if (phone.isEmpty()) {
                binding.phone.error = "Enter phone number"
            }
            else if (!phone.matches(Regex("^[6-9][0-9]{9}$"))) {
                binding.phone.error = "Enter valid 10-digit number"
            }
            else {

                updateUserData(
                    firstName,
                    middleName,
                    lastName,
                    email,
                    houseNo,
                    street,
                    city,
                    state,
                    pincode,
                    phone
                )
            }
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

    private fun updateUserData(
        firstName: String,
        middleName: String,
        lastName: String,
        email: String,
        houseNo: String,
        street: String,
        city: String,
        state: String,
        pincode: String,
        phone: String
    ) {
        val fullName = "$firstName $middleName $lastName".trim()
        // ✅ Full Address Combine
        val fullAddress =
            "$houseNo, $street, $city, $state - $pincode"
        val userId = auth.currentUser?.uid
        if(userId != null)
        {
            val userReference: DatabaseReference =
                database.reference.child("user").child(userId)
            val userData:HashMap<String,String> = hashMapOf(
                "name" to fullName,
                "firstName" to firstName,
                "middleName" to middleName,
                "lastName" to lastName,
                "email" to email,
                "address" to fullAddress,
                "houseNo" to houseNo,
                "street" to street,
                "city" to city,
                "state" to state,
                "pincode" to pincode,
                "phone" to phone
            )
            // ✅ First update Authentication email
            auth.currentUser?.updateEmail(email)
                ?.addOnSuccessListener {

                    // ✅ Then update Realtime Database
                    userReference.updateChildren(userData.toMap())
                        .addOnSuccessListener {

                            Toast.makeText(
                                requireContext(),
                                "Profile updated successfully",
                                Toast.LENGTH_SHORT
                            ).show()

                            // ✅ Disable fields after save
                            binding.name.isEnabled = false
                            binding.email.isEnabled = false
                            binding.houseNo.isEnabled = false
                            binding.street.isEnabled = false
                            binding.city.isEnabled = false
                            binding.state.isEnabled = false
                            binding.pincode.isEnabled = false
                            binding.phone.isEnabled = false

                        }
                        .addOnFailureListener {

                            Toast.makeText(
                                requireContext(),
                                "Failed to update profile",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                }
                ?.addOnFailureListener {

                    Toast.makeText(
                        requireContext(),
                        "Authentication email update failed",
                        Toast.LENGTH_LONG
                    ).show()
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
                            val fullName = listOf(
                                userProfile.firstName,
                                userProfile.middleName,
                                userProfile.lastName
                            ).filter { !it.isNullOrEmpty() }
                                .joinToString(" ")

                            binding.name.setText(fullName)
                            binding.houseNo.setText(userProfile.houseNo)
                            binding.street.setText(userProfile.street)
                            binding.city.setText(userProfile.city)
                            binding.state.setText(userProfile.state)
                            binding.pincode.setText(userProfile.pincode)
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


