package com.example.happyplaces.ui.home

import androidx.recyclerview.widget.DiffUtil
import com.example.happyplaces.data.HappyPlaceModel

class HappyPlacesDiffCallback(
    var oldItemList: ArrayList<HappyPlaceModel>,
    var newItemList: ArrayList<HappyPlaceModel>
) : DiffUtil.Callback() {
    override fun getOldListSize(): Int = oldItemList.size

    override fun getNewListSize(): Int = newItemList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldItem = oldItemList[oldItemPosition]
        val newItem = newItemList[newItemPosition]
        return oldItem.title == newItem.title && oldItem.image == newItem.image
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldItem = oldItemList[oldItemPosition]
        val newItem = newItemList[newItemPosition]
        return oldItem.id == newItem.id
    }
}