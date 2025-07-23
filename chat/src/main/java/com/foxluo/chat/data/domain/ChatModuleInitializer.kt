// chat/src/main/java/com/foxluo/chat/data/domain/ChatModuleInitializer.kt
package com.foxluo.chat.data.domain

import android.app.Application
import androidx.room.Room
import com.foxluo.baselib.data.manager.AuthManager
import com.foxluo.baselib.domain.ModuleInitializer
import com.foxluo.chat.data.database.ChatDatabase
import com.foxluo.chat.data.database.FriendEntity
import com.foxluo.chat.data.database.mergre.MIGRATION_1_2
import com.foxluo.chat.data.database.mergre.MIGRATION_2_3
import com.foxluo.chat.data.database.mergre.MIGRATION_3_4
import com.foxluo.chat.data.database.mergre.MIGRATION_4_5
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

class ChatModuleInitializer : ModuleInitializer {
    // 单例管理
    companion object {
        private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

        lateinit var chatDb: ChatDatabase

        // 对外暴露的只读StateFlow
        val friendshipFlow: StateFlow<List<FriendEntity>>
            get() = _friendshipState.asStateFlow()

        private val _friendshipState = MutableStateFlow<List<FriendEntity>>(emptyList())
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override suspend fun onAppCreate(app: Application) {
        // 初始化数据库
        chatDb = Room.databaseBuilder(
            app,
            ChatDatabase::class.java, "fox_resource_chat"
        ).addMigrations(MIGRATION_1_2)
            .addMigrations(MIGRATION_2_3)
            .addMigrations(MIGRATION_3_4)
            .addMigrations(MIGRATION_4_5)
            .build()

        // 启动监听
        scope.launch {
            AuthManager.userInfoStateFlow
                .flatMapLatest { user ->
                    if (user != null) {
                        // 用户登录状态变化时重新绑定查询
                        chatDb.friendDao().getAllFriends(user.id.toInt())
                    } else {
                        // 用户注销时清空数据
                        flowOf(emptyList())
                    }
                }
                .catch { e ->
                    // 错误处理
                    _friendshipState.value = emptyList()
                }
                .collect { friends ->
                    _friendshipState.value = friends
                }
        }
    }
}