package com.cancleeric.dominoblockade.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.cancleeric.dominoblockade.domain.model.AppTheme
import com.cancleeric.dominoblockade.domain.model.DominoStyle
import com.cancleeric.dominoblockade.domain.model.ThemePreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

@Singleton
class UserPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val APP_THEME = stringPreferencesKey("app_theme")
        val DOMINO_STYLE = stringPreferencesKey("domino_style")
    }

    val themePreferences: Flow<ThemePreferences> = context.dataStore.data.map { prefs ->
        ThemePreferences(
            appTheme = prefs[Keys.APP_THEME]
                ?.let { runCatching { AppTheme.valueOf(it) }.getOrNull() }
                ?: AppTheme.CLASSIC,
            dominoStyle = prefs[Keys.DOMINO_STYLE]
                ?.let { runCatching { DominoStyle.valueOf(it) }.getOrNull() }
                ?: DominoStyle.DOTS
        )
    }

    suspend fun setAppTheme(appTheme: AppTheme) {
        context.dataStore.edit { prefs ->
            prefs[Keys.APP_THEME] = appTheme.name
        }
    }

    suspend fun setDominoStyle(dominoStyle: DominoStyle) {
        context.dataStore.edit { prefs ->
            prefs[Keys.DOMINO_STYLE] = dominoStyle.name
        }
    }
}
