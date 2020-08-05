package com.firebase.rest.neli.auth

import com.google.gson.annotations.SerializedName


internal data class AccessTokenBody(
    @SerializedName("grant_type")
    val grantType: String,
    @SerializedName("refresh_token")
    val refreshToken: String?
)