package com.cancleeric.dominoblockade.domain.model

enum class ShopCategory {
    BASIC_SKINS,
    PREMIUM_SKINS
}

data class ShopItem(
    val id: String,
    val title: String,
    val description: String,
    val category: ShopCategory,
    val price: Int,
    val skin: DominoSkin
)

object ShopCatalog {
    const val DEFAULT_SKIN_ID = "skin_classic"

    val items: List<ShopItem> = listOf(
        ShopItem(
            id = DEFAULT_SKIN_ID,
            title = "Classic",
            description = "Standard domino finish",
            category = ShopCategory.BASIC_SKINS,
            price = 0,
            skin = DominoSkin.CLASSIC
        ),
        ShopItem(
            id = "skin_marble",
            title = "Marble",
            description = "Polished marble dominoes",
            category = ShopCategory.BASIC_SKINS,
            price = 120,
            skin = DominoSkin.MARBLE
        ),
        ShopItem(
            id = "skin_neon",
            title = "Neon",
            description = "High-contrast neon style",
            category = ShopCategory.PREMIUM_SKINS,
            price = 250,
            skin = DominoSkin.NEON
        ),
        ShopItem(
            id = "skin_gold",
            title = "Gold",
            description = "Legendary gold trim",
            category = ShopCategory.PREMIUM_SKINS,
            price = 400,
            skin = DominoSkin.GOLD
        )
    )

    fun find(itemId: String): ShopItem? = items.firstOrNull { it.id == itemId }
}
