package com.looker.droidify.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.looker.droidify.database.CursorOwner

class MainActivityViewModelX : ViewModel() {

    val activeRequests = mutableMapOf<Int, CursorOwner.ActiveRequest>()
}