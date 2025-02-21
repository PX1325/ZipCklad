package com.example.zipcklad.network

import com.example.zipcklad.ZIPItemEntity
import com.google.gson.annotations.SerializedName
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private const val BASE_URL = "http://192.168.1.102:5000/api/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    private val client = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    val instance: ZipApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ZipApiService::class.java)
    }
    data class ZipItemResponse(
        @SerializedName("Название")
        val name: String?,
        @SerializedName("Заказной номер")
        val partNumber: String?,
        @SerializedName("Количество")
        val quantity: Int?,
        @SerializedName("Местоположение")
        val location: String?
    )
        fun ZipItemResponse.toEntity() = ZIPItemEntity(
    name = name ?: "Без названия",
    partNumber = partNumber ?: "Без номера",
    quantity = quantity ?: 0,
    location = location ?: "Не указано"

    )
}