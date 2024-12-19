package model

data class HistoryItem(val expression: String, val result: String)

val historyList = mutableListOf<HistoryItem>()
