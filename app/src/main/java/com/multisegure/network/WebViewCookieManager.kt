package com.multisegure.network

import android.content.Context
import android.webkit.CookieManager
import java.io.File

class WebViewCookieManager(private val context: Context) {

    fun clearProfileCookies(profileId: Int) {
        val cookieManager = CookieManager.getInstance()
        cookieManager.removeAllCookies(null)
        cookieManager.flush()
    }

    fun setupProfileDataDirectory(profileId: Int): File {
        val profileDir = File(context.dataDir, "app_webview_profile_$profileId")
        if (!profileDir.exists()) {
            profileDir.mkdirs()
        }
        return profileDir
    }

    fun clearAllData(profileId: Int) {
        val profileDir = File(context.dataDir, "app_webview_profile_$profileId")
        profileDir.deleteRecursively()
    }
}
