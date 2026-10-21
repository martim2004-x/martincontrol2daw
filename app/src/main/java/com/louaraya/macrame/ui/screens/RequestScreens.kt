package com.louaraya.macrame.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Campaign
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Event
import androidx.compose.material.icons.rounded.Height
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.Layers
import androidx.compose.material.icons.rounded.NotificationsNone
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.louaraya.macrame.data.NotificationEntity
import com.louaraya.macrame.data.RequestEntity
import com.louaraya.macrame.domain.FormOptions
import com.louaraya.macrame.domain.NotificationType
import com.louaraya.macrame.domain.RequestStatus
import com.louaraya.macrame.domain.YarnColors
import com.louaraya.macrame.ui.components.AnimatedCheck
import com.louaraya.macrame.ui.components.AppCard
import com.louaraya.macrame.ui.components.AppDropdownField
import com.louaraya.macrame.ui.components.AppTextField
import com.louaraya.macrame.ui.components.DateField
import com.louaraya.macrame.ui.components.EmptyState
import com.louaraya.macrame.ui.components.InlineError
import com.louaraya.macrame.ui.components.Pill
import com.louaraya.macrame.ui.components.PrimaryButton
import com.louaraya.macrame.ui.components.RoundIconButton
import com.louaraya.macrame.ui.components.SageChip
import com.louaraya.macrame.ui.components.ScreenHeader
import com.louaraya.macrame.ui.components.SoftButton
import com.louaraya.macrame.ui.components.StatusPill
import com.louaraya.macrame.ui.theme.MacrameColors
import com.louaraya.macrame.ui.viewmodel.NotificationGroup
import com.louaraya.macrame.ui.viewmodel.NotificationsViewModel
import com.louaraya.macrame.ui.viewmodel.RequestFormViewModel
import com.louaraya.macrame.ui.viewmodel.RequestsViewModel
import com.louaraya.macrame.ui.viewmodel.macrameViewModel
import com.louaraya.macrame.util.Dates
import com.louaraya.macrame.util.Formatters

// ───────────── 10. cotizacion y personalizacion ─────────────

@Composable
fun RequestFormScreen(productId: Long?, onBack: () -> Unit, onViewRequests: () -> Unit) {
    val vm = macrameViewModel(key = "request-form-${productId ?: 0L}") {
        RequestFormViewModel(productId, it.repository, it.session)
    }
    val state by vm.state.collectAsStateWithLifecycle()
    val focus = LocalFocusManager.current

    Box(Modifier.fillMaxSize()) {
        Column(
            Modifier
                .fillMaxSize()
                .imePadding()
                .verticalScroll(rememberScrollState())
        ) {
            ScreenHeader("Pieza a tu medida", subtitle = "Cuéntanos tu idea y te cotizamos", onBack = onBack)
            Column(
                Modifier.padding(horizontal = 20.dp).padding(bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                state.product?.let { product ->
                    Surface(color = MacrameColors.SageTint, shape = RoundedCornerShape(16.dp)) {
                        Row(
                            Modifier.fillMaxWidth().padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(Icons.Rounded.Inventory2, contentDescription = null, tint = MacrameColors.Forest)
                            Column {
                                Text("Basada en", style = MaterialTheme.typography.bodySmall, color = MacrameColors.InactiveText)
                                Text(product.name, style = MaterialTheme.typography.titleSmall, color = MacrameColors.Forest)
                            }
                        }
                    }
                }

                AppDropdownField(
                    value = state.eventType.value, options = FormOptions.eventTypes, onSelect = vm::onEventType,
                    label = "Tipo de evento", leadingIcon = Icons.Rounded.Event, error = state.eventType.error
                )
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    AppTextField(
                        value = state.width.value, onValueChange = vm::onWidth, label = "Ancho (cm)",
                        leadingIcon = Icons.Rounded.SwapHoriz, error = state.width.error,
                        keyboardType = KeyboardType.Number, modifier = Modifier.weight(1f)
                    )
                    AppTextField(
                        value = state.height.value, onValueChange = vm::onHeight, label = "Alto (cm)",
                        leadingIcon = Icons.Rounded.Height, error = state.height.error,
                        keyboardType = KeyboardType.Number, modifier = Modifier.weight(1f)
                    )
                }
                AppDropdownField(
                    value = state.material.value, options = FormOptions.materials, onSelect = vm::onMaterial,
                    label = "Material preferido", leadingIcon = Icons.Rounded.Layers, error = state.material.error
                )
                ColorPicker(selected = state.colors, error = state.colorsError, onToggle = vm::toggleColor)
                DateField(
                    millis = state.dateMillis, onChange = vm::onDate, label = "Fecha requerida",
                    error = state.dateError, minMillis = Dates.todayUtcMillis()
                )
                AppTextField(
                    value = state.notes.value, onValueChange = vm::onNotes, label = "Observaciones (opcional)",
                    leadingIcon = Icons.Rounded.Description, error = state.notes.error,
                    helper = "${state.notes.value.length}/300", singleLine = false, minLines = 3,
                    maxLength = 300, imeAction = ImeAction.Default
                )
                InlineError(state.generalError)
                PrimaryButton(
                    "Enviar solicitud",
                    onClick = { focus.clearFocus(); vm.submit() },
                    loading = state.submitting
                )
            }
        }

        AnimatedVisibility(
            visible = state.createdCode != null,
            enter = fadeIn(tween(250)) + scaleIn(tween(300), initialScale = 0.92f),
            exit = fadeOut()
        ) {
            SuccessOverlay(code = state.createdCode.orEmpty(), onViewRequests = onViewRequests, onBack = onBack)
        }
    }
}

@Composable
private fun ColorPicker(selected: List<String>, error: String?, onToggle: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Paleta de colores", style = MaterialTheme.typography.titleSmall, color = MacrameColors.Ink)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            YarnColors.forEach { yarn ->
                val isSelected = yarn.name in selected
                val scale by animateFloatAsState(if (isSelected) 1.12f else 1f, tween(200), label = "colorScale")
                val dark = yarn.name == "Bosque" || yarn.name == "Grafito" || yarn.name == "Terracota"
                Column(
                    Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        Modifier
                            .graphicsLayer { scaleX = scale; scaleY = scale }
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(yarn.argb))
                            .border(
                                if (isSelected) 3.dp else 1.dp,
                                if (isSelected) MacrameColors.Forest else MacrameColors.Outline,
                                CircleShape
                            )
                            .clickable { onToggle(yarn.name) },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(
                                Icons.Rounded.Check, contentDescription = "Seleccionado",
                                tint = if (dark) Color.White else MacrameColors.Forest, modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Text(yarn.name, style = MaterialTheme.typography.labelSmall, color = MacrameColors.Muted, maxLines = 1)
                }
            }
        }
        InlineError(error)
    }
}

@Composable
private fun SuccessOverlay(code: String, onViewRequests: () -> Unit, onBack: () -> Unit) {
    Box(
        Modifier
            .fillMaxSize()
            .background(MacrameColors.Ash)
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {},
        contentAlignment = Alignment.Center
    ) {
        Column(
            Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            AnimatedCheck(Modifier.size(104.dp))
            Spacer(Modifier.height(6.dp))
            Text("¡Solicitud enviada!", style = MaterialTheme.typography.headlineSmall, color = MacrameColors.Ink)
            Pill(code, MacrameColors.SageTint, MacrameColors.Forest)
            Text(
                "Guardamos tu solicitud con este código. Te avisaremos cuando cambie su estado.",
                style = MaterialTheme.typography.bodyMedium, color = MacrameColors.Muted, textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(10.dp))
            PrimaryButton("Ver mis solicitudes", onViewRequests)
            SoftButton("Volver", onBack)
        }
    }
}

// ───────────── 13. Historial y gestion de solicitud ─────────────

@Composable
fun RequestsScreen(onNewRequest: () -> Unit, onCatalog: () -> Unit) {
    val vm = macrameViewModel { RequestsViewModel(it.repository, it.session) }
    val state by vm.state.collectAsStateWithLifecycle()
    var expandedId by rememberSaveable { mutableStateOf<Long?>(null) }

    Column(Modifier.fillMaxSize()) {
        ScreenHeader(
            title = if (state.isAdmin) "Solicitudes" else "Mis solicitudes",
            subtitle = "${state.total} en total",
            actions = {
                if (!state.isAdmin) RoundIconButton(Icons.Rounded.Add, "Nueva solicitud", onNewRequest)
            }
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(RequestStatus.all) { status ->
                SageChip(status, selected = state.filter == status, onClick = { vm.onFilter(status) })
            }
        }

        if (state.requests.isEmpty() && !state.loading) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                EmptyState(
                    Icons.Rounded.ReceiptLong,
                    if (state.total == 0) "Aún no hay solicitudes" else "Sin solicitudes con este estado",
                    if (state.isAdmin) "Cuando los clientes envíen cotizaciones aparecerán aquí."
                    else "Pide una cotización personalizada desde el detalle de un diseño o con el botón +."
                )
                if (!state.isAdmin && state.total == 0) {
                    Column(Modifier.padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        PrimaryButton("Crear solicitud", onNewRequest)
                        SoftButton("Explorar catálogo", onCatalog)
                    }
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.requests, key = { it.id }) { request ->
                    RequestCard(
                        request = request,
                        expanded = expandedId == request.id,
                        isAdmin = state.isAdmin,
                        onToggle = { expandedId = if (expandedId == request.id) null else request.id },
                        onStatus = { vm.updateStatus(request.id, it) },
                        modifier = Modifier.animateItem()
                    )
                }
            }
        }
    }
}

@Composable
private fun RequestCard(
    request: RequestEntity,
    expanded: Boolean,
    isAdmin: Boolean,
    onToggle: () -> Unit,
    onStatus: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val rotation by animateFloatAsState(if (expanded) 180f else 0f, tween(250), label = "chevron")
    AppCard(modifier.fillMaxWidth(), onClick = onToggle) {
        Column(Modifier.padding(16.dp).animateContentSize(tween(250)), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    Formatters.requestCode(request.id), style = MaterialTheme.typography.titleSmall,
                    color = MacrameColors.Forest, modifier = Modifier.weight(1f)
                )
                StatusPill(request.status)
                Icon(
                    Icons.Rounded.KeyboardArrowDown, contentDescription = if (expanded) "Contraer" else "Expandir",
                    tint = MacrameColors.Muted, modifier = Modifier.rotate(rotation)
                )
            }
            Text(
                request.productName ?: "Diseño personalizado",
                style = MaterialTheme.typography.titleMedium, color = MacrameColors.Ink,
                maxLines = 1, overflow = TextOverflow.Ellipsis
            )
            Text(
                (if (isAdmin) "${request.clientName} · " else "") + Formatters.dateTime(request.createdAt),
                style = MaterialTheme.typography.bodySmall, color = MacrameColors.Muted
            )

            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn(tween(200)) + expandVertically(tween(250)),
                exit = fadeOut(tween(120)) + shrinkVertically(tween(200))
            ) {
                Column(Modifier.padding(top = 6.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    DetailLine("Evento", request.eventType)
                    DetailLine("Medidas", Formatters.dimensions(request.widthCm, request.heightCm))
                    DetailLine("Material", request.material)
                    DetailLine("Colores", request.colors.replace(",", ", "))
                    DetailLine("Fecha requerida", Formatters.date(request.requiredDate))
                    if (request.notes.isNotBlank()) DetailLine("Observaciones", request.notes)

                    if (isAdmin) {
                        Text(
                            "Actualizar estado", style = MaterialTheme.typography.titleSmall,
                            color = MacrameColors.Ink, modifier = Modifier.padding(top = 6.dp)
                        )
                        Row(
                            Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            RequestStatus.all.forEach { status ->
                                SageChip(status, selected = request.status == status, onClick = { onStatus(status) })
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailLine(label: String, value: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            label, style = MaterialTheme.typography.bodySmall, color = MacrameColors.Muted,
            modifier = Modifier.width(112.dp)
        )
        Text(value, style = MaterialTheme.typography.bodyMedium, color = MacrameColors.Ink, modifier = Modifier.weight(1f))
    }
}

// ───────────── 14. notificaciones ─────────────

@Composable
fun NotificationsScreen(onBack: () -> Unit, onOpen: (NotificationEntity) -> Unit) {
    val vm = macrameViewModel { NotificationsViewModel(it.repository, it.session) }
    val state by vm.state.collectAsStateWithLifecycle()

    Column(Modifier.fillMaxSize()) {
        ScreenHeader(
            title = "Notificaciones",
            subtitle = if (state.unread > 0) "${state.unread} sin leer" else "Todo al día",
            onBack = onBack,
            actions = {
                if (state.unread > 0) {
                    TextButton(onClick = vm::markAllRead) {
                        Text("Marcar leídas", color = MacrameColors.Forest, style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(NotificationGroup.entries) { group ->
                SageChip(group.label, selected = state.group == group, onClick = { vm.onGroup(group) })
            }
        }

        if (state.items.isEmpty()) {
            EmptyState(
                Icons.Rounded.NotificationsNone, "Sin notificaciones",
                "Aquí verás novedades de campañas, ferias y cambios en tus solicitudes."
            )
        } else {
            LazyColumn(
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(state.items, key = { it.id }) { notification ->
                    NotificationRow(
                        notification,
                        onClick = {
                            vm.markRead(notification.id)
                            onOpen(notification)
                        },
                        modifier = Modifier.animateItem()
                    )
                }
            }
        }
    }
}

@Composable
private fun NotificationRow(notification: NotificationEntity, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val icon = when (notification.type) {
        NotificationType.CAMPAIGN -> Icons.Rounded.Campaign
        NotificationType.REQUEST -> Icons.Rounded.ReceiptLong
        NotificationType.FAIR -> Icons.Rounded.Event
        else -> Icons.Rounded.Inventory2
    }
    AppCard(modifier.fillMaxWidth(), onClick = onClick) {
        Row(
            Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier.size(44.dp).clip(CircleShape).background(MacrameColors.SageTint),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = MacrameColors.Forest)
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    notification.title, style = MaterialTheme.typography.titleSmall, color = MacrameColors.Ink,
                    fontWeight = if (notification.read) FontWeight.Medium else FontWeight.Bold
                )
                Text(notification.body, style = MaterialTheme.typography.bodyMedium, color = MacrameColors.InactiveText)
                Text(
                    Formatters.relative(notification.createdAt),
                    style = MaterialTheme.typography.bodySmall, color = MacrameColors.Muted
                )
            }
            if (!notification.read) {
                Box(Modifier.size(10.dp).clip(CircleShape).background(MacrameColors.Forest))
            }
        }
    }
}
