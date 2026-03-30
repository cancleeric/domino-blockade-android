package com.cancleeric.dominoblockade.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.cancleeric.dominoblockade.domain.repository.TutorialRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.tutorialDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "tutorial_preferences"
)

class TutorialPreferencesRepository(private val context: Context) : TutorialRepository {

    companion object {
        private val TUTORIAL_COMPLETED = booleanPreferencesKey("tutorial_completed")
    }

    override val isTutorialCompleted: Flow<Boolean> = context.tutorialDataStore.data
        .map { preferences -> preferences[TUTORIAL_COMPLETED] ?: false }

    override suspend fun markTutorialCompleted() {
        context.tutorialDataStore.edit { preferences ->
            preferences[TUTORIAL_COMPLETED] = true
        }
    }
}
