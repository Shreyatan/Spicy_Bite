package com.example.spicybite

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.spicybite.databinding.ActivityDetailsBinding
import com.example.spicybite.model.CartItems
import com.google.android.gms.auth.api.Auth
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class DetailsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailsBinding
    private var foodName: String? = null
    private var foodPrice: String? = null
    private var foodDescriptions: String? = null
    private var foodIngrediant : String? = null
    private var foodImage: String? = null
    private lateinit var auth: FirebaseAuth
    private var itemAvailable: Boolean = true
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailsBinding.inflate(layoutInflater)

        setContentView(binding.root)
        //init9alise firebase
        auth = FirebaseAuth.getInstance()

        foodName = intent.getStringExtra("MenuItemName")
        foodPrice = intent.getStringExtra("MenuItemPrice")
        foodDescriptions = intent.getStringExtra("MenuItemDescription")
        foodIngrediant= intent.getStringExtra("MenuItemIngredients")
        foodImage = intent.getStringExtra("MenuItemImage")
        itemAvailable = intent.getBooleanExtra("itemAvailable", true)

        with(binding) {
            detailFoodName.text = foodName
            descriptionTextView.text = foodDescriptions
            ingredientTextView.text = foodIngrediant?.replace("/n", "\n")

            Glide.with(this@DetailsActivity)
                .load(Uri.parse(foodImage))
                .into(detailedfoodimage)

            if (itemAvailable != true) {

                detailbutton.isEnabled = false
                detailbutton.text = "Out Of Stock"
                detailbutton.alpha = 0.6f
            }
        }
        binding.imageButton.setOnClickListener {
            finish()
        }
        binding.detailbutton.setOnClickListener {
            addItemToCart()
        }

    }

    private fun addItemToCart() {

        if (itemAvailable != true) {

            Toast.makeText(
                this,
                "Item is currently out of stock",
                Toast.LENGTH_SHORT
            ).show()

            return
        }
        val database = FirebaseDatabase.getInstance().reference
        val userId = auth.currentUser?.uid

        if (userId == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }
        val cartRef = database.child("user").child(userId).child("cartItems")

        cartRef.addListenerForSingleValueEvent(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {

                var itemExists = false

                for (dataSnapshot in snapshot.children) {

                    val item = dataSnapshot.getValue(CartItems::class.java)

                    if (item?.foodName == foodName) {

                        itemExists = true

                        val newQuantity = (item?.foodQuantity ?: 0) + 1

                        dataSnapshot.ref.child("foodQuantity").setValue(newQuantity)

                        Toast.makeText(
                            this@DetailsActivity,
                            "Item quantity updated",
                            Toast.LENGTH_SHORT
                        ).show()

                        break
                    }
                }

                if (!itemExists) {

                    val cartItem = CartItems(
                        foodName = foodName,
                        foodPrice = foodPrice,
                        foodDescription = foodDescriptions,
                        foodIngrediant = foodIngrediant,
                        foodQuantity = 1,
                        foodImage = foodImage
                    )

                    cartRef.push().setValue(cartItem)
                        .addOnSuccessListener {
                            Toast.makeText(
                                this@DetailsActivity,
                                "Item added to cart",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                }
            }

            override fun onCancelled(error: DatabaseError) {

                Toast.makeText(
                    this@DetailsActivity,
                    "Database Error",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }
}