package com.sefacicek.glassicoapp

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface GlasssDao {

    @Query("SELECT * FROM glass_table WHERE glass_owner = :first ORDER BY UID DESC" )
    fun findGlassByMail(first: String): LiveData<List<Glass>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(glass: Glass)

    @Update
    suspend fun update(glass: Glass)

    @Delete
    suspend fun delete(glass: Glass)

}