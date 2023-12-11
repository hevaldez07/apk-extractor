package domilopment.apkextractor.ui.composables

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.preference.PreferenceManager
import domilopment.apkextractor.R
import domilopment.apkextractor.utils.settings.ApkSortOptions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApkSortMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState,
    sort: (ApkSortOptions) -> Unit
) {
    val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(LocalContext.current)
    if (expanded) ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        sheetState = sheetState,
        windowInsets = WindowInsets(bottom = 24.dp)
    ) {
        DropdownMenuItem(text = { Text(text = stringResource(id = R.string.menu_sort_apk_file_name_asc)) },
            onClick = {
                sharedPreferences.edit()
                    .putString("apk_sort", ApkSortOptions.SORT_BY_FILE_NAME_ASC.name).apply()
                sort(ApkSortOptions.SORT_BY_FILE_NAME_ASC)
            })
        DropdownMenuItem(text = { Text(text = stringResource(id = R.string.menu_sort_apk_file_name_desc)) },
            onClick = {
                sharedPreferences.edit()
                    .putString("apk_sort", ApkSortOptions.SORT_BY_FILE_NAME_DESC.name).apply()
                sort(ApkSortOptions.SORT_BY_FILE_NAME_DESC)
            })
        DropdownMenuItem(text = { Text(text = stringResource(id = R.string.menu_sort_apk_file_mod_date_desc)) },
            onClick = {
                sharedPreferences.edit()
                    .putString("apk_sort", ApkSortOptions.SORT_BY_LAST_MODIFIED_DESC.name).apply()
                sort(ApkSortOptions.SORT_BY_LAST_MODIFIED_DESC)
            })
        DropdownMenuItem(text = { Text(text = stringResource(id = R.string.menu_sort_apk_file_mod_date_asc)) },
            onClick = {
                sharedPreferences.edit()
                    .putString("apk_sort", ApkSortOptions.SORT_BY_LAST_MODIFIED_ASC.name).apply()
                sort(ApkSortOptions.SORT_BY_LAST_MODIFIED_ASC)
            })
        DropdownMenuItem(text = { Text(text = stringResource(id = R.string.menu_sort_apk_file_size_desc)) },
            onClick = {
                sharedPreferences.edit()
                    .putString("apk_sort", ApkSortOptions.SORT_BY_FILE_SIZE_DESC.name).apply()
                sort(ApkSortOptions.SORT_BY_FILE_SIZE_DESC)
            })
        DropdownMenuItem(text = { Text(text = stringResource(id = R.string.menu_sort_apk_file_size_asc)) },
            onClick = {
                sharedPreferences.edit()
                    .putString("apk_sort", ApkSortOptions.SORT_BY_FILE_SIZE_ASC.name).apply()
                sort(ApkSortOptions.SORT_BY_FILE_SIZE_ASC)
            })
    }
}