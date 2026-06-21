package com.multisegure.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "profiles")
data class BrowserProfile(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val startUrl: String = "https://www.google.com",
    val proxyHost: String = "",
    val proxyPort: String = "",
    val proxyType: ProxyType = ProxyType.HTTP,
    val proxyUsername: String = "",
    val proxyPassword: String = "",
    val userAgent: String = "",
    val isIncognito: Boolean = false,
    val javaScriptEnabled: Boolean = true,
    val blockTrackers: Boolean = false
) {
    val hasProxy: Boolean
        get() = proxyHost.isNotBlank() && proxyPort.isNotBlank()

    enum class ProxyType {
        HTTP, HTTPS, SOCKS4, SOCKS5
    }
}
