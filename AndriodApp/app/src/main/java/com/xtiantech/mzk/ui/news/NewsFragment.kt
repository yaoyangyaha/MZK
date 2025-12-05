package com.xtiantech.mzk.ui.news

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment
import com.xtiantech.mzk.databinding.FragmentNewsBinding
import com.xtiantech.mzk.utils.Constants

class NewsFragment : Fragment() {
    // ViewBinding（可空+委托，避免内存泄漏）
    private var _binding: FragmentNewsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNewsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 配置WebView
        binding.webView.apply {
            settings.javaScriptEnabled = true // 启用JS（加载网页必需）
            settings.domStorageEnabled = true // 启用DOM存储
            settings.loadWithOverviewMode = true // 适配屏幕
            settings.useWideViewPort = true // 宽视图模式

            // WebView客户端（拦截页面跳转，不打开系统浏览器）
            webViewClient = object : WebViewClient() {
                override fun onPageStarted(view: android.webkit.WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                    super.onPageStarted(view, url, favicon)
                    binding.progressBar.visibility = View.VISIBLE // 显示加载进度
                }

                override fun onPageFinished(view: android.webkit.WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    binding.progressBar.visibility = View.GONE // 隐藏加载进度
                }

                override fun shouldOverrideUrlLoading(
                    view: android.webkit.WebView?,
                    request: WebResourceRequest?
                ): Boolean {
                    view?.loadUrl(request?.url.toString()) // 在WebView内加载新链接
                    return true
                }
            }

            // 加载资讯网页
            loadUrl(Constants.WEBVIEW_URL)
        }
    }

    // 生命周期：销毁View时释放Binding
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}