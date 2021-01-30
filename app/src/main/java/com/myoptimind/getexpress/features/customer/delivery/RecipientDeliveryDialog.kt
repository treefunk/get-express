package com.myoptimind.getexpress.features.customer.delivery

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.myoptimind.getexpress.R
import com.myoptimind.getexpress.features.shared.BaseDialogFragment

class RecipientDeliveryDialog : BaseDialogFragment() {

    companion object {
        fun newInstance(): RecipientDeliveryDialog {
            val args = Bundle()

            val fragment = RecipientDeliveryDialog ()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_recipient_delivery,container,false)
    }
}