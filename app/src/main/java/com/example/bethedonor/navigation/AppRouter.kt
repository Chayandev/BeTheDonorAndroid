package com.example.bethedonor.navigation

import com.example.bethedonor.data.dataModels.Donor
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
sealed class Destination {
    @Serializable
    data object Registration : Destination()

    @Serializable
    data object Login : Destination()

    @Serializable
    data object Home : Destination()

    @Serializable
    data object AllRequest : Destination()

    @Serializable
    data object CreateRequest : Destination()

    @Serializable
    data object History : Destination()

    @Serializable
    data object Profile : Destination()

    @Serializable
    data object ProfileEdit : Destination()

    @Serializable
    data object EmailEdit : Destination()

    @Serializable
    data class DonorProfile(
        val name: String,
        val email: String,
        val state: String,
        val city: String,
        val district: String,
        val pin: String,
        val phoneNumber: String,
        val bloodGroup: String?
    ) : Destination()
}

