package udemy.android.photoapp

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class Adapter(val context: Context,val pictures: ArrayList<PictureData>): RecyclerView.Adapter<PictureViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PictureViewHolder {
        val pictureView = LayoutInflater.from(context).inflate(R.layout.picture_layout, parent, false)
        return PictureViewHolder(pictureView,context)
    }

    override fun onBindViewHolder(holder: PictureViewHolder, position: Int) {
        holder.setImageView(pictures[position].realImage!!)
        holder.setImageData(pictures[position].id,pictures[position].photographer,pictures[position].medium)
    }

    override fun getItemCount(): Int {
        return pictures.size
    }

}