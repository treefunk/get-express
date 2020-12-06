package com.myoptimind.getexpress.features.edit_profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.myoptimind.getexpress.R
import com.myoptimind.getexpress.features.shared.BaseDialogFragment

class DialogAddAddress: BaseDialogFragment() {
    
    companion object {
        fun newInstance(): DialogAddAddress {
            val args = Bundle()
            
            val fragment = DialogAddAddress ()
            fragment.arguments = args
            return fragment
        }
    }
    
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.dialog_add_address,container,false)
    }
}