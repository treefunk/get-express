package com.myoptimind.get_express.features.shared

import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.myoptimind.get_express.features.login.data.User
import kotlinx.android.synthetic.main.partial_profile_admin.*

open class ProfileFragment: TitleOnlyFragment() {
    override fun getTitle() = "Edit Profile"
    fun updateUserLabels(user: User){
        Glide.with(requireContext())
                .load(user.profilePicture)
                .into(iv_profile_picture)

        tv_profile_full_name.text = "Welcome, ${user.fullName}!"
    }
}