package com.example.spicybite

import android.os.Bundle
import android.widget.Toast

import androidx.appcompat.app.AppCompatActivity

import com.google.firebase.auth.FirebaseAuth

import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.example.spicybite.databinding.ActivityPayOutBinding
import com.example.spicybite.model.OrderModel

class PayOutActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference

    private lateinit var name: String
    private lateinit var address: String
    private lateinit var phone: String
    private lateinit var totalAmount: String

    private var foodItemName: ArrayList<String> = arrayListOf()
    private var foodItemPrice: ArrayList<String> = arrayListOf()
    private var foodItemImage: ArrayList<String> = arrayListOf()
    private var foodItemDescription: ArrayList<String> = arrayListOf()
    private var foodItemIngredient: ArrayList<String> = arrayListOf()
    private var foodItemQuantities: ArrayList<Int> = arrayListOf()
    lateinit var binding: ActivityPayOutBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPayOutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().reference
        setUserData()

        foodItemName = intent.getStringArrayListExtra("foodItemName") ?: arrayListOf()
        foodItemPrice = intent.getStringArrayListExtra("foodItemPrice") ?: arrayListOf()
        foodItemImage = intent.getStringArrayListExtra("foodItemImage") ?: arrayListOf()
        foodItemDescription = intent.getStringArrayListExtra("foodItemDescription") ?: arrayListOf()
        foodItemIngredient = intent.getStringArrayListExtra("foodItemIngredient") ?: arrayListOf()

        val receivedQuantities = intent.getIntegerArrayListExtra("foodItemQuantities")
        foodItemQuantities = receivedQuantities ?: arrayListOf()

        val prices = foodItemPrice
        val quantities = if (foodItemQuantities.isNotEmpty()) {
            foodItemQuantities
        }
        else {
            ArrayList(List(prices.size) { 1 })
        }
        totalAmount = if (prices.isEmpty()) {
            "₹0"
        } else {
            "₹" + calculateTotalAmount(prices, quantities).toString()
        }
        
        binding.etTotalAmount.isEnabled = false
        binding.etTotalAmount.setText(totalAmount)
        binding.backButton.setOnClickListener {
            finish()
        }


        binding.PlaceMyOrder.setOnClickListener {
            //get data from textview
            name = binding.name.text.toString()
            address = binding.address.text.toString()
            phone = binding.phone.text.toString()
            if(name.isBlank()||address.isBlank()||phone.isBlank()){
                Toast.makeText(this,"please enter all details",Toast.LENGTH_SHORT).show()

            }else{
                placeOrder()
            }
        }
    }

    private fun placeOrder() {

        val userId = auth.currentUser?.uid ?: ""
        val time = System.currentTimeMillis()
        val orderId = databaseReference.child("Orders").push().key!!
        val restaurantLat = 26.8435   // Banasthali (example)
        val restaurantLng = 75.5660

        val customerLat = 26.9124     // dynamic hona chahiye ideally
        val customerLng = 75.7873
        val order = OrderModel(
            itemPushKey = orderId,
            userUid = userId,
            userName = name,
            address = address,
            phoneNumber = phone,
            totalPrice = totalAmount,
            foodNames = foodItemName,
            foodImages = foodItemImage,
            foodPrices = foodItemPrice,
            foodQuantities = foodItemQuantities,
            status = "pending",
            assignedTo = "",
            orderAccepted = false,
            paymentReceived = false,
            currentTime = time,
            restaurantLat = 26.8435,   // Banasthali (example)
         restaurantLng = 75.5660,

        customerLat = 26.9124 ,    // dynamic hona chahiye ideally
        customerLng = 75.7873
        )

        databaseReference.child("Orders").child(orderId)
            .setValue(order)
            .addOnSuccessListener {

                val bottomSheetDialog = CongratsBottomSheet()
                bottomSheetDialog.show(supportFragmentManager, "Test")

                removeItemFromCart()
                addOrderToHistory(order)
            }
    }
    private fun addOrderToHistory(orderDetails: OrderModel) {

        val userId = auth.currentUser?.uid ?: return

        databaseReference
            .child("user")
            .child(userId)
            .child("BuyHistory")
            .child(orderDetails.itemPushKey!!)   // ✅ SAME KEY
            .setValue(orderDetails)
            .addOnSuccessListener {
                // Toast.makeText(this, "Order Saved in History", Toast.LENGTH_SHORT).show()
            }
    }

    private fun removeItemFromCart() {

        val userId = auth.currentUser?.uid ?: return

        val cartItemsReference = databaseReference
            .child("user")
            .child(userId)
            .child("cartItems")

        cartItemsReference.removeValue()
    }

    private fun calculateTotalAmount(prices: ArrayList<String>, quantities: ArrayList<Int>): Int {

        var totalAmount = 0

        for (i in prices.indices) {

            val price = prices[i]
                .replace("₹", "")
                .replace("$", "")
                .toIntOrNull() ?: 0

            // agar quantity list empty ho to default 1
            val quantity = if (i < quantities.size)
                quantities[i]
            else
                1

            totalAmount += price * quantity
        }

        return totalAmount
    }

    private fun setUserData() {
        val user: FirebaseUser? = auth.currentUser
        if (user != null) {
            val userId: String = user.uid
            val userReference: DatabaseReference = databaseReference.child("user").child(userId)
            userReference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val userName = snapshot.child("name").value.toString()
                        val userAddress = snapshot.child("address").value.toString()
                        val userPhone = snapshot.child("phone").value.toString()
                        binding.name.setText(userName)
                        binding.address.setText(userAddress)
                        binding.phone.setText(userPhone)
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
        }
    }
}
