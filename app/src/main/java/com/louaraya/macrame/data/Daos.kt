package com.louaraya.macrame.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(user: UserEntity): Long

    @Update
    suspend fun update(user: UserEntity)

    @Query("SELECT * FROM users WHERE email = :email COLLATE NOCASE LIMIT 1")
    suspend fun findByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users WHERE alias = :alias COLLATE NOCASE LIMIT 1")
    suspend fun findByAlias(alias: String): UserEntity?

    @Query("SELECT * FROM users WHERE id = :id")
    suspend fun findById(id: Long): UserEntity?

    @Query("SELECT * FROM users WHERE id = :id")
    fun observe(id: Long): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE role = 'CLIENT' AND notificationsEnabled = 1")
    suspend fun notifiableClients(): List<UserEntity>

    @Query("SELECT * FROM users WHERE role = 'ADMIN' LIMIT 1")
    suspend fun firstAdmin(): UserEntity?

    @Query("UPDATE users SET notificationsEnabled = :enabled WHERE id = :id")
    suspend fun setNotifications(id: Long, enabled: Boolean)

    @Query("SELECT COUNT(*) FROM users")
    suspend fun count(): Int
}

@Dao
interface ProductDao {
    @Insert
    suspend fun insert(product: ProductEntity): Long

    @Insert
    suspend fun insertAll(products: List<ProductEntity>): List<Long>

    @Update
    suspend fun update(product: ProductEntity)

    @Query("SELECT * FROM products ORDER BY id DESC")
    fun observeAll(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE active = 1 ORDER BY id DESC")
    fun observeActive(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE id = :id")
    fun observe(id: Long): Flow<ProductEntity?>

    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun find(id: Long): ProductEntity?

    @Query("SELECT * FROM products WHERE code = :code COLLATE NOCASE LIMIT 1")
    suspend fun findByCode(code: String): ProductEntity?

    @Query("UPDATE products SET active = :active WHERE id = :id")
    suspend fun setActive(id: Long, active: Boolean)

    @Query("SELECT MAX(id) FROM products")
    suspend fun maxId(): Long?
}

@Dao
interface FavoriteDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun add(favorite: FavoriteEntity): Long

    @Query("DELETE FROM favorites WHERE userId = :userId AND productId = :productId")
    suspend fun remove(userId: Long, productId: Long): Int

    @Query("SELECT COUNT(*) FROM favorites WHERE userId = :userId AND productId = :productId")
    suspend fun exists(userId: Long, productId: Long): Int

    @Query("SELECT productId FROM favorites WHERE userId = :userId")
    fun observeIds(userId: Long): Flow<List<Long>>

    @Query(
        "SELECT p.* FROM products p INNER JOIN favorites f ON f.productId = p.id " +
            "WHERE f.userId = :userId AND p.active = 1 ORDER BY f.addedAt DESC"
    )
    fun observeProducts(userId: Long): Flow<List<ProductEntity>>
}

@Dao
interface RequestDao {
    @Insert
    suspend fun insert(request: RequestEntity): Long

    @Insert
    suspend fun insertAll(requests: List<RequestEntity>): List<Long>

    @Query("SELECT * FROM requests WHERE id = :id")
    suspend fun find(id: Long): RequestEntity?

    @Query("SELECT * FROM requests WHERE userId = :userId ORDER BY createdAt DESC")
    fun observeByUser(userId: Long): Flow<List<RequestEntity>>

    @Query("SELECT * FROM requests ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<RequestEntity>>

    @Query("UPDATE requests SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: Long, status: String)

    @Query("SELECT COUNT(*) FROM requests WHERE status = 'Pendiente'")
    fun observePendingCount(): Flow<Int>
}

@Dao
interface NotificationDao {
    @Insert
    suspend fun insert(notification: NotificationEntity): Long

    @Insert
    suspend fun insertAll(notifications: List<NotificationEntity>)

    @Query("SELECT * FROM notifications WHERE userId = :userId ORDER BY createdAt DESC")
    fun observeByUser(userId: Long): Flow<List<NotificationEntity>>

    @Query("SELECT COUNT(*) FROM notifications WHERE userId = :userId AND read = 0")
    fun observeUnreadCount(userId: Long): Flow<Int>

    @Query("UPDATE notifications SET read = 1 WHERE userId = :userId")
    suspend fun markAllRead(userId: Long)

    @Query("UPDATE notifications SET read = 1 WHERE id = :id")
    suspend fun markRead(id: Long)
}

@Dao
interface FairDao {
    @Insert
    suspend fun insertAll(fairs: List<FairEntity>)

    @Query("SELECT * FROM fairs ORDER BY id ASC")
    fun observeAll(): Flow<List<FairEntity>>
}

@Dao
interface CampaignDao {
    @Insert
    suspend fun insert(campaign: CampaignEntity): Long

    @Update
    suspend fun update(campaign: CampaignEntity)

    @Query("SELECT * FROM campaigns ORDER BY startDate DESC")
    fun observeAll(): Flow<List<CampaignEntity>>

    @Query("SELECT * FROM campaigns WHERE id = :id")
    suspend fun find(id: Long): CampaignEntity?

    @Query("SELECT productId FROM campaign_products WHERE campaignId = :campaignId")
    suspend fun productIds(campaignId: Long): List<Long>

    @Query("DELETE FROM campaign_products WHERE campaignId = :campaignId")
    suspend fun clearProducts(campaignId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRefs(refs: List<CampaignProductRef>)

    @Query("SELECT * FROM campaign_products")
    fun observeRefs(): Flow<List<CampaignProductRef>>
}

@Dao
interface ScanLogDao {
    @Insert
    suspend fun insert(log: ScanLogEntity): Long

    @Query("SELECT COUNT(*) FROM scan_logs WHERE userId = :userId AND code = :code AND scannedAt > :since")
    suspend fun countRecent(userId: Long, code: String, since: Long): Int
}
