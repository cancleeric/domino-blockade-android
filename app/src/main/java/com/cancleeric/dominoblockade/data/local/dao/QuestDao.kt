package com.cancleeric.dominoblockade.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.cancleeric.dominoblockade.data.local.entity.QuestProfileEntity
import com.cancleeric.dominoblockade.data.local.entity.QuestTaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface QuestDao {
    @Query("SELECT * FROM quest_tasks ORDER BY type ASC, id ASC")
    fun observeTasks(): Flow<List<QuestTaskEntity>>

    @Query("SELECT * FROM quest_tasks ORDER BY type ASC, id ASC")
    suspend fun getTasksOnce(): List<QuestTaskEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTasks(tasks: List<QuestTaskEntity>)

    @Query("DELETE FROM quest_tasks WHERE type = :type")
    suspend fun deleteTasksByType(type: String)

    @Query("SELECT * FROM quest_profile WHERE id = 1")
    fun observeProfile(): Flow<QuestProfileEntity?>

    @Query("SELECT * FROM quest_profile WHERE id = 1")
    suspend fun getProfileOnce(): QuestProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProfile(profile: QuestProfileEntity)
}
