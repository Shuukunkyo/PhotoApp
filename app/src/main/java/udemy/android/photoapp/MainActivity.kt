package udemy.android.photoapp

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager

/**
 * 作为 MVVM 架构中的 View 层，此类负责展示 UI、观察 ViewModel 的数据变化以及将用户交互事件通知给 ViewModel。
 */
class MainActivity : AppCompatActivity() {

    // 用户输入搜索关键词的文本框
    private lateinit var searchText: EditText
    // 触发搜索操作的按钮
    private lateinit var searchButton: Button
    // 用于以瀑布流形式展示图片列表的视图
    private lateinit var recyclerView: RecyclerView
    // 在数据加载时显示的进度条
    private lateinit var progressBar: ProgressBar

    // RecyclerView 的数据适配器，负责将图片数据绑定到具体视图项
    private lateinit var adapter: Adapter
    // 通过 by viewModels() 委托属性，获取与此 Activity 生命周期绑定的 MainViewModel 实例
    private val viewModel: MainViewModel by viewModels()

    /**
     * Activity 的生命周期回调方法，是应用启动和初始化的入口。
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 1. 初始化在 XML 布局中定义的各个视图组件
        searchText = findViewById(R.id.search_text)
        searchButton = findViewById(R.id.search_btn)
        recyclerView = findViewById(R.id.recycle_view)
        progressBar = findViewById(R.id.progress_bar)

        // 2. 配置 RecyclerView，设置其布局管理器和适配器
        setupRecyclerView()

        // 3. 设置对 ViewModel 中 LiveData 的观察，实现 UI 的响应式更新
        observeViewModel()

        // 4. 为 UI 组件设置监听器，以捕获用户交互
        setupListeners()

        // 5. 通知 ViewModel 执行初始数据加载逻辑
        viewModel.initialLoad()
    }

    /**
     * 私有辅助方法，用于初始化和配置 RecyclerView。
     */
    private fun setupRecyclerView() {
        // 创建 Adapter 实例，初始时传入一个空列表
        adapter = Adapter(this, ArrayList())
        // 设置布局管理器为 StaggeredGridLayoutManager，实现三列的瀑布流效果
        recyclerView.layoutManager = StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL)
        // 将适配器与 RecyclerView 关联
        recyclerView.adapter = adapter
    }

    /**
     * 集中处理对 ViewModel 中所有 LiveData 的观察，这是 View 层响应数据变化的核心。
     */
    private fun observeViewModel() {
        // 观察图片数据列表的变化
        viewModel.pictures.observe(this) { pictureList ->
            // 当 viewModel 中的图片列表更新时，调用 adapter 的 updateData 方法来刷新 RecyclerView
            adapter.updateData(pictureList)
        }

        // 观察加载状态的变化，以控制进度条的显示与隐藏
        viewModel.isLoading.observe(this) { isLoading ->
            progressBar.visibility = if (isLoading) View.VISIBLE else View.INVISIBLE
        }

        // 观察搜索按钮的可点击状态
        viewModel.isSearchButtonEnabled.observe(this) { isEnabled ->
            searchButton.isEnabled = isEnabled
        }

        // 观察 Toast 消息，当 ViewModel 需要向用户显示短暂提示时触发
        viewModel.toastMessage.observe(this) { message ->
            // 确保消息不为空，避免显示空的 Toast
            if (message.isNotEmpty()) {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * 集中设置所有用户交互事件的监听器。
     */
    private fun setupListeners() {
        // 为搜索按钮设置点击事件监听器
        searchButton.setOnClickListener {
            // 获取用户在输入框中输入的文本
            val query = searchText.text.toString()
            // 操作结束后隐藏软键盘，提升用户体验
            val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(searchText.windowToken, 0)

            // 将搜索请求委托给 ViewModel 处理，Activity 本身不参与搜索逻辑
            viewModel.searchPictures(query)
        }

        // 为 RecyclerView 添加滚动监听，以实现无限滚动加载（上拉加载更多）
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            /**
             * 滚动状态改变时的回调
             */
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val layoutManager = recyclerView.layoutManager as StaggeredGridLayoutManager
                // 获取当前屏幕上可见的最后一项的位置数组
                val lastVisibleItemPositions = layoutManager.findLastVisibleItemPositions(null)
                val itemCount = layoutManager.itemCount

                // 检查可见的最后一项中是否有任何一项接近列表的末尾（此处阈值为3，即倒数第3个时开始加载）
                if (lastVisibleItemPositions.any { it >= itemCount - 3 }) {
                    // 如果满足条件，通知 ViewModel 加载更多图片
                    viewModel.loadMorePictures()
                }
            }
        })
    }
    //这是我的master  MainActivity
}
