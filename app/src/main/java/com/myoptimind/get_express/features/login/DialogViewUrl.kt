package com.myoptimind.get_express.features.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebViewClient
import com.bumptech.glide.Glide
import com.myoptimind.get_express.R
import com.myoptimind.get_express.features.shared.BaseDialogFragment
import kotlinx.android.synthetic.main.dialog_view_url.*
import timber.log.Timber

private const val ARGS_KEY_URL = "args_key_url"
private const val ARGS_IS_IMAGE = "args_is_web_view"

class DialogViewUrl: BaseDialogFragment() {

    companion object {
        fun newInstance(url: String, isImage: Boolean = true): DialogViewUrl {
            val args = Bundle()
            args.putString(ARGS_KEY_URL,url)
            args.putBoolean(ARGS_IS_IMAGE,isImage)
            val fragment = DialogViewUrl()
            fragment.arguments = args
            return fragment
        }
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.dialog_view_url,container,false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val url = arguments?.getString(ARGS_KEY_URL)
        val isImage = arguments?.getBoolean(ARGS_IS_IMAGE) as Boolean
        Timber.v("loading url $url")
        if(isImage){ // if image load url to image view
            wv_webview.visibility = View.GONE
            iv_image.visibility = View.VISIBLE
            Glide.with(this)
                    .load(url)
                    .fitCenter()
                    .into(iv_image)

        }else{ // if not an image load url as web view
            if(url != null && url.isNotBlank()){
                wv_webview.visibility = View.VISIBLE
                iv_image.visibility = View.INVISIBLE
                wv_webview.webViewClient = WebViewClient()
                wv_webview.settings.apply {
                    javaScriptEnabled = true
                }
                wv_webview.loadUrl(url)
            }
        }

        ib_close.setOnClickListener { this.dismiss() }
    }
}