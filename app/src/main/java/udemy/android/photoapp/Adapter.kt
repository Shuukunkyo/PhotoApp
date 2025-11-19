package udemy.android.photoapp

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

/**
 * RecyclerView 的适配器，是连接数据源 (pictureList) 和视图 (ViewHolder) 的桥梁。
 * @param context 上下文环境，主要用于视图加载器 (LayoutInflater) 和 ViewHolder 中的事件处理。
 * @param pictureList 图片数据对象的列表，是适配器需要展示的数据源。
 */
class Adapter(val context: Context, var pictureList: ArrayList<PictureData>) : RecyclerView.Adapter<PictureViewHolder>() {

    /**
     * 当 RecyclerView 需要一个新的 ViewHolder 时调用此方法。这个过程是为列表项创建视图骨架。
     * @param parent ViewHolder 将被添加到此父视图组中。
     * @param viewType 视图类型，在有多种 item 类型时使用（此项目只有一种）。
     * @return 返回一个新的、初始化好的 ViewHolder 实例。
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PictureViewHolder {
        // 使用 LayoutInflater 从 XML 布局文件 (R.layout.picture_layout) 创建一个视图对象。
        val itemView = LayoutInflater.from(context).inflate(R.layout.picture_layout, parent, false)
        // 将创建的视图和上下文传递给 ViewHolder 的构造函数并返回。
        return PictureViewHolder(itemView, context)
    }

    /**
     * 将数据绑定到指定位置的 ViewHolder 上。这个过程是为视图骨架填充内容。
     * @param holder 将要被绑定数据的 ViewHolder。
     * @param position 列表中的数据项位置。
     */
    override fun onBindViewHolder(holder: PictureViewHolder, position: Int) {
        // 从数据列表中获取指定位置的图片数据对象。
        val picture = pictureList[position]

        // 1. 设置图像本身
        // 将解码后的 Bitmap 图像设置到 ViewHolder 的 ImageView 上。
        if (picture.realImage != null) {
            holder.setImageView(picture.realImage!!)
        }

        // 2. 设置点击事件所需的数据
        // 将图片的元数据（ID、作者、URL）传递给 ViewHolder。
        holder.setImageData(picture.id, picture.photographer, picture.medium)
    }

    /**
     * 返回数据源中的项目总数。RecyclerView 需要此信息来知道要展示多少个列表项。
     * @return 列表的大小。
     */
    override fun getItemCount(): Int {
        return pictureList.size
    }

    /**
     * 公开给外部（如 Activity）的方法，用于更新适配器的数据源。
     * 在 MVVM 架构中，当 ViewModel 的 LiveData 更新时，此方法被调用。
     * @param newPictureList 新的图片数据列表。
     */
    fun updateData(newPictureList: ArrayList<PictureData>) {
        this.pictureList = newPictureList
        // 通知适配器数据已发生变化，这将触发 RecyclerView 的重绘。
        // 在有大量数据更新时，使用 DiffUtil 会更高效。
        notifyDataSetChanged()
        Log.d("adapter", "adapter changed")
    }
}