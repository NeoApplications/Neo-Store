package com.machiav3lli.fdroid.entity

import com.machiav3lli.fdroid.utility.KParcelable

sealed class Section : KParcelable {
    object All : Section() {
        @Suppress("unused")
        @JvmField
        val CREATOR = KParcelable.creator { All }
    }

    object FAVORITE : Section() {
        @Suppress("unused")
        @JvmField
        val CREATOR = KParcelable.creator { FAVORITE }
    }
}