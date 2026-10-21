@file:OptIn(ExperimentalMaterial3Api::class)

package com.louaraya.macrame.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.louaraya.macrame.ui.theme.MacrameColors
import com.louaraya.macrame.util.Formatters

private val FieldShape = RoundedCornerShape(16.dp)

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = MacrameColors.Ink,
    unfocusedTextColor = MacrameColors.Ink,
    errorTextColor = MacrameColors.Ink,
    focusedContainerColor = Color.White,
    unfocusedContainerColor = Color.White,
    errorContainerColor = Color.White,
    cursorColor = MacrameColors.Forest,
    errorCursorColor = MacrameColors.Error,
    focusedBorderColor = MacrameColors.Forest,
    unfocusedBorderColor = MacrameColors.Outline,
    errorBorderColor = MacrameColors.Error,
    focusedLeadingIconColor = MacrameColors.Forest,
    unfocusedLeadingIconColor = MacrameColors.Muted,
    errorLeadingIconColor = MacrameColors.Error,
    focusedTrailingIconColor = MacrameColors.Muted,
    unfocusedTrailingIconColor = MacrameColors.Muted,
    errorTrailingIconColor = MacrameColors.Error,
    focusedLabelColor = MacrameColors.Forest,
    unfocusedLabelColor = MacrameColors.Muted,
    errorLabelColor = MacrameColors.Error,
    focusedSupportingTextColor = MacrameColors.Muted,
    unfocusedSupportingTextColor = MacrameColors.Muted,
    errorSupportingTextColor = MacrameColors.Error
)

/**
 * Campo de texto estándar de la app: ícono inicial, ícono de estado (error / valido),
 * solo muestra el error que se le asigno en realidad no valida nada
 *
 */
@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    leadingIcon: ImageVector,
    modifier: Modifier = Modifier,
    error: String? = null,
    helper: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    capitalization: KeyboardCapitalization = KeyboardCapitalization.None,
    isPassword: Boolean = false,
    singleLine: Boolean = true,
    minLines: Int = 1,
    maxLength: Int? = null,
    showValid: Boolean = false,
    readOnly: Boolean = false,
    onClick: (() -> Unit)? = null,
    trailingIcon: ImageVector? = null,
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    var passwordVisible by remember { mutableStateOf(false) }
    val isError = error != null

    val passwordToggle: @Composable () -> Unit = {
        IconButton(onClick = { passwordVisible = !passwordVisible }) {
            Icon(
                imageVector = if (passwordVisible) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                contentDescription = if (passwordVisible) "Ocultar contraseña" else "Mostrar contraseña"
            )
        }
    }
    val errorIcon: @Composable () -> Unit = {
        Icon(Icons.Rounded.ErrorOutline, contentDescription = "Campo con error", tint = MacrameColors.Error)
    }
    val validIcon: @Composable () -> Unit = {
        Icon(Icons.Rounded.CheckCircle, contentDescription = "Campo válido", tint = MacrameColors.Success)
    }
    val trailing: (@Composable () -> Unit)? = when {
        isPassword -> passwordToggle
        isError -> errorIcon
        trailingIcon != null -> {
            val extra: @Composable () -> Unit = { Icon(trailingIcon, contentDescription = null) }
            extra
        }
        showValid && value.isNotBlank() -> validIcon
        else -> null
    }

    val supporting: (@Composable () -> Unit)? = if (error != null || helper != null) {
        val text = error ?: helper.orEmpty()
        val block: @Composable () -> Unit = { Text(text) }
        block
    } else null

    Box(modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = { if (maxLength == null || it.length <= maxLength) onValueChange(it) },
            label = { Text(label) },
            leadingIcon = { Icon(leadingIcon, contentDescription = null) },
            trailingIcon = trailing,
            supportingText = supporting,
            isError = isError,
            singleLine = singleLine,
            minLines = minLines,
            readOnly = readOnly,
            visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
            keyboardOptions = KeyboardOptions(
                keyboardType = if (isPassword) KeyboardType.Password else keyboardType,
                imeAction = imeAction,
                capitalization = capitalization
            ),
            keyboardActions = keyboardActions,
            shape = FieldShape,
            colors = fieldColors(),
            modifier = Modifier.fillMaxWidth()
        )
        if (onClick != null) {
            Box(
                Modifier
                    .matchParentSize()
                    .clip(FieldShape)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onClick
                    )
            )
        }
    }
}

/** Selector desplegable de una sola opcion. */
@Composable
fun AppDropdownField(
    value: String,
    options: List<String>,
    onSelect: (String) -> Unit,
    label: String,
    leadingIcon: ImageVector,
    modifier: Modifier = Modifier,
    error: String? = null
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier) {
        AppTextField(
            value = value,
            onValueChange = {},
            label = label,
            leadingIcon = leadingIcon,
            readOnly = true,
            error = error,
            trailingIcon = Icons.Rounded.KeyboardArrowDown,
            onClick = { expanded = true }
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(Color.White)
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelect(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

/** Campo de fecha que abre el selector de calendario de Material 3. */
@Composable
fun DateField(
    millis: Long?,
    onChange: (Long) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    error: String? = null,
    minMillis: Long? = null
) {
    var open by remember { mutableStateOf(false) }
    AppTextField(
        value = millis?.let { Formatters.date(it) }.orEmpty(),
        onValueChange = {},
        label = label,
        leadingIcon = Icons.Rounded.CalendarMonth,
        readOnly = true,
        error = error,
        onClick = { open = true },
        modifier = modifier
    )
    if (open) {
        val state = rememberDatePickerState(
            initialSelectedDateMillis = millis,
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean =
                    minMillis == null || utcTimeMillis >= minMillis
            }
        )
        DatePickerDialog(
            onDismissRequest = { open = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let(onChange)
                    open = false
                }) { Text("Aceptar", color = MacrameColors.Forest) }
            },
            dismissButton = {
                TextButton(onClick = { open = false }) { Text("Cancelar", color = MacrameColors.Muted) }
            }
        ) {
            DatePicker(state = state)
        }
    }
}

/** Mensaje de error general con animacion. */
@Composable
fun InlineError(message: String?, modifier: Modifier = Modifier) {
    AnimatedVisibility(
        visible = message != null,
        enter = fadeIn(tween(200)) + expandVertically(tween(250)),
        exit = fadeOut(tween(150)) + shrinkVertically(tween(200)),
        modifier = modifier
    ) {
        Surface(color = MacrameColors.ErrorTint, shape = RoundedCornerShape(14.dp)) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(Icons.Rounded.ErrorOutline, contentDescription = null, tint = MacrameColors.Error, modifier = Modifier.size(20.dp))
                Text(message.orEmpty(), style = MaterialTheme.typography.bodyMedium, color = MacrameColors.Error)
            }
        }
    }
}

/** Boton principal (verde bosque). Muestra un indicador de carga y se bloquea mientras `loading`. */
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    icon: ImageVector? = null
) {
    Button(
        onClick = onClick,
        enabled = enabled && !loading,
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MacrameColors.Forest,
            contentColor = Color.White,
            disabledContainerColor = MacrameColors.Inactive,
            disabledContentColor = MacrameColors.InactiveText
        ),
        modifier = modifier.fillMaxWidth().height(54.dp)
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp), strokeWidth = 2.5.dp, color = Color.White
            )
        } else {
            if (icon != null) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp))
                Box(Modifier.width(8.dp))
            }
            Text(text, style = MaterialTheme.typography.labelLarge)
        }
    }
}

/** Botón secundario */
@Composable
fun SoftButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MacrameColors.Inactive,
            contentColor = MacrameColors.InactiveText,
            disabledContainerColor = MacrameColors.Inactive.copy(alpha = 0.5f),
            disabledContentColor = MacrameColors.InactiveText.copy(alpha = 0.5f)
        ),
        modifier = modifier.fillMaxWidth().height(54.dp)
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp))
            Box(Modifier.width(8.dp))
        }
        Text(text, style = MaterialTheme.typography.labelLarge)
    }
}

/** filtro */
@Composable
fun SageChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null
) {
    val bg by animateColorAsState(
        if (selected) MacrameColors.Forest else MacrameColors.Inactive, tween(220), label = "chipBg"
    )
    val fg by animateColorAsState(
        if (selected) Color.White else MacrameColors.InactiveText, tween(220), label = "chipFg"
    )
    Surface(onClick = onClick, shape = CircleShape, color = bg, modifier = modifier) {
        Row(
            Modifier.padding(horizontal = 16.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (icon != null) Icon(icon, contentDescription = null, tint = fg, modifier = Modifier.size(16.dp))
            Text(label, color = fg, style = MaterialTheme.typography.labelLarge)
        }
    }
}

/** Borde sutil reutilizable para tarjetas. */
val CardBorder = BorderStroke(1.dp, MacrameColors.Outline.copy(alpha = 0.7f))
