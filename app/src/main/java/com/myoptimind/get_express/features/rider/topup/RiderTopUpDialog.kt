package com.myoptimind.get_express.features.rider.topup

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.myoptimind.get_express.R
import com.myoptimind.get_express.features.rider.topup.data.WalletOffer
import com.myoptimind.get_express.features.shared.BaseDialogFragment
import kotlinx.android.parcel.Parcelize
import kotlinx.android.synthetic.main.dialog_rider_top_up.*

class RiderTopUpDialog: BaseDialogFragment() {

    lateinit var adapter: WalletOfferAdapter

    companion object {
        private const val ARGS_WALLET_OFFERS = "args_wallet_offers"
        private const val ARGS_PROMO_DESCRIPTION = "args_promo_description"
        const val DATA_WALLET_OFFER = "data_wallet_order"
        const val DATA_PAYMENT_TYPE = "data_payment_type"

        fun newInstance(walletOffers: List<WalletOffer>, promoDescription: String): RiderTopUpDialog {
            val args = Bundle()

            args.putString(ARGS_PROMO_DESCRIPTION,promoDescription)
            args.putParcelableArrayList(ARGS_WALLET_OFFERS,ArrayList(walletOffers))
            val fragment = RiderTopUpDialog()
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
        return inflater.inflate(R.layout.dialog_rider_top_up, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ib_close.setOnClickListener {
            dismiss()
        }

        tv_skip_link.setOnClickListener {
            dismiss()
        }
        val walletOffers = requireArguments().getParcelableArrayList<WalletOffer>(ARGS_WALLET_OFFERS)!!
        val walletDescription = requireArguments().getString(ARGS_PROMO_DESCRIPTION)!!

        tv_description.text = walletDescription

        adapter = WalletOfferAdapter(walletOffers, object: WalletOfferAdapter.WalletOfferListener{
            override fun onPressed(walletOffer: WalletOffer, index: Int) {
                adapter.selected = index
            }
        })
        btn_rider_topup.setOnClickListener {

            if(adapter.selected == null){
                Toast.makeText(requireContext(),"Please select a Wallet Offer",Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val walletOffer = adapter.walletOffers[adapter.selected!!]


            val items = ArrayList(listOf("GCash", "Wallet"))


            if(walletOffer.price.toDouble() < 100){
                items.remove("GCash")
            }

            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Select Payment type")
                .setItems(items.toTypedArray()) { dialog, which ->
                    // Respond to item chosen
                    val paymentType = getPaymentType(items[which])
                    if(paymentType == TOPUP_PAYMENT_TYPE.WALLET){
                        MaterialAlertDialogBuilder(requireContext())
                            .setTitle("Confirmation Needed")
                            .setMessage("Top up with wallet?")
                            .setNeutralButton("NO") { dialog, which ->

                            }
                            .setPositiveButton("YES") { dialog, which ->
                                proceedToPayment(walletOffer, paymentType)
                                this@RiderTopUpDialog.dismiss()

                            }
                            .show()
                    }else{
                        proceedToPayment(walletOffer, paymentType)
                        this@RiderTopUpDialog.dismiss()

                    }
                }
                .show()

        }

        rv_rider_topup.layoutManager = LinearLayoutManager(requireContext(),RecyclerView.VERTICAL,false)
        rv_rider_topup.adapter = adapter
    }

    private fun proceedToPayment(walletOffer: WalletOffer, paymentType: TOPUP_PAYMENT_TYPE){
        targetFragment?.onActivityResult(
            targetRequestCode,
            Activity.RESULT_OK,
            Intent().apply {
                putExtra(DATA_WALLET_OFFER,walletOffer) // PARSE THIS ("yyyy M d")
                putExtra(DATA_PAYMENT_TYPE, paymentType as Parcelable)
            }
        )
    }

    private fun getPaymentType(paymentType: String): TOPUP_PAYMENT_TYPE {
        if(paymentType == "GCash"){
            return TOPUP_PAYMENT_TYPE.GCASH
        }else if(paymentType == "Wallet"){
            return TOPUP_PAYMENT_TYPE.WALLET
        }else{
            throw Exception("payment not listed!")
        }
    }

}

@Parcelize
enum class TOPUP_PAYMENT_TYPE(): Parcelable {
    GCASH,WALLET
}


