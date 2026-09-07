package com.rss.foodcourtrst

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.webkit.CookieManager
import android.webkit.URLUtil
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceError
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.rss.foodcourtrst.databinding.ActivityMainBinding

/**
 * Single-Activity WebView wrapper around the "Foodcourt RST - Kas Kantin"
 * Google Apps Script web app. The web app itself lives on Google's servers
 * (Google Sheets backend), this shell just gives it a native app icon,
 * splash, full-screen chrome-less window, offline message and download
 * support, and makes Google Sign-In inside the WebView actually work.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private var filePathCallback: ValueCallback<Array<Uri>>? = null

    companion object {
        // Change this if you redeploy the Apps Script web app and get a new URL.
        const val APP_URL =
            "https://script.google.com/macros/s/AKfycbxJL3HyDAqEDZSIvtrh_G3Tc2_Gcib5aymBLuoO-Lg7zguwFvShToDYxtWeGVWygwnXwA/exec"
        private const val TAG = "FoodcourtRST"
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Android 15+ (API 35+) always draws edge-to-edge for apps targeting
        // that SDK, so we pad the root view ourselves instead of relying on
        // android:statusBarColor, which is otherwise ignored.
        WindowCompat.setDecorFitsSystemWindows(window, false)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        setupWebView()
        setupSwipeRefresh()
        setupBackNavigation()

        if (savedInstanceState == null) {
            loadApp()
        } else {
            binding.webView.restoreState(savedInstanceState)
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        val webView = binding.webView
        val settings: WebSettings = webView.settings

        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.databaseEnabled = true
        settings.setSupportZoom(false)
        settings.builtInZoomControls = false
        // IMPORTANT: loadWithOverviewMode=true + useWideViewPort=true together
        // make the WebView render the page as if it were a desktop layout and
        // then zoom it out to fit — which is exactly what makes an otherwise
        // mobile-optimized page look like "a shrunk website" instead of a
        // native app. The web app already ships a proper mobile viewport meta
        // tag, so we let it drive sizing directly instead of overriding it.
        settings.loadWithOverviewMode = false
        settings.useWideViewPort = true
        settings.cacheMode = WebSettings.LOAD_DEFAULT
        settings.mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW

        // Removes the grey/white overscroll "glow" flash at the top/bottom
        // when scrolling past the edge — a small but very telling "this is
        // just a browser" cue that native apps don't have.
        webView.overScrollMode = View.OVER_SCROLL_NEVER

        // Google actively blocks sign-in pages when it detects the default
        // Android WebView user-agent (it contains "; wv"). Presenting a
        // regular mobile Chrome user agent lets Google Sign-In work inside
        // the WebView instead of showing "This browser may not be secure".
        val defaultUa = settings.userAgentString
        settings.userAgentString = defaultUa.replace("; wv", "")

        // Needed so the Google auth session cookies set on accounts.google.com
        // are visible when the WebView navigates back to script.google.com.
        val cookieManager = CookieManager.getInstance()
        cookieManager.setAcceptCookie(true)
        cookieManager.setAcceptThirdPartyCookies(webView, true)

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView,
                request: WebResourceRequest
            ): Boolean {
                // Keep everything related to using/authenticating the app
                // inside our WebView (Apps Script + Google account domains).
                val host = request.url.host ?: ""
                val staysInApp = host.endsWith("script.google.com") ||
                    host.endsWith("script.googleusercontent.com") ||
                    host.endsWith("accounts.google.com") ||
                    host.endsWith("google.com")
                return if (staysInApp) {
                    false
                } else {
                    // Anything else (e.g. a link to an outside site) opens
                    // in the system browser instead of hijacking our shell.
                    try {
                        startActivity(Intent(Intent.ACTION_VIEW, request.url))
                    } catch (e: Exception) {
                        Log.w(TAG, "No app to open ${request.url}", e)
                    }
                    true
                }
            }

            override fun onPageFinished(view: WebView, url: String) {
                super.onPageFinished(view, url)
                binding.swipeRefresh.isRefreshing = false
                binding.offlineView.visibility = View.GONE
                binding.webView.visibility = View.VISIBLE
            }

            override fun onReceivedError(
                view: WebView,
                request: WebResourceRequest,
                error: WebResourceError
            ) {
                super.onReceivedError(view, request, error)
                if (request.isForMainFrame) {
                    showOffline()
                }
            }
        }

        webView.webChromeClient = object : WebChromeClient() {
            // Lets target="_blank" / window.open() popups (used by some
            // Google sign-in flows) render inside the same WebView instead
            // of silently doing nothing.
            override fun onCreateWindow(
                view: WebView,
                isDialog: Boolean,
                isUserGesture: Boolean,
                resultMsg: android.os.Message
            ): Boolean {
                val newWebView = WebView(this@MainActivity)
                view.addView(newWebView)
                val transport = resultMsg.obj as WebView.WebViewTransport
                transport.webView = newWebView
                resultMsg.sendToTarget()
                newWebView.webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(
                        v: WebView,
                        request: WebResourceRequest
                    ): Boolean {
                        binding.webView.loadUrl(request.url.toString())
                        view.removeView(newWebView)
                        newWebView.destroy()
                        return true
                    }
                }
                return true
            }
        }

        webView.setDownloadListener { url, _, contentDisposition, mimeType, _ ->
            try {
                val request = DownloadManager.Request(Uri.parse(url))
                request.setMimeType(mimeType)
                val cookie = CookieManager.getInstance().getCookie(url)
                request.addRequestHeader("cookie", cookie)
                request.setNotificationVisibility(
                    DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED
                )
                val fileName = URLUtil.guessFileName(url, contentDisposition, mimeType)
                request.setDestinationInExternalPublicDir(
                    Environment.DIRECTORY_DOWNLOADS,
                    fileName
                )
                val dm = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                dm.enqueue(request)
                Toast.makeText(this, getString(R.string.downloading, fileName), Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Log.e(TAG, "Download failed", e)
                Toast.makeText(this, R.string.download_failed, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            if (isOnline()) {
                binding.webView.reload()
            } else {
                binding.swipeRefresh.isRefreshing = false
                showOffline()
            }
        }
        binding.retryButton.setOnClickListener { loadApp() }
    }

    private fun setupBackNavigation() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (binding.webView.canGoBack()) {
                    binding.webView.goBack()
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })
    }

    private fun loadApp() {
        if (isOnline()) {
            binding.offlineView.visibility = View.GONE
            binding.webView.visibility = View.VISIBLE
            binding.webView.loadUrl(APP_URL)
        } else {
            showOffline()
        }
    }

    private fun showOffline() {
        binding.swipeRefresh.isRefreshing = false
        binding.webView.visibility = View.GONE
        binding.offlineView.visibility = View.VISIBLE
    }

    private fun isOnline(): Boolean {
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return false
        val capabilities = cm.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.webView.saveState(outState)
    }

    override fun onDestroy() {
        binding.webView.destroy()
        super.onDestroy()
    }
}
