package com.myoptimind.getexpress.features.shared

import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.myoptimind.getexpress.features.login.data.User
import kotlinx.android.synthetic.main.partial_profile_admin.*

open class ProfileFragment: Fragment() {
    fun updateUserLabels(user: User){
        Glide.with(requireContext())
                .load(user.profilePicture)
                .into(iv_profile_picture)

        tv_profile_full_name.text = "Welcome, ${user.fullName}"
    }
}