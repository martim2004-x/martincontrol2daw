package com.louaraya.macrame.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "users",
    indices = [Index(value = ["email"], unique = true), Index(value = ["alias"], unique = true)]
)
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fullName: String,
    val alias: String,
    val email: String,
    val phone: String,
    val address: String = "",
    val passwordHash: String,
    val avatarIndex: Int = 0,
    val role: String,
    val notificationsEnabled: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "products", indices = [Index(value = ["code"], unique = true)])
data class ProductEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    /** Identificador con el qr de la etiqyueta(ej. LAM-0001). */
    val code: String,
    val name: String,
    val description: String,
    val category: String,
    val widthCm: Int,
    val heightCm: Int,
    val material: String,
    val priceClp: Int,
    val stock: Int,
    /** true = trabajo confeccionado a pedido false = pieza disponible en stock. */
    val madeToOrder: Boolean = false,
    val active: Boolean = true,
    /** URI (FileProvider) de la foto tomada con la cámara null usa la ilustración generada. */
    val imageUri: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "favorites", primaryKeys = ["userId", "productId"])
data class FavoriteEntity(
    val userId: Long,
    val productId: Long,
    val addedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "requests")
data class RequestEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val clientName: String,
    val productId: Long?,
    val productName: String?,
    val eventType: String,
    val widthCm: Int,
    val heightCm: Int,
    val material: String,
    /** Nombres de colores separados por coma. */
    val colors: String,
    val requiredDate: Long,
    val notes: String,
    val status: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val title: String,
    val body: String,
    val type: String,
    val refId: Long? = null,
    val read: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "fairs")
data class FairEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: String,
    val place: String,
    val dateText: String,
    val description: String,
    val lat: Double,
    val lng: Double
)

@Entity(tableName = "campaigns")
data class CampaignEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String,
    val theme: String,
    /** Medianoche UTC del día de inicio. */
    val startDate: Long,
    /** Medianoche UTC del día de término. */
    val endDate: Long,
    val published: Boolean,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "campaign_products", primaryKeys = ["campaignId", "productId"])
data class CampaignProductRef(
    val campaignId: Long,
    val productId: Long
)

/** Registro local de lecturas de QR (pendiente de sincronizar con el backend en la EFT). */
@Entity(tableName = "scan_logs")
data class ScanLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val code: String,
    val found: Boolean,
    val scannedAt: Long = System.currentTimeMillis(),
    val synced: Boolean = false
)
