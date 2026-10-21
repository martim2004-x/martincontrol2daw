@file:OptIn(ExperimentalCoroutinesApi::class)

package com.louaraya.macrame.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.louaraya.macrame.data.CampaignEntity
import com.louaraya.macrame.data.MacrameRepository
import com.louaraya.macrame.data.ProductEntity
import com.louaraya.macrame.data.RequestEntity
import com.louaraya.macrame.data.SessionManager
import com.louaraya.macrame.data.UserEntity
import com.louaraya.macrame.domain.CampaignPhase
import com.louaraya.macrame.domain.RequestStatus
import com.louaraya.macrame.domain.Role
import com.louaraya.macrame.domain.phase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Ids de los productos favoritos del perfil activo (vacío si no hay sesión). */
private fun favoriteIds(repository: MacrameRepository, session: SessionManager): Flow<Set<Long>> =
    session.currentUser.flatMapLatest { user ->
        if (user == null) flowOf(emptySet())
        else repository.observeFavoriteIds(user.id).map { it.toSet() }
    }

// ───────────── Inicio / Dashboard ─────────────

data class FeaturedCollection(val campaign: CampaignEntity, val products: List<ProductEntity>)

data class AdminStats(
    val pending: Int = 0,
    val activeProducts: Int = 0,
    val activeCampaigns: Int = 0,
    val pendingRequests: List<RequestEntity> = emptyList()
)

class HomeViewModel(
    repository: MacrameRepository,
    session: SessionManager
) : ViewModel() {
    val user: StateFlow<UserEntity?> = session.currentUser

    val unread: StateFlow<Int> = session.currentUser
        .flatMapLatest { u -> if (u == null) flowOf(0) else repository.observeUnreadCount(u.id) }
        .asState(viewModelScope, 0)

    val favoritesCount: StateFlow<Int> = favoriteIds(repository, session)
        .map { it.size }
        .asState(viewModelScope, 0)

    val products: StateFlow<List<ProductEntity>> = repository.observeActiveProducts()
        .asState(viewModelScope, emptyList())

    /** Primera campaña activa con sus productos: es la "colección destacada" del cliente. */
    val featured: StateFlow<FeaturedCollection?> = combine(
        repository.observeCampaigns(),
        repository.observeCampaignRefs(),
        repository.observeActiveProducts()
    ) { campaigns, refs, products ->
        val active = campaigns.firstOrNull { it.phase() == CampaignPhase.ACTIVA }
        if (active == null) {
            null
        } else {
            val ids = refs.filter { it.campaignId == active.id }.map { it.productId }.toSet()
            FeaturedCollection(active, products.filter { it.id in ids })
        }
    }.asState(viewModelScope, null)

    val adminStats: StateFlow<AdminStats> = combine(
        repository.observeAllRequests(),
        repository.observeAllProducts(),
        repository.observeCampaigns()
    ) { requests, products, campaigns ->
        val pending = requests.filter { it.status == RequestStatus.PENDIENTE }
        AdminStats(
            pending = pending.size,
            activeProducts = products.count { it.active },
            activeCampaigns = campaigns.count { it.phase() == CampaignPhase.ACTIVA },
            pendingRequests = pending.take(3)
        )
    }.asState(viewModelScope, AdminStats())
}

// ───────────── Catálogo ─────────────

data class CatalogUiState(
    val products: List<ProductEntity> = emptyList(),
    val favorites: Set<Long> = emptySet(),
    val query: String = "",
    val category: String? = null,
    val isAdmin: Boolean = false,
    val loading: Boolean = true
)

class CatalogViewModel(
    private val repository: MacrameRepository,
    private val session: SessionManager
) : ViewModel() {
    private val query = MutableStateFlow("")
    private val category = MutableStateFlow<String?>(null)

    val state: StateFlow<CatalogUiState> = combine(
        repository.observeActiveProducts(),
        favoriteIds(repository, session),
        query,
        category,
        session.currentUser
    ) { products, favorites, q, cat, user ->
        val term = q.trim()
        val filtered = products.filter { p ->
            (cat == null || p.category == cat) &&
                (term.isEmpty() ||
                    p.name.contains(term, ignoreCase = true) ||
                    p.material.contains(term, ignoreCase = true) ||
                    p.code.contains(term, ignoreCase = true))
        }
        CatalogUiState(filtered, favorites, q, cat, user?.role == Role.ADMIN, loading = false)
    }.asState(viewModelScope, CatalogUiState())

    fun onQuery(value: String) {
        query.value = value
    }

    /** Tocar la categoría activa la desactiva. */
    fun onCategory(value: String?) = category.update { if (it == value) null else value }

    fun toggleFavorite(productId: Long) {
        val user = session.currentUser.value ?: return
        viewModelScope.launch { repository.toggleFavorite(user.id, productId) }
    }
}

// ───────────── Detalle de producto ─────────────

data class DetailUiState(
    val product: ProductEntity? = null,
    val isFavorite: Boolean = false,
    val isAdmin: Boolean = false,
    val loading: Boolean = true
)

class ProductDetailViewModel(
    productId: Long,
    private val repository: MacrameRepository,
    private val session: SessionManager
) : ViewModel() {
    val state: StateFlow<DetailUiState> = combine(
        repository.observeProduct(productId),
        favoriteIds(repository, session),
        session.currentUser
    ) { product, favorites, user ->
        DetailUiState(
            product = product,
            isFavorite = product != null && product.id in favorites,
            isAdmin = user?.role == Role.ADMIN,
            loading = false
        )
    }.asState(viewModelScope, DetailUiState())

    fun toggleFavorite() {
        val product = state.value.product ?: return
        val user = session.currentUser.value ?: return
        viewModelScope.launch { repository.toggleFavorite(user.id, product.id) }
    }
}

// ───────────── Favoritos ─────────────

data class FavoritesUiState(
    val products: List<ProductEntity> = emptyList(),
    val category: String? = null,
    val total: Int = 0
)

class FavoritesViewModel(
    private val repository: MacrameRepository,
    private val session: SessionManager
) : ViewModel() {
    private val category = MutableStateFlow<String?>(null)

    val state: StateFlow<FavoritesUiState> = combine(
        session.currentUser.flatMapLatest { u ->
            if (u == null) flowOf(emptyList()) else repository.observeFavoriteProducts(u.id)
        },
        category
    ) { products, cat ->
        FavoritesUiState(
            products = if (cat == null) products else products.filter { it.category == cat },
            category = cat,
            total = products.size
        )
    }.asState(viewModelScope, FavoritesUiState())

    fun onCategory(value: String?) = category.update { if (it == value) null else value }

    fun remove(productId: Long) {
        val user = session.currentUser.value ?: return
        viewModelScope.launch { repository.toggleFavorite(user.id, productId) }
    }
}
