package com.multisegure.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "profiles")
data class BrowserProfile(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val proxyHost: String = "",
    val proxyPort: Int = 0,
    val proxyUsername: String = "",
    val proxyPassword: String = "",
    val proxyType: String = "NONE",
    val userAgent: String = "",
    val isIncognito: Boolean = false,
    val isJavaScriptEnabled: Boolean = true,
    val blockTrackers: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
