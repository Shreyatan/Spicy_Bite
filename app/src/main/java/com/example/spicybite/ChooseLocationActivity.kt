package com.example.spicybite

import android.os.Bundle
import android.widget.PopupMenu
import androidx.appcompat.app.AppCompatActivity
import com.example.spicybite.databinding.ActivityChooseLocationBinding

class ChooseLocationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChooseLocationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChooseLocationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val locationList = listOf("Jaipur", "Odisha", "Bundi", "Sikar")

        binding.locationBox.setOnClickListener {
            val popupMenu = PopupMenu(this, binding.locationBox)

            locationList.forEach {
                popupMenu.menu.add(it)
            }

            popupMenu.setOnMenuItemClickListener { item ->
                binding.tvLocation.text = item.title
                true
            }

            popupMenu.show()
        }
    }
}
