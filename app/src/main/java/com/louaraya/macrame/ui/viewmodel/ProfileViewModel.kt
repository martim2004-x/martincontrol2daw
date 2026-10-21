package com.louaraya.macrame.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.louaraya.macrame.data.AuthResult
import com.louaraya.macrame.data.MacrameRepository
import com.louaraya.macrame.data.SessionManager
import com.louaraya.macrame.data.UserEntity
import com.louaraya.macrame.domain.Validators
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileUiState(
    val editing: Boolean = false,
    val fullName: FormField = FormField(),
    val alias: FormField = FormField(),
    val phone: FormField = FormField(),
    val address: FormField = FormField(),
    val avatar: Int = 0,
    val saving: Boolean = false,
    val generalError: String? = null,
    /** Cambia cada vez que se guarda con éxito, para disparar el mensaje de confirmación. */
    val savedCounter: Int = 0
)

class ProfileViewModel(
    private val repository: MacrameRepository,
    private val session: SessionManager
) : ViewModel() {
    val user: StateFlow<UserEntity?> = session.currentUser

    private val _state = MutableStateFlow(ProfileUiState())
    val state: StateFlow<ProfileUiState> = _state.asStateFlow()

    fun startEditing() {
        val u = user.value ?: return
        _state.update {
            it.copy(
                editing = true, fullName = FormField(u.fullName), alias = FormField(u.alias),
                phone = FormField(u.phone), address = FormField(u.address), avatar = u.avatarIndex,
                generalError = null
            )
        }
    }

    fun cancelEditing() = _state.update { it.copy(editing = false, generalError = null) }

    fun onFullName(v: String) = _state.update { it.copy(fullName = FormField(v, Validators.fullName(v))) }
    fun onAlias(v: String) = _state.update { it.copy(alias = FormField(v, Validators.alias(v))) }
    fun onPhone(v: String) = _state.update { it.copy(phone = FormField(v, Validators.phone(v))) }
    fun onAddress(v: String) = _state.update { it.copy(address = FormField(v, Validators.optionalAddress(v))) }
    fun onAvatar(i: Int) = _state.update { it.copy(avatar = i) }

    fun save() {
        val s = _state.value
        val u = user.value ?: return
        if (s.saving) return
        val next = s.copy(
            fullName = s.fullName.copy(error = Validators.fullName(s.fullName.value)),
            alias = s.alias.copy(error = Validators.alias(s.alias.value)),
            phone = s.phone.copy(error = Validators.phone(s.phone.value)),
            address = s.address.copy(error = Validators.optionalAddress(s.address.value))
        )
        _state.value = next
        if (listOf(next.fullName, next.alias, next.phone, next.address).any { it.error != null }) return

        viewModelScope.launch {
            _state.update { it.copy(saving = true, generalError = null) }
            val result = repository.updateProfile(
                u.id, s.fullName.value, s.alias.value, s.phone.value, s.address.value, s.avatar
            )
            when (result) {
                is AuthResult.Success -> _state.update {
                    it.copy(saving = false, editing = false, savedCounter = it.savedCounter + 1)
                }
                is AuthResult.Error -> _state.update {
                    if (result.field == "alias") it.copy(saving = false, alias = it.alias.copy(error = result.message))
                    else it.copy(saving = false, generalError = result.message)
                }
            }
        }
    }

    fun setNotifications(enabled: Boolean) {
        val u = user.value ?: return
        viewModelScope.launch { repository.setNotificationsEnabled(u.id, enabled) }
    }

    fun signOut() = session.signOut()
}
