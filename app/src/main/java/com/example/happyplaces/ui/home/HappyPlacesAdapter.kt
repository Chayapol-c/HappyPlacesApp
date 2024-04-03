package com.example.happyplaces.ui.home

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Adapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.example.happyplaces.data.DatabaseHandler
import com.example.happyplaces.data.HappyPlaceModel
import com.example.happyplaces.databinding.ItemHappyPlaceBinding
import com.example.happyplaces.ui.MainActivity
import com.example.happyplaces.ui.add.AddHappyPlaceActivity

class HappyPlacesAdapter(
    private val context: Context,
    private var list: ArrayList<HappyPlaceModel>,
    private val onClickItem: (Int, HappyPlaceModel) -> Unit
) : ListAdapter<HappyPlaceModel, HappyPlacesViewHolder>(HappyPlacesDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HappyPlacesViewHolder {
        return HappyPlacesViewHolder(
            ItemHappyPlaceBinding.inflate(LayoutInflater.from(context)),
            onClickItem
        )
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: HappyPlacesViewHolder, position: Int) {
        holder.setUpView(list[position], position)
    }

    fun notifyEditItem(activity: Activity, position: Int, requestCode: Int) {
        val intent = Intent(context, AddHappyPlaceActivity::class.java)
        intent.putExtra(MainActivity.EXTRA_PLACE_DETAILS, list[position])

        activity.startActivityForResult(intent, requestCode)
        notifyItemChanged(position)
    }

    fun removeAt(position: Int) {
        val dbHandler = DatabaseHandler(context)
        val isDelete = dbHandler.deleteHappyPlace(list[position])
        if (isDelete > 0) {
            list.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    class HappyPlacesDiffCallback : DiffUtil.ItemCallback<HappyPlaceModel>() {
        override fun areItemsTheSame(oldItem: HappyPlaceModel, newItem: HappyPlaceModel): Boolean {
            return oldItem.title == newItem.title && oldItem.image == newItem.image
        }

        override fun areContentsTheSame(
            oldItem: HappyPlaceModel,
            newItem: HappyPlaceModel
        ): Boolean {
            return oldItem.id == newItem.id
        }

    }
}