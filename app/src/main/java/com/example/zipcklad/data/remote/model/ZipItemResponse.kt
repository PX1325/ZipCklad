package com.example.zipcklad.data.remote.model

import com.google.gson.annotations.SerializedName

data class ZipItemResponse(
    @SerializedName("Название") val name: String,
    @SerializedName("Заказной номер") val partNumber: String,
    @SerializedName("Количество") val quantity: Int,
    @SerializedName("Местоположение") val location: String
)