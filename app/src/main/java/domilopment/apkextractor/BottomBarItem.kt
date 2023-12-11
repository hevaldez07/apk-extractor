package domilopment.apkextractor

import androidx.compose.ui.graphics.vector.ImageVector

data class BottomBarItem(
    val icon: ImageVector,
    val onClick: () -> Unit
)
