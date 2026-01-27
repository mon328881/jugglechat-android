package com.juggle.im.android.chat

import android.content.Context
import android.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.juggle.im.android.widget.ChatListItemView

/**
 * 聊天列表增强功能
 * 提供下拉刷新、搜索、长按菜单等功能
 */
class ChatListEnhanced {
    
    /**
     * 聊天列表操作回调
     */
    interface ChatListCallback {
        fun onChatItemClick(chatId: String)
        fun onChatItemLongClick(chatId: String)
        fun onDeleteChat(chatId: String)
        fun onPinChat(chatId: String)
        fun onMuteChat(chatId: String)
        fun onRefresh()
        fun onSearch(query: String)
    }
    
    /**
     * 显示聊天项长按菜单
     */
    fun showContextMenu(
        context: Context,
        view: android.view.View,
        chatId: String,
        callback: ChatListCallback
    ) {
        val popupMenu = PopupMenu(context, view)
        popupMenu.menu.apply {
            add("删除").setOnMenuItemClickListener {
                callback.onDeleteChat(chatId)
                true
            }
            add("置顶").setOnMenuItemClickListener {
                callback.onPinChat(chatId)
                true
            }
            add("静音").setOnMenuItemClickListener {
                callback.onMuteChat(chatId)
                true
            }
        }
        popupMenu.show()
    }
    
    /**
     * 设置下拉刷新
     */
    fun setupPullToRefresh(
        refreshLayout: androidx.swiperefreshlayout.widget.SwipeRefreshLayout,
        callback: ChatListCallback
    ) {
        refreshLayout.setOnRefreshListener {
            callback.onRefresh()
            refreshLayout.isRefreshing = false
        }
    }
    
    /**
     * 设置搜索功能
     */
    fun setupSearch(
        searchView: androidx.appcompat.widget.SearchView,
        callback: ChatListCallback
    ) {
        searchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (!query.isNullOrEmpty()) {
                    callback.onSearch(query)
                }
                return true
            }
            
            override fun onQueryTextChange(newText: String?): Boolean {
                if (!newText.isNullOrEmpty()) {
                    callback.onSearch(newText)
                } else {
                    callback.onSearch("")
                }
                return true
            }
        })
    }
    
    /**
     * 过滤聊天列表
     */
    fun filterChatList(
        chatList: List<ChatListItemView.ChatItem>,
        query: String
    ): List<ChatListItemView.ChatItem> {
        if (query.isEmpty()) {
            return chatList
        }
        
        return chatList.filter { chat ->
            chat.participantName.contains(query, ignoreCase = true) ||
            chat.lastMessage.contains(query, ignoreCase = true)
        }
    }
    
    /**
     * 排序聊天列表
     */
    fun sortChatList(
        chatList: List<ChatListItemView.ChatItem>
    ): List<ChatListItemView.ChatItem> {
        return chatList.sortedWith(compareBy<ChatListItemView.ChatItem> { !it.isPinned }
            .thenBy { it.lastMessageTime })
    }
}
