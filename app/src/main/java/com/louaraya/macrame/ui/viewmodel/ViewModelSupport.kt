package com.louaraya.macrame.ui.viewmodel

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.louaraya.macrame.AppContainer
import com.louaraya.macrame.LocalAppContainer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

/** Valor de un campo de formulario junto con su mensaje de error (null = válido). */
data class FormField(val value: String = "", val error: String? = null)

class VmFactory(private val builder: () -> ViewModel) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = builder() as T
}

/** Crea (o recupera) un ViewModel inyectándole el [AppContainer]. */
@Composable
inline fun <reified VM : ViewModel> macrameViewModel(
    key: String? = null,
    crossinline builder: (AppContainer) -> VM
): VM {
    val container = LocalAppContainer.current
    return viewModel(key = key, factory = VmFactory { builder(container) })
}

fun <T> Flow<T>.asState(scope: CoroutineScope, initial: T): StateFlow<T> =
    stateIn(scope, SharingStarted.WhileSubscribed(5_000), initial)

/** Valida solo si el usuario ya escribió algo (el campo vacío no muestra error hasta enviar). */
inline fun liveError(value: String, validator: (String) -> String?): String? =
    if (value.isEmpty()) null else validator(value)
