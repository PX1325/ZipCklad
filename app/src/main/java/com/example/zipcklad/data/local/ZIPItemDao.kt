package com.example.zipcklad.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ZIPItemDao {
    @Query("SELECT * FROM zip_items")
    fun getAll(): Flow<List<ZIPItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<ZIPItemEntity>)

    @Insert
    suspend fun insert(item: ZIPItemEntity)

    @Update
    suspend fun update(item: ZIPItemEntity)

    @Query("SELECT * FROM zip_items WHERE partNumber = :partNumber")
    suspend fun getByPartNumber(partNumber: String): ZIPItemEntity?

    @Query("""
    SELECT * FROM zip_items 
    WHERE name LIKE '%' || :query || '%' 
    OR partNumber LIKE '%' || :query || '%'
    OR location LIKE '%' || :query || '%'
""")
    fun search(query: String): Flow<List<ZIPItemEntity>>

    @Query("SELECT * FROM zip_items WHERE location = :exactLocation")
    fun getByLocation(exactLocation: String): Flow<List<ZIPItemEntity>>

    @Query("SELECT * FROM zip_items WHERE isDirty = 1")
    suspend fun getUnsyncedItems(): List<ZIPItemEntity>

    @Query("UPDATE zip_items SET isDirty = 0 WHERE id IN (:ids)")
    suspend fun markAsSynced(ids: List<Int>)

    @Transaction
    suspend fun syncWithServer(items: List<ZIPItemEntity>) {
        deleteAll()
        insertAll(items)
    }

    @Query("DELETE FROM zip_items")
    suspend fun deleteAll()

    @Delete
    suspend fun delete(item: ZIPItemEntity)

}
