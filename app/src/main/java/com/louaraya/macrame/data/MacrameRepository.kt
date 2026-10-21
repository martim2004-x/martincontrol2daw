package com.louaraya.macrame.data

import androidx.room.withTransaction
import com.louaraya.macrame.domain.NotificationType
import com.louaraya.macrame.domain.RequestStatus
import com.louaraya.macrame.domain.Role
import com.louaraya.macrame.util.Dates
import com.louaraya.macrame.util.Formatters
import com.louaraya.macrame.util.PasswordHasher
import kotlinx.coroutines.flow.Flow

sealed interface AuthResult {
    data class Success(val user: UserEntity) : AuthResult
    data class Error(val message: String, val field: String? = null) : AuthResult
}

data class RegisterInput(
    val fullName: String,
    val alias: String,
    val email: String,
    val phone: String,
    val password: String,
    val address: String,
    val avatarIndex: Int
)

data class RequestInput(
    val product: ProductEntity?,
    val eventType: String,
    val widthCm: Int,
    val heightCm: Int,
    val material: String,
    val colors: List<String>,
    val requiredDate: Long,
    val notes: String
)

/**
 * unica puerta de acceso a los datos locales (Room/SQLite).
 * Los ViewModels hablan con esta clase
 */
class MacrameRepository(private val db: AppDatabase) {
    private val userDao = db.userDao()
    private val productDao = db.productDao()
    private val favoriteDao = db.favoriteDao()
    private val requestDao = db.requestDao()
    private val notificationDao = db.notificationDao()
    private val fairDao = db.fairDao()
    private val campaignDao = db.campaignDao()
    private val scanLogDao = db.scanLogDao()

    // ───────────── Usuarios y sesión ─────────────

    suspend fun login(email: String, password: String, expectedRole: String): AuthResult {
        val user = userDao.findByEmail(email.trim())
            ?: return AuthResult.Error("No encontramos una cuenta con ese correo", "email")
        if (!PasswordHasher.verify(password, user.passwordHash)) {
            return AuthResult.Error("Contraseña incorrecta", "password")
        }
        if (user.role != expectedRole) {
            val msg = if (expectedRole == Role.ADMIN) "Esta cuenta no tiene permisos de administrador"
            else "Esta es una cuenta de administrador: cambia a la pestaña Administrador"
            return AuthResult.Error(msg)
        }
        return AuthResult.Success(user)
    }

    suspend fun register(input: RegisterInput): AuthResult {
        if (userDao.findByEmail(input.email.trim()) != null) {
            return AuthResult.Error("Ya existe una cuenta con este correo", "email")
        }
        if (userDao.findByAlias(input.alias.trim()) != null) {
            return AuthResult.Error("Este alias ya está en uso", "alias")
        }
        val id = userDao.insert(
            UserEntity(
                fullName = input.fullName.trim(),
                alias = input.alias.trim(),
                email = input.email.trim().lowercase(),
                phone = input.phone.trim(),
                address = input.address.trim(),
                passwordHash = PasswordHasher.hash(input.password),
                avatarIndex = input.avatarIndex,
                role = Role.CLIENT
            )
        )
        val user = userDao.findById(id) ?: return AuthResult.Error("No se pudo crear la cuenta")
        notificationDao.insert(
            NotificationEntity(
                userId = id, title = "¡Bienvenida/o a Lou Araya Macramé!",
                body = "Explora el catálogo y guarda tus diseños favoritos.", type = NotificationType.CATALOG
            )
        )
        return AuthResult.Success(user)
    }

    fun observeUser(id: Long): Flow<UserEntity?> = userDao.observe(id)

    suspend fun findUser(id: Long): UserEntity? = userDao.findById(id)

    suspend fun updateProfile(
        userId: Long, fullName: String, alias: String, phone: String, address: String, avatarIndex: Int
    ): AuthResult {
        val current = userDao.findById(userId) ?: return AuthResult.Error("Usuario no encontrado")
        val other = userDao.findByAlias(alias.trim())
        if (other != null && other.id != userId) return AuthResult.Error("Este alias ya está en uso", "alias")
        val updated = current.copy(
            fullName = fullName.trim(), alias = alias.trim(), phone = phone.trim(),
            address = address.trim(), avatarIndex = avatarIndex
        )
        userDao.update(updated)
        return AuthResult.Success(updated)
    }

    suspend fun setNotificationsEnabled(userId: Long, enabled: Boolean) =
        userDao.setNotifications(userId, enabled)

    // ───────────── Productos ─────────────

    fun observeAllProducts(): Flow<List<ProductEntity>> = productDao.observeAll()
    fun observeActiveProducts(): Flow<List<ProductEntity>> = productDao.observeActive()
    fun observeProduct(id: Long): Flow<ProductEntity?> = productDao.observe(id)
    suspend fun findProduct(id: Long): ProductEntity? = productDao.find(id)
    suspend fun findProductByCode(code: String): ProductEntity? = productDao.findByCode(code)

    suspend fun saveProduct(product: ProductEntity): Long = db.withTransaction {
        if (product.id == 0L) {
            val next = (productDao.maxId() ?: 0L) + 1
            val id = productDao.insert(product.copy(code = Formatters.productCode(next)))
            if (product.active) {
                notifyClients(
                    "Nuevo producto en catálogo", "Conoce «${product.name}», recién publicada.",
                    NotificationType.CATALOG, id
                )
            }
            id
        } else {
            productDao.update(product)
            product.id
        }
    }

    suspend fun setProductActive(id: Long, active: Boolean) = productDao.setActive(id, active)

    // ───────────── Favoritos ─────────────

    fun observeFavoriteIds(userId: Long): Flow<List<Long>> = favoriteDao.observeIds(userId)
    fun observeFavoriteProducts(userId: Long): Flow<List<ProductEntity>> = favoriteDao.observeProducts(userId)

    suspend fun toggleFavorite(userId: Long, productId: Long) {
        if (favoriteDao.exists(userId, productId) > 0) favoriteDao.remove(userId, productId)
        else favoriteDao.add(FavoriteEntity(userId, productId))
    }

    // ───────────── Solicitudes ─────────────

    fun observeRequestsByUser(userId: Long): Flow<List<RequestEntity>> = requestDao.observeByUser(userId)
    fun observeAllRequests(): Flow<List<RequestEntity>> = requestDao.observeAll()
    fun observePendingCount(): Flow<Int> = requestDao.observePendingCount()

    suspend fun createRequest(user: UserEntity, input: RequestInput): Long = db.withTransaction {
        val id = requestDao.insert(
            RequestEntity(
                userId = user.id, clientName = user.fullName,
                productId = input.product?.id, productName = input.product?.name,
                eventType = input.eventType, widthCm = input.widthCm, heightCm = input.heightCm,
                material = input.material, colors = input.colors.joinToString(","),
                requiredDate = input.requiredDate, notes = input.notes.trim(),
                status = RequestStatus.PENDIENTE
            )
        )
        userDao.firstAdmin()?.let { admin ->
            notificationDao.insert(
                NotificationEntity(
                    userId = admin.id, title = "Nueva solicitud",
                    body = "${user.alias} envió la solicitud ${Formatters.requestCode(id)}.",
                    type = NotificationType.REQUEST, refId = id
                )
            )
        }
        id
    }

    suspend fun updateRequestStatus(requestId: Long, status: String) {
        val request = requestDao.find(requestId) ?: return
        if (request.status == status) return
        requestDao.updateStatus(requestId, status)
        notificationDao.insert(
            NotificationEntity(
                userId = request.userId, title = "Tu solicitud cambió de estado",
                body = "${Formatters.requestCode(requestId)} ahora está: $status.",
                type = NotificationType.REQUEST, refId = requestId
            )
        )
    }

    // ───────────── Notificaciones ─────────────

    fun observeNotifications(userId: Long): Flow<List<NotificationEntity>> = notificationDao.observeByUser(userId)
    fun observeUnreadCount(userId: Long): Flow<Int> = notificationDao.observeUnreadCount(userId)
    suspend fun markAllNotificationsRead(userId: Long) = notificationDao.markAllRead(userId)
    suspend fun markNotificationRead(id: Long) = notificationDao.markRead(id)

    private suspend fun notifyClients(title: String, body: String, type: String, refId: Long?) {
        val now = System.currentTimeMillis()
        val list = userDao.notifiableClients().map {
            NotificationEntity(userId = it.id, title = title, body = body, type = type, refId = refId, createdAt = now)
        }
        if (list.isNotEmpty()) notificationDao.insertAll(list)
    }

    // ───────────── Ferias ─────────────

    fun observeFairs(): Flow<List<FairEntity>> = fairDao.observeAll()

    // ───────────── Campañas ─────────────

    fun observeCampaigns(): Flow<List<CampaignEntity>> = campaignDao.observeAll()
    fun observeCampaignRefs(): Flow<List<CampaignProductRef>> = campaignDao.observeRefs()
    suspend fun findCampaign(id: Long): CampaignEntity? = campaignDao.find(id)
    suspend fun campaignProductIds(id: Long): List<Long> = campaignDao.productIds(id)

    suspend fun saveCampaign(campaign: CampaignEntity, productIds: Collection<Long>): Long = db.withTransaction {
        val wasPublished = if (campaign.id == 0L) false else campaignDao.find(campaign.id)?.published ?: false
        val id = if (campaign.id == 0L) {
            campaignDao.insert(campaign)
        } else {
            campaignDao.update(campaign)
            campaign.id
        }
        campaignDao.clearProducts(id)
        campaignDao.insertRefs(productIds.map { CampaignProductRef(id, it) })
        if (campaign.published && !wasPublished) {
            notifyClients(
                "Nueva campaña disponible", "Descubre «${campaign.name}».", NotificationType.CAMPAIGN, id
            )
        }
        id
    }

    suspend fun setCampaignPublished(id: Long, published: Boolean) {
        val campaign = campaignDao.find(id) ?: return
        campaignDao.update(campaign.copy(published = published))
        if (published && !campaign.published) {
            notifyClients(
                "Nueva campaña disponible", "Descubre «${campaign.name}».", NotificationType.CAMPAIGN, id
            )
        }
    }

    // ───────────── Escáner QR ─────────────

    /** Registra la lectura sin duplicar: ignora el mismo código del mismo usuario en los últimos 10 s. */
    suspend fun logScan(userId: Long, code: String, found: Boolean) {
        val since = System.currentTimeMillis() - 10_000
        if (scanLogDao.countRecent(userId, code, since) > 0) return
        scanLogDao.insert(ScanLogEntity(userId = userId, code = code, found = found))
    }

    // ───────────── Datos iniciales ─────────────

    suspend fun seedIfNeeded() {
        if (userDao.count() > 0) return
        db.withTransaction {
            val adminId = userDao.insert(
                UserEntity(
                    fullName = "Lou Araya", alias = "lou.admin", email = SeedData.ADMIN_EMAIL, phone = "+56 9 8765 4321",
                    passwordHash = PasswordHasher.hash(SeedData.ADMIN_PASSWORD), avatarIndex = 1, role = Role.ADMIN
                )
            )
            val clientId = userDao.insert(
                UserEntity(
                    fullName = "Valentina Rojas", alias = "vale.rojas", email = SeedData.DEMO_EMAIL, phone = "+56 9 1234 5678",
                    address = "Los Leones 1234, Providencia",
                    passwordHash = PasswordHasher.hash(SeedData.DEMO_PASSWORD), avatarIndex = 0, role = Role.CLIENT
                )
            )

            val productIds = productDao.insertAll(SeedData.products())
            fairDao.insertAll(SeedData.fairs())

            SeedData.campaigns().forEachIndexed { i, campaign ->
                val campaignId = campaignDao.insert(campaign)
                val refs = SeedData.campaignProductIndexes[i].map { CampaignProductRef(campaignId, productIds[it - 1]) }
                campaignDao.insertRefs(refs)
            }

            favoriteDao.add(FavoriteEntity(clientId, productIds[1]))
            favoriteDao.add(FavoriteEntity(clientId, productIds[2]))

            val now = System.currentTimeMillis()
            val day = 86_400_000L
            val requestIds = requestDao.insertAll(
                listOf(
                    RequestEntity(
                        userId = clientId, clientName = "Valentina Rojas", productId = productIds[0],
                        productName = "Arco Ceremonial Aliento", eventType = "Matrimonio", widthCm = 220, heightCm = 180,
                        material = "Algodón natural", colors = "Crudo,Salvia", requiredDate = Dates.utcMillisFromNow(75),
                        notes = "Ceremonia en jardín, necesito que sea desarmable.", status = RequestStatus.CONFIRMADO,
                        createdAt = now - 6 * day
                    ),
                    RequestEntity(
                        userId = clientId, clientName = "Valentina Rojas", productId = productIds[1],
                        productName = "Tapiz Mural Sol Andino", eventType = "Hogar / Decoración", widthCm = 120, heightCm = 90,
                        material = "Algodón natural", colors = "Arena,Terracota", requiredDate = Dates.utcMillisFromNow(30),
                        notes = "Versión más grande para el comedor.", status = RequestStatus.PENDIENTE,
                        createdAt = now - 2 * day
                    ),
                    RequestEntity(
                        userId = clientId, clientName = "Valentina Rojas", productId = null, productName = null,
                        eventType = "Evento corporativo", widthCm = 300, heightCm = 200, material = "Lino",
                        colors = "Bosque,Crudo", requiredDate = Dates.utcMillisFromNow(120),
                        notes = "Backdrop para lanzamiento de marca.", status = RequestStatus.EXPLORACION,
                        createdAt = now - 20 * day
                    )
                )
            )

            notificationDao.insertAll(
                listOf(
                    NotificationEntity(
                        userId = clientId, title = "Nueva campaña disponible",
                        body = "Descubre nuestra colección Bodas de Ensueño.", type = NotificationType.CAMPAIGN,
                        createdAt = now - 2 * 3_600_000L
                    ),
                    NotificationEntity(
                        userId = clientId, title = "Tu solicitud fue confirmada",
                        body = "${Formatters.requestCode(requestIds[0])} · Arco Ceremonial Aliento fue confirmado.",
                        type = NotificationType.REQUEST, refId = requestIds[0], createdAt = now - 5 * 3_600_000L
                    ),
                    NotificationEntity(
                        userId = clientId, title = "Feria este fin de semana",
                        body = "Te esperamos en el Parque Bicentenario, Vitacura.", type = NotificationType.FAIR,
                        read = true, createdAt = now - day
                    ),
                    NotificationEntity(
                        userId = clientId, title = "Nuevo producto en catálogo",
                        body = "Conoce el Colgante de Plantas Quillay.", type = NotificationType.CATALOG,
                        refId = productIds[2], read = true, createdAt = now - 2 * day
                    )
                )
            )
            notificationDao.insert(
                NotificationEntity(
                    userId = adminId, title = "Nueva solicitud",
                    body = "vale.rojas envió la solicitud ${Formatters.requestCode(requestIds[1])}.",
                    type = NotificationType.REQUEST, refId = requestIds[1], createdAt = now - 2 * day
                )
            )
        }
    }
}
