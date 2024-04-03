package com.example.happyplaces.ui.home

import android.net.Uri
import androidx.recyclerview.widget.RecyclerView
import com.example.happyplaces.data.HappyPlaceModel
import com.example.happyplaces.databinding.ItemHappyPlaceBinding

class HappyPlacesViewHolder (
    private val binding: ItemHappyPlaceBinding,
    private val onClickItem: (Int, HappyPlaceModel) -> Unit
) : RecyclerView.ViewHolder(binding.root) {

    fun setUpView(place: HappyPlaceModel, position: Int) {
        binding.apply {
            itemView.apply {
                ivPlaceImage.setImageURI(Uri.parse(place.image))
                tvTitle.text = place.title
                tvDescription.text = place.description

                setOnClickListener {
                    onClickItem(position, place)
                }
            }
        }
    }

}