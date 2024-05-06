package domilopment.apkextractor.ui.viewModels

import android.content.pm.PackageInstaller
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import domilopment.apkextractor.R
import domilopment.apkextractor.data.ProgressDialogUiState
import domilopment.apkextractor.data.UiText
import domilopment.apkextractor.domain.usecase.installer.InstallUseCase
import domilopment.apkextractor.utils.InstallApkResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InstallerActivityViewModel @Inject constructor(
    private val installUseCase: InstallUseCase,
) : ViewModel() {
    var uiState by mutableStateOf(
        ProgressDialogUiState(
            title = UiText(R.string.progress_dialog_title_install, "XAPK"),
            process = null,
            progress = 0F,
            tasks = 100,
            shouldBeShown = true
        )
    )
        private set

    private var session: PackageInstaller.Session? = null
    private var task: Job? = null

    fun updateState(
        packageName: String? = uiState.process, progress: Float = uiState.progress / 100
    ) {
        uiState = uiState.copy(process = packageName, progress = progress * 100)
    }

    fun setProgressDialogActive(active: Boolean) {
        uiState = uiState.copy(shouldBeShown = active)
    }

    fun installXAPK(fileUri: Uri) {
        task = viewModelScope.launch {
            installUseCase(fileUri).collect {
                when (it) {
                    is InstallApkResult.OnPrepare -> {
                        session = it.session
                        updateState(packageName = it.packageName, 0F)
                    }

                    is InstallApkResult.OnProgress -> updateState(
                        packageName = it.packageName, progress = it.progress
                    )

                    is InstallApkResult.OnSuccess, is InstallApkResult.OnFail -> setProgressDialogActive(
                        false
                    )
                }
            }
        }
    }

    fun cancel() {
        task?.cancel()
        task = null
        val temp = session
        session = null
        temp?.abandon()
    }
}
