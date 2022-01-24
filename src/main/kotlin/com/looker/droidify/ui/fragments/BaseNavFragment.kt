package com.looker.droidify.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment

abstract class BaseNavFragment : Fragment() {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupAdapters()
        setupLayout()
    }

    abstract fun setupAdapters()
    abstract fun setupLayout()
}