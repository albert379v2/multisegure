package com.multisegure.ui.browser

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Process
import android.webkit.*
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.multisegure.network.ProxyManager
import com.multisegure.ui.theme.MultiSegureTheme
import com.multisegure.viewmodel.BrowserViewModel

class BrowserActivity : ComponentActivity() {

    private val viewModel: BrowserViewModel by viewModels()

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val profileId = intent.getIntExtra("profile_id", 0)
        val profileName = intent.getStringExtra("profile_name") ?: "Navegador"
        val proxyHost = intent.getStringExtra("proxy_host") ?: ""
        val proxyPort = intent.getIntExtra("proxy_port", 0)
        val proxyUsername = intent.getStringExtra("proxy_username") ?: ""
        val proxyPassword = intent.getStringExtra("proxy_password") ?: ""
        val proxyType = intent.getStringExtra("proxy_type") ?: "NONE"
        val userAgent = intent.getStringExtra("user_agent") ?: ""
        val incognito = intent.getBooleanExtra("incognito", false)
        val jsEnabled = intent.getBooleanExtra("javascript", true)
        val blockTrackers = intent.getBooleanExtra("block_trackers", false)

        setContent {
            MultiSegureTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    BrowserScreen(
                        profileName = profileName,
                        profileId = profileId,
                        proxyHost = proxyHost,
                        proxyPort = proxyPort,
                        proxyUsername = proxyUsername,
                        proxyPassword = proxyPassword,
                        proxyType = proxyType,
                        userAgent = userAgent,
                        incognito = incognito,
                        jsEnabled = jsEnabled,
                        blockTrackers = blockTrackers,
                        viewModel = viewModel
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Process.killProcess(Process.myPid())
    }
}
