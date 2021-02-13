package com.myoptimind.get_express.features.customer.food_grocery.selected_store

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.myoptimind.get_express.R
import com.myoptimind.get_express.features.customer.food_grocery.data.Store
import com.myoptimind.get_express.features.shared.BaseDialogFragment
import kotlinx.android.synthetic.main.dialog_about_store.*
import timber.log.Timber

class AboutStoreDialog: BaseDialogFragment() {

    private lateinit var supportMapFragment: SupportMapFragment

    companion object {

        private const val ARGS_STORE = "args_store"

        fun newInstance(store: Store): AboutStoreDialog {
            val args = Bundle()
            args.putParcelable(ARGS_STORE,store)
            val fragment = AboutStoreDialog ()
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
        return inflater.inflate(R.layout.dialog_about_store,container,false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        supportMapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment

        val store = requireArguments().getParcelable<Store>(ARGS_STORE)!!

        Timber.v("store -> \n" + store.toString())


        ib_close.setOnClickListener {
            this.dismiss()
        }
        tv_address.text = store.locationText
        tv_about.text = store.about
        tv_contact_no.text = store.contactNumbers
        tv_opening_days.text = store.storeAvailability
        tv_opening_hours.text = store.storeSchedule

        supportMapFragment.getMapAsync { googleMap ->
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(store.latLng, 15F))
            val markerOptions = MarkerOptions()
                .position(store.latLng)
                .title(store.name)
            googleMap.addMarker(markerOptions)
        }

    }
}