package com.anish.mediscan

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class HistoryAdapter(
    private var items: List<ScanHistoryItem>,
    private val onItemClick: (ScanHistoryItem) -> Unit,
    private val onFavoriteClick: (ScanHistoryItem) -> Unit,
    private val onDeleteClick: (ScanHistoryItem) -> Unit
) : RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivIcon: ImageView = itemView.findViewById(R.id.ivIcon)
        val tvName: TextView = itemView.findViewById(R.id.tvMedicineName)
        val tvType: TextView = itemView.findViewById(R.id.tvTypeBadge)
        val tvDosage: TextView = itemView.findViewById(R.id.tvDosage)
        val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        val btnFav: ImageView = itemView.findViewById(R.id.btnFav)
        val btnDelete: ImageView = itemView.findViewById(R.id.btnDelete)
    }

    fun updateItems(newItems: List<ScanHistoryItem>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_history_scan, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.tvName.text = item.name
        holder.tvType.text = item.type
        holder.tvDosage.text = item.dosage
        holder.tvDate.text = item.date

        safeSetIcon(holder.ivIcon, item.iconRes)
        updateFavIcon(holder.btnFav, item.isFavorite)

        holder.btnFav.setOnClickListener {
            onFavoriteClick(item)
        }

        holder.btnDelete.setOnClickListener {
            onDeleteClick(item)
        }

        holder.itemView.setOnClickListener {
            onItemClick(item)
        }
    }

    private fun safeSetIcon(imageView: ImageView, iconRes: Int) {
        try {
            if (iconRes != 0) {
                imageView.setImageResource(iconRes)
            } else {
                imageView.setImageResource(R.drawable.ic_pill)
            }
        } catch (e: Exception) {
            imageView.setImageResource(R.drawable.ic_pill)
        }
    }

    private fun updateFavIcon(imageView: ImageView, isFav: Boolean) {
        if (isFav) {
            imageView.setImageResource(R.drawable.ic_favorite_filled)
        } else {
            imageView.setImageResource(R.drawable.ic_favorite)
        }
    }

    override fun getItemCount(): Int = items.size
}
