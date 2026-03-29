package com.cancleeric.dominoblockade.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import app.cash.turbine.test
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class SettingsRepositoryImplTest {

    private lateinit var dataStore: DataStore<Preferences>
    private lateinit var repository: SettingsRepositoryImpl

    @Before
    fun setup() {
        dataStore = mockk(relaxed = true)
        repository = SettingsRepositoryImpl(dataStore)
    }

    @Test
    fun `soundEnabled returns true by default`() = runTest {
        val prefs = mockk<Preferences>()
        every { prefs[booleanPreferencesKey("sound_enabled")] } returns null
        every { dataStore.data } returns flowOf(prefs)

        repository.soundEnabled.test {
            assertEquals(true, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `soundEnabled returns stored value`() = runTest {
        val prefs = mockk<Preferences>()
        every { prefs[booleanPreferencesKey("sound_enabled")] } returns false
        every { dataStore.data } returns flowOf(prefs)

        repository.soundEnabled.test {
            assertEquals(false, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `defaultAiDifficulty returns medium by default`() = runTest {
        val prefs = mockk<Preferences>()
        every { prefs[stringPreferencesKey("default_ai_difficulty")] } returns null
        every { dataStore.data } returns flowOf(prefs)

        repository.defaultAiDifficulty.test {
            assertEquals("medium", awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `defaultPlayerCount returns 2 by default`() = runTest {
        val prefs = mockk<Preferences>()
        every { prefs[intPreferencesKey("default_player_count")] } returns null
        every { dataStore.data } returns flowOf(prefs)

        repository.defaultPlayerCount.test {
            assertEquals(2, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `language returns zh-TW by default`() = runTest {
        val prefs = mockk<Preferences>()
        every { prefs[stringPreferencesKey("language")] } returns null
        every { dataStore.data } returns flowOf(prefs)

        repository.language.test {
            assertEquals("zh-TW", awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `language returns stored value`() = runTest {
        val prefs = mockk<Preferences>()
        every { prefs[stringPreferencesKey("language")] } returns "en"
        every { dataStore.data } returns flowOf(prefs)

        repository.language.test {
            assertEquals("en", awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `darkMode returns system by default`() = runTest {
        val prefs = mockk<Preferences>()
        every { prefs[stringPreferencesKey("dark_mode")] } returns null
        every { dataStore.data } returns flowOf(prefs)

        repository.darkMode.test {
            assertEquals("system", awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `darkMode returns stored value`() = runTest {
        val prefs = mockk<Preferences>()
        every { prefs[stringPreferencesKey("dark_mode")] } returns "dark"
        every { dataStore.data } returns flowOf(prefs)

        repository.darkMode.test {
            assertEquals("dark", awaitItem())
            awaitComplete()
        }
    }
}
