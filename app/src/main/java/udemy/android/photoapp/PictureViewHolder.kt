package udemy.android.photoapp

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import java.io.ByteArrayOutputStream

/**
 * RecyclerView 中单个图片项的 ViewHolder。
 * 它持有列表项的视图引用，并负责处理该项的用户交互事件。
 * @param pictureview 列表项的根视图（例如，一个 LinearLayout 或 CardView）。
 * @param context 上下文环境，用于创建 Intent 以启动新 Activity。
 */
class PictureViewHolder(val pictureview: View, context: Context) : RecyclerView.ViewHolder(pictureview) {
    // 列表项布局中的 ImageView 组件，用于实际显示照片。
    private val imageView: ImageView = this.pictureview.findViewById(R.id.image_view)

    // 图片的元数据，这些数据由 Adapter 在绑定时设置，用于点击事件。
    private lateinit var id: String
    private lateinit var authorName: String
    private lateinit var url: String

    /**
     * init 代码块在 ViewHolder 实例创建时执行。
     * 这里是为视图设置固定监听器（如点击事件）的理想位置。
     */
    init {
        // 为图片视图设置点击事件监听器。
        imageView.setOnClickListener {
            Log.d("PictureViewHolder", "Image clicked start")

            // 创建一个 Intent，用于导航到全屏显示图片的 PictureActivity。
            val intent = Intent(context, PictureActivity::class.java)

            // 将图片的字符串类型元数据（ID、作者、URL）放入 Intent 中，传递给下一个 Activity。
            intent.putExtra("id", id)
            intent.putExtra("authorName", authorName)
            intent.putExtra("url", url)

            // 为了传递图片本身，需要将 Bitmap 转换为字节数组。
            // 这种方式可以避免在新 Activity 中重新下载图片，但如果图片过大，可能会消耗较多内存。
            val stream = ByteArrayOutputStream()
            // 从 ImageView 的 Drawable 中获取 Bitmap。
            val bmp = (imageView.drawable as BitmapDrawable).bitmap
            // 将 Bitmap 压缩为 PNG 格式并写入流中。
            bmp.compress(Bitmap.CompressFormat.PNG, 100, stream)
            // 将流转换为字节数组。
            val byteArray = stream.toByteArray()
            // 将字节数组放入 Intent。
            intent.putExtra("bitmap", byteArray)

            // 启动 PictureActivity。
            context.startActivity(intent)
            Log.d("PictureViewHolder", "Image clicked end")

        }
    }

    /**
     * 设置图片的元数据（ID、作者、URL）。
     * 此数据主要用于点击事件发生时传递给下一个 Activity。
     * @param pictureId 图片的唯一 ID。
     * @param authorName 摄影师的名字。
     * @param pictureURL 图片的原始来源 URL。
     */
    fun setImageData(pictureId: String, authorName: String, pictureURL: String) {
        this.id = pictureId
        this.authorName = authorName
        this.url = pictureURL
    }

    /**
     * 将下载好的 Bitmap 图像设置到 ImageView 上。
     * @param bitmap 解码后的图片位图。
     */
    fun setImageView(bitmap: Bitmap) {
        imageView.setImageBitmap(bitmap)
    }
}
