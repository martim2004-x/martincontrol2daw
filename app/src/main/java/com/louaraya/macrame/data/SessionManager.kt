package com.louaraya.macrame.data

import android.content.Context
import androidx.core.content.edit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Mantiene que perfil está activo en el dispositivo. Como el celular admite varios clientes,
 * el id activo se recuerda en SharedPreferences y todos los datos favoritos, solicitudes,
 * notificaciones se consultan filtrando por ese id.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SessionManager(
    context: Context,
    private val repository: MacrameRepository,
    scope: CoroutineScope
) {
    private val prefs = context.getSharedPreferences("session", Context.MODE_PRIVATE)
    private val userId = MutableStateFlow(prefs.getLong(KEY_USER_ID, NO_USER))

    /** Usuario activo se actualiza solo cuando cambia en la base de datos (ej al editar el perfil). */
    val currentUser: StateFlow<UserEntity?> = userId
        .flatMapLatest { id -> if (id == NO_USER) flowOf(null) else repository.observeUser(id) }
        .stateIn(scope, SharingStarted.Eagerly, null)

    val loggedIn: StateFlow<Boolean> = userId
        .map { it != NO_USER }
        .stateIn(scope, SharingStarted.Eagerly, userId.value != NO_USER)

    init {
        // Si el usuario guardado ya no existe (base de datos limpiada), se cierra la sesión.
        scope.launch {
            val saved = userId.value
            if (saved != NO_USER && repository.findUser(saved) == null) signOut()
        }
    }

    fun savedUserId(): Long = prefs.getLong(KEY_USER_ID, NO_USER)

    /** Rol del último usuario activo permite elegir la barra de navegación correcta sin esperar a la base de datos. */
    fun savedRole(): String? = prefs.getString(KEY_ROLE, null)

    fun signIn(user: UserEntity) {
        prefs.edit {
            putLong(KEY_USER_ID, user.id)
            putString(KEY_ROLE, user.role)
        }
        userId.value = user.id
    }

    fun signOut() {
        prefs.edit {
            remove(KEY_USER_ID)
            remove(KEY_ROLE)
        }
        userId.value = NO_USER
    }

    private companion object {
        const val KEY_USER_ID = "user_id"
        const val KEY_ROLE = "role"
        const val NO_USER = -1L
    }
}
