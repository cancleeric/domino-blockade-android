package com.cancleeric.dominoblockade.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "game_moves")
data class GameMoveEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val replayId: Long,
    val moveIndex: Int,
    val playerIndex: Int,
    val playerName: String,
    val moveType: String,
    val dominoLeft: Int,
    val dominoRight: Int,
    val boardState: String,
    val boneyardSize: Int
)
