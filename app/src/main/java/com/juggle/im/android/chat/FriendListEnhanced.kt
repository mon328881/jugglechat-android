package com.juggle.im.android.chat

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.juggle.im.android.widget.FriendListItemView

/**
 * 朋友列表增强功能
 * 支持搜索、筛选和字母分组
 */
class FriendListEnhanced(
    private val context: Context,
    private val recyclerView: RecyclerView
) {
    
    /**
     * 朋友数据类
     */
    data class Friend(
        val id: String,
        val nickname: String,
        val avatar: String,
        val isOnline: Boolean = false,
        val lastActiveTime: String = "",
        val signature: String = ""
    )
    
    private val allFriends = mutableListOf<Friend>()
    private val filteredFriends = mutableListOf<Friend>()
    private val friendsByLetter = mutableMapOf<String, MutableList<Friend>>()
    
    private var searchQuery = ""
    private var onFriendClickListener: ((Friend) -> Unit)? = null
    
    init {
        recyclerView.layoutManager = LinearLayoutManager(context)
    }
    
    /**
     * 设置朋友列表数据
     */
    fun setFriends(friends: List<Friend>) {
        allFriends.clear()
        allFriends.addAll(friends)
        groupFriendsByLetter()
        filterAndSort()
    }
    
    /**
     * 按首字母分组朋友
     */
    private fun groupFriendsByLetter() {
        friendsByLetter.clear()
        
        for (friend in allFriends) {
            val firstLetter = friend.nickname.firstOrNull()?.uppercaseChar()?.toString() ?: "#"
            
            // 如果首字母不是字母，归类到 #
            val letter = if (firstLetter.matches(Regex("[A-Z]"))) firstLetter else "#"
            
            if (!friendsByLetter.containsKey(letter)) {
                friendsByLetter[letter] = mutableListOf()
            }
            friendsByLetter[letter]?.add(friend)
        }
    }
    
    /**
     * 过滤和排序朋友列表
     */
    private fun filterAndSort() {
        filteredFriends.clear()
        
        // 按搜索条件过滤
        val filtered = if (searchQuery.isEmpty()) {
            allFriends
        } else {
            allFriends.filter { friend ->
                friend.nickname.contains(searchQuery, ignoreCase = true) ||
                friend.id.contains(searchQuery, ignoreCase = true)
            }
        }
        
        // 按首字母排序
        val sortedLetters = filtered
            .groupBy { friend ->
                val firstLetter = friend.nickname.firstOrNull()?.uppercaseChar()?.toString() ?: "#"
                if (firstLetter.matches(Regex("[A-Z]"))) firstLetter else "#"
            }
            .toSortedMap()
        
        // 添加到过滤列表
        for ((_, friends) in sortedLetters) {
            filteredFriends.addAll(friends.sortedBy { it.nickname })
        }
        
        updateRecyclerView()
    }
    
    /**
     * 更新 RecyclerView
     */
    private fun updateRecyclerView() {
        // 这里应该更新 RecyclerView 的适配器
        // 由于这是一个增强功能类，具体的适配器实现由调用者提供
    }
    
    /**
     * 搜索朋友
     */
    fun searchFriends(query: String) {
        searchQuery = query
        filterAndSort()
    }
    
    /**
     * 获取过滤后的朋友列表
     */
    fun getFilteredFriends(): List<Friend> = filteredFriends.toList()
    
    /**
     * 获取按首字母分组的朋友列表
     */
    fun getFriendsByLetter(): Map<String, List<Friend>> {
        return friendsByLetter.mapValues { it.value.toList() }
    }
    
    /**
     * 获取所有朋友
     */
    fun getAllFriends(): List<Friend> = allFriends.toList()
    
    /**
     * 设置朋友点击监听
     */
    fun setOnFriendClickListener(listener: (Friend) -> Unit) {
        onFriendClickListener = listener
    }
    
    /**
     * 创建搜索框
     */
    fun createSearchBox(parent: LinearLayout): EditText {
        val searchBox = EditText(context).apply {
            hint = "搜索朋友..."
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(16, 16, 16, 16)
            }
            
            addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    searchFriends(s?.toString() ?: "")
                }
                
                override fun afterTextChanged(s: Editable?) {}
            })
        }
        
        parent.addView(searchBox, 0)
        return searchBox
    }
    
    /**
     * 获取朋友总数
     */
    fun getFriendCount(): Int = allFriends.size
    
    /**
     * 获取过滤后的朋友总数
     */
    fun getFilteredFriendCount(): Int = filteredFriends.size
    
    /**
     * 清空搜索
     */
    fun clearSearch() {
        searchQuery = ""
        filterAndSort()
    }
}
