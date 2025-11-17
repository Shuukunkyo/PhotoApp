package udemy.android.photoapp

import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class PictureActivity: AppCompatActivity(){

    private lateinit var imageView: ImageView
    private lateinit var idText: TextView
    private lateinit var authorText: TextView
    private lateinit var urlText: TextView

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.picture_activity_layout)

        imageView = findViewById(R.id.image)
        idText = findViewById(R.id.id)
        authorText = findViewById(R.id.photographer)
        urlText = findViewById(R.id.url)

        idText.text = "Photo ID: ${intent.getStringExtra("id")}"
        authorText.text = "Author: ${intent.getStringExtra("authorName")}"
        urlText.text = "URL: ${intent.getStringExtra("url")}"

        val byteArray = intent.getByteArrayExtra("bitmap")
        val image = BitmapFactory.decodeByteArray(byteArray, 0, byteArray!!.size)
        imageView.setImageBitmap(image)

    }
}