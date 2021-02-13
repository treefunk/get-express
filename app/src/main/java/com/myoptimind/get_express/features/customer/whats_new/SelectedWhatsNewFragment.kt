package com.myoptimind.get_express.features.customer.whats_new

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.myoptimind.get_express.R
import com.myoptimind.get_express.features.shared.TitleOnlyFragment
import kotlinx.android.synthetic.main.fragment_selected_whats_new.*
import kotlinx.android.synthetic.main.item_whats_new.view.*

class SelectedWhatsNewFragment: TitleOnlyFragment() {

    private val args by navArgs<SelectedWhatsNewFragmentArgs>()

    override fun getTitle() = "What's New"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.fragment_selected_whats_new,container,false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val whatsNew = args.whatsNew

        tv_title_header.text = whatsNew.title
        Glide.with(requireContext())
                .load(whatsNew.bannerImage)
                .into(iv_main_image)
        tv_type.text = whatsNew.category
        tv_title.text = whatsNew.title
        tv_body.text = whatsNew.body
    }
}