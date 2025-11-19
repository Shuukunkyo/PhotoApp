package udemy.android.photoapp

import android.graphics.BitmapFactory
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

/**
 * MVVM 架构中的 ViewModel，作为业务逻辑和数据的处理中心。
 * 它独立于 UI，负责从网络获取数据、处理数据并管理相关状态。
 */
class MainViewModel : ViewModel() {

    // --- LiveData --- //
    // LiveData 是可被观察的数据持有者类。它们能感知生命周期，确保只在 Activity/Fragment 处于活跃状态时才更新 UI。

    // 私有的 MutableLiveData，只能在 ViewModel 内部修改。持有图片数据列表。
    private val _pictures = MutableLiveData<ArrayList<PictureData>>()
    // 对外暴露的不可变的 LiveData，UI 层（Activity）只能观察此数据，不能修改，符合单向数据流原则。
    val pictures: LiveData<ArrayList<PictureData>> = _pictures

    // 私有的加载状态 LiveData。
    private val _isLoading = MutableLiveData<Boolean>()
    // 对外暴露的加载状态，用于控制 ProgressBar 的显示和隐藏。
    val isLoading: LiveData<Boolean> = _isLoading

    // 私有的搜索按钮可用状态 LiveData。
    private val _isSearchButtonEnabled = MutableLiveData<Boolean>()
    // 对外暴露的搜索按钮可用状态。
    val isSearchButtonEnabled: LiveData<Boolean> = _isSearchButtonEnabled

    // 私有的 Toast 消息 LiveData，用于向 UI 层发送短暂的提示信息。
    private val _toastMessage = MutableLiveData<String>()
    // 对外暴露的 Toast 消息 LiveData。
    val toastMessage: LiveData<String> = _toastMessage

    // --- 内部状态和逻辑 --- //

    // Pexels API 密钥。
    private val API_KEY = "ZmUzraDnLgETF8OzN4xlzQ76bQMi4Ytk5oBiL445C5UQKO6Kq55iputJ"
    // 内部持有的图片数据列表，作为数据操作的主要载体。
    private var picturesFrmAPI: ArrayList<PictureData> = ArrayList()
    // 当前分页页码。
    private var page = 1
    // 每页请求的数量。
    private var perPage = 15
    // 当前正在搜索的关键词。
    private var currentSearchingText = ""

    /**
     * 执行初始数据加载。此方法由 MainActivity 在创建时调用。
     */
    fun initialLoad() {
        // 只有在列表为空时才执行加载，防止屏幕旋转等配置变更时重复加载数据。
        if (picturesFrmAPI.isEmpty()) {
            loadPictures("https://api.pexels.com/v1/curated?page=1&per_page=$perPage")
        }
    }

    /**
     * 根据关键词执行搜索。此方法由 MainActivity 在用户点击搜索按钮时调用。
     * @param query 搜索关键词。
     */
    fun searchPictures(query: String) {
        // 清空旧的搜索结果。
        picturesFrmAPI.clear()
        // 更新 LiveData，立即清空 UI 上的列表。
        _pictures.value = picturesFrmAPI

        currentSearchingText = query
        page = 1
        val url = if (currentSearchingText.isEmpty()) {
            "https://api.pexels.com/v1/curated?page=$page&per_page=$perPage"
        } else {
            "https://api.pexels.com/v1/search?query=$currentSearchingText&page=$page&per_page=$perPage"
        }
        loadPictures(url)
    }

    /**
     * 加载更多图片（分页加载）。此方法由 MainActivity 在滚动到列表底部时调用。
     */
    fun loadMorePictures() {
        // 如果当前正在加载，则直接返回，防止重复触发加载。
        if (_isLoading.value == true) return

        _toastMessage.postValue("正在加载新图片...") // 使用 postValue 可在任何线程中更新 LiveData
        page += 1
        val url = if (currentSearchingText.isEmpty()) {
            "https://api.pexels.com/v1/curated?page=$page&per_page=$perPage"
        } else {
            "https://api.pexels.com/v1/search?query=$currentSearchingText&page=$page&per_page=$perPage"
        }
        loadPictures(url)
    }

    /**
     * 核心的图片加载函数，使用协程处理异步任务。
     * @param url 要请求的 API 地址。
     */
    private fun loadPictures(url: String) {
        // 在 ViewModel 的生命周期内启动一个协程。
        viewModelScope.launch {
            // 更新 UI 状态：开始加载。
            _isLoading.value = true
            _isSearchButtonEnabled.value = false

            // 使用 withContext(Dispatchers.IO) 将网络请求切换到 IO 线程执行。
            val newPictureData = withContext(Dispatchers.IO) {
                loadDataFromAPI(url)
            }
            // 将新获取的数据添加到列表中。
            picturesFrmAPI.addAll(newPictureData)

            // 同样在后台协程中下载图片 Bitmap。
            loadImageFromAPI()

            // 所有后台任务完成后，将最终结果更新到 LiveData，通知 UI 刷新。
            _pictures.value = picturesFrmAPI
            // 更新 UI 状态：加载结束。
            _isLoading.value = false
            _isSearchButtonEnabled.value = true
        }
    }

    /**
     * 从 Pexels API 获取图片元数据（如 URL、作者等），此函数在 IO 线程中执行。
     * @param url API 请求地址。
     * @return 返回一个包含图片元数据的 PictureData 列表。
     */
    private suspend fun loadDataFromAPI(url: String): List<PictureData> {
        val newPics = ArrayList<PictureData>()
        try {
            val urlObject = URL(url)
            val connection = urlObject.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("Authorization", API_KEY)

            // 使用 .use 扩展函数可以自动关闭流。
            val result = connection.inputStream.bufferedReader().use { it.readText() }
            connection.disconnect()

            val photos = JSONObject(result).getJSONArray("photos")
            for (i in 0 until photos.length()) {
                val photo = photos.getJSONObject(i)
                newPics.add(
                    PictureData(
                        photo.getString("id"),
                        photo.getString("photographer"),
                        photo.getJSONObject("src").getString("medium"),
                        null // Bitmap 初始为 null
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace() // 在实际项目中，这里应该有更完善的错误处理逻辑。
        }
        return newPics
    }

    /**
     * 根据图片 URL 下载实际的 Bitmap 图像，此函数在 IO 线程中执行。
     * 它会并发下载所有尚未加载的图片。
     */
    private suspend fun loadImageFromAPI() {
        // 筛选出列表中 realImage 属性为 null 的项。
        val itemsToLoad = picturesFrmAPI.filter { it.realImage == null }

        // 为每个需要下载的图片创建一个独立的协程任务。
        val jobs = itemsToLoad.map { pictureData ->
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    val inputStream: InputStream = URL(pictureData.medium).openStream()
                    pictureData.realImage = BitmapFactory.decodeStream(inputStream)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        // 等待所有下载任务完成。
        jobs.joinAll()
    }
}
