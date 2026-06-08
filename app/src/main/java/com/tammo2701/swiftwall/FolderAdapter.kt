package com.tammo2701.swiftwall

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import java.io.File

class FolderAdapter(
    private val folders: MutableList<Folder>,
    private val onClick: (Folder, Int) -> Unit,
    private val onLongClick: (Folder, Int) -> Unit
) : RecyclerView.Adapter<FolderAdapter.VH>() {

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val imgCover: ImageView = view.findViewById(R.id.imgCover)
        val tvName: TextView = view.findViewById(R.id.tvFolderName)
        val tvCount: TextView = view.findViewById(R.id.tvImageCount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_folder, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val folder = folders[position]
        holder.tvName.text = folder.name
        val count = folder.images.size
        holder.tvCount.text = if (count == 1) "1 Bild" else "$count Bilder"

        if (folder.images.isNotEmpty()) {
            Glide.with(holder.imgCover)
                .load(File(folder.images.first()))
                .centerCrop()
                .into(holder.imgCover)
        } else {
            holder.imgCover.setImageDrawable(null)
        }

        holder.itemView.setOnClickListener { onClick(folder, position) }
        holder.itemView.setOnLongClickListener { onLongClick(folder, position); true }
    }

    override fun getItemCount() = folders.size
}