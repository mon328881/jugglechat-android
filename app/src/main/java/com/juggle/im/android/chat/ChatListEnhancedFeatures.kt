package com.juggle.im.android.chat

import android.widget.EditText
import android.widget.ListView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.juggle.im.android.model.UiConversation

/**
 * 聊天列表增强功能
 * 实现下拉刷新、搜索和未读高亮显示
 */
class ChatListEnhancedFeatures {
    
    /**
     * 初始化下拉刷新功能
     * 验证: 需求 4.5
     */
    fun setupPullToRefresh(
        swipeRefreshLayout: SwipeRefreshLayout?,
        onRefresh: () -> Unit
    ) {
        swipeRefreshLayout?.setOnRefreshListener {
            onRefresh()
        }
    }
    
    /**
     * 停止刷新动画
     */
    fun stopRefreshing(swipeRefreshLayout: SwipeRefreshLayout?) {
        swipeRefreshLayout?.isRefreshing = false
    }
    
    /**
     * 初始化搜索功能
     * 验证: 需求 4.6
     */
    fun setupSearch(
        searchInput: EditText?,
        conversations: List<UiConversation>,
        onSearchResult: (List<UiConversation>) -> Unit
    ) {
        searchInput?.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString().trim().lowercase()
                
                if (query.isEmpty()) {
                    // 显示所有对话
                    onSearchResult(conversations)
                } else {
                    // 搜索匹配的对话
                    val results = conversations.filter { conversation ->
                        conversation.name?.lowercase()?.contains(query) == true ||
                        conversation.lastMessage?.lowercase()?.contains(query) == true
                    }
                    onSearchResult(results)
                }
            }
            
            override fun afterTextChanged(s: android.text.Editable?) {}
        })
    }
    
    /**
     * 高亮搜索结果中的匹配文本
     * 验证: 需求 4.6
     */
    fun highlightSearchText(text: String, query: String): android.text.Spannable {
        val spannable = android.text.SpannableString(text)
        val query_lower = query.lowercase()
        val text_lower = text.lowercase()
        
        var startIndex = 0
        while (startIndex < text_lower.length) {
            val index = text_lower.indexOf(query_lower, startIndex)
            if (index == -1) break
            
            spannable.setSpan(
                android.text.style.BackgroundColorSpan(0xFFFFFF00.toInt()),
                index,
                index + query.length,
                android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            startIndex = index + query.length
        }
        
        return spannable
    }
    
    /**
     * 过滤未读聊天
     * 验证: 需求 4.3
     */
    fun filterUnreadConversations(conversations: List<UiConversation>): List<UiConversation> {
        return conversations.filter { it.unreadCount > 0 }
    }
    
    /**
     * 按未读状态排序对话
     * 验证: 需求 4.3
     */
    fun sortByUnreadStatus(conversations: List<UiConversation>): List<UiConversation> {
        return conversations.sortedWith(compareBy(
            { it.unreadCount == 0 },  // 未读的在前
            { it.lastMessageTime }     // 然后按时间排序
        ))
    }
}
