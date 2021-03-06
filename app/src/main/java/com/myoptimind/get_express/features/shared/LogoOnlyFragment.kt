package com.myoptimind.get_express.features.shared

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.myoptimind.get_express.BaseFragment

abstract class LogoOnlyFragment: BaseFragment() {

    override fun onResume() {
        parentActivity.showLogoOnly()
        super.onResume()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
//        hideLoading()
        return super.onCreateView(inflater, container, savedInstanceState)
    }

}