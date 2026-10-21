package com.louaraya.macrame.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.louaraya.macrame.data.AuthResult
import com.louaraya.macrame.data.MacrameRepository
import com.louaraya.macrame.data.RegisterInput
import com.louaraya.macrame.data.SeedData
import com.louaraya.macrame.data.SessionManager
import com.louaraya.macrame.domain.Role
import com.louaraya.macrame.domain.Validators
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
    val role: String = Role.CLIENT,
    val email: FormField = FormField(),
    val password: FormField = FormField(),
    val loading: Boolean = false,
    val error: String? = null,
    val loggedIn: Boolean = false
)

class LoginViewModel(
    private val repository: MacrameRepository,
    private val session: SessionManager
) : ViewModel() {
    private val _state = MutableStateFlow(LoginUiState())
    val state: StateFlow<LoginUiState> = _state.asStateFlow()

    fun onRole(role: String) = _state.update { it.copy(role = role, error = null) }

    fun onEmail(v: String) = _state.update {
        it.copy(email = FormField(v, liveError(v, Validators::email)), error = null)
    }

    fun onPassword(v: String) = _state.update { it.copy(password = FormField(v), error = null) }

    /** Rellena las credenciales ficticias de la demostración. */
    fun fillDemo() = _state.update {
        if (it.role == Role.ADMIN) it.copy(
            email = FormField(SeedData.ADMIN_EMAIL), password = FormField(SeedData.ADMIN_PASSWORD), error = null
        ) else it.copy(
            email = FormField(SeedData.DEMO_EMAIL), password = FormField(SeedData.DEMO_PASSWORD), error = null
        )
    }

    fun submit() {
        val s = _state.value
        if (s.loading) return
        val emailError = Validators.email(s.email.value)
        val passError = Validators.required(s.password.value, "Ingresa tu contraseña")
        if (emailError != null || passError != null) {
            _state.update {
                it.copy(email = it.email.copy(error = emailError), password = it.password.copy(error = passError))
            }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            when (val result = repository.login(s.email.value, s.password.value, s.role)) {
                is AuthResult.Success -> {
                    session.signIn(result.user)
                    _state.update { it.copy(loading = false, loggedIn = true) }
                }
                is AuthResult.Error -> _state.update {
                    when (result.field) {
                        "email" -> it.copy(loading = false, email = it.email.copy(error = result.message))
                        "password" -> it.copy(loading = false, password = it.password.copy(error = result.message))
                        else -> it.copy(loading = false, error = result.message)
                    }
                }
            }
        }
    }
}

data class RegisterUiState(
    val fullName: FormField = FormField(),
    val alias: FormField = FormField(),
    val email: FormField = FormField(),
    val phone: FormField = FormField(),
    val password: FormField = FormField(),
    val confirm: FormField = FormField(),
    val address: FormField = FormField(),
    val avatar: Int = 0,
    val accepted: Boolean = false,
    val termsError: String? = null,
    val loading: Boolean = false,
    val generalError: String? = null,
    val registered: Boolean = false
)

class RegisterViewModel(
    private val repository: MacrameRepository,
    private val session: SessionManager
) : ViewModel() {
    private val _state = MutableStateFlow(RegisterUiState())
    val state: StateFlow<RegisterUiState> = _state.asStateFlow()

    fun onFullName(v: String) = _state.update { it.copy(fullName = FormField(v, liveError(v, Validators::fullName))) }
    fun onAlias(v: String) = _state.update { it.copy(alias = FormField(v, liveError(v, Validators::alias))) }
    fun onEmail(v: String) = _state.update { it.copy(email = FormField(v, liveError(v, Validators::email))) }
    fun onPhone(v: String) = _state.update { it.copy(phone = FormField(v, liveError(v, Validators::phone))) }
    fun onAddress(v: String) = _state.update { it.copy(address = FormField(v, Validators.optionalAddress(v))) }
    fun onAvatar(index: Int) = _state.update { it.copy(avatar = index) }
    fun onAccepted(accepted: Boolean) = _state.update { it.copy(accepted = accepted, termsError = null) }

    fun onPassword(v: String) = _state.update { s ->
        val confirm = if (s.confirm.value.isEmpty()) s.confirm
        else s.confirm.copy(error = Validators.confirmPassword(v, s.confirm.value))
        s.copy(password = FormField(v, liveError(v, Validators::password)), confirm = confirm)
    }

    fun onConfirm(v: String) = _state.update { s ->
        s.copy(confirm = FormField(v, liveError(v) { Validators.confirmPassword(s.password.value, it) }))
    }

    fun submit() {
        val s = _state.value
        if (s.loading) return
        val next = s.copy(
            fullName = s.fullName.copy(error = Validators.fullName(s.fullName.value)),
            alias = s.alias.copy(error = Validators.alias(s.alias.value)),
            email = s.email.copy(error = Validators.email(s.email.value)),
            phone = s.phone.copy(error = Validators.phone(s.phone.value)),
            password = s.password.copy(error = Validators.password(s.password.value)),
            confirm = s.confirm.copy(error = Validators.confirmPassword(s.password.value, s.confirm.value)),
            address = s.address.copy(error = Validators.optionalAddress(s.address.value)),
            termsError = if (s.accepted) null else "Debes aceptar los términos para continuar"
        )
        _state.value = next
        val hasErrors = listOf(
            next.fullName, next.alias, next.email, next.phone, next.password, next.confirm, next.address
        ).any { it.error != null } || next.termsError != null
        if (hasErrors) return

        viewModelScope.launch {
            _state.update { it.copy(loading = true, generalError = null) }
            val result = repository.register(
                RegisterInput(
                    fullName = s.fullName.value, alias = s.alias.value, email = s.email.value,
                    phone = s.phone.value, password = s.password.value, address = s.address.value,
                    avatarIndex = s.avatar
                )
            )
            when (result) {
                is AuthResult.Success -> {
                    session.signIn(result.user)
                    _state.update { it.copy(loading = false, registered = true) }
                }
                is AuthResult.Error -> _state.update {
                    when (result.field) {
                        "email" -> it.copy(loading = false, email = it.email.copy(error = result.message))
                        "alias" -> it.copy(loading = false, alias = it.alias.copy(error = result.message))
                        else -> it.copy(loading = false, generalError = result.message)
                    }
                }
            }
        }
    }
}
