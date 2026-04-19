package com.cancleeric.dominoblockade.presentation.notification

interface RankChangeNotifier {
    fun notifyRankChange(newRank: Int, previousRank: Int?)
}
