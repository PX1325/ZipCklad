package com.example.zipcklad.network

import com.example.zipcklad.ZIPItemEntity

object NetworkMappers {
    fun toSyncRequest(entity: ZIPItemEntity) = ZipItemSyncRequest(
        name = entity.name ?: "",  // Замена null на пустую строку
        partNumber = entity.partNumber,
        quantity = entity.quantity,
        location = entity.location,
        isDirty = entity.isDirty
    )

    fun toEntity(response: ZipItemResponse) = ZIPItemEntity(
        name = response.name,
        partNumber = response.partNumber,
        quantity = response.quantity,
        location = response.location,
        isDirty = false
    )
}