package com.louaraya.macrame.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Celebration
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Face
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material.icons.rounded.Badge
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material.icons.rounded.Straighten
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.louaraya.macrame.data.SeedData
import com.louaraya.macrame.domain.Category
import com.louaraya.macrame.domain.Role
import com.louaraya.macrame.ui.components.AppTextField
import com.louaraya.macrame.ui.components.Avatar
import com.louaraya.macrame.ui.components.AvatarCount
import com.louaraya.macrame.ui.components.InlineError
import com.louaraya.macrame.ui.components.PrimaryButton
import com.louaraya.macrame.ui.components.ProductArt
import com.louaraya.macrame.ui.components.ScreenHeader
import com.louaraya.macrame.ui.theme.MacrameColors
import com.louaraya.macrame.ui.viewmodel.LoginViewModel
import com.louaraya.macrame.ui.viewmodel.RegisterViewModel
import com.louaraya.macrame.ui.viewmodel.macrameViewModel

// ───────────── 1. inicio ─────────────

@Composable
fun WelcomeScreen(onStart: () -> Unit, onRegister: () -> Unit) {
    val visible = remember { MutableTransitionState(false).apply { targetState = true } }
    val breathing = rememberInfiniteTransition(label = "breathing")
    val scale by breathing.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(tween(3600, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "heroScale"
    )
    val features = listOf(
        Icons.Rounded.Spa to "Diseños artesanales únicos",
        Icons.Rounded.Celebration to "Para hogares, eventos y matrimonios",
        Icons.Rounded.LocationOn to "Encuéntranos en ferias y showrooms",
        Icons.Rounded.Straighten to "Cotiza piezas a tu medida"
    )

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        AnimatedVisibility(
            visibleState = visible,
            enter = fadeIn(tween(700)) + slideInVertically(tween(700)) { it / 10 }
        ) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .clip(RoundedCornerShape(36.dp))
            ) {
                ProductArt(
                    Category.MATRIMONIOS, 2,
                    Modifier.fillMaxSize().graphicsLayer { scaleX = scale; scaleY = scale }
                )
                Surface(
                    modifier = Modifier.align(Alignment.TopStart).padding(16.dp),
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.9f)
                ) {
                    Row(
                        Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Rounded.Spa, contentDescription = null, tint = MacrameColors.Forest, modifier = Modifier.size(16.dp))
                        Text("Hecho a mano en Chile", style = MaterialTheme.typography.labelMedium, color = MacrameColors.Forest)
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))
        AnimatedVisibility(
            visibleState = visible,
            enter = fadeIn(tween(600, delayMillis = 200)) + slideInVertically(tween(600, delayMillis = 200)) { it / 8 }
        ) {
            Column {
                Text("Lou Araya\nMacramé", style = MaterialTheme.typography.displaySmall, color = MacrameColors.Ink)
                Spacer(Modifier.height(8.dp))
                Text(
                    "Fibras, nudos e historias para tus espacios.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MacrameColors.Muted
                )
            }
        }

        Spacer(Modifier.height(24.dp))
        features.forEachIndexed { index, (icon, text) ->
            AnimatedVisibility(
                visibleState = visible,
                enter = fadeIn(tween(500, delayMillis = 350 + index * 120)) +
                        slideInHorizontally(tween(500, delayMillis = 350 + index * 120)) { -it / 5 }
            ) {
                Row(
                    Modifier.padding(vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        Modifier.size(38.dp).clip(CircleShape).background(MacrameColors.SageTint),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(icon, contentDescription = null, tint = MacrameColors.Forest, modifier = Modifier.size(20.dp))
                    }
                    Text(text, style = MaterialTheme.typography.bodyLarge, color = MacrameColors.Ink)
                }
            }
        }

        Spacer(Modifier.height(28.dp))
        PrimaryButton("Comenzar", onStart)
        Spacer(Modifier.height(4.dp))
        TextButton(onClick = onRegister, modifier = Modifier.fillMaxWidth()) {
            Text("¿Primera vez? Crea tu cuenta", color = MacrameColors.Forest, style = MaterialTheme.typography.labelLarge)
        }
    }
}

// ───────────── 3. inicio sesion ─────────────

@Composable
fun LoginScreen(onLoggedIn: () -> Unit, onRegister: () -> Unit, onBack: () -> Unit) {
    val vm = macrameViewModel { LoginViewModel(it.repository, it.session) }
    val state by vm.state.collectAsStateWithLifecycle()
    val focus = LocalFocusManager.current

    LaunchedEffect(state.loggedIn) {
        if (state.loggedIn) onLoggedIn()
    }

    Column(
        Modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
    ) {
        ScreenHeader(title = "Inicia sesión", subtitle = "Qué bueno verte de nuevo", onBack = onBack)
        Column(
            Modifier.padding(horizontal = 20.dp).padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            RoleSwitch(selected = state.role, onSelect = vm::onRole)
            AppTextField(
                value = state.email.value,
                onValueChange = vm::onEmail,
                label = "Correo electrónico",
                leadingIcon = Icons.Rounded.Email,
                error = state.email.error,
                keyboardType = KeyboardType.Email,
                showValid = true
            )
            AppTextField(
                value = state.password.value,
                onValueChange = vm::onPassword,
                label = "Contraseña",
                leadingIcon = Icons.Rounded.Lock,
                error = state.password.error,
                isPassword = true,
                imeAction = ImeAction.Done,
                keyboardActions = KeyboardActions(onDone = {
                    focus.clearFocus()
                    vm.submit()
                })
            )
            InlineError(state.error)
            PrimaryButton("Iniciar sesión", onClick = { focus.clearFocus(); vm.submit() }, loading = state.loading)

            Surface(color = MacrameColors.SageTint, shape = RoundedCornerShape(16.dp)) {
                Row(
                    Modifier.fillMaxWidth().padding(start = 14.dp, top = 8.dp, bottom = 8.dp, end = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(Icons.Rounded.Info, contentDescription = null, tint = MacrameColors.Forest, modifier = Modifier.size(20.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Cuenta de demostración", style = MaterialTheme.typography.labelLarge, color = MacrameColors.Forest)
                        val demo = if (state.role == Role.ADMIN) "${SeedData.ADMIN_EMAIL} · ${SeedData.ADMIN_PASSWORD}"
                        else "${SeedData.DEMO_EMAIL} · ${SeedData.DEMO_PASSWORD}"
                        Text(demo, style = MaterialTheme.typography.bodySmall, color = MacrameColors.InactiveText)
                    }
                    TextButton(onClick = vm::fillDemo) {
                        Text("Usar", color = MacrameColors.Forest, style = MaterialTheme.typography.labelLarge)
                    }
                }
            }

            TextButton(onClick = onRegister, modifier = Modifier.fillMaxWidth()) {
                Text("¿No tienes cuenta? Regístrate", color = MacrameColors.Forest, style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

/** seleccionar Cliente Administrador. */
@Composable
private fun RoleSwitch(selected: String, onSelect: (String) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(MacrameColors.Inactive)
            .padding(4.dp)
    ) {
        listOf(Role.CLIENT to "Cliente", Role.ADMIN to "Administrador").forEach { (role, label) ->
            val isSelected = role == selected
            val bg by animateColorAsState(if (isSelected) MacrameColors.Forest else Color.Transparent, tween(220), label = "roleBg")
            val fg by animateColorAsState(if (isSelected) Color.White else MacrameColors.InactiveText, tween(220), label = "roleFg")
            Box(
                Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .background(bg)
                    .clickable { onSelect(role) }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(label, color = fg, style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

// ───────────── 2. registro clientes ─────────────

@Composable
fun RegisterScreen(onRegistered: () -> Unit, onBack: () -> Unit) {
    val vm = macrameViewModel { RegisterViewModel(it.repository, it.session) }
    val state by vm.state.collectAsStateWithLifecycle()
    val focus = LocalFocusManager.current

    LaunchedEffect(state.registered) {
        if (state.registered) onRegistered()
    }

    Column(
        Modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
    ) {
        ScreenHeader(title = "Crea tu cuenta", subtitle = "Únete a la comunidad Lou Araya", onBack = onBack)
        Column(
            Modifier.padding(horizontal = 20.dp).padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Elige tu avatar", style = MaterialTheme.typography.titleSmall, color = MacrameColors.Ink)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp), contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 6.dp, horizontal = 4.dp)) {
                itemsIndexed(List(AvatarCount) { it }) { _, index ->
                    Avatar(
                        index = index, size = 60.dp, selected = state.avatar == index,
                        modifier = Modifier.clip(CircleShape).clickable { vm.onAvatar(index) }
                    )
                }
            }

            AppTextField(
                value = state.fullName.value, onValueChange = vm::onFullName, label = "Nombre completo",
                leadingIcon = Icons.Rounded.Person, error = state.fullName.error,
                capitalization = KeyboardCapitalization.Words, showValid = true
            )
            AppTextField(
                value = state.alias.value, onValueChange = vm::onAlias, label = "Alias",
                leadingIcon = Icons.Rounded.Badge, error = state.alias.error,
                helper = "Así te verán en la app", showValid = true
            )
            AppTextField(
                value = state.email.value, onValueChange = vm::onEmail, label = "Correo electrónico",
                leadingIcon = Icons.Rounded.Email, error = state.email.error,
                keyboardType = KeyboardType.Email, showValid = true
            )
            AppTextField(
                value = state.phone.value, onValueChange = vm::onPhone, label = "Teléfono",
                leadingIcon = Icons.Rounded.Phone, error = state.phone.error,
                keyboardType = KeyboardType.Phone, helper = "Celular chileno, ej: +56 9 1234 5678", showValid = true
            )
            AppTextField(
                value = state.password.value, onValueChange = vm::onPassword, label = "Contraseña",
                leadingIcon = Icons.Rounded.Lock, error = state.password.error, isPassword = true,
                helper = "Mínimo 8 caracteres, con mayúscula, minúscula y número"
            )
            AppTextField(
                value = state.confirm.value, onValueChange = vm::onConfirm, label = "Repite la contraseña",
                leadingIcon = Icons.Rounded.Lock, error = state.confirm.error, isPassword = true
            )
            AppTextField(
                value = state.address.value, onValueChange = vm::onAddress, label = "Dirección (opcional)",
                leadingIcon = Icons.Rounded.Home, error = state.address.error,
                capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Done,
                keyboardActions = KeyboardActions(onDone = { focus.clearFocus() })
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = state.accepted, onCheckedChange = vm::onAccepted,
                    colors = CheckboxDefaults.colors(
                        checkedColor = MacrameColors.Forest, uncheckedColor = MacrameColors.Muted
                    )
                )
                Text(
                    "Acepto los Términos y Condiciones",
                    style = MaterialTheme.typography.bodyMedium, color = MacrameColors.Ink
                )
            }
            InlineError(state.termsError)
            InlineError(state.generalError)

            PrimaryButton("Crear cuenta", onClick = { focus.clearFocus(); vm.submit() }, loading = state.loading)
        }
    }
}