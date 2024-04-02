package domilopment.apkextractor.ui.dialogs

import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AssistChip
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetState
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import domilopment.apkextractor.R
import domilopment.apkextractor.data.apkList.PackageArchiveModel
import domilopment.apkextractor.ui.components.ExpandableText
import domilopment.apkextractor.ui.components.SnackbarHostModalBottomSheet
import domilopment.apkextractor.utils.FileUtil
import domilopment.apkextractor.utils.Utils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApkOptionBottomSheet(
    apk: PackageArchiveModel,
    onDismissRequest: () -> Unit,
    sheetState: SheetState,
    onRefresh: () -> Unit,
    onActionShare: () -> Unit,
    onActionInstall: () -> Unit,
    onActionDelete: () -> Unit,
    onActionUninstall: () -> Unit,
    deletedDocumentFound: (PackageArchiveModel) -> Unit
) {
    val context = LocalContext.current
    if (!FileUtil.doesDocumentExist(context, apk.fileUri)) {
        deletedDocumentFound(apk)
        return
    }

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    LifecycleEventEffect(event = Lifecycle.Event.ON_START) {
        if (!FileUtil.doesDocumentExist(context, apk.fileUri)) {
            deletedDocumentFound(apk)
            onDismissRequest()
        }
    }

    SnackbarHostModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        snackbarHostState = snackbarHostState
    ) {
        ApkSheetHeader(
            apkName = apk.appName,
            fileName = apk.fileName,
            packageName = apk.appPackageName,
            appIcon = apk.appIcon,
            isRefreshing = apk.isPackageArchiveInfoLoading,
            onRefresh = onRefresh
        )
        HorizontalDivider(modifier = Modifier.padding(4.dp))
        ApkSheetInfo(
            modifier = Modifier.weight(1f, fill = false),
            sourceDirectory = apk.fileUri,
            apkFileName = apk.fileName,
            apkSize = apk.fileSize,
            apkCreated = apk.fileLastModified,
            minSdk = apk.appMinSdkVersion,
            targetSdk = apk.appTargetSdkVersion,
            versionName = apk.appVersionName,
            versionNumber = apk.appVersionCode,
        )
        HorizontalDivider(modifier = Modifier.padding(4.dp))
        ApkSheetActions(
            packageName = apk.appPackageName,
            onActionShare = onActionShare,
            onActionInstall = onActionInstall,
            onActionDelete = onActionDelete,
            onActionUninstall = onActionUninstall
        )
    }
}

@Composable
fun ApkSheetActions(
    packageName: String?,
    onActionShare: () -> Unit,
    onActionInstall: () -> Unit,
    onActionDelete: () -> Unit,
    onActionUninstall: () -> Unit
) {
    val scrollState = rememberScrollState()
    Row(
        modifier = Modifier
            .horizontalScroll(scrollState)
            .padding(8.dp, 0.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        AssistChip(onClick = onActionShare,
            label = { Text(text = stringResource(id = R.string.alert_apk_selected_share)) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Share, contentDescription = null
                )
            })
        AssistChip(onClick = onActionInstall,
            label = { Text(text = stringResource(id = R.string.alert_apk_selected_install)) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Android, contentDescription = null
                )
            })
        AssistChip(onClick = onActionDelete,
            label = { Text(text = stringResource(id = R.string.alert_apk_selected_delete)) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Delete, contentDescription = null
                )
            })
        AssistChip(onClick = onActionUninstall,
            label = { Text(text = stringResource(id = R.string.apk_action_uninstall_app)) },
            enabled = packageName != null && Utils.isPackageInstalled(
                LocalContext.current.packageManager, packageName
            ),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Clear, contentDescription = null
                )
            })
    }
}

@Composable
fun ApkSheetInfo(
    modifier: Modifier = Modifier,
    sourceDirectory: Uri,
    apkFileName: String,
    apkSize: Long,
    apkCreated: Long,
    minSdk: Int?,
    targetSdk: Int?,
    versionName: String?,
    versionNumber: Long?
) {
    val scrollState = rememberScrollState()
    Column(
        modifier = modifier
            .verticalScroll(scrollState)
            .padding(horizontal = 12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        ExpandableText(
            text = stringResource(
                id = R.string.apk_bottom_sheet_source_uri, sourceDirectory
            ).let {
                AnnotatedString(
                    it, listOf(
                        AnnotatedString.Range(
                            SpanStyle(
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            ), 0, it.indexOf(':') + 1
                        )
                    )
                )
            }, maxLines = 1, overflow = TextOverflow.Ellipsis
        )
        InfoText(
            text = stringResource(id = R.string.apk_bottom_sheet_file_name, apkFileName),
        )
        InfoText(
            text = stringResource(
                id = R.string.info_bottom_sheet_apk_size, apkSize / (1000F * 1000F)
            )
        )
        InfoText(
            text = stringResource(
                id = R.string.apk_bottom_sheet_last_modified, Utils.getAsFormattedDate(apkCreated)
            )
        )
        InfoText(
            text = stringResource(
                id = R.string.info_bottom_sheet_min_sdk,
                minSdk ?: -1,
                Utils.androidApiLevel[minSdk] ?: ""
            )
        )
        InfoText(
            text = stringResource(
                id = R.string.info_bottom_sheet_target_sdk,
                targetSdk ?: -1,
                Utils.androidApiLevel[targetSdk] ?: ""
            )
        )
        InfoText(
            text = stringResource(
                id = R.string.info_bottom_sheet_version_name, versionName.toString()
            )
        )
        InfoText(
            text = stringResource(
                id = R.string.info_bottom_sheet_version_number, versionNumber ?: 0
            )
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ApkSheetHeader(
    apkName: CharSequence?,
    fileName: String,
    packageName: String?,
    appIcon: Drawable?,
    isRefreshing: Boolean,
    onRefresh: () -> Unit
) {
    ListItem(headlineContent = {
        Text(
            text = apkName?.toString() ?: fileName,
            modifier = Modifier.fillMaxWidth(),
            fontSize = 16.sp,
            maxLines = 1,
            textAlign = TextAlign.Center,
            overflow = TextOverflow.Ellipsis
        )
    }, modifier = Modifier.height(72.dp), supportingContent = {
        if (packageName != null) Text(
            text = packageName,
            modifier = Modifier
                .fillMaxWidth()
                .basicMarquee(),
            fontSize = 12.sp,
            maxLines = 1,
            textAlign = TextAlign.Center,
            overflow = TextOverflow.Ellipsis
        )
    }, leadingContent = {
        val context = LocalContext.current
        Image(
            painter = rememberDrawablePainter(
                drawable = appIcon ?: ResourcesCompat.getDrawable(
                    context.resources, android.R.drawable.sym_def_app_icon, context.theme
                )
            ),
            contentDescription = stringResource(id = R.string.list_item_Image_description),
            modifier = Modifier.width(64.dp)
        )
    }, trailingContent = {
        Box(modifier = Modifier.width(64.dp), contentAlignment = Alignment.Center) {
            if (isRefreshing) CircularProgressIndicator(
                color = MaterialTheme.colorScheme.secondary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            ) else FilledTonalIconButton(onClick = onRefresh) {
                Icon(imageVector = Icons.Default.Refresh, contentDescription = null)
            }
        }
    })
}

@Composable
private fun InfoText(text: String) {
    Text(
        text = AnnotatedString(
            text, listOf(
                AnnotatedString.Range(
                    SpanStyle(
                        color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold
                    ), 0, text.indexOf(':') + 1
                )
            )
        ), maxLines = 1, overflow = TextOverflow.Ellipsis
    )
}
