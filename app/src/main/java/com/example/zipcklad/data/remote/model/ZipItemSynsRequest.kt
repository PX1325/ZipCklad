package com.example.zipcklad.data.remote.model

import com.google.gson.annotations.SerializedName

data class ZipItemSyncRequest(
    @SerializedName("name")
    val name: String,

    @SerializedName("partNumber")
    val partNumber: String,

    @SerializedName("quantity")
    val quantity: Int,

    @SerializedName("location")
    val location: String,

    @SerializedName("isDirty")
    val isDirty: Boolean
)