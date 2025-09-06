package com.musclehack.targetedHypertrophyTraining

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment
import com.musclehack.targetedHypertrophyTraining.utilities.BrowserUtils
import com.musclehack.targetedHypertrophyTraining.utilities.KEY_FILE
import com.musclehack.targetedHypertrophyTraining.utilities.KEY_ZOOM_CONTROLS


class SimpleContentFragment : Fragment() {
    private lateinit var webView: WebView

    companion object {
        fun newInstance(file: String, zoomControls: Boolean): SimpleContentFragment {
            val f = SimpleContentFragment()

            val bundle = Bundle()

            bundle.putString(KEY_FILE, file)
            bundle.putBoolean(KEY_ZOOM_CONTROLS, zoomControls)
            f.arguments = bundle

            return f
        }
    }

    private val page: String?
        get() = arguments?.getString(KEY_FILE)

    private val zoomControlsPref: Boolean?
        get() = arguments?.getBoolean(KEY_ZOOM_CONTROLS)


    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val result = inflater.inflate(R.layout.fragment_simple_content, container, false)
        webView = result.findViewById(R.id.webView)
        val pageTemp = page
        if (pageTemp == null) {
            println("Page argument is null. Not loading content.")
            return null
        }
        webView.loadUrl(pageTemp)

        webView.settings.javaScriptEnabled = true
        webView.settings.setSupportZoom(zoomControlsPref!!)
        webView.settings.builtInZoomControls = true
        webView.loadUrl(pageTemp)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            webView.webViewClient = object : WebViewClient() {
                @SuppressLint("NewApi")
                override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest)
                        : Boolean {
                    BrowserUtils.openUrlInChromeCustomTabs(context, request.url.toString())
                    return true
                }
            }
        } else {
            webView.webViewClient = object : WebViewClient() {
                @Suppress("OverridingDeprecatedMember")
                override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                    if (url != null)
                        BrowserUtils.openUrlInChromeCustomTabs(context, url)
                    @Suppress("DEPRECATION")
                    return super.shouldOverrideUrlLoading(view, url)
                }
            }
        }
        return result
    }
}
