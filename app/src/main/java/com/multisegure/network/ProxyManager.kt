package com.multisegure.network

import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import com.multisegure.data.model.BrowserProfile
import okhttp3.*
import okhttp3.Credentials
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Proxy
import java.util.concurrent.TimeUnit

class ProxyManager(private val profile: BrowserProfile) {

    private var client: OkHttpClient = buildClient()

    private fun buildClient(): OkHttpClient {
        val builder = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .followRedirects(true)
            .followSslRedirects(true)

        if (profile.proxyType != "NONE" && profile.proxyHost.isNotBlank() && profile.proxyPort > 0) {
            val proxyType = when (profile.proxyType) {
                "SOCKS4", "SOCKS5" -> Proxy.Type.SOCKS
                else -> Proxy.Type.HTTP
            }
            val proxy = Proxy(proxyType, InetSocketAddress(profile.proxyHost, profile.proxyPort))
            builder.proxy(proxy)

            if (profile.proxyUsername.isNotBlank() && profile.proxyPassword.isNotBlank()) {
                builder.proxyAuthenticator { _, response ->
                    val credential = Credentials.basic(profile.proxyUsername, profile.proxyPassword)
                    response.request.newBuilder()
                        .header("Proxy-Authorization", credential)
                        .build()
                }
            }
        }

        return builder.build()
    }

    fun fetch(request: WebResourceRequest): WebResourceResponse? {
        return try {
            val okRequest = Request.Builder()
                .url(request.url.toString())
                .apply {
                    request.requestHeaders?.forEach { (key, value) ->
                        addHeader(key, value)
                    }
                    if (profile.userAgent.isNotBlank()) {
                        removeHeader("User-Agent")
                        addHeader("User-Agent", profile.userAgent)
                    }
                }
                .build()

            val response = client.newCall(okRequest).execute()
            val contentType = response.header("Content-Type") ?: "text/html"
            val mimeType = contentType.split(";").firstOrNull()?.trim() ?: "text/html"
            val encoding = if (contentType.contains("charset=")) {
                contentType.split("charset=").getOrNull(1)?.trim() ?: "UTF-8"
            } else "UTF-8"

            WebResourceResponse(
                mimeType,
                encoding,
                response.code,
                response.message.ifBlank { "OK" },
                response.headers.toMultimap().mapValues { it.value.joinToString(", ") },
                response.body?.byteStream()
            )
        } catch (e: IOException) {
            null
        }
    }
}
