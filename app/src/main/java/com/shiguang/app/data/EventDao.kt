package com.shiguang.app.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface EventDao {

    @Query("SELECT * FROM events ORDER BY targetEpochDay ASC")
    fun observeAll(): Flow<List<EventEntity>>

    @Query("SELECT * FROM events WHERE id = :id")
    suspend fun byId(id: Long): EventEntity?

    @Query("SELECT * FROM events")
    suspend fun all(): List<EventEntity>

    @Insert
    suspend fun insert(entity: EventEntity): Long

    @Update
    suspend fun update(entity: EventEntity)

    @Delete
    suspend fun delete(entity: EventEntity)
}
