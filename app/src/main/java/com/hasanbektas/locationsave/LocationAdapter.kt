package com.hasanbektas.locationsave

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.hasanbektas.locationsave.databinding.RecyclerRowBinding

class LocationAdapter (val placeList: List<Location>): RecyclerView.Adapter<LocationAdapter.LocationHolder>(){
    class LocationHolder(val recyclerRowBinding: RecyclerRowBinding) : RecyclerView.ViewHolder(recyclerRowBinding.root) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocationHolder {
        val recyclerRowBinding = RecyclerRowBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return LocationHolder(recyclerRowBinding)
    }

    override fun onBindViewHolder(holder: LocationHolder, position: Int) {
        holder.recyclerRowBinding.recyclerViewTextView.text = placeList.get(position).name
        holder.itemView.setOnClickListener {

            val intent = Intent(holder.itemView.context, MapsActivity::class.java)
            intent.putExtra("selectedPlace",placeList.get(position))
            intent.putExtra("info","old")
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return placeList.size
    }
}