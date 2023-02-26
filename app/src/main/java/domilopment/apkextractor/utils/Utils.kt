package domilopment.apkextractor.utils

import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import domilopment.apkextractor.data.ApplicationModel
import java.util.*

object Utils {
    @Throws(PackageManager.NameNotFoundException::class)
    fun getPackageInfo(packageManager: PackageManager, packageName: String): PackageInfo {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) packageManager.getPackageInfo(
            packageName, PackageManager.PackageInfoFlags.of(0L)
        )
        else packageManager.getPackageInfo(packageName, 0)
    }

    fun versionCode(packageInfo: PackageInfo): Long {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) packageInfo.longVersionCode
        else packageInfo.versionCode.toLong()
    }

    val listOfKnownStores: Map<String, String> = mapOf(
        "com.android.vending" to "https://play.google.com/store/apps/details?id=",
        "com.sec.android.app.samsungapps" to "samsungapps://ProductDetail/",
        "com.amazon.venezia" to "amzn://apps/android?p=",
        "org.fdroid.fdroid" to "fdroid.app://details?id="
    ).withDefault {
        "market://details?id="
    }

    /**
     * Check if Application is Installed
     * @param packageName
     * Package Name of App
     */
    fun isPackageInstalled(packageManager: PackageManager, packageName: String): Boolean {
        return try {
            getPackageInfo(packageManager, packageName)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    /**
     * Checks if app is a System app
     * @param app
     * App to check
     */
    fun isSystemApp(app: ApplicationModel): Boolean {
        return app.appFlags and ApplicationInfo.FLAG_SYSTEM == ApplicationInfo.FLAG_SYSTEM
    }
}