package domilopment.apkextractor.ui.viewModels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import domilopment.apkextractor.UpdateTrigger
import domilopment.apkextractor.data.ApkListFragmentUIState
import domilopment.apkextractor.data.ApkOptionsBottomSheetUIState
import domilopment.apkextractor.data.PackageArchiveModel
import domilopment.apkextractor.utils.ListOfAPKs
import domilopment.apkextractor.utils.settings.ApkSortOptions
import domilopment.apkextractor.utils.settings.SettingsManager
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.CancellationException

class ApkListViewModel(application: Application) : AndroidViewModel(application) {
    private val _packageArchives: MutableLiveData<List<PackageArchiveModel>> by lazy {
        MutableLiveData<List<PackageArchiveModel>>().also {
            viewModelScope.launch {
                it.value = loadApks()
            }
        }
    }
    val packageArchives: LiveData<List<PackageArchiveModel>> = _packageArchives

    private val _apkListFragmentState: MutableStateFlow<ApkListFragmentUIState> =
        MutableStateFlow(ApkListFragmentUIState())
    val apkListFragmentState: StateFlow<ApkListFragmentUIState> =
        _apkListFragmentState.asStateFlow()

    private val _apkOptionsBottomSheetState: MutableStateFlow<ApkOptionsBottomSheetUIState> =
        MutableStateFlow(ApkOptionsBottomSheetUIState())
    val akpOptionsBottomSheetUIState: StateFlow<ApkOptionsBottomSheetUIState> =
        _apkOptionsBottomSheetState.asStateFlow()

    private val _searchQuery: MutableLiveData<String?> = MutableLiveData(null)
    val searchQuery: LiveData<String?> = _searchQuery

    private val context get() = getApplication<Application>().applicationContext

    private var loadArchiveInfoJob: Deferred<Unit>? = null

    init {
        // Set applications in view once they are loaded
        _packageArchives.observeForever { apps ->
            val sortedApps = SettingsManager(context).sortApkData(apps)
            _apkListFragmentState.update { state ->
                state.copy(
                    appList = sortedApps, isRefreshing = false, updateTrigger = UpdateTrigger(true)
                )
            }
            loadArchiveInfoJob = viewModelScope.async(Dispatchers.IO) {
                sortedApps.forEach {
                    it.loadPackageArchiveInfo(context)
                }
            }
        }
    }

    /**
     * Select a specific Application from list in view
     * and set it in BottomSheet state
     * @param app selected application
     */
    fun selectPackageArchive(app: PackageArchiveModel?) {
        if (app?.isPackageArchiveInfoLoaded == false) app.loadPackageArchiveInfo(context)
        _apkOptionsBottomSheetState.update { state ->
            state.copy(
                packageArchiveModel = app
            )
        }
    }

    /**
     * Set query string from search in App List, sets empty string if null
     * @param query last input Search String
     */
    fun searchQuery(query: String?) {
        _searchQuery.value = query
    }

    /**
     * Update App list
     */
    fun updatePackageArchives() {
        _apkListFragmentState.update {
            it.copy(isRefreshing = true)
        }
        viewModelScope.launch {
            val load = async(Dispatchers.IO) {
                return@async loadApks()
            }
            val apps = load.await()
            _packageArchives.postValue(apps)
        }
    }

    fun remove(apk: PackageArchiveModel) {
        _packageArchives.value = _packageArchives.value?.let { apps ->
            apps.toMutableList().apply {
                remove(apk)
            }
        }
    }

    fun forceRefresh(apk: PackageArchiveModel) {
        viewModelScope.async(Dispatchers.IO) { apk.forceRefresh(context) }
    }

    fun sort(sortPreferenceId: ApkSortOptions) {
        loadArchiveInfoJob?.cancel(CancellationException("New sort order"))

        _apkListFragmentState.update { state ->
            state.copy(isRefreshing = true)
        }

        var sortedApps: List<PackageArchiveModel>? = null

        _apkListFragmentState.update { state ->
            sortedApps = SettingsManager(context).sortApkData(state.appList, sortPreferenceId)
            state.copy(
                appList = sortedApps!!, isRefreshing = false
            )
        }
        loadArchiveInfoJob = viewModelScope.async(Dispatchers.IO) {
            sortedApps?.forEach {
                it.loadPackageArchiveInfo(context)
            }
        }
    }

    /**
     * Load apps from device
     */
    private suspend fun loadApks(): List<PackageArchiveModel> = withContext(Dispatchers.IO) {
        // Do an asynchronous operation to fetch users.
        return@withContext ListOfAPKs(context).apkFiles()
    }
}
