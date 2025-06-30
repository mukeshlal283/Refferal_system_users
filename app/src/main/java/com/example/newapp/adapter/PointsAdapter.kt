package com.example.newapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.newapp.databinding.PointsAddViewBinding
import com.example.newapp.model.NewUser

class PointsAdapter : RecyclerView.Adapter<PointsAdapter.PointsViewHolder>() {

    class PointsViewHolder(val binding: PointsAddViewBinding) :
        RecyclerView.ViewHolder(binding.root)

    val DiffUtil = object: DiffUtil.ItemCallback<NewUser>() {
        override fun areItemsTheSame(oldItem: NewUser, newItem: NewUser): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: NewUser, newItem: NewUser): Boolean {
            return oldItem == newItem
        }

    }

    val differ = AsyncListDiffer(this, DiffUtil)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PointsViewHolder {
        return PointsViewHolder(PointsAddViewBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: PointsViewHolder, position: Int) {
        val data = differ.currentList[position]

        holder.binding.apply {
            name.text = data.name
            points.text = data.points.toString()
        }
    }

}