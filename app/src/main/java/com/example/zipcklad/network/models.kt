package com.example.zipcklad.network

import com.google.gson.annotations.SerializedName

data class ZipItemResponse(
    @SerializedName("Название") val name: String,
    @SerializedName("Заказной номер") val partNumber: String,
    @SerializedName("Количество") val quantity: Int,
    @SerializedName("Местоположение") val location: String
)
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

data class SyncResponse(
    val status: String,
    val updatedCount: Int
)