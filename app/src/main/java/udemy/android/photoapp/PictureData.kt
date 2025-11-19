package udemy.android.photoapp

import android.graphics.Bitmap

/**
 * 数据类 (data class)，用于封装从 Pexels API 获取的单张图片的所有相关信息。
 * 作为 MVVM 架构中的 Model 层，它纯粹地定义了数据的结构。
 * @property id 图片的唯一标识符。
 * @property photographer 摄影师的名字。
 * @property medium 中等尺寸图片的 URL 地址，用于在列表中显示。
 * @property realImage 下载并解码后的 Bitmap 对象。初始时为 null，在下载完成后被赋值。
 */
data class PictureData(
    val id: String,
    val photographer: String,
    val medium: String,
    var realImage: Bitmap?
)
