package com.multisegure.viewmodel

import android.webkit.WebView
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class BrowserViewModel : ViewModel() {

    private val _url = MutableStateFlow("")
    val url: StateFlow<String> = _url

    private val _canGoBack = MutableStateFlow(false)
    val canGoBack: StateFlow<Boolean> = _canGoBack

    private val _canGoForward = MutableStateFlow(false)
    val canGoForward: StateFlow<Boolean> = _canGoForward

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _title = MutableStateFlow("")
    val title: StateFlow<String> = _title

    var webView: WebView? = null

    fun updateUrl(url: String) {
        _url.value = url
    }

    fun updateNavigationState(canBack: Boolean, canForward: Boolean) {
        _canGoBack.value = canBack
        _canGoForward.value = canForward
    }

    fun updateLoadingState(loading: Boolean) {
        _isLoading.value = loading
    }

    fun updateTitle(title: String) {
        _title.value = title
    }

    fun navigateTo(url: String) {
        _url.value = url
        webView?.loadUrl(url)
    }

    fun goBack() {
        webView?.goBack()
    }

    fun goForward() {
        webView?.goForward()
    }

    fun reload() {
        webView?.reload()
    }

    fun stopLoading() {
        webView?.stopLoading()
    }

    override fun onCleared() {
        webView = null
        super.onCleared()
    }
}
