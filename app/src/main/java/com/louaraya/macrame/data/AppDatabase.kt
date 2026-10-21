package com.louaraya.macrame.data

import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * Base de datos local (SQLite) de la aplicaciion
 * Todas las tablas viven aquí el acceso se hace a través de [MacrameRepository].
 */
@Database(
    entities = [
        UserEntity::class,
        ProductEntity::class,
        FavoriteEntity::class,
        RequestEntity::class,
        NotificationEntity::class,
        FairEntity::class,
        CampaignEntity::class,
        CampaignProductRef::class,
        ScanLogEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun productDao(): ProductDao
    abstract fun favoriteDao(): FavoriteDao
    abstract fun requestDao(): RequestDao
    abstract fun notificationDao(): NotificationDao
    abstract fun fairDao(): FairDao
    abstract fun campaignDao(): CampaignDao
    abstract fun scanLogDao(): ScanLogDao
}
