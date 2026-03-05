package com.lyrasaver.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.lyrasaver.database.entity.DeletedMessage
import kotlinx.coroutines.flow.Flow

@Dao
interface DeletedMessageDao {
    @Insert
    suspend fun insert(message: DeletedMessage)

    @Delete
    suspend fun delete(message: DeletedMessage)

    @Query("SELECT * FROM deleted_message ORDER BY deletedAt DESC")
    fun getAllDeletedMessages(): Flow<List<DeletedMessage>>

    @Query("SELECT * FROM deleted_message WHERE senderName LIKE '%' || :senderName || '%' ORDER BY deletedAt DESC")
    fun getMessagesBySender(senderName: String): Flow<List<DeletedMessage>>

    @Query("DELETE FROM deleted_message WHERE id = :id")
    suspend fun deleteMessageById(id: Int)

    @Query("DELETE FROM deleted_message WHERE deletedAt < :timestampInMillis")
    suspend fun deleteOldMessages(timestampInMillis: Long)

    @Query("SELECT COUNT(*) FROM deleted_message")
    fun getTotalCount(): Flow<Int>
}
