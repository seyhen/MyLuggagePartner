package com.myluggagepartner.app.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "trips")
data class TripEntity(
    @PrimaryKey val id: Long,
    val name: String,
    val destination: String,
    val dates: String,
    val type: String,
    val hasPhoto: Boolean,
    val departureDateEpoch: Long? = null,
)

@Entity(
    tableName = "pack_items",
    foreignKeys = [ForeignKey(
        entity = TripEntity::class,
        parentColumns = ["id"],
        childColumns = ["tripId"],
        onDelete = ForeignKey.CASCADE,
    )],
    indices = [Index("tripId")],
)
data class PackItemEntity(
    @PrimaryKey val id: Long,
    val tripId: Long,
    val category: String,
    val name: String,
    val qty: Int,
    val checked: Boolean,
    val sortOrder: Int,
)

@Entity(tableName = "templates")
data class TemplateEntity(
    @PrimaryKey val id: Long,
    val name: String,
    val type: String,
)

@Entity(
    tableName = "template_items",
    foreignKeys = [ForeignKey(
        entity = TemplateEntity::class,
        parentColumns = ["id"],
        childColumns = ["templateId"],
        onDelete = ForeignKey.CASCADE,
    )],
    indices = [Index("templateId")],
)
data class TemplateItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val templateId: Long,
    val category: String,
    val name: String,
)
