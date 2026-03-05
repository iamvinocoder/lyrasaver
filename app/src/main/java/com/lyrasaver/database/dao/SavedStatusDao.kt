package com.lyrasaver.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.lyrasaver.database.entity.SavedStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedStatusDao {
    @Insert
    suspend fun insert(status: SavedStatus)

    @Update
    suspend fun update(status: SavedStatus)

    @Delete
    suspend fun delete(status: SavedStatus)

    @Query("SELECT * FROM saved_status ORDER BY savedAt DESC")
    fun getAllStatuses(): Flow<List<SavedStatus>>

    @Query("SELECT * FROM saved_status WHERE mediaType = :mediaType ORDER BY savedAt DESC")
    fun getStatusesByType(mediaType: String): Flow<List<SavedStatus>>

    @Query("SELECT * FROM saved_status WHERE isFavorite = 1 ORDER BY savedAt DESC")
    fun getFavoriteStatuses(): Flow<List<SavedStatus>>

    @Query("DELETE FROM saved_status WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("SELECT COUNT(*) FROM saved_status")
    fun getTotalCount(): Flow<Int>

    @Query("SELECT SUM(fileSize) FROM saved_status")
    fun getTotalSize(): Flow<Long>
}
