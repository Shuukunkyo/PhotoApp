package udemy.android.photoapp

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.view.View
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import java.io.ByteArrayOutputStream

class PictureViewHolder(val pictureview: View,context: Context) : RecyclerView.ViewHolder(pictureview){
    private val imageView: ImageView = this.pictureview.findViewById(R.id.image_view)
    private lateinit var id: String
    private lateinit var authorName: String
    private lateinit var url: String

    init {
        imageView.setOnClickListener {
            val intent = Intent(context, PictureActivity::class.java)
            intent.putExtra("id", id)
            intent.putExtra("authorName", authorName)
            intent.putExtra("url", url)

            val stream = ByteArrayOutputStream()
            val bmp = (imageView.drawable as BitmapDrawable).bitmap
            bmp.compress(Bitmap.CompressFormat.PNG, 100, stream)
            val byteArray = stream.toByteArray()
            intent.putExtra("bitmap", byteArray)

            context.startActivity(intent)
        }
    }



    fun setImageData(pictureId:String, authorName:String, pictureURL: String){
        this.id=pictureId
        this.authorName=authorName
        this.url=pictureURL
    }

    fun setImageView(bitmap: Bitmap){
        imageView.setImageBitmap(bitmap)
    }
}