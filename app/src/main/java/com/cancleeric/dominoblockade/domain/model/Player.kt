package com.cancleeric.dominoblockade.domain.model

data class Player(
    val id: String,
    val name: String,
    val hand: List<Domino> = emptyList(),
    val score: Int = 0
)
