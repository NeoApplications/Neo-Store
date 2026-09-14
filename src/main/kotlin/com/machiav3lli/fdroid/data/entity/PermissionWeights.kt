package com.machiav3lli.fdroid.data.entity

import kotlinx.serialization.Serializable

@Serializable
data class PermissionWeights(
    // physical_data
    val location: Float = 1.0f,
    val camera: Float = 1.0f,
    val microphone: Float = 1.0f,
    val nearbyDevices: Float = 1.0f,
    // identification_data
    val contacts: Float = 1.0f,
    val calendar: Float = 1.0f,
    val phone: Float = 1.0f,
    val sms: Float = 1.0f,
    val storage: Float = 1.0f,
    val internet: Float = 1.0f,
) {
    companion object {
        val DEFAULT = PermissionWeights()

        fun getWeightForGroup(weights: PermissionWeights, group: PermissionGroup): Float {
            return when (group) {
                PermissionGroup.Location      -> weights.location
                PermissionGroup.Camera        -> weights.camera
                PermissionGroup.Microphone    -> weights.microphone
                PermissionGroup.NearbyDevices -> weights.nearbyDevices
                PermissionGroup.Contacts      -> weights.contacts
                PermissionGroup.Calendar      -> weights.calendar
                PermissionGroup.Phone         -> weights.phone
                PermissionGroup.SMS           -> weights.sms
                PermissionGroup.Storage       -> weights.storage
                PermissionGroup.Internet      -> weights.internet
                else                          -> 1.0f
            }
        }

        fun setWeightForGroup(
            weights: PermissionWeights,
            group: PermissionGroup,
            value: Float
        ): PermissionWeights {
            return when (group) {
                PermissionGroup.Location      -> weights.copy(location = value)
                PermissionGroup.Camera        -> weights.copy(camera = value)
                PermissionGroup.Microphone    -> weights.copy(microphone = value)
                PermissionGroup.NearbyDevices -> weights.copy(nearbyDevices = value)
                PermissionGroup.Contacts      -> weights.copy(contacts = value)
                PermissionGroup.Calendar      -> weights.copy(calendar = value)
                PermissionGroup.Phone         -> weights.copy(phone = value)
                PermissionGroup.SMS           -> weights.copy(sms = value)
                PermissionGroup.Storage       -> weights.copy(storage = value)
                PermissionGroup.Internet      -> weights.copy(internet = value)
                else                          -> weights
            }
        }
    }
}