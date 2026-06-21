package com.multisegure.ui.browser

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.view.ViewGroup
import android.webkit.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.multisegure.network.ProxyManager
import com.multisegure.viewmodel.BrowserViewModel

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun BrowserScreen(
    profileName: String,
    profileId: Int,
    proxyHost: String,
    proxyPort: Int,
    proxyUsername: String,
    proxyPassword: String,
    proxyType: String,
    userAgent: String,
    incognito: Boolean,
    jsEnabled: Boolean,
    blockTrackers: Boolean,
    viewModel: BrowserViewModel
) {
    val context = LocalContext.current
    var urlText by remember { mutableStateOf("") }
    var showMenu by remember { mutableStateOf(false) }

    val canGoBack by viewModel.canGoBack.collectAsState()
    val canGoForward by viewModel.canGoForward.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val currentUrl by viewModel.url.collectAsState()

    LaunchedEffect(currentUrl) {
        if (currentUrl.isNotBlank() && urlText != currentUrl) {
            urlText = currentUrl
        }
    }

    val proxyManager = remember {
        val profile = com.multisegure.data.model.BrowserProfile(
            id = profileId,
            name = profileName,
            proxyHost = proxyHost,
            proxyPort = proxyPort,
            proxyUsername = proxyUsername,
            proxyPassword = proxyPassword,
            proxyType = proxyType,
            userAgent = userAgent,
            isIncognito = incognito,
            isJavaScriptEnabled = jsEnabled,
            blockTrackers = blockTrackers
        )
        ProxyManager(profile)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(profileName, maxLines = 1) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                navigationIcon = {
                    IconButton(onClick = {
                        (context as? android.app.Activity)?.finish()
                    }) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar")
                    }
                },
                actions = {
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Menú")
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Recargar") },
                                onClick = {
                                    viewModel.reload()
                                    showMenu = false
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Refresh, null)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Página de inicio") },
                                onClick = {
                                    viewModel.navigateTo("https://www.google.com")
                                    showMenu = false
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Home, null)
                                }
                            )
                            if (incognito) {
                                DropdownMenuItem(
                                    text = { Text("Modo Incógnito activo") },
                                    onClick = { showMenu = false },
                                    leadingIcon = {
                                        Icon(Icons.Default.VisibilityOff, null)
                                    }
                                )
                            }
                        }
                    }
                }
            )
        },
        bottomBar = {
            BottomAppBar(
                tonalElevation = 3.dp
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { viewModel.goBack() },
                        enabled = canGoBack
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                    IconButton(
                        onClick = { viewModel.goForward() },
                        enabled = canGoForward
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Adelante")
                    }
                    IconButton(onClick = { viewModel.reload() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Recargar")
                    }
                    IconButton(onClick = {
                        viewModel.navigateTo("https://www.google.com")
                    }) {
                        Icon(Icons.Default.Home, contentDescription = "Inicio")
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            OutlinedTextField(
                value = urlText,
                onValueChange = { urlText = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                placeholder = { Text("Buscar o escribir URL") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                keyboardActions = KeyboardActions(
                    onGo = {
                        val url = if (urlText.startsWith("http://") || urlText.startsWith("https://")) {
                            urlText
                        } else if (urlText.contains(".") && !urlText.contains(" ")) {
                            "https://$urlText"
                        } else {
                            "https://www.google.com/search?q=${urlText.replace(" ", "+")}"
                        }
                        viewModel.navigateTo(url)
                    }
                ),
                trailingIcon = {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        IconButton(onClick = {
                            val url = if (urlText.startsWith("http://") || urlText.startsWith("https://")) {
                                urlText
                            } else if (urlText.contains(".") && !urlText.contains(" ")) {
                                "https://$urlText"
                            } else {
                                "https://www.google.com/search?q=${urlText.replace(" ", "+")}"
                            }
                            viewModel.navigateTo(url)
                        }) {
                            Icon(Icons.Default.Search, contentDescription = "Ir")
                        }
                    }
                }
            )

            AndroidView(
                factory = { ctx ->
                    createWebView(
                        context = ctx,
                        userAgent = userAgent,
                        jsEnabled = jsEnabled,
                        incognito = incognito,
                        blockTrackers = blockTrackers,
                        proxyManager = proxyManager,
                        viewModel = viewModel
                    )
                },
                modifier = Modifier.fillMaxSize(),
                update = { webView ->
                    viewModel.webView = webView
                }
            )
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
private fun createWebView(
    context: Context,
    userAgent: String,
    jsEnabled: Boolean,
    incognito: Boolean,
    blockTrackers: Boolean,
    proxyManager: ProxyManager,
    viewModel: BrowserViewModel
): WebView {
    return WebView(context).apply {
        layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )

        settings.apply {
            javaScriptEnabled = jsEnabled
            domStorageEnabled = true
            databaseEnabled = true
            cacheMode = if (incognito) WebSettings.LOAD_NO_CACHE else WebSettings.LOAD_DEFAULT
            userAgentString = userAgent.ifBlank { defaultUserAgentString }
            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            useWideViewPort = true
            loadWithOverviewMode = true
            setSupportZoom(true)
            builtInZoomControls = true
            displayZoomControls = false
            allowFileAccess = false
            allowContentAccess = false
        }

        if (incognito && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            settings.safeBrowsingEnabled = false
        }

        webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                request?.url?.toString()?.let { url ->
                    if (url.startsWith("http://") || url.startsWith("https://")) {
                        view?.loadUrl(url)
                        return true
                    }
                }
                return false
            }

            override fun shouldInterceptRequest(
                view: WebView?,
                request: WebResourceRequest?
            ): WebResourceResponse? {
                if (request == null) return null
                return proxyManager.fetch(request) ?: super.shouldInterceptRequest(view, request)
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                url?.let { viewModel.updateUrl(it) }
                viewModel.updateLoadingState(true)
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                viewModel.updateLoadingState(false)
                view?.let {
                    viewModel.updateNavigationState(it.canGoBack(), it.canGoForward())
                }
            }
        }

        webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                viewModel.updateLoadingState(newProgress < 100)
            }

            override fun onReceivedTitle(view: WebView?, title: String?) {
                title?.let { viewModel.updateTitle(it) }
            }
        }

        if (incognito) {
            CookieManager.getInstance().setAcceptCookie(false)
        } else {
            CookieManager.getInstance().setAcceptCookie(true)
        }

        loadUrl("https://www.google.com")
    }
}
