package com.juggle.im.android.chat

import android.widget.ListView
import androidx.recyclerview.widget.RecyclerView
import com.juggle.im.android.model.UiMessage

/**
 * 历史消息自动加载功能
 * 当用户滚动到聊天顶部时自动加载历史消息
 */
class HistoryMessageLoader {
    
    private var isLoading = false
    private var hasMoreMessages = true
    private var onLoadMore: (() -> Unit)? = null
    
    /**
     * 初始化历史消息加载
     * 验证: 需求 5.7
     */
    fun setupHistoryMessageLoading(
        recyclerView: RecyclerView?,
        onLoadMore: () -> Unit
    ) {
        this.onLoadMore = onLoadMore
        
        recyclerView?.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                
                // 检查是否滚动到顶部
                if (!recyclerView.canScrollVertically(-1)) {
                    // 已滚动到顶部
                    if (!isLoading && hasMoreMessages) {
                        loadMoreMessages()
                    }
                }
            }
        })
    }
    
    /**
     * 初始化历史消息加载（ListView 版本）
     */
    fun setupHistoryMessageLoadingListView(
        listView: ListView?,
        onLoadMore: () -> Unit
    ) {
        this.onLoadMore = onLoadMore
        
        listView?.setOnScrollListener(object : android.widget.AbsListView.OnScrollListener {
            override fun onScrollStateChanged(view: android.widget.AbsListView?, scrollState: Int) {}
            
            override fun onScroll(
                view: android.widget.AbsListView?,
                firstVisibleItem: Int,
                visibleItemCount: Int,
                totalItemCount: Int
            ) {
                // 检查是否滚动到顶部
                if (firstVisibleItem == 0) {
                    if (!isLoading && hasMoreMessages) {
                        loadMoreMessages()
                    }
                }
            }
        })
    }
    
    /**
     * 加载更多消息
     */
    private fun loadMoreMessages() {
        isLoading = true
        onLoadMore?.invoke()
    }
    
    /**
     * 完成加载
     */
    fun onLoadComplete(hasMore: Boolean) {
        isLoading = false
        hasMoreMessages = hasMore
    }
    
    /**
     * 重置加载状态
     */
    fun reset() {
        isLoading = false
        hasMoreMessages = true
    }
    
    /**
     * 检查是否正在加载
     */
    fun isLoading(): Boolean = isLoading
    
    /**
     * 检查是否还有更多消息
     */
    fun hasMoreMessages(): Boolean = hasMoreMessages
}
