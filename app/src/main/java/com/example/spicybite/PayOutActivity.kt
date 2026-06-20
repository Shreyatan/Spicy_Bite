package com.example.spicybite

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.example.spicybite.databinding.ActivityPayOutBinding
import com.example.spicybite.model.OrderModel
import com.razorpay.Checkout
import com.razorpay.PaymentResultListener
import org.json.JSONObject

class PayOutActivity : AppCompatActivity(), PaymentResultListener
{

    private lateinit var auth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference

    private lateinit var name: String
    private lateinit var address: String
    private lateinit var phone: String
    private lateinit var totalAmount: String

    private var selectedPaymentMethod = "Cash On Delivery"

    private var foodItemName: ArrayList<String> = arrayListOf()
    private var foodItemPrice: ArrayList<String> = arrayListOf()
    private var foodItemImage: ArrayList<String> = arrayListOf()
    private var foodItemDescription: ArrayList<String> = arrayListOf()
    private var foodItemIngredient: ArrayList<String> = arrayListOf()
    private var foodItemQuantities: ArrayList<Int> = arrayListOf()

    lateinit var binding: ActivityPayOutBinding

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)

        binding = ActivityPayOutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().reference

        setUserData()

        setupPaymentSpinner()

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
        } else {
            ArrayList(List(prices.size) { 1 })
        }

        totalAmount = if (prices.isEmpty()) {
            "₹0"
        } else {
            "₹${calculateTotalAmount(prices, quantities)}"
        }

        binding.etTotalAmount.isEnabled = false
        binding.etTotalAmount.setText(totalAmount)

        binding.backButton.setOnClickListener {
            finish()
        }

        binding.PlaceMyOrder.setOnClickListener {

            name = binding.name.text.toString().trim()
            address = binding.address.text.toString().trim()
            phone = binding.phone.text.toString().trim()

            if (foodItemName.isEmpty()) {
                Toast.makeText(this, "Cart is empty 🚫", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (
                name.isBlank() || name == "null" ||
                address.isBlank() || address == "null" ||
                phone.isBlank() || phone == "null"
            ) {
                Toast.makeText(
                    this,
                    "Please complete profile details",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            if (!phone.matches(Regex("^[0-9]{10}$"))) {
                binding.phone.error = "Enter valid 10-digit number"
                return@setOnClickListener
            }

            when (selectedPaymentMethod) {

                "UPI" -> {
                    startRazorpayPayment()
                }

                "Credit/Debit Card" -> {
                    startRazorpayPayment()
                }

                "Net Banking" -> {
                    startRazorpayPayment()
                }

                "Cash On Delivery" -> {
                    placeOrder()
                }

                else -> {
                    Toast.makeText(
                        this,
                        "Please select payment method",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun startRazorpayPayment() {

        val checkout = Checkout()
        checkout.setKeyID("rzp_test_SfgOOm510YFFzO")

        try {

            val options = JSONObject()

            options.put("name", "Spicy Bite")
            options.put("description", "Food Order")
            options.put("currency", "INR")

            val amountInPaise =
                totalAmount.replace("₹", "")
                    .trim()
                    .toInt() * 100

            options.put("amount", amountInPaise)

            val prefill = JSONObject()
            prefill.put("contact", phone)
            prefill.put("email", "customer@test.com")

            options.put("prefill", prefill)

            checkout.open(this, options)

        } catch (e: Exception) {

            Toast.makeText(
                this,
                "Razorpay Error: ${e.message}",
                Toast.LENGTH_LONG
            ).show()

            e.printStackTrace()
        }
    }
    private fun setupPaymentSpinner() {

        val paymentMethods = resources.getStringArray(R.array.payment_methods)

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            paymentMethods
        )

        binding.spinnerPaymentMethod.adapter = adapter

        adapter.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item
        )

        binding.spinnerPaymentMethod.adapter = adapter

        binding.spinnerPaymentMethod.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    selectedPaymentMethod = paymentMethods[position]
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
    }

    private fun placeOrder() {

        if (foodItemName.isEmpty()) {
            Toast.makeText(this, "Cart empty ❌", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = auth.currentUser?.uid ?: ""
        val time = System.currentTimeMillis()
        val orderId = databaseReference.child("Orders").push().key!!

        val orderData = hashMapOf<String, Any>(
            "itemPushKey" to orderId,
            "userUid" to userId,
            "userName" to name,
            "address" to address,
            "phoneNumber" to phone,
            "totalPrice" to totalAmount,
            "foodNames" to foodItemName,
            "foodImages" to foodItemImage,
            "foodPrices" to foodItemPrice,
            "foodQuantities" to foodItemQuantities,
            "paymentMethod" to selectedPaymentMethod,
            "status" to "pending",
            "assignedTo" to "",
            "orderAccepted" to false,
            "paymentReceived" to false,
            "currentTime" to time,
            "restaurantLat" to 26.8435,
            "restaurantLng" to 75.5660,
            "customerLat" to 26.9124,
            "customerLng" to 75.7873
        )

        // 1. Save to global Orders node for Admin/Delivery app
        databaseReference.child("Orders")
            .child(orderId)
            .setValue(orderData)
            .addOnSuccessListener {

                // 2. ALSO save to User's personal BuyHistory so it appears in HistoryFragment
                databaseReference.child("user")
                    .child(userId)
                    .child("BuyHistory")
                    .child(orderId)
                    .setValue(orderData)
                    .addOnSuccessListener {
                        val bottomSheetDialog = CongratsBottomSheet()
                        bottomSheetDialog.show(
                            supportFragmentManager,
                            "OrderPlaced"
                        )
                        removeItemFromCart()
                    }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to place order: ${it.message}", Toast.LENGTH_SHORT).show()
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

    private fun calculateTotalAmount(
        prices: ArrayList<String>,
        quantities: ArrayList<Int>
    ): Int {

        var totalAmount = 0

        for (i in prices.indices) {

            val price = prices[i]
                .replace("₹", "")
                .replace("$", "")
                .toIntOrNull() ?: 0

            val quantity =
                if (i < quantities.size)
                    quantities[i]
                else
                    1

            totalAmount += price * quantity
        }

        return totalAmount
    }

    override fun onPaymentSuccess(paymentId: String?) {
        Toast.makeText(this, "Payment Successful", Toast.LENGTH_SHORT).show()
        placeOrder()
    }

    override fun onPaymentError(code: Int, response: String?) {
        Toast.makeText(this, "Payment Failed", Toast.LENGTH_LONG).show()
    }

    private fun setUserData() {

        val user: FirebaseUser? = auth.currentUser

        if (user != null) {

            val userId = user.uid

            val userReference =
                databaseReference.child("user").child(userId)

            userReference.addListenerForSingleValueEvent(
                object : ValueEventListener {

                    override fun onDataChange(snapshot: DataSnapshot) {

                        if (snapshot.exists()) {

                            val userName =
                                snapshot.child("name").value.toString()

                            val userAddress =
                                snapshot.child("address").value.toString()

                            val userPhone =
                                snapshot.child("phone").value.toString()

                            binding.name.setText(userName)
                            binding.address.setText(userAddress)
                            binding.phone.setText(userPhone)
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {}
                }
            )
        }
    }
}