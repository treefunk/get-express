package com.myoptimind.get_express.features.rider.topup

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.myoptimind.get_express.R
import com.myoptimind.get_express.features.shared.BaseDialogFragment
import kotlinx.android.synthetic.main.dialog_my_wallet.*

class MyWalletDialog: BaseDialogFragment() {


    companion object {
        private const val ARGS_CURRENT_AMOUNT = "ARGS_CURRENT_AMOUNT"
        private const val ARGS_CASH_ON_HAND   = "ARGS_CASH_ON_HAND"
        private const val MAX_LIMIT_TO_TOPUP  = 100000
        const val DATA_AMOUNT_TO_LOAD = "DATA_AMOUNT_TO_LOAD"


        fun newInstance(currentAmount: String, isCashOnHand: Boolean = false): MyWalletDialog {
            val args = Bundle()
            args.putString(ARGS_CURRENT_AMOUNT,currentAmount)
            args.putBoolean(ARGS_CASH_ON_HAND, isCashOnHand)
            val fragment = MyWalletDialog()
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
        return inflater.inflate(R.layout.dialog_my_wallet,container,false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val currentAmount = requireArguments().getString(ARGS_CURRENT_AMOUNT)!!
        val isCashOnHand  = requireArguments().getBoolean(ARGS_CASH_ON_HAND)

        if(isCashOnHand){
            label_my_wallet.text = "Cash On Hand"
            label_amount_to_load.text = "Enter New Amount"
            btn_load_now.setImageDrawable(ContextCompat.getDrawable(requireContext(),R.drawable.btn_update))
            label_minimum_amount.visibility = View.INVISIBLE
        }

        tv_current_amount.text = currentAmount
        ib_close.setOnClickListener { dismiss() }

        btn_load_now.setOnClickListener {
            val amount = et_amount_to_load.text.toString().trim()

            if(amount.isBlank() || amount.isEmpty()){
                Toast.makeText(requireContext(),"Please enter an amount.",Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if(amount.toDouble() < 100 && isCashOnHand.not()){
                Toast.makeText(requireContext(),"Amount must be greater than 100.",Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if(isCashOnHand.not() && (amount.toDouble() + currentAmount.toDouble()) >= MAX_LIMIT_TO_TOPUP){
                Toast.makeText(requireContext(),"Amount must not exceed 100000",Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if(isCashOnHand && amount.toDouble() >= MAX_LIMIT_TO_TOPUP){
                Toast.makeText(requireContext(),"Amount must not exceed 100000",Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val data = Intent().apply {
                putExtra(DATA_AMOUNT_TO_LOAD,amount.toDouble())
            }

            targetFragment?.onActivityResult(
                targetRequestCode,Activity.RESULT_OK,data
            )

            dismiss()

        }
    }
}