package com.multisegure.network

import android.content.Context
import androidx.webkit.ProxyConfig
import androidx.webkit.ProxyController
import androidx.webkit.WebViewFeature
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WebViewProxyManager(private val context: Context) {

    private val mainExecutor = context.mainExecutor

    suspend fun applyProxy(
        host: String,
        port: String,
        type: ProxyType,
        username: String = "",
        password: String = ""
    ): Result<Unit> = withContext(Dispatchers.Main) {
        if (!WebViewFeature.isFeatureSupported(WebViewFeature.PROXY_OVERRIDE)) {
            return@withContext Result.failure(
                IllegalStateException("ProxyController no soportado en este WebView")
            )
        }

        val proxyRule = buildProxyRule(host, port, type, username, password)

        val config = ProxyConfig.Builder()
            .addProxyRule(proxyRule)
            .addDirect()
            .build()

        return@withContext try {
            ProxyController.getInstance().setProxyOverride(config, mainExecutor) {
                // Proxy aplicado exitosamente
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun clearProxy(): Result<Unit> = withContext(Dispatchers.Main) {
        if (!WebViewFeature.isFeatureSupported(WebViewFeature.PROXY_OVERRIDE)) {
            return@withContext Result.failure(
                IllegalStateException("ProxyController no soportado")
            )
        }

        return@withContext try {
            ProxyController.getInstance().clearProxyOverride(mainExecutor) {
                // Proxy limpiado
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun isProxySupported(): Boolean {
        return WebViewFeature.isFeatureSupported(WebViewFeature.PROXY_OVERRIDE)
    }

    private fun buildProxyRule(
        host: String,
        port: String,
        type: ProxyType,
        username: String,
        password: String
    ): String {
        val auth = if (username.isNotBlank() && password.isNotBlank()) {
            "$username:$password@"
        } else ""

        return when (type) {
            ProxyType.HTTP -> "http://$auth$host:$port"
            ProxyType.HTTPS -> "https://$auth$host:$port"
            ProxyType.SOCKS4 -> "socks4://$auth$host:$port"
            ProxyType.SOCKS5 -> "socks5://$auth$host:$port"
        }
    }

    enum class ProxyType {
        HTTP, HTTPS, SOCKS4, SOCKS5
    }
}
