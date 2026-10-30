package com.anish.mediscan

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class RecentScanAdapter(
    private val items: List<ScanHistoryItem>,
    private val onItemClick: (ScanHistoryItem) -> Unit
) : RecyclerView.Adapter<RecentScanAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivIcon: ImageView = itemView.findViewById(R.id.ivRecentIcon)
        val tvBadge: TextView = itemView.findViewById(R.id.tvRecentBadge)
        val tvName: TextView = itemView.findViewById(R.id.tvRecentMedicineName)
        val tvType: TextView = itemView.findViewById(R.id.tvRecentType)
        val tvDate: TextView = itemView.findViewById(R.id.tvRecentDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_recent_scan_card, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.tvName.text = item.name
        holder.tvType.text = item.type
        holder.tvDate.text = item.date

        safeSetIcon(holder.ivIcon, item.iconRes)

        if (item.requiresPrescription) {
            holder.tvBadge.text = "Rx"
            holder.tvBadge.setBackgroundResource(R.drawable.bg_badge_check)
        } else {
            holder.tvBadge.text = "Safe"
            holder.tvBadge.setBackgroundResource(R.drawable.bg_badge_safe)
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

    override fun getItemCount(): Int = items.size
}
