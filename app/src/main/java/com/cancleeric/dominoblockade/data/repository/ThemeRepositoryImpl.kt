package com.cancleeric.dominoblockade.data.repository

import com.cancleeric.dominoblockade.data.local.dao.ThemeDao
import com.cancleeric.dominoblockade.data.local.entity.ThemeEntity
import com.cancleeric.dominoblockade.domain.model.AppTheme
import com.cancleeric.dominoblockade.domain.model.DominoStyle
import com.cancleeric.dominoblockade.domain.repository.ThemeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ThemeRepositoryImpl @Inject constructor(
    private val themeDao: ThemeDao
) : ThemeRepository {

    private val defaultTheme = ThemeEntity()

    override fun getAppTheme(): Flow<AppTheme> =
        themeDao.getTheme().map { entity ->
            val name = entity?.appTheme ?: defaultTheme.appTheme
            AppTheme.entries.firstOrNull { it.name == name } ?: AppTheme.CLASSIC
        }

    override fun getDominoStyle(): Flow<DominoStyle> =
        themeDao.getTheme().map { entity ->
            val name = entity?.dominoStyle ?: defaultTheme.dominoStyle
            DominoStyle.entries.firstOrNull { it.name == name } ?: DominoStyle.DOTS
        }

    override suspend fun setAppTheme(theme: AppTheme) {
        val current = themeDao.getThemeOnce() ?: defaultTheme
        themeDao.upsertTheme(current.copy(appTheme = theme.name))
    }

    override suspend fun setDominoStyle(style: DominoStyle) {
        val current = themeDao.getThemeOnce() ?: defaultTheme
        themeDao.upsertTheme(current.copy(dominoStyle = style.name))
    }
}
