package com.lyrasaver.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.lyrasaver.database.dao.DeletedMessageDao
import com.lyrasaver.database.dao.SavedStatusDao
import com.lyrasaver.database.entity.DeletedMessage
import com.lyrasaver.database.entity.SavedStatus

@Database(
    entities = [SavedStatus::class, DeletedMessage::class],
    version = 1,
    exportSchema = false
)
abstract class LyraSaverDatabase : RoomDatabase() {
    abstract fun savedStatusDao(): SavedStatusDao
    abstract fun deletedMessageDao(): DeletedMessageDao
}
