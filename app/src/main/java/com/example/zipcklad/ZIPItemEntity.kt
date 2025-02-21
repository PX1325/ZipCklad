package com.example.zipcklad

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "zip_items")
data class ZIPItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val partNumber: String,
    var quantity: Int,
    val location: String,
    val isDirty: Boolean = false
)