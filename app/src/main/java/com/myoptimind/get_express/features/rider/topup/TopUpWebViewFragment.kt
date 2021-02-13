package com.myoptimind.get_express.features.rider.topup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.navigation.fragment.navArgs
import com.myoptimind.get_express.R
import com.myoptimind.get_express.features.shared.TitleOnlyFragment
import kotlinx.android.synthetic.main.fragment_web_view.*

class TopUpWebViewFragment : TitleOnlyFragment() {

    override fun getTitle() = "Top Up"

    private val args by navArgs<TopUpWebViewFragmentArgs>()


    companion object {

        private const val ARGS_URL = "args_url"

        fun newInstance(url: String): TopUpWebViewFragment {
            val args = Bundle()
            args.putString(ARGS_URL, url)
            val fragment = TopUpWebViewFragment()
            fragment.arguments = args
            return fragment
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.fragment_web_view, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

//        val url = requireArguments().getString(ARGS_URL)
        if (args.url.isNotEmpty()) {
            webView.settings.javaScriptEnabled = true
            webView.webViewClient = object: WebViewClient(){
                override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                    webView.loadUrl(url!!)
                    return true
                }
            }
            webView.loadUrl(args.url)
        }

    }
}