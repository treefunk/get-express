package com.myoptimind.getexpress.features.CUSTOMER.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.myoptimind.getexpress.R
import com.myoptimind.getexpress.features.shared.LogoOnlyFragment

class HomeFragment: LogoOnlyFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home,container,false)
    }
}