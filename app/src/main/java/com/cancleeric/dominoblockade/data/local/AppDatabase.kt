package com.cancleeric.dominoblockade.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [GameRecord::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase()
