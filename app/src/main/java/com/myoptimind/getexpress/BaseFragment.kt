package com.myoptimind.getexpress

import android.os.Bundle
import androidx.fragment.app.Fragment
import timber.log.Timber

abstract class BaseFragment: Fragment() {
    internal lateinit var parentActivity: MainActivity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        try {
            parentActivity = (activity as MainActivity)
        } catch (exception: TypeCastException) {
            Timber.e("Parent Activity must be of type \"MainActivity\"!!")
        }
    }

/*    fun showLoading(){
        parentActivity.showLoading()
    }

    fun hideLoading(){
        parentActivity.hideLoading()
    }*/

}