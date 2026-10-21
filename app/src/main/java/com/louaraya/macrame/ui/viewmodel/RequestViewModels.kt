@file:OptIn(ExperimentalCoroutinesApi::class)

package com.louaraya.macrame.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.louaraya.macrame.data.MacrameRepository
import com.louaraya.macrame.data.NotificationEntity
import com.louaraya.macrame.data.ProductEntity
import com.louaraya.macrame.data.RequestEntity
import com.louaraya.macrame.data.RequestInput
import com.louaraya.macrame.data.SessionManager
import com.louaraya.macrame.domain.NotificationType
import com.louaraya.macrame.domain.Role
import com.louaraya.macrame.domain.Validators
import com.louaraya.macrame.util.Formatters
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ───────────── Formulario de solicitud / cotización ─────────────

data class RequestFormState(
    val product: ProductEntity? = null,
    val eventType: FormField = FormField(),
    val width: FormField = FormField(),
    val height: FormField = FormField(),
    val material: FormField = FormField(),
    val colors: List<String> = emptyList(),
    val colorsError: String? = null,
    val dateMillis: Long? = null,
    val dateError: String? = null,
    val notes: FormField = FormField(),
    val submitting: Boolean = false,
    val createdCode: String? = null,
    val generalError: String? = null
)

class RequestFormViewModel(
    productId: Long?,
    private val repository: MacrameRepository,
    private val session: SessionManager
) : ViewModel() {
    private val _state = MutableStateFlow(RequestFormState())
    val state: StateFlow<RequestFormState> = _state.asStateFlow()

    init {
        if (productId != null) {
            viewModelScope.launch {
                repository.findProduct(productId)?.let { product ->
                    _state.update { it.copy(product = product, material = FormField(product.material)) }
                }
            }
        }
    }

    fun onEventType(v: String) = _state.update { it.copy(eventType = FormField(v)) }
    fun onMaterial(v: String) = _state.update { it.copy(material = FormField(v)) }

    fun onWidth(v: String) {
        val clean = v.filter { it.isDigit() }.take(3)
        _state.update { it.copy(width = FormField(clean, liveError(clean) { x -> Validators.dimension(x, "el ancho") })) }
    }

    fun onHeight(v: String) {
        val clean = v.filter { it.isDigit() }.take(3)
        _state.update { it.copy(height = FormField(clean, liveError(clean) { x -> Validators.dimension(x, "el alto") })) }
    }

    fun onNotes(v: String) = _state.update { it.copy(notes = FormField(v, Validators.notes(v))) }

    fun onDate(millis: Long) = _state.update { it.copy(dateMillis = millis, dateError = Validators.requiredDate(millis)) }

    fun toggleColor(name: String) = _state.update { s ->
        val updated = if (name in s.colors) s.colors - name else s.colors + name
        s.copy(colors = updated, colorsError = if (updated.isEmpty()) null else Validators.colors(updated))
    }

    fun submit() {
        val s = _state.value
        if (s.submitting) return
        val next = s.copy(
            eventType = s.eventType.copy(error = Validators.choice(s.eventType.value, "el tipo de evento")),
            width = s.width.copy(error = Validators.dimension(s.width.value, "el ancho")),
            height = s.height.copy(error = Validators.dimension(s.height.value, "el alto")),
            material = s.material.copy(error = Validators.choice(s.material.value, "un material")),
            colorsError = Validators.colors(s.colors),
            dateError = Validators.requiredDate(s.dateMillis),
            notes = s.notes.copy(error = Validators.notes(s.notes.value))
        )
        _state.value = next
        val hasErrors = listOf(next.eventType, next.width, next.height, next.material, next.notes).any { it.error != null } ||
            next.colorsError != null || next.dateError != null
        if (hasErrors) return

        val user = session.currentUser.value
        if (user == null) {
            _state.update { it.copy(generalError = "Tu sesión expiró. Vuelve a iniciar sesión.") }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(submitting = true, generalError = null) }
            val id = repository.createRequest(
                user,
                RequestInput(
                    product = s.product,
                    eventType = s.eventType.value,
                    widthCm = s.width.value.toInt(),
                    heightCm = s.height.value.toInt(),
                    material = s.material.value,
                    colors = s.colors,
                    requiredDate = s.dateMillis ?: 0L,
                    notes = s.notes.value
                )
            )
            _state.update { it.copy(submitting = false, createdCode = Formatters.requestCode(id)) }
        }
    }
}

// ───────────── Historial y gestión de solicitudes ─────────────

data class RequestsUiState(
    val requests: List<RequestEntity> = emptyList(),
    val total: Int = 0,
    val filter: String? = null,
    val isAdmin: Boolean = false,
    val loading: Boolean = true
)

class RequestsViewModel(
    private val repository: MacrameRepository,
    private val session: SessionManager
) : ViewModel() {
    private val filter = MutableStateFlow<String?>(null)

    val state: StateFlow<RequestsUiState> = combine(
        session.currentUser.flatMapLatest { u ->
            when {
                u == null -> flowOf(emptyList())
                u.role == Role.ADMIN -> repository.observeAllRequests()
                else -> repository.observeRequestsByUser(u.id)
            }
        },
        filter,
        session.currentUser
    ) { requests, status, user ->
        RequestsUiState(
            requests = if (status == null) requests else requests.filter { it.status == status },
            total = requests.size,
            filter = status,
            isAdmin = user?.role == Role.ADMIN,
            loading = false
        )
    }.asState(viewModelScope, RequestsUiState())

    fun onFilter(status: String?) = filter.update { if (it == status) null else status }

    fun updateStatus(requestId: Long, status: String) {
        viewModelScope.launch { repository.updateRequestStatus(requestId, status) }
    }
}

// ───────────── Notificaciones ─────────────

/** Agrupa los tipos de notificación en las pestañas visibles. */
enum class NotificationGroup(val label: String, val types: Set<String>) {
    CAMPAIGNS("Campañas", setOf(NotificationType.CAMPAIGN, NotificationType.CATALOG)),
    REQUESTS("Solicitudes", setOf(NotificationType.REQUEST)),
    EVENTS("Eventos", setOf(NotificationType.FAIR))
}

data class NotificationsUiState(
    val items: List<NotificationEntity> = emptyList(),
    val group: NotificationGroup? = null,
    val unread: Int = 0
)

class NotificationsViewModel(
    private val repository: MacrameRepository,
    private val session: SessionManager
) : ViewModel() {
    private val group = MutableStateFlow<NotificationGroup?>(null)

    val state: StateFlow<NotificationsUiState> = combine(
        session.currentUser.flatMapLatest { u ->
            if (u == null) flowOf(emptyList()) else repository.observeNotifications(u.id)
        },
        group
    ) { items, g ->
        NotificationsUiState(
            items = if (g == null) items else items.filter { it.type in g.types },
            group = g,
            unread = items.count { !it.read }
        )
    }.asState(viewModelScope, NotificationsUiState())

    fun onGroup(value: NotificationGroup?) = group.update { if (it == value) null else value }

    fun markAllRead() {
        val user = session.currentUser.value ?: return
        viewModelScope.launch { repository.markAllNotificationsRead(user.id) }
    }

    fun markRead(id: Long) {
        viewModelScope.launch { repository.markNotificationRead(id) }
    }
}
