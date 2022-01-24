package com.looker.droidify.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope

abstract class BaseNavFragment : Fragment() {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lifecycleScope.launchWhenCreated {
            setupAdapters()
            setupLayout()
        }
    }

    abstract suspend fun setupAdapters()
    abstract suspend fun setupLayout()
}