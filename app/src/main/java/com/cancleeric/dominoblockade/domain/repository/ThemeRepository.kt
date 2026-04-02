package com.cancleeric.dominoblockade.domain.repository

import com.cancleeric.dominoblockade.domain.model.AppTheme
import com.cancleeric.dominoblockade.domain.model.DominoStyle
import kotlinx.coroutines.flow.Flow

interface ThemeRepository {
    fun getAppTheme(): Flow<AppTheme>
    fun getDominoStyle(): Flow<DominoStyle>
    suspend fun setAppTheme(theme: AppTheme)
    suspend fun setDominoStyle(style: DominoStyle)
}
