package com.cancleeric.dominoblockade.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.cancleeric.dominoblockade.data.local.entity.ThemeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ThemeDao {
    @Query("SELECT * FROM theme_settings WHERE id = 1")
    fun getTheme(): Flow<ThemeEntity?>

    @Query("SELECT * FROM theme_settings WHERE id = 1")
    suspend fun getThemeOnce(): ThemeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTheme(theme: ThemeEntity)
}
