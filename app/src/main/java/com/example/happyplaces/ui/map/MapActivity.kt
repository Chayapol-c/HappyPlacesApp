package com.example.happyplaces.ui.map

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.happyplaces.R
import com.example.happyplaces.data.HappyPlaceModel
import com.example.happyplaces.databinding.ActivityMapBinding
import com.example.happyplaces.ui.MainActivity
import com.example.happyplaces.utils.getParcelableExtraProvider
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    private var mHappyPlaceDetails: HappyPlaceModel? = null

    private lateinit var binding: ActivityMapBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapBinding.inflate(layoutInflater)

        binding.apply {
            setContentView(root)
        }

        if (intent.hasExtra(MainActivity.EXTRA_PLACE_DETAILS)) {
            mHappyPlaceDetails = intent.getParcelableExtraProvider<HappyPlaceModel>(MainActivity.EXTRA_PLACE_DETAILS)
        }
        mHappyPlaceDetails?.let {
            setSupportActionBar(binding.toolbarMap)
            supportActionBar?.apply {
                setDisplayHomeAsUpEnabled(true)
                title = it.title
            }

            binding.toolbarMap.setNavigationOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }
            val supportMapFragment = supportFragmentManager.findFragmentById(R.id.map)
            (supportMapFragment as SupportMapFragment).getMapAsync(this@MapActivity)
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mHappyPlaceDetails?.let {
            val position = LatLng(it.latitude, it.longitude)
            googleMap.addMarker(MarkerOptions().position(position).title(it.location))
            val updatePosition = CameraUpdateFactory.newLatLngZoom(position, 10f)
            googleMap.animateCamera(updatePosition)
        }
    }
}