package domilopment.apkextractor.data

data class ProgressDialogUiState(
    val title: String? = null,
    val process: String? = null,
    val progress: Float = 0f,
    val tasks: Int = 0,
    val shouldBeShown: Boolean = false
)