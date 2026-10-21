package com.louaraya.macrame.ui.viewmodel

import android.os.SystemClock
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.louaraya.macrame.data.FairEntity
import com.louaraya.macrame.data.MacrameRepository
import com.louaraya.macrame.data.ProductEntity
import com.louaraya.macrame.data.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ───────────── Mapa de ferias y showrooms ─────────────

data class MapUiState(
    val fairs: List<FairEntity> = emptyList(),
    val typeFilter: String? = null,
    val selected: FairEntity? = null
)

class MapViewModel(repository: MacrameRepository) : ViewModel() {
    private val typeFilter = MutableStateFlow<String?>(null)
    private val selectedId = MutableStateFlow<Long?>(null)

    val state: StateFlow<MapUiState> = combine(
        repository.observeFairs(), typeFilter, selectedId
    ) { fairs, type, selected ->
        val shown = if (type == null) fairs else fairs.filter { it.type == type }
        MapUiState(shown, type, shown.firstOrNull { it.id == selected })
    }.asState(viewModelScope, MapUiState())

    fun onTypeFilter(type: String?) = typeFilter.update { if (it == type) null else type }
    fun select(id: Long?) {
        selectedId.value = id
    }
}

// ───────────── Escáner QR ─────────────

sealed interface ScanOutcome {
    data class Found(val product: ProductEntity) : ScanOutcome
    data class NotFound(val scanned: String) : ScanOutcome
}

class ScannerViewModel(
    private val repository: MacrameRepository,
    private val session: SessionManager
) : ViewModel() {
    private val _outcome = MutableStateFlow<ScanOutcome?>(null)
    val outcome: StateFlow<ScanOutcome?> = _outcome

    private var lastCode: String? = null
    private var lastAt = 0L

    /**
     * Recibe el texto de un QR (o un código escrito a mano). Ignora lecturas repetidas del mismo código
     * durante 3 segundos y mientras haya un resultado sin atender, para no duplicar operaciones.
     */
    fun onCode(raw: String) {
        val text = raw.trim()
        if (text.isEmpty() || _outcome.value != null) return
        val now = SystemClock.elapsedRealtime()
        if (text == lastCode && now - lastAt < 3_000) return
        lastCode = text
        lastAt = now

        viewModelScope.launch {
            val code = extractProductCode(text)
            val product = code?.let { repository.findProductByCode(it) }
            val found = product != null && product.active
            repository.logScan(session.currentUser.value?.id ?: 0L, text.take(60), found)
            _outcome.value = if (found && product != null) ScanOutcome.Found(product)
            else ScanOutcome.NotFound(text.take(40))
        }
    }

    fun consume() {
        _outcome.value = null
    }

    companion object {
        private val codeRegex = Regex("LAM-\\d{4}", RegexOption.IGNORE_CASE)

        /** Acepta "LAM-0001" o una URL que lo contenga (ej. https://louaraya.cl/p/LAM-0001). */
        fun extractProductCode(raw: String): String? = codeRegex.find(raw)?.value?.uppercase()
    }
}
