package domilopment.apkextractor.data

import android.net.Uri
import androidx.appcompat.app.AppCompatDelegate
import domilopment.apkextractor.utils.apkActions.ApkActionsOptions

data class SettingsScreenState(
    val autoBackupAppsListState: SettingsScreenAppAutoBackUpListState = SettingsScreenAppAutoBackUpListState(emptyList()),
    val saveDir: Uri? = null,
    val saveName: Set<String> = setOf("0:name"),
    val autoBackupService: Boolean = false,
    val autoBackupList: Set<String> = emptySet(),
    val nightMode: Int = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM,
    val useMaterialYou: Boolean = true,
    val rightSwipeAction: String = ApkActionsOptions.SAVE.preferenceValue,
    val leftSwipeAction: String = ApkActionsOptions.SHARE.preferenceValue,
    val swipeActionCustomThreshold: Boolean = false,
    val swipeActionThresholdMod: Float = 32f,
    val checkUpdateOnStart: Boolean = false,
    val backupModeXapk: Boolean = false
)
