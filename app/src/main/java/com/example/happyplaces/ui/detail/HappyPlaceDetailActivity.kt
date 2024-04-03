package com.example.happyplaces.ui.detail

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.happyplaces.data.HappyPlaceModel
import com.example.happyplaces.databinding.ActivityHappyPlaceDetailBinding
import com.example.happyplaces.ui.MainActivity
import com.example.happyplaces.ui.map.MapActivity
import com.example.happyplaces.utils.getParcelableExtraProvider

class HappyPlaceDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHappyPlaceDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHappyPlaceDetailBinding.inflate(layoutInflater)

        val happyPlaceModel = intent.getParcelableExtraProvider<HappyPlaceModel>(MainActivity.EXTRA_PLACE_DETAILS)

        binding.apply {
            setContentView(root)

            happyPlaceModel?.let {place ->
                setSupportActionBar(toolbarHappyPlaceDetail)
                supportActionBar?.apply {
                    setDisplayHomeAsUpEnabled(true)
                    title = place.title
                }

                toolbarHappyPlaceDetail.setNavigationOnClickListener {
                    onBackPressedDispatcher.onBackPressed()
                }

                ivPlaceImage.setImageURI(Uri.parse(place.image))
                tvDescription.text = place.description
                tvLocation.text = place.location

                btnViewOnMap.setOnClickListener {
                    val intent = Intent(this@HappyPlaceDetailActivity, MapActivity::class.java)
                    intent.putExtra(MainActivity.EXTRA_PLACE_DETAILS, place)
                    startActivity(intent)
                }
            }
        }
    }
}