package com.cancleeric.dominoblockade.presentation.menu

import androidx.lifecycle.ViewModel
import com.cancleeric.dominoblockade.domain.repository.TutorialRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class MenuViewModel @Inject constructor(
    private val tutorialRepository: TutorialRepository
) : ViewModel() {

    /**
     * Emits true if the tutorial should auto-start (not yet completed).
     * Returns null while the DataStore value is being loaded.
     */
    val shouldAutoStartTutorial: Flow<Boolean?> =
        tutorialRepository.isTutorialCompleted.map { completed -> !completed }
}
