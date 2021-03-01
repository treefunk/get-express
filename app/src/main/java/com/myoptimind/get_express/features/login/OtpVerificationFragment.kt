package com.myoptimind.get_express.features.login

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import com.myoptimind.get_express.R
import com.myoptimind.get_express.features.login.data.UserType
import kotlinx.android.synthetic.main.fragment_otp_verification.*
import kotlinx.android.synthetic.main.fragment_otp_verification.view.*

class OtpVerificationFragment: BaseLoginFragment(UserType.CUSTOMER) {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.fragment_otp_verification,container,false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



        //GenericTextWatcher here works only for moving to next EditText when a number is entered
//first parameter is the current EditText and second parameter is next EditText
        etp_otp_1.addTextChangedListener(GenericTextWatcher(etp_otp_1, etp_otp_2))
        etp_otp_2.addTextChangedListener(GenericTextWatcher(etp_otp_2, etp_otp_3))
        etp_otp_3.addTextChangedListener(GenericTextWatcher(etp_otp_3, etp_otp_4))
        etp_otp_4.addTextChangedListener(GenericTextWatcher(etp_otp_4, null))

//GenericKeyEvent here works for deleting the element and to switch back to previous EditText
//first parameter is the current EditText and second parameter is previous EditText
        etp_otp_1.setOnKeyListener(GenericKeyEvent(etp_otp_1, null))
        etp_otp_2.setOnKeyListener(GenericKeyEvent(etp_otp_2, etp_otp_1))
        etp_otp_3.setOnKeyListener(GenericKeyEvent(etp_otp_3, etp_otp_2))
        etp_otp_4.setOnKeyListener(GenericKeyEvent(etp_otp_4,etp_otp_3))

    }

    class GenericTextWatcher internal constructor(private val currentView: View, private val nextView: View?) : TextWatcher {
        override fun afterTextChanged(editable: Editable) {
            val text = editable.toString()
            when (currentView.id) {
                R.id.etp_otp_1 -> if (text.length == 1) nextView!!.requestFocus()
                R.id.etp_otp_2 -> if (text.length == 1) nextView!!.requestFocus()
                R.id.etp_otp_3 -> if (text.length == 1) nextView!!.requestFocus()
                //You can use EditText4 same as above to hide the keyboard
            }
        }

        override fun beforeTextChanged(
            arg0: CharSequence,
            arg1: Int,
            arg2: Int,
            arg3: Int
        ) { // TODO Auto-generated method stub
        }

        override fun onTextChanged(
            arg0: CharSequence,
            arg1: Int,
            arg2: Int,
            arg3: Int
        ) { // TODO Auto-generated method stub
        }

    }

    class GenericKeyEvent internal constructor(private val currentView: EditText, private val previousView: EditText?) : View.OnKeyListener{
        override fun onKey(p0: View?, keyCode: Int, event: KeyEvent?): Boolean {
            if(event!!.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DEL && currentView.id != R.id.etp_otp_1 && currentView.text.isEmpty()) {
                //If current is empty then previous EditText's number will also be deleted
                previousView!!.text = null
                previousView.requestFocus()
                return true
            }
            return false
        }


    }
}

