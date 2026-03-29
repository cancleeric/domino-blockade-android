package com.cancleeric.dominoblockade.domain.model

enum class BoardEnd { LEFT, RIGHT }

data class PlacedDomino(
    val domino: Domino,
    val isHorizontal: Boolean = true
)
