package com.myluggagepartner.app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {

    // ——— Trips ———
    @Query("SELECT * FROM trips ORDER BY id DESC")
    fun allTrips(): Flow<List<TripEntity>>

    @Query("SELECT * FROM trips ORDER BY id DESC")
    suspend fun allTripsOnce(): List<TripEntity>

    @Query("SELECT * FROM pack_items WHERE tripId = :tripId ORDER BY sortOrder")
    fun itemsForTrip(tripId: Long): Flow<List<PackItemEntity>>

    @Query("SELECT * FROM pack_items WHERE tripId = :tripId ORDER BY sortOrder")
    suspend fun itemsForTripOnce(tripId: Long): List<PackItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrip(trip: TripEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<PackItemEntity>)

    @Query("DELETE FROM trips WHERE id = :tripId")
    suspend fun deleteTrip(tripId: Long)

    @Query("UPDATE trips SET name = :name WHERE id = :id")
    suspend fun renameTrip(id: Long, name: String)

    @Query("UPDATE pack_items SET checked = :checked WHERE id = :id")
    suspend fun setItemChecked(id: Long, checked: Boolean)

    @Query("UPDATE pack_items SET qty = :qty WHERE id = :id")
    suspend fun setItemQty(id: Long, qty: Int)

    @Query("DELETE FROM pack_items WHERE id = :id")
    suspend fun deleteItem(id: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: PackItemEntity)

    @Query("UPDATE pack_items SET checked = 0 WHERE tripId = :tripId")
    suspend fun resetChecks(tripId: Long)

    // ——— Templates ———
    @Query("SELECT * FROM templates ORDER BY id DESC")
    fun allTemplates(): Flow<List<TemplateEntity>>

    @Query("SELECT * FROM template_items WHERE templateId = :templateId")
    suspend fun templateItems(templateId: Long): List<TemplateItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTemplate(template: TemplateEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTemplateItems(items: List<TemplateItemEntity>)

    @Query("DELETE FROM templates WHERE id = :id")
    suspend fun deleteTemplate(id: Long)

    @Transaction
    suspend fun insertTemplateWithItems(template: TemplateEntity, items: List<TemplateItemEntity>) {
        insertTemplate(template)
        insertTemplateItems(items)
    }

    @Transaction
    suspend fun insertTripWithItems(trip: TripEntity, items: List<PackItemEntity>) {
        insertTrip(trip)
        insertItems(items)
    }
}
