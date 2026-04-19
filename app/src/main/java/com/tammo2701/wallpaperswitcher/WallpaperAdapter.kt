package com.tammo2701.wallpaperswitcher

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.DownsampleStrategy
import com.bumptech.glide.request.RequestOptions
import java.io.File

class WallpaperAdapter(
    private val images: MutableList<String>,
    private val onClick: (String) -> Unit,
    private val onLongClick: (String, Int) -> Unit
) : RecyclerView.Adapter<WallpaperAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.imageView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_wallpaper, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val path = images[position]
        Glide.with(holder.imageView.context)
            .load(File(path))
            .apply(
                RequestOptions()
                    .centerCrop()
                    .downsample(DownsampleStrategy.CENTER_INSIDE)
            )
            .into(holder.imageView)

        holder.imageView.setOnClickListener { onClick(path) }
        holder.imageView.setOnLongClickListener {
            onLongClick(path, holder.adapterPosition)
            true
        }
    }

    override fun getItemCount() = images.size
}