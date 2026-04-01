package com.cancleeric.dominoblockade.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import com.cancleeric.dominoblockade.domain.repository.TutorialRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TutorialRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : TutorialRepository {

    private val completedKey = booleanPreferencesKey("tutorial_completed")

    override val isTutorialCompleted: Flow<Boolean> =
        dataStore.data.map { it[completedKey] ?: false }

    override suspend fun markTutorialCompleted() {
        dataStore.edit { it[completedKey] = true }
    }
}
