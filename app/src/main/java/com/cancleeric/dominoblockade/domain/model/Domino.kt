package com.cancleeric.dominoblockade.domain.model

data class Domino(
    val left: Int,
    val right: Int
) {
    val total: Int get() = left + right
    val isDouble: Boolean get() = left == right

    /** Accessibility description for TalkBack, e.g. "雙6骨牌" or "骨牌3對5，共8點" */
    fun contentDescription(): String = if (isDouble) {
        "雙${left}骨牌"
    } else {
        "骨牌${left}對${right}，共${total}點"
    }
}
