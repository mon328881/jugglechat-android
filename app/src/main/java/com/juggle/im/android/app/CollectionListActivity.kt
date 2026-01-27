package com.juggle.im.android.app

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.juggle.im.android.model.CollectedMessage
import com.juggle.im.android.theme.ThemeManager
import com.juggle.im.android.utils.CollectionManager

/**
 * 消息收藏列表界面
 * 显示所有收藏的消息，支持搜索和筛选
 */
class CollectionListActivity : AppCompatActivity() {
    
    private lateinit var collectionManager: CollectionManager
    private lateinit var themeManager: ThemeManager
    private lateinit var recyclerView: RecyclerView
    private lateinit var searchEditText: EditText
    private lateinit var filterContainer: LinearLayout
    private lateinit var emptyView: TextView
    
    private lateinit var adapter: CollectionListAdapter
    private var allCollections: List<CollectedMessage> = emptyList()
    private var filteredCollections: List<CollectedMessage> = emptyList()
    
    private var currentFilterType: String? = null
    private var currentFilterStartTime: Long? = null
    private var currentFilterEndTime: Long? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        collectionManager = CollectionManager.getInstance(this)
        themeManager = ThemeManager.getInstance(this)
        
        // 创建主容器
        val mainContainer = LinearLayout(this).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(themeManager.getCurrentTheme().colors.background)
        }
        
        // 创建搜索框
        searchEditText = EditText(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(
                    themeManager.getCurrentTheme().spacing.md,
                    themeManager.getCurrentTheme().spacing.md,
                    themeManager.getCurrentTheme().spacing.md,
                    themeManager.getCurrentTheme().spacing.md
                )
            }
            hint = "搜索收藏的消息"
            setTextColor(themeManager.getCurrentTheme().colors.onBackground)
            setHintTextColor(themeManager.getCurrentTheme().colors.onBackground.and(0x80FFFFFF.toInt()))
            setPadding(
                themeManager.getCurrentTheme().spacing.md,
                themeManager.getCurrentTheme().spacing.sm,
                themeManager.getCurrentTheme().spacing.md,
                themeManager.getCurrentTheme().spacing.sm
            )
            setBackgroundColor(themeManager.getCurrentTheme().colors.surface)
        }
        
        // 创建筛选容器
        filterContainer = LinearLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            orientation = LinearLayout.HORIZONTAL
            setPadding(
                themeManager.getCurrentTheme().spacing.md,
                0,
                themeManager.getCurrentTheme().spacing.md,
                themeManager.getCurrentTheme().spacing.md
            )
        }
        
        // 创建空状态视图
        emptyView = TextView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            text = "暂无收藏的消息"
            textSize = 16f
            setTextColor(themeManager.getCurrentTheme().colors.onBackground)
            gravity = android.view.Gravity.CENTER
            visibility = View.GONE
        }
        
        // 创建RecyclerView
        recyclerView = RecyclerView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
                1f
            )
            layoutManager = LinearLayoutManager(this@CollectionListActivity)
            adapter = CollectionListAdapter(
                emptyList(),
                themeManager,
                collectionManager,
                ::onCollectionRemoved
            ).also { this@CollectionListActivity.adapter = it }
        }
        
        // 添加视图到主容器
        mainContainer.addView(searchEditText)
        mainContainer.addView(filterContainer)
        mainContainer.addView(recyclerView)
        mainContainer.addView(emptyView)
        
        setContentView(mainContainer)
        
        // 设置搜索监听器
        setupSearchListener()
        
        // 加载收藏列表
        loadCollections()
    }
    
    /**
     * 设置搜索监听器
     */
    private fun setupSearchListener() {
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val keyword = s?.toString() ?: ""
                filterCollections(keyword)
            }
            
            override fun afterTextChanged(s: Editable?) {}
        })
    }
    
    /**
     * 加载所有收藏的消息
     */
    private fun loadCollections() {
        allCollections = collectionManager.getAllCollections()
        filteredCollections = allCollections
        updateUI()
    }
    
    /**
     * 按关键词筛选收藏
     */
    private fun filterCollections(keyword: String) {
        filteredCollections = if (keyword.isEmpty()) {
            allCollections
        } else {
            collectionManager.searchCollections(keyword)
        }
        updateUI()
    }
    
    /**
     * 按消息类型筛选
     */
    fun filterByType(messageType: String) {
        currentFilterType = messageType
        filteredCollections = collectionManager.filterCollectionsByType(messageType)
        updateUI()
    }
    
    /**
     * 按时间范围筛选
     */
    fun filterByTimeRange(startTime: Long, endTime: Long) {
        currentFilterStartTime = startTime
        currentFilterEndTime = endTime
        filteredCollections = collectionManager.filterCollectionsByTimeRange(startTime, endTime)
        updateUI()
    }
    
    /**
     * 清除所有筛选
     */
    fun clearFilters() {
        currentFilterType = null
        currentFilterStartTime = null
        currentFilterEndTime = null
        searchEditText.text.clear()
        loadCollections()
    }
    
    /**
     * 更新UI
     */
    private fun updateUI() {
        adapter.updateData(filteredCollections)
        
        if (filteredCollections.isEmpty()) {
            recyclerView.visibility = View.GONE
            emptyView.visibility = View.VISIBLE
        } else {
            recyclerView.visibility = View.VISIBLE
            emptyView.visibility = View.GONE
        }
    }
    
    /**
     * 收藏被移除时的回调
     */
    private fun onCollectionRemoved(collectionId: String) {
        allCollections = allCollections.filter { it.collectionId != collectionId }
        filteredCollections = filteredCollections.filter { it.collectionId != collectionId }
        updateUI()
    }
}

/**
 * 收藏列表适配器
 */
class CollectionListAdapter(
    private var collections: List<CollectedMessage>,
    private val themeManager: ThemeManager,
    private val collectionManager: CollectionManager,
    private val onRemoved: (String) -> Unit
) : RecyclerView.Adapter<CollectionListAdapter.ViewHolder>() {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LinearLayout(parent.context).apply {
            layoutParams = RecyclerView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            orientation = LinearLayout.VERTICAL
            setPadding(
                themeManager.getCurrentTheme().spacing.md,
                themeManager.getCurrentTheme().spacing.md,
                themeManager.getCurrentTheme().spacing.md,
                themeManager.getCurrentTheme().spacing.md
            )
            setBackgroundColor(themeManager.getCurrentTheme().colors.surface)
        }
        return ViewHolder(itemView)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(collections[position])
    }
    
    override fun getItemCount(): Int = collections.size
    
    fun updateData(newCollections: List<CollectedMessage>) {
        collections = newCollections
        notifyDataSetChanged()
    }
    
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val container = itemView as LinearLayout
        
        fun bind(message: CollectedMessage) {
            container.removeAllViews()
            
            // 发送者名称
            val senderNameView = TextView(container.context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                text = "来自: ${message.senderName}"
                textSize = 14f
                setTextColor(themeManager.getCurrentTheme().colors.onBackground)
            }
            container.addView(senderNameView)
            
            // 消息内容
            val contentView = TextView(container.context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    topMargin = themeManager.getCurrentTheme().spacing.sm
                }
                text = message.messageSummary.ifEmpty { message.content }
                textSize = 16f
                setTextColor(themeManager.getCurrentTheme().colors.onBackground)
                maxLines = 3
            }
            container.addView(contentView)
            
            // 消息类型和时间
            val metaView = TextView(container.context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    topMargin = themeManager.getCurrentTheme().spacing.sm
                }
                text = "${message.messageType} • ${formatTime(message.collectionTimestamp)}"
                textSize = 12f
                setTextColor(themeManager.getCurrentTheme().colors.onBackground.and(0x80FFFFFF.toInt()))
            }
            container.addView(metaView)
            
            // 取消收藏按钮
            val removeButton = TextView(container.context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    topMargin = themeManager.getCurrentTheme().spacing.md
                }
                text = "取消收藏"
                textSize = 12f
                setTextColor(themeManager.getCurrentTheme().colors.primary)
                setOnClickListener {
                    collectionManager.uncollectMessage(message.collectionId)
                    onRemoved(message.collectionId)
                }
            }
            container.addView(removeButton)
        }
        
        private fun formatTime(timestamp: Long): String {
            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault())
            return sdf.format(java.util.Date(timestamp))
        }
    }
}
