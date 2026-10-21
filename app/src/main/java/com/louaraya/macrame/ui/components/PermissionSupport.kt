package com.louaraya.macrame.ui.components

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner

/** Estado de un permiso de ejecucion si está concedido y como pedirlo. */
class PermissionState(val granted: Boolean, val request: () -> Unit)

fun hasAnyPermission(context: Context, permissions: Array<out String>): Boolean =
    permissions.any { ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED }

/**
 * Observa uno o varios permisos se vuelve a evaluar al volver desde los ajustes del sistema.
 */
@Composable
fun rememberPermission(vararg permissions: String): PermissionState {
    val context = LocalContext.current
    var granted by remember { mutableStateOf(hasAnyPermission(context, permissions)) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
        granted = result.values.any { it } || hasAnyPermission(context, permissions)
    }
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) granted = hasAnyPermission(context, permissions)
        }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer) }
    }
    return PermissionState(granted) { launcher.launch(arrayOf(*permissions)) }
}

/** Abre la pantalla de ajustes de la app para que el usuario habilite un permiso denegado. */
fun openAppSettings(context: Context) {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", context.packageName, null))
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(intent)
}
