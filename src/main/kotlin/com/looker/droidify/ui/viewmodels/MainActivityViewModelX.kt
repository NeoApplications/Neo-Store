package com.looker.droidify.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.looker.droidify.database.CursorOwner
import com.looker.droidify.ui.activities.MainActivityX

class MainActivityViewModelX() : ViewModel() {

    val activeRequests = mutableMapOf<Int, CursorOwner.ActiveRequest>()
}