package com.appcasa.features.dashboard.presentation.model

import androidx.compose.ui.graphics.vector.ImageVector

data class SearchItem(
    val id: String,
    val title: String,
    val type: SearchType,
    val icon: ImageVector,
    val route: Any
)

enum class SearchType {
    TASK, LIST, MEMBER, STOCK, EXPENSE, MAINTENANCE
}
