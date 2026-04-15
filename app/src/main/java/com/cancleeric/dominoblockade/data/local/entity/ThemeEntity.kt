package com.cancleeric.dominoblockade.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.cancleeric.dominoblockade.domain.model.AppTheme
import com.cancleeric.dominoblockade.domain.model.DominoSkin
import com.cancleeric.dominoblockade.domain.model.DominoStyle

@Entity(tableName = "theme_settings")
data class ThemeEntity(
    @PrimaryKey val id: Int = 1,
    val appTheme: String = AppTheme.CLASSIC.name,
    val dominoStyle: String = DominoStyle.DOTS.name,
    val dominoSkin: String = DominoSkin.CLASSIC.name
)
