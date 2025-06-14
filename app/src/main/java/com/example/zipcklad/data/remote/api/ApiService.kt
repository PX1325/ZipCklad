package com.example.zipcklad.data.remote.api

import com.example.zipcklad.data.remote.model.SyncResponse
import com.example.zipcklad.data.remote.model.ZipItemResponse
import com.example.zipcklad.data.remote.model.ZipItemSyncRequest
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
}