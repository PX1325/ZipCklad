package com.example.zipcklad.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST

interface ZipApiService {

    @GET("items")
    suspend fun getAllItems(): Response<List<ZipItemResponse>>

    @POST("sync")
    @Headers("Content-Type: application/json")
    suspend fun syncItems(@Body items: List<ZipItemSyncRequest>): Response<SyncResponse>
    /*data class SyncRequest(
        val partNumber: String,
        val quantity: Int,
        val name: String? = null,
        val location: String? = null
    )*/
}