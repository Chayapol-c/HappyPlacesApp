package com.example.happyplaces.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.happyplaces.data.DatabaseHandler
import com.example.happyplaces.data.HappyPlaceModel
import com.example.happyplaces.databinding.ActivityMainBinding
import com.example.happyplaces.ui.add.AddHappyPlaceActivity
import com.example.happyplaces.ui.detail.HappyPlaceDetailActivity
import com.example.happyplaces.ui.home.HappyPlacesAdapter
import com.example.happyplaces.utils.SwipeToDeleteCallback
import com.example.happyplaces.utils.SwipeToEditCallback

class MainActivity : AppCompatActivity() {

    private lateinit var mAdapter: HappyPlacesAdapter
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)

        binding.apply {
            setContentView(root)
            fabAddHappyPlace.setOnClickListener {
                val intent = Intent(this@MainActivity, AddHappyPlaceActivity::class.java)
                startActivityForResult(intent, ADD_PLACE_ACTIVITY_REQUEST_CODE)
            }
        }

        getHappyPlacesListFromLocalDB()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == ADD_PLACE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                getHappyPlacesListFromLocalDB()
            } else {
                Log.e("activity", "Activity Fail")
            }
        }
    }

    private fun setUpHappyPlacesRecyclerView(happyPlaceList: ArrayList<HappyPlaceModel>) {
        mAdapter = HappyPlacesAdapter(
            context = this@MainActivity,
            list = happyPlaceList,
            onClickItem = { position, model ->
                val intent = Intent(this@MainActivity, HappyPlaceDetailActivity::class.java)
                intent.putExtra(EXTRA_PLACE_DETAILS, model)
                startActivity(intent)
            }
        )
        binding.rvHappyPlacesList.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = mAdapter
            setHasFixedSize(true)
        }

        val editSwipeHandler = object : SwipeToEditCallback(this) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val adapter = binding.rvHappyPlacesList.adapter as HappyPlacesAdapter
                adapter.notifyEditItem(this@MainActivity, viewHolder.adapterPosition, ADD_PLACE_ACTIVITY_REQUEST_CODE)
            }
        }

        val editTouchHelper = ItemTouchHelper(editSwipeHandler)
        editTouchHelper.attachToRecyclerView(binding.rvHappyPlacesList)

        val deleteSwipeHandler = object : SwipeToDeleteCallback(this) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val adapter = binding.rvHappyPlacesList.adapter as HappyPlacesAdapter
                adapter.removeAt(viewHolder.adapterPosition)

                getHappyPlacesListFromLocalDB()
            }
        }

        val deleteSwipeHelper = ItemTouchHelper(deleteSwipeHandler)
        deleteSwipeHelper.attachToRecyclerView(binding.rvHappyPlacesList)

    }

    private fun getHappyPlacesListFromLocalDB() {
        val dbHandler = DatabaseHandler(this)
        val happyPlaceList = dbHandler.getHappyPlacesList()

        if (happyPlaceList.size > 0) {
            binding.apply {
                rvHappyPlacesList.visibility = View.VISIBLE
                tvNoRecordsAvailable.visibility = View.GONE
            }
            setUpHappyPlacesRecyclerView(happyPlaceList)
        } else {
            binding.apply {
                rvHappyPlacesList.visibility = View.GONE
                tvNoRecordsAvailable.visibility = View.VISIBLE
            }
        }
    }

    companion object {
        const val ADD_PLACE_ACTIVITY_REQUEST_CODE = 1
        const val EXTRA_PLACE_DETAILS = "extra_place_details"
    }
}