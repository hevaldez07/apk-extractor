package domilopment.apkextractor.installApk

import android.content.pm.PackageInstaller
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import domilopment.apkextractor.R
import domilopment.apkextractor.ui.ProgressDialogFragment
import domilopment.apkextractor.ui.fragments.ApkListFragment
import domilopment.apkextractor.ui.viewModels.MainViewModel

class PackageInstallerSessionCallback(
    private val apkListFragment: ApkListFragment,
    private val model: MainViewModel
) : PackageInstaller.SessionCallback() {
    private val packageInstaller =
        apkListFragment.requireContext().applicationContext.packageManager.packageInstaller
    private var packageName: String? = null
    var initialSessionId: Int = -1

    override fun onCreated(sessionId: Int) {
        if (sessionId != initialSessionId) return

        val progressDialog =
            ProgressDialogFragment.newInstance(R.string.progress_dialog_title_install)
        progressDialog.show(apkListFragment.parentFragmentManager, "ProgressDialogFragment")
    }

    override fun onBadgingChanged(sessionId: Int) {
        // Not used
    }

    override fun onActiveChanged(sessionId: Int, active: Boolean) {
        // Not used
    }

    override fun onProgressChanged(sessionId: Int, progress: Float) {
        if (sessionId != initialSessionId) return

        packageName = packageInstaller.getSessionInfo(sessionId)?.appPackageName
        //model.updateInstallApkStatus(progress, packageName)
    }

    override fun onFinished(sessionId: Int, success: Boolean) {
        if (sessionId != initialSessionId) return

        packageInstaller.unregisterSessionCallback(this)
        model.resetProgress()
        MaterialAlertDialogBuilder(apkListFragment.requireContext()).apply {
            if (success) {
                setMessage(apkListFragment.getString(R.string.installation_result_dialog_success_message, packageName))
                setTitle(R.string.installation_result_dialog_success_title)
                model.updateApps()
            } else {
                setMessage(apkListFragment.getString(R.string.installation_result_dialog_failed_message, packageName))
                setTitle(R.string.installation_result_dialog_failed_title)
            }
            setPositiveButton(R.string.installation_result_dialog_ok) { alert, _ ->
                alert.dismiss()
            }
        }.show()
    }
}