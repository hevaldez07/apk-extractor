package domilopment.apkextractor.dependencyInjection.packageArchive

import android.content.Context
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap
import dagger.hilt.android.qualifiers.ApplicationContext
import domilopment.apkextractor.data.room.dao.ApkDao
import domilopment.apkextractor.data.room.entities.PackageArchiveEntity
import domilopment.apkextractor.domain.mapper.PackageArchiveModelToPackageArchiveEntityMapper
import domilopment.apkextractor.domain.mapper.mapAll
import domilopment.apkextractor.utils.Utils
import domilopment.apkextractor.utils.settings.PackageArchiveUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import javax.inject.Inject

interface PackageArchiveRepository {
    val apks: Flow<List<PackageArchiveEntity>>
    suspend fun updateApps()
    suspend fun addApk(apk: PackageArchiveEntity)
    suspend fun removeApk(apk: PackageArchiveEntity)
    suspend fun updateApp(apk: PackageArchiveEntity)
}

class MyPackageArchiveRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val apkDao: ApkDao,
    private val packageArchiveService: ListOfAPKs
) : PackageArchiveRepository {
    private val updateTrigger = MutableStateFlow(true)

    override val apks: Flow<List<PackageArchiveEntity>> =
        apkDao.getApks().combine(updateTrigger) { list, _ -> list }

    override suspend fun updateApps() = withContext(Dispatchers.IO) {
        val onDisk = packageArchiveService.apks.first()
        val inDb = apkDao.getApks().first()

        val mustAdd = onDisk.filter { it.fileUri !in inDb.map { it.fileUri } }.also {
            apkDao.upsertApks(it.map {
                PackageArchiveEntity(
                    fileUri = it.fileUri,
                    fileName = it.fileName,
                    fileType = it.fileType,
                    fileLastModified = it.fileLastModified,
                    fileSize = it.fileSize
                )
            })
        }.let {
            PackageArchiveModelToPackageArchiveEntityMapper(context.packageManager).mapAll(it)
        }
        val mustDelete = inDb.filter { it.fileUri !in onDisk.map { it.fileUri } }
        val mustLoad = inDb.filter { !it.loaded && it !in mustDelete }.mapNotNull { apk ->
            PackageArchiveUtils.getApkFileFromDocument(context, apk.fileUri, apk.fileType)?.let { file ->
                val apk = PackageArchiveUtils.getPackageInfoFromApkFile(context.packageManager, file)
                    ?.let { packageInfo ->
                        apk.copy(
                            appName = packageInfo.applicationInfo.loadLabel(context.packageManager)
                                .toString(),
                            appPackageName = packageInfo.applicationInfo.packageName,
                            appIcon = packageInfo.applicationInfo.loadIcon(context.packageManager)
                                ?.toBitmap()?.asImageBitmap(),
                            appVersionName = packageInfo.versionName,
                            appVersionCode = Utils.versionCode(packageInfo),
                            appMinSdkVersion = packageInfo.applicationInfo.minSdkVersion,
                            appTargetSdkVersion = packageInfo.applicationInfo.targetSdkVersion,
                            loaded = true
                        )
                    }
                file.delete()
                apk
            }
        }

        val add = (mustAdd + mustLoad).toSet().toList()

        apkDao.update(add, mustDelete)
        updateTrigger.update { state -> !state }
    }

    override suspend fun addApk(apk: PackageArchiveEntity) {
        apkDao.upsertApk(apk)
    }

    override suspend fun removeApk(apk: PackageArchiveEntity) {
        apkDao.deleteApk(apk)
    }

    override suspend fun updateApp(apk: PackageArchiveEntity): Unit = withContext(Dispatchers.IO) {
        apk.let {
            PackageArchiveUtils.getApkFileFromDocument(context, apk.fileUri, apk.fileType)?.let { file ->
                val apk = PackageArchiveUtils.getPackageInfoFromApkFile(context.packageManager, file)
                    ?.let { packageInfo ->
                        apk.copy(
                            appName = packageInfo.applicationInfo.loadLabel(context.packageManager)
                                .toString(),
                            appPackageName = packageInfo.applicationInfo.packageName,
                            appIcon = packageInfo.applicationInfo.loadIcon(context.packageManager)
                                ?.toBitmap()?.asImageBitmap(),
                            appVersionName = packageInfo.versionName,
                            appVersionCode = Utils.versionCode(packageInfo),
                            appMinSdkVersion = packageInfo.applicationInfo.minSdkVersion,
                            appTargetSdkVersion = packageInfo.applicationInfo.targetSdkVersion,
                            loaded = true
                        )
                    }
                file.delete()
                apk
            }
        }?.also {
            apkDao.upsertApk(it)
        }
    }
}