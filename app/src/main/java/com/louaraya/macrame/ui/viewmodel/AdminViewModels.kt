package com.louaraya.macrame.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.louaraya.macrame.data.CampaignEntity
import com.louaraya.macrame.data.MacrameRepository
import com.louaraya.macrame.data.ProductEntity
import com.louaraya.macrame.domain.CampaignPhase
import com.louaraya.macrame.domain.Validators
import com.louaraya.macrame.domain.phase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ───────────── Gestión de productos ─────────────

enum class ProductFilter(val label: String) { ALL("Todos"), ACTIVE("Activos"), INACTIVE("Inactivos") }

data class AdminProductsUiState(
    val products: List<ProductEntity> = emptyList(),
    val query: String = "",
    val filter: ProductFilter = ProductFilter.ALL,
    val total: Int = 0
)

class AdminProductsViewModel(private val repository: MacrameRepository) : ViewModel() {
    private val query = MutableStateFlow("")
    private val filter = MutableStateFlow(ProductFilter.ALL)

    val state: StateFlow<AdminProductsUiState> = combine(
        repository.observeAllProducts(), query, filter
    ) { products, q, f ->
        val term = q.trim()
        val shown = products.filter { p ->
            val byState = when (f) {
                ProductFilter.ALL -> true
                ProductFilter.ACTIVE -> p.active
                ProductFilter.INACTIVE -> !p.active
            }
            byState && (term.isEmpty() || p.name.contains(term, true) || p.code.contains(term, true))
        }
        AdminProductsUiState(shown, q, f, products.size)
    }.asState(viewModelScope, AdminProductsUiState())

    fun onQuery(v: String) {
        query.value = v
    }

    fun onFilter(f: ProductFilter) {
        filter.value = f
    }

    fun setActive(productId: Long, active: Boolean) {
        viewModelScope.launch { repository.setProductActive(productId, active) }
    }
}

data class ProductFormState(
    val loading: Boolean = false,
    val isNew: Boolean = true,
    val name: FormField = FormField(),
    val category: FormField = FormField(),
    val width: FormField = FormField(),
    val height: FormField = FormField(),
    val material: FormField = FormField(),
    val price: FormField = FormField(),
    val stock: FormField = FormField("1"),
    val description: FormField = FormField(),
    val madeToOrder: Boolean = false,
    val active: Boolean = true,
    val photoUri: String? = null,
    val saving: Boolean = false,
    val saved: Boolean = false,
    val generalError: String? = null
)

/** [productId] = 0 crea un producto nuevo; cualquier otro valor edita el existente. */
class ProductFormViewModel(
    private val productId: Long,
    private val repository: MacrameRepository
) : ViewModel() {
    private val _state = MutableStateFlow(ProductFormState(loading = productId != 0L, isNew = productId == 0L))
    val state: StateFlow<ProductFormState> = _state.asStateFlow()
    private var original: ProductEntity? = null

    init {
        if (productId != 0L) {
            viewModelScope.launch {
                val p = repository.findProduct(productId)
                if (p == null) {
                    _state.update { it.copy(loading = false, generalError = "No se encontró el producto") }
                } else {
                    original = p
                    _state.update {
                        it.copy(
                            loading = false, isNew = false,
                            name = FormField(p.name), category = FormField(p.category),
                            width = FormField(p.widthCm.toString()), height = FormField(p.heightCm.toString()),
                            material = FormField(p.material), price = FormField(p.priceClp.toString()),
                            stock = FormField(p.stock.toString()), description = FormField(p.description),
                            madeToOrder = p.madeToOrder, active = p.active, photoUri = p.imageUri
                        )
                    }
                }
            }
        }
    }

    fun onName(v: String) = _state.update { it.copy(name = FormField(v, Validators.productName(v))) }
    fun onCategory(v: String) = _state.update { it.copy(category = FormField(v)) }
    fun onMaterial(v: String) = _state.update { it.copy(material = FormField(v)) }
    fun onDescription(v: String) = _state.update { it.copy(description = FormField(v, Validators.description(v))) }
    fun onMadeToOrder(v: Boolean) = _state.update { it.copy(madeToOrder = v) }
    fun onActive(v: Boolean) = _state.update { it.copy(active = v) }
    fun onPhoto(uri: String?) = _state.update { it.copy(photoUri = uri) }

    fun onWidth(v: String) {
        val c = v.filter { it.isDigit() }.take(3)
        _state.update { it.copy(width = FormField(c, liveError(c) { x -> Validators.dimension(x, "el ancho") })) }
    }

    fun onHeight(v: String) {
        val c = v.filter { it.isDigit() }.take(3)
        _state.update { it.copy(height = FormField(c, liveError(c) { x -> Validators.dimension(x, "el alto") })) }
    }

    fun onPrice(v: String) {
        val c = v.filter { it.isDigit() }.take(7)
        _state.update { it.copy(price = FormField(c, liveError(c, Validators::price))) }
    }

    fun onStock(v: String) {
        val c = v.filter { it.isDigit() }.take(3)
        _state.update { it.copy(stock = FormField(c, liveError(c, Validators::stock))) }
    }

    fun save() {
        val s = _state.value
        if (s.saving) return
        val next = s.copy(
            name = s.name.copy(error = Validators.productName(s.name.value)),
            category = s.category.copy(error = Validators.choice(s.category.value, "una categoría")),
            width = s.width.copy(error = Validators.dimension(s.width.value, "el ancho")),
            height = s.height.copy(error = Validators.dimension(s.height.value, "el alto")),
            material = s.material.copy(error = Validators.choice(s.material.value, "un material")),
            price = s.price.copy(error = Validators.price(s.price.value)),
            stock = s.stock.copy(error = Validators.stock(s.stock.value)),
            description = s.description.copy(error = Validators.description(s.description.value))
        )
        _state.value = next
        val fields = listOf(
            next.name, next.category, next.width, next.height, next.material, next.price, next.stock, next.description
        )
        if (fields.any { it.error != null }) return

        viewModelScope.launch {
            _state.update { it.copy(saving = true, generalError = null) }
            val base = original
            val entity = ProductEntity(
                id = base?.id ?: 0L,
                code = base?.code ?: "",
                name = s.name.value.trim(),
                description = s.description.value.trim(),
                category = s.category.value,
                widthCm = s.width.value.toInt(),
                heightCm = s.height.value.toInt(),
                material = s.material.value,
                priceClp = s.price.value.toInt(),
                stock = if (s.madeToOrder) 0 else s.stock.value.toInt(),
                madeToOrder = s.madeToOrder,
                active = s.active,
                imageUri = s.photoUri,
                createdAt = base?.createdAt ?: System.currentTimeMillis()
            )
            repository.saveProduct(entity)
            _state.update { it.copy(saving = false, saved = true) }
        }
    }
}

// ───────────── Colecciones y campañas ─────────────

data class CampaignRow(val campaign: CampaignEntity, val productCount: Int, val phase: CampaignPhase)

class CampaignsViewModel(private val repository: MacrameRepository) : ViewModel() {
    val rows: StateFlow<List<CampaignRow>> = combine(
        repository.observeCampaigns(), repository.observeCampaignRefs()
    ) { campaigns, refs ->
        campaigns.map { c -> CampaignRow(c, refs.count { it.campaignId == c.id }, c.phase()) }
    }.asState(viewModelScope, emptyList())

    fun setPublished(campaignId: Long, published: Boolean) {
        viewModelScope.launch { repository.setCampaignPublished(campaignId, published) }
    }
}

data class CampaignFormState(
    val loading: Boolean = false,
    val isNew: Boolean = true,
    val name: FormField = FormField(),
    val description: FormField = FormField(),
    val theme: FormField = FormField(),
    val startDate: Long? = null,
    val startError: String? = null,
    val endDate: Long? = null,
    val endError: String? = null,
    val selected: Set<Long> = emptySet(),
    val productsError: String? = null,
    val published: Boolean = true,
    val saving: Boolean = false,
    val saved: Boolean = false
)

class CampaignFormViewModel(
    private val campaignId: Long,
    private val repository: MacrameRepository
) : ViewModel() {
    private val _state = MutableStateFlow(CampaignFormState(loading = campaignId != 0L, isNew = campaignId == 0L))
    val state: StateFlow<CampaignFormState> = _state.asStateFlow()
    private var original: CampaignEntity? = null

    val products: StateFlow<List<ProductEntity>> = repository.observeActiveProducts()
        .asState(viewModelScope, emptyList())

    init {
        if (campaignId != 0L) {
            viewModelScope.launch {
                val c = repository.findCampaign(campaignId)
                if (c != null) {
                    original = c
                    val ids = repository.campaignProductIds(campaignId).toSet()
                    _state.update {
                        it.copy(
                            loading = false, isNew = false, name = FormField(c.name),
                            description = FormField(c.description), theme = FormField(c.theme),
                            startDate = c.startDate, endDate = c.endDate, selected = ids, published = c.published
                        )
                    }
                } else {
                    _state.update { it.copy(loading = false) }
                }
            }
        }
    }

    fun onName(v: String) = _state.update { it.copy(name = FormField(v, Validators.campaignName(v))) }
    fun onDescription(v: String) = _state.update { it.copy(description = FormField(v, Validators.campaignDescription(v))) }
    fun onTheme(v: String) = _state.update { it.copy(theme = FormField(v)) }
    fun onPublished(v: Boolean) = _state.update { it.copy(published = v) }

    fun onStart(millis: Long) = _state.update {
        it.copy(
            startDate = millis, startError = null,
            endError = if (it.endDate == null) null else Validators.endDate(millis, it.endDate)
        )
    }

    fun onEnd(millis: Long) = _state.update { it.copy(endDate = millis, endError = Validators.endDate(it.startDate, millis)) }

    fun toggleProduct(id: Long) = _state.update {
        val updated = if (id in it.selected) it.selected - id else it.selected + id
        it.copy(selected = updated, productsError = if (updated.isEmpty()) null else Validators.campaignProducts(updated))
    }

    fun save() {
        val s = _state.value
        if (s.saving) return
        val next = s.copy(
            name = s.name.copy(error = Validators.campaignName(s.name.value)),
            description = s.description.copy(error = Validators.campaignDescription(s.description.value)),
            theme = s.theme.copy(error = Validators.choice(s.theme.value, "un tema")),
            startError = Validators.startDate(s.startDate),
            endError = Validators.endDate(s.startDate, s.endDate),
            productsError = Validators.campaignProducts(s.selected)
        )
        _state.value = next
        val hasErrors = listOf(next.name, next.description, next.theme).any { it.error != null } ||
            next.startError != null || next.endError != null || next.productsError != null
        if (hasErrors) return

        viewModelScope.launch {
            _state.update { it.copy(saving = true) }
            val base = original
            repository.saveCampaign(
                CampaignEntity(
                    id = base?.id ?: 0L,
                    name = s.name.value.trim(),
                    description = s.description.value.trim(),
                    theme = s.theme.value,
                    startDate = s.startDate ?: 0L,
                    endDate = s.endDate ?: 0L,
                    published = s.published,
                    createdAt = base?.createdAt ?: System.currentTimeMillis()
                ),
                s.selected
            )
            _state.update { it.copy(saving = false, saved = true) }
        }
    }
}
