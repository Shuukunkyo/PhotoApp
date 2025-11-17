package udemy.android.photoapp

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {
    private val API_KEY = "ZmUzraDnLgETF8OzN4xlzQ76bQMi4Ytk5oBiL445C5UQKO6Kq55iputJ"
    private lateinit var searchText: EditText
    private lateinit var searchButton: Button
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private val singleThreadExecutor = Executors.newSingleThreadExecutor()
    private var handler = Handler(Looper.getMainLooper())
    private var picturesFrmAPI: ArrayList<PictureData> = ArrayList()
    private val newIndices: ArrayList<Int> = ArrayList()
    private val cachedThreadPoolExecutor = Executors.newCachedThreadPool()

    private lateinit var layoutManager: RecyclerView.LayoutManager
    private lateinit var adapter: Adapter
    private val recyclerViewBottomImageContainer = intArrayOf(0, 0, 0)

    private var page = 1
    private var perPage = 15
    private var currentSearchingText = ""
    private var loadingNewImages = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        searchText = findViewById(R.id.search_text)
        searchButton = findViewById(R.id.search_btn)
        recyclerView = findViewById(R.id.recycle_view)
        progressBar = findViewById(R.id.progress_bar)
        progressBar.visibility = View.INVISIBLE

        //先做一个URL，把网址写进去，表明我们要访问的网站
        Thread {
            handler.post {
                // 设定为不能按的状态
                searchButton.isEnabled = false
                // 设定为不可见的状态
                progressBar.visibility = View.VISIBLE
            }
            loadDataFromAPI("https://api.pexels.com/v1/curated?page=1&per_page=15")
            loadImageFromAPI()

            //確認用
//            for (picture in picturesFrmAPI) {
//                println("${picture.id}, ${picture.photographer},${picture.medium}, ${picture.realImage}")
//            }

            handler.post {
                layoutManager = StaggeredGridLayoutManager(3,StaggeredGridLayoutManager.VERTICAL)
                adapter = Adapter(this,picturesFrmAPI)
                recyclerView.layoutManager = layoutManager
                recyclerView.adapter = adapter

                progressBar.visibility = View.INVISIBLE
                searchButton.isEnabled = true
            }
        }.start()

        searchButton.setOnClickListener {
            Thread{
                handler.post{
                    loadingNewImages = true
                    searchButton.isEnabled = false
                    progressBar.visibility=View.VISIBLE
                    picturesFrmAPI.clear()
                    adapter.notifyDataSetChanged()
                    val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    inputMethodManager.hideSoftInputFromWindow(searchText.windowToken,0)
                }

                currentSearchingText = searchText.text.toString()
                page = 1
                if(currentSearchingText == ""){
                    loadDataFromAPI("https://api.pexels.com/v1/curated?page=$page&per_page=$perPage")
                }else{
                    loadDataFromAPI("https://api.pexels.com/v1/search?query=$currentSearchingText&page=$page&per_page=$perPage")
                }
                loadImageFromAPI()


                handler.post {
                    searchButton.isEnabled = true
                    progressBar.visibility = View.INVISIBLE
                    adapter.notifyDataSetChanged()
                    loadingNewImages = false
                }
            }.start()
        }

        recyclerView.addOnScrollListener(ScrollListener(this))
    }

    private fun loadDataFromAPI(url: String) {
        val urlObject = URL(url)
        try {
            val connection: HttpURLConnection = urlObject.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("Authorization", API_KEY)

            val inputStreamReader = InputStreamReader(connection.inputStream, "UTF-8")
            val bufferedReader = BufferedReader(inputStreamReader)
            val result = bufferedReader.readLine()
            val response = JSONObject(result)
            val photos = response.getJSONArray("photos")
            for (i in 0 until photos.length()) {
                val photo = photos.getJSONObject(i)
                picturesFrmAPI.add(
                    PictureData(
                        photo.getString("id"),
                        photo.getString("photographer"),
                        photo.getJSONObject("src").getString("medium"),
                        null
                    )
                )
                newIndices.add(picturesFrmAPI.size - 1)
            }
            inputStreamReader.close()
            bufferedReader.close()
            connection.disconnect()

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun loadImageFromAPI() {
        val latch = CountDownLatch(newIndices.size)
        for (i in newIndices){
            cachedThreadPoolExecutor.execute {
                try{
                    val inputStream: InputStream = URL(picturesFrmAPI[i].medium).openStream()
                    picturesFrmAPI[i].realImage = BitmapFactory.decodeStream(inputStream)
                    latch.countDown()
                }catch (e: Exception){
                    e.printStackTrace()
                }
            }
        }
        try {
            latch.await()
        } catch (e: Exception){
            e.printStackTrace()
        }
        newIndices.clear()
    }

    inner class ScrollListener(val context: Context):RecyclerView.OnScrollListener(){
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            println("we are calling the scoll method")

            val layoutManager = recyclerView.layoutManager as StaggeredGridLayoutManager
            val lastVisibleItemPosition = layoutManager.findLastVisibleItemPositions(recyclerViewBottomImageContainer)
            val itemCount = layoutManager.itemCount

            if(!loadingNewImages && (lastVisibleItemPosition[0] == itemCount - 1 || lastVisibleItemPosition[1] == itemCount - 1 || lastVisibleItemPosition[2] == itemCount - 1)){
                Thread{
                    handler.post {
                        loadingNewImages = true
                        searchButton.isEnabled = false
                        progressBar.visibility = View.VISIBLE
                        Toast.makeText(context,R.string.new_image,Toast.LENGTH_SHORT).show()
                    }
                    page += 1
                    if(currentSearchingText == ""){
                        loadDataFromAPI("https://api.pexels.com/v1/curated?page=$page&per_page=$perPage")
                    }else{
                        loadDataFromAPI("https://api.pexels.com/v1/search?query=$currentSearchingText&page=$page&per_page=$perPage")
                    }
                    loadImageFromAPI()
                    handler.post {
                        searchButton.isEnabled = true
                        progressBar.isVisible = false
                        adapter.notifyDataSetChanged()
                        loadingNewImages = false
                    }
                }.start()
            }
        }
    }


}
