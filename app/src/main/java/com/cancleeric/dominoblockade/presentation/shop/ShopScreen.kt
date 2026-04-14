package com.cancleeric.dominoblockade.presentation.shop

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cancleeric.dominoblockade.domain.model.ShopCatalog
import com.cancleeric.dominoblockade.domain.model.ShopCategory
import com.cancleeric.dominoblockade.domain.model.ShopItem

private const val PADDING_DP = 16
private const val ITEM_SPACING_DP = 12

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShopScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ShopViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var confirmItemId by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val categories = ShopCategory.entries
    val filteredItems = uiState.items.filter { it.category == uiState.selectedCategory }

    LaunchedEffect(Unit) {
        viewModel.events.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("In-Game Shop") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(PADDING_DP.dp),
            verticalArrangement = Arrangement.spacedBy(ITEM_SPACING_DP.dp)
        ) {
            item {
                CoinBalanceCard(balance = uiState.coinBalance)
            }
            item {
                CategoryRow(
                    categories = categories,
                    selected = uiState.selectedCategory,
                    onCategorySelected = viewModel::selectCategory
                )
            }
            items(filteredItems, key = { it.id }) { item ->
                ShopItemCard(
                    item = item,
                    isOwned = uiState.ownedItemIds.contains(item.id),
                    isEquipped = uiState.selectedSkinItemId == item.id,
                    onBuy = { confirmItemId = item.id },
                    onEquip = { viewModel.equip(item.id) }
                )
            }
        }
    }

    confirmItemId?.let { itemId ->
        val item = ShopCatalog.find(itemId)
        if (item != null) {
            AlertDialog(
                onDismissRequest = { confirmItemId = null },
                title = { Text("Confirm Purchase") },
                text = { Text("Buy ${item.title} for ${item.price} coins?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.purchase(item.id)
                            confirmItemId = null
                        }
                    ) { Text("Buy") }
                },
                dismissButton = {
                    TextButton(onClick = { confirmItemId = null }) {
                        Text("Cancel")
                    }
                }
            )
        } else {
            confirmItemId = null
        }
    }
}

@Composable
private fun CoinBalanceCard(balance: Int) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(PADDING_DP.dp)) {
            Text(text = "Coin Balance", style = MaterialTheme.typography.titleMedium)
            Text(text = "🪙 $balance", style = MaterialTheme.typography.headlineSmall)
        }
    }
}

@Composable
private fun CategoryRow(
    categories: List<ShopCategory>,
    selected: ShopCategory,
    onCategorySelected: (ShopCategory) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        categories.forEach { category ->
            FilterChip(
                selected = category == selected,
                onClick = { onCategorySelected(category) },
                label = {
                    Text(
                        when (category) {
                            ShopCategory.BASIC_SKINS -> "Basic Skins"
                            ShopCategory.PREMIUM_SKINS -> "Premium Skins"
                        }
                    )
                }
            )
        }
    }
}

@Composable
private fun ShopItemCard(
    item: ShopItem,
    isOwned: Boolean,
    isEquipped: Boolean,
    onBuy: () -> Unit,
    onEquip: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(PADDING_DP.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = item.title, style = MaterialTheme.typography.titleMedium)
            Text(text = item.description, style = MaterialTheme.typography.bodyMedium)
            Text(text = "Cost: ${item.price} coins", style = MaterialTheme.typography.labelLarge)
            when {
                isEquipped -> {
                    Button(onClick = {}, enabled = false) { Text("Equipped") }
                }
                isOwned -> {
                    Button(onClick = onEquip) { Text("Equip") }
                }
                else -> {
                    Button(onClick = onBuy) { Text("Buy") }
                }
            }
        }
    }
}
