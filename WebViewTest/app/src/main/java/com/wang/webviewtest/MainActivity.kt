package com.wang.webviewtest

import android.graphics.Bitmap
import android.net.http.SslError
import android.os.Bundle
import android.webkit.CookieManager
import android.webkit.SslErrorHandler
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.Date

class MainActivity : AppCompatActivity() {
    var text = ""
    lateinit var textView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        textView = findViewById<TextView>(R.id.text_view)
        val webView = findViewById<WebView>(R.id.web_view)
        val scrollView = findViewById<ScrollView>(R.id.scroll_view)
        findViewById<Button>(R.id.button_google).setOnClickListener {
            webView.stopLoading()
            webView.clearView()
            text = ""
            textView.text = text
            webView.settings.javaScriptEnabled = true
            CookieManager.getInstance().removeAllCookies(null)
            CookieManager.getInstance().flush()
            webView.loadUrl("https://www.google.com")
        }
        findViewById<Button>(R.id.button_dropbox_home).setOnClickListener {
            webView.stopLoading()
            text = ""
            textView.text = text
            webView.settings.javaScriptEnabled = true
            CookieManager.getInstance().removeAllCookies(null)
            CookieManager.getInstance().flush()
            webView.loadUrl("https://www.dropbox.com")
        }
        findViewById<Button>(R.id.button_dropbox_sso).setOnClickListener {
            webView.stopLoading()
            text = ""
            textView.text = text
            webView.settings.javaScriptEnabled = true
            CookieManager.getInstance().removeAllCookies(null)
            CookieManager.getInstance().flush()
            webView.loadUrl("https://www.dropbox.com/oauth2/authorize?client_id=qfddshz7knsjq35&response_type=code&token_access_type=offline&redirect_uri=https://www.optoma.com/smartplusforbusiness/filemanager/&state=SQIsWRKvW0AAAAAAAAACddOLMPk73JWoZ45qZQyrzX7emuZCdFoV7GS8Ky6mvLyb")
        }

        findViewById<Button>(R.id.button_stop_loading).setOnClickListener {
            webView.stopLoading()
        }

        CookieManager.getInstance().removeAllCookies(null)
        CookieManager.getInstance().flush()

        webView.settings.javaScriptEnabled = true
        webView.settings.userAgentString = "Mozilla/5.0 (Linux; Android 10) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/83.0.4103.120 Mobile Safari/537.36"
        webView.webChromeClient = WebChromeClient()

        webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                updateMessage("onPageStarted $url")
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                updateMessage("onPageFinished $url")
            }

            override fun onLoadResource(view: WebView?, url: String?) {
                super.onLoadResource(view, url)
                updateMessage("onLoadResource $url")
            }

            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                super.onReceivedError(view, request, error)
                updateMessage("onReceivedError ${error?.errorCode}")
            }

            override fun onReceivedHttpError(
                view: WebView?,
                request: WebResourceRequest?,
                errorResponse: WebResourceResponse?
            ) {
                super.onReceivedHttpError(view, request, errorResponse)
                updateMessage("onReceivedHttpError ${errorResponse?.statusCode}")
            }

            override fun onReceivedSslError(
                view: WebView?,
                handler: SslErrorHandler?,
                error: SslError?
            ) {
                super.onReceivedSslError(view, handler, error)
                updateMessage("onReceivedSslError ${error?.toString()}")
            }
        }

        webView.loadUrl("https://www.google.com")
//        webView.loadUrl("https://www.dropbox.com")
//        webView.loadUrl("https://www.dropbox.com/oauth2/authorize?client_id=qfddshz7knsjq35&response_type=code&token_access_type=offline&redirect_uri=https://www.optoma.com/smartplusforbusiness/filemanager/&state=SQIsWRKvW0AAAAAAAAACddOLMPk73JWoZ45qZQyrzX7emuZCdFoV7GS8Ky6mvLyb")
    }

    fun updateMessage(msg: String) {
        text = "${getTime()}: $msg\n" + text
        textView.text = text
    }

    fun getTime(): String {
        val sdf = SimpleDateFormat("YYYY/MM/dd HH:mm:ss:SSS")
        return sdf.format(Date())
    }
}