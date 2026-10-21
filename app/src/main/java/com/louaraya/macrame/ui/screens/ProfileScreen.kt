package com.louaraya.macrame.ui.screens

import android.Manifest
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.Badge
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.louaraya.macrame.domain.Role
import com.louaraya.macrame.ui.components.AppCard
import com.louaraya.macrame.ui.components.AppTextField
import com.louaraya.macrame.ui.components.Avatar
import com.louaraya.macrame.ui.components.AvatarCount
import com.louaraya.macrame.ui.components.InlineError
import com.louaraya.macrame.ui.components.Pill
import com.louaraya.macrame.ui.components.PrimaryButton
import com.louaraya.macrame.ui.components.RoundIconButton
import com.louaraya.macrame.ui.components.ScreenHeader
import com.louaraya.macrame.ui.components.SoftButton
import com.louaraya.macrame.ui.components.openAppSettings
import com.louaraya.macrame.ui.components.rememberPermission
import com.louaraya.macrame.ui.theme.MacrameColors
import com.louaraya.macrame.ui.viewmodel.ProfileViewModel
import com.louaraya.macrame.ui.viewmodel.macrameViewModel

// ───────────── 15. perfil y configuracion  ─────────────

@Composable
fun ProfileScreen(onSwitchUser: () -> Unit, onNotifications: () -> Unit) {
    val vm = macrameViewModel { ProfileViewModel(it.repository, it.session) }
    val user by vm.user.collectAsStateWithLifecycle()
    val state by vm.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val focus = LocalFocusManager.current
    val snackbar = remember { SnackbarHostState() }

    val camera = rememberPermission(Manifest.permission.CAMERA)
    val location = rememberPermission(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)

    LaunchedEffect(state.savedCounter) {
        if (state.savedCounter > 0) snackbar.showSnackbar("Perfil actualizado")
    }

    val current = user
    if (current == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = MacrameColors.Forest)
        }
        return
    }

    Box(Modifier.fillMaxSize()) {
        Column(
            Modifier
                .fillMaxSize()
                .imePadding()
                .verticalScroll(rememberScrollState())
        ) {
            ScreenHeader(
                title = "Mi perfil",
                subtitle = if (current.role == Role.ADMIN) "Cuenta de administración" else "Tus datos y preferencias",
                actions = { RoundIconButton(Icons.Rounded.Notifications, "Notificaciones", onNotifications) }
            )
            Column(
                Modifier.padding(horizontal = 20.dp).padding(bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // identidad
                AppCard(Modifier.fillMaxWidth()) {
                    Column(
                        Modifier.fillMaxWidth().padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Avatar(current.avatarIndex, 88.dp)
                        Text(current.fullName, style = MaterialTheme.typography.titleLarge, color = MacrameColors.Ink, textAlign = TextAlign.Center)
                        Text("@${current.alias}", style = MaterialTheme.typography.bodyMedium, color = MacrameColors.Muted)
                        Pill(
                            if (current.role == Role.ADMIN) "Administrador" else "Cliente",
                            MacrameColors.SageTint, MacrameColors.Forest
                        )
                    }
                }

                // Datos personales lectura y edicion
                AppCard(Modifier.fillMaxWidth()) {
                    AnimatedContent(
                        targetState = state.editing,
                        transitionSpec = { fadeIn(tween(220)) togetherWith fadeOut(tween(150)) },
                        label = "profileEdit"
                    ) { editing ->
                        if (editing) {
                            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text("Editar mis datos", style = MaterialTheme.typography.titleMedium, color = MacrameColors.Ink)
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                                ) {
                                    itemsIndexed(List(AvatarCount) { it }) { _, index ->
                                        Avatar(
                                            index, 56.dp, selected = state.avatar == index,
                                            modifier = Modifier.clip(CircleShape).clickable { vm.onAvatar(index) }
                                        )
                                    }
                                }
                                AppTextField(
                                    state.fullName.value, vm::onFullName, "Nombre completo", Icons.Rounded.Person,
                                    error = state.fullName.error, capitalization = KeyboardCapitalization.Words, showValid = true
                                )
                                AppTextField(
                                    state.alias.value, vm::onAlias, "Alias", Icons.Rounded.Badge,
                                    error = state.alias.error, showValid = true
                                )
                                AppTextField(
                                    state.phone.value, vm::onPhone, "Teléfono", Icons.Rounded.Phone,
                                    error = state.phone.error, keyboardType = KeyboardType.Phone, showValid = true
                                )
                                AppTextField(
                                    state.address.value, vm::onAddress, "Dirección (opcional)", Icons.Rounded.Home,
                                    error = state.address.error, imeAction = ImeAction.Done,
                                    keyboardActions = KeyboardActions(onDone = { focus.clearFocus() })
                                )
                                InlineError(state.generalError)
                                PrimaryButton("Guardar cambios", onClick = { focus.clearFocus(); vm.save() }, loading = state.saving)
                                SoftButton("Cancelar", onClick = vm::cancelEditing)
                            }
                        } else {
                            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        "Mis datos personales", style = MaterialTheme.typography.titleMedium,
                                        color = MacrameColors.Ink, modifier = Modifier.weight(1f)
                                    )
                                    TextButton(onClick = vm::startEditing) {
                                        Icon(Icons.Rounded.Edit, contentDescription = null, tint = MacrameColors.Forest, modifier = Modifier.size(18.dp))
                                        Text("  Editar", color = MacrameColors.Forest, style = MaterialTheme.typography.labelLarge)
                                    }
                                }
                                InfoRow(Icons.Rounded.Email, "Correo", current.email)
                                InfoRow(Icons.Rounded.Phone, "Teléfono", current.phone)
                                InfoRow(Icons.Rounded.Home, "Dirección", current.address.ifBlank { "Sin dirección registrada" })
                            }
                        }
                    }
                }

                // Preferencias
                AppCard(Modifier.fillMaxWidth()) {
                    Row(
                        Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        IconBadge(Icons.Rounded.Notifications)
                        Column(Modifier.weight(1f)) {
                            Text("Notificaciones", style = MaterialTheme.typography.titleSmall, color = MacrameColors.Ink)
                            Text(
                                if (current.role == Role.ADMIN) "Alertas de nuevas solicitudes"
                                else "Campañas, ferias y estado de tus solicitudes",
                                style = MaterialTheme.typography.bodySmall, color = MacrameColors.Muted
                            )
                        }
                        Switch(
                            checked = current.notificationsEnabled,
                            onCheckedChange = vm::setNotifications,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White, checkedTrackColor = MacrameColors.Forest,
                                uncheckedThumbColor = MacrameColors.Muted, uncheckedTrackColor = MacrameColors.Inactive,
                                uncheckedBorderColor = MacrameColors.Outline
                            )
                        )
                    }
                }

                // Permisos del dispositivo
                AppCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Text("Permisos del dispositivo", style = MaterialTheme.typography.titleMedium, color = MacrameColors.Ink)
                        PermissionRow(
                            Icons.Rounded.CameraAlt, "Cámara", "Escanear QR y fotografiar productos",
                            camera.granted, camera.request
                        )
                        PermissionRow(
                            Icons.Rounded.LocationOn, "Ubicación", "Mostrar tu posición en el mapa de ferias",
                            location.granted, location.request
                        )
                        TextButton(onClick = { openAppSettings(context) }, modifier = Modifier.fillMaxWidth()) {
                            Text("Abrir ajustes de la app", color = MacrameColors.Forest, style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }

                // Sesion
                SoftButton("Cambiar de usuario", onClick = onSwitchUser, icon = Icons.Rounded.SwapHoriz)
                Button(
                    onClick = vm::signOut,
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MacrameColors.ErrorTint, contentColor = MacrameColors.Error
                    ),
                    modifier = Modifier.fillMaxWidth().height(54.dp)
                ) {
                    Icon(Icons.AutoMirrored.Rounded.Logout, contentDescription = null, modifier = Modifier.size(20.dp))
                    Text("  Cerrar sesión", style = MaterialTheme.typography.labelLarge)
                }
            }
        }

        SnackbarHost(
            hostState = snackbar,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 16.dp),
            snackbar = { data ->
                Snackbar(
                    snackbarData = data, containerColor = MacrameColors.Forest, contentColor = Color.White,
                    shape = RoundedCornerShape(14.dp)
                )
            }
        )
    }
}

@Composable
private fun IconBadge(icon: ImageVector) {
    Box(
        Modifier.size(40.dp).clip(CircleShape).background(MacrameColors.SageTint),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = MacrameColors.Forest, modifier = Modifier.size(22.dp))
    }
}

@Composable
private fun InfoRow(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        IconBadge(icon)
        Column {
            Text(label, style = MaterialTheme.typography.bodySmall, color = MacrameColors.Muted)
            Text(value, style = MaterialTheme.typography.bodyLarge, color = MacrameColors.Ink)
        }
    }
}

@Composable
private fun PermissionRow(
    icon: ImageVector,
    label: String,
    description: String,
    granted: Boolean,
    onRequest: () -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        IconBadge(icon)
        Column(Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.titleSmall, color = MacrameColors.Ink)
            Text(description, style = MaterialTheme.typography.bodySmall, color = MacrameColors.Muted)
        }
        if (granted) {
            Pill("Concedido", MacrameColors.SuccessTint, MacrameColors.Success)
        } else {
            Column(horizontalAlignment = Alignment.End) {
                Pill("Denegado", MacrameColors.ErrorTint, MacrameColors.Error)
                TextButton(onClick = onRequest, contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)) {
                    Text("Permitir", color = MacrameColors.Forest, style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}
