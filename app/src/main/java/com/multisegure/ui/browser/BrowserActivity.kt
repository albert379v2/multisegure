package com.multisegure.ui.browser

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.multisegure.data.model.BrowserProfile
import com.multisegure.network.WebViewProxyManager
import kotlinx.coroutines.launch

class BrowserActivity : ComponentActivity() {

    private val viewModel: BrowserViewModel by viewModels()
    private lateinit var proxyManager: WebViewProxyManager
    private lateinit var webView: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        proxyManager = WebViewProxyManager(this)
        
        val profileId = intent.getIntExtra(EXTRA_PROFILE_ID, -1)
        if (profileId == -1) {
            finish()
            return
        }

        setContent {
            BrowserScreen(
                profileId = profileId,
                onWebViewCreated = { wv ->
                    webView = wv
                    configureWebView(profileId)
                },
                onNavigate = { url ->
                    webView.loadUrl(url)
                },
                onGoBack = {
                    if (webView.canGoBack()) webView.goBack()
                },
                onGoForward = {
                    if (webView.canGoForward()) webView.goForward()
                },
                onReload = {
                    webView.reload()
                },
                onClose = {
                    finish()
                }
            )
        }
    }

    private fun configureWebView(profileId: Int) {
        val profile = viewModel.getProfileSync(profileId) ?: return
        
        // ✅ Aplicar proxy del perfil ANTES de cargar URL
        applyProxyForProfile(profile)
        
        // ✅ Configurar WebView para reCAPTCHA y sitios modernos
        webView.configureForModernWeb(profile)
        
        // ✅ Cargar URL inicial
        webView.loadUrl(profile.startUrl)
    }

    private fun applyProxyForProfile(profile: BrowserProfile) {
        if (!profile.hasProxy) {
            lifecycleScope.launch {
                proxyManager.clearProxy()
            }
            return
        }

        if (!proxyManager.isProxySupported()) {
            // TODO: mostrar mensaje al usuario
            return
        }

        lifecycleScope.launch {
            val result = proxyManager.applyProxy(
                host = profile.proxyHost,
                port = profile.proxyPort,
                type = when (profile.proxyType) {
                    BrowserProfile.ProxyType.HTTP -> WebViewProxyManager.ProxyType.HTTP
                    BrowserProfile.ProxyType.HTTPS -> WebViewProxyManager.ProxyType.HTTPS
                    BrowserProfile.ProxyType.SOCKS4 -> WebViewProxyManager.ProxyType.SOCKS4
                    BrowserProfile.ProxyType.SOCKS5 -> WebViewProxyManager.ProxyType.SOCKS5
                },
                username = profile.proxyUsername,
                password = profile.proxyPassword
            )

            result.onFailure { e ->
                // TODO: mostrar error al usuario
            }
        }
    }

    override fun onDestroy() {
        // ❌ QUITAR: Process.killProcess(Process.myPid())
        // El proxy se limpia automáticamente al cerrar la app
        super.onDestroy()
    }

    companion object {
        const val EXTRA_PROFILE_ID = "profile_id"

        fun createIntent(context: Context, profileId: Int): Intent {
            return Intent(context, BrowserActivity::class.java).apply {
                putExtra(EXTRA_PROFILE_ID, profileId)
            }
        }
    }
}

// ✅ Extensión para configurar WebView
fun WebView.configureForModernWeb(profile: BrowserProfile) {
    settings.apply {
        javaScriptEnabled = profile.javaScriptEnabled
        domStorageEnabled = true
        databaseEnabled = true
        loadsImagesAutomatically = true
        mediaPlaybackRequiresUserGesture = false
        supportMultipleWindows = true
        javaScriptCanOpenWindowsAutomatically = true
        useWideViewPort = true
        loadWithOverviewMode = true
        cacheMode = if (profile.isIncognito) {
            WebSettings.LOAD_NO_CACHE
        } else {
            WebSettings.LOAD_DEFAULT
        }
        geolocationEnabled = true
        supportZoom = true
        builtInZoomControls = false
        displayZoomControls = false
        textZoom = 100
        saveFormData = !profile.isIncognito

        // ✅ CLAVE para reCAPTCHA: permitir contenido mixto
        mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW

        // ✅ CLAVE para reCAPTCHA: User-Agent sin "; wv"
        val originalUA = WebSettings.getDefaultUserAgent(context)
        userAgentString = if (profile.userAgent.isNotBlank()) {
            profile.userAgent
        } else {
            originalUA.replace("; wv", "")
        }
    }

    // ✅ Cookies: aceptar third-party (necesario para reCAPTCHA)
    CookieManager.getInstance().apply {
        setAcceptCookie(true)
        setAcceptThirdPartyCookies(this@configureForModernWeb, true)
    }

    // ✅ WebViewClient SIMPLE — sin shouldInterceptRequest
    webViewClient = object : WebViewClient() {
        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            // ViewModel puede observar esto
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            // ViewModel puede observar esto
        }

        override fun shouldOverrideUrlLoading(
            view: WebView?,
            request: WebResourceRequest?
        ): Boolean {
            return false // Dejar que WebView maneje todo
        }
    }

    // WebChromeClient para diálogos JS, permisos, etc.
    webChromeClient = object : WebChromeClient() {
        // Implementar según necesites: onJsAlert, onPermissionRequest, etc.
    }

    // Renderizado por hardware
    setLayerType(View.LAYER_TYPE_HARDWARE, null)
}
