package com.cancleeric.dominoblockade.presentation.shop

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cancleeric.dominoblockade.domain.model.ShopCatalog
import com.cancleeric.dominoblockade.domain.model.ShopCategory
import com.cancleeric.dominoblockade.domain.model.ShopItem
import com.cancleeric.dominoblockade.domain.repository.PurchaseResult
import com.cancleeric.dominoblockade.domain.repository.ShopRepository
import com.cancleeric.dominoblockade.domain.repository.ThemeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ShopUiState(
    val coinBalance: Int = 0,
    val selectedCategory: ShopCategory = ShopCategory.BASIC_SKINS,
    val selectedSkinItemId: String = ShopCatalog.DEFAULT_SKIN_ID,
    val ownedItemIds: Set<String> = setOf(ShopCatalog.DEFAULT_SKIN_ID),
    val items: List<ShopItem> = ShopCatalog.items
)

@HiltViewModel
class ShopViewModel @Inject constructor(
    private val shopRepository: ShopRepository,
    private val themeRepository: ThemeRepository
) : ViewModel() {

    private val selectedCategory = MutableStateFlow(ShopCategory.BASIC_SKINS)
    private val _events = MutableSharedFlow<String>()
    val events = _events.asSharedFlow()

    val uiState: StateFlow<ShopUiState> = combine(
        shopRepository.observeCoinBalance(),
        shopRepository.observeOwnedItemIds(),
        themeRepository.getDominoSkin(),
        selectedCategory
    ) { balance, ownedIds, selectedSkin, category ->
        val selectedItem = ShopCatalog.items.firstOrNull { it.skin == selectedSkin }?.id
            ?: ShopCatalog.DEFAULT_SKIN_ID
        ShopUiState(
            coinBalance = balance,
            selectedCategory = category,
            selectedSkinItemId = selectedItem,
            ownedItemIds = ownedIds,
            items = ShopCatalog.items
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, ShopUiState())

    init {
        viewModelScope.launch {
            runCatching { shopRepository.syncWithRemote() }
                .onFailure { _events.emit("Shop sync unavailable. Using offline cache.") }
        }
    }

    fun selectCategory(category: ShopCategory) {
        selectedCategory.value = category
    }

    fun purchase(itemId: String) {
        viewModelScope.launch {
            when (val result = shopRepository.purchase(itemId)) {
                PurchaseResult.Success -> _events.emit("Purchase successful")
                PurchaseResult.NotFound -> _events.emit("Item not found")
                PurchaseResult.AlreadyOwned -> _events.emit("Item already owned")
                PurchaseResult.InsufficientCoins -> _events.emit("Not enough coins")
                is PurchaseResult.Error -> _events.emit(result.message)
            }
        }
    }

    fun equip(itemId: String) {
        val item = ShopCatalog.find(itemId) ?: return
        if (!uiState.value.ownedItemIds.contains(itemId)) return
        viewModelScope.launch {
            themeRepository.setDominoSkin(item.skin)
            _events.emit("${item.title} equipped")
        }
    }
}
