package com.skylens.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.skylens.data.local.entities.OfflinePackEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface OfflinePackDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(pack: OfflinePackEntity)

    @Update
    suspend fun update(pack: OfflinePackEntity)

    @Query("SELECT * FROM offline_packs WHERE route = :route LIMIT 1")
    suspend fun getPackByRoute(route: String): OfflinePackEntity?

    @Query("SELECT * FROM offline_packs WHERE route = :route LIMIT 1")
    fun observePackByRoute(route: String): Flow<OfflinePackEntity?>

    @Query("SELECT * FROM offline_packs ORDER BY downloaded_at DESC")
    suspend fun getAllPacks(): List<OfflinePackEntity>

    @Query("SELECT * FROM offline_packs WHERE status = 'completed' ORDER BY downloaded_at DESC")
    suspend fun getCompletedPacks(): List<OfflinePackEntity>

    @Query("DELETE FROM offline_packs WHERE route = :route")
    suspend fun deletePackByRoute(route: String)

    @Query("DELETE FROM offline_packs")
    suspend fun deleteAllPacks()
}
