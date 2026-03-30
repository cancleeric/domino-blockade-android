package com.dominoblockade.domain.model

data class Domino(
    val left: Int,   // 左邊點數 0-6
    val right: Int,  // 右邊點數 0-6
    val id: Int      // 唯一識別碼
) {
    val isDouble: Boolean get() = left == right
    val totalPips: Int get() = left + right
    fun hasValue(value: Int): Boolean = left == value || right == value
    fun flip(): Domino = copy(left = right, right = left)
}
