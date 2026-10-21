package com.louaraya.macrame.ui.screens

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddAPhoto
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AttachMoney
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Event
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.Layers
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SearchOff
import androidx.compose.material.icons.rounded.Straighten
import androidx.compose.material.icons.rounded.Title
import androidx.compose.material.icons.rounded.Campaign
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.louaraya.macrame.data.ProductEntity
import com.louaraya.macrame.domain.Category
import com.louaraya.macrame.domain.CampaignPhase
import com.louaraya.macrame.domain.FormOptions
import com.louaraya.macrame.ui.components.AppCard
import com.louaraya.macrame.ui.components.AppDropdownField
import com.louaraya.macrame.ui.components.AppTextField
import com.louaraya.macrame.ui.components.DateField
import com.louaraya.macrame.ui.components.EmptyState
import com.louaraya.macrame.ui.components.InlineError
import com.louaraya.macrame.ui.components.Pill
import com.louaraya.macrame.ui.components.PrimaryButton
import com.louaraya.macrame.ui.components.ProductArt
import com.louaraya.macrame.ui.components.ProductImage
import com.louaraya.macrame.ui.components.RoundIconButton
import com.louaraya.macrame.ui.components.SageChip
import com.louaraya.macrame.ui.components.ScreenHeader
import com.louaraya.macrame.ui.components.SoftButton
import com.louaraya.macrame.ui.components.rememberPermission
import com.louaraya.macrame.ui.theme.MacrameColors
import com.louaraya.macrame.ui.viewmodel.AdminProductsViewModel
import com.louaraya.macrame.ui.viewmodel.CampaignFormViewModel
import com.louaraya.macrame.ui.viewmodel.CampaignsViewModel
import com.louaraya.macrame.ui.viewmodel.ProductFilter
import com.louaraya.macrame.ui.viewmodel.ProductFormViewModel
import com.louaraya.macrame.ui.viewmodel.macrameViewModel
import com.louaraya.macrame.util.Formatters
import java.io.File

@Composable
private fun SwitchRow(title: String, subtitle: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall, color = MacrameColors.Ink)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MacrameColors.Muted)
        }
        Switch(
            checked = checked, onCheckedChange = onChange,
            colors = SwitchDefaults.colors(
                checkedTrackColor = MacrameColors.Forest, checkedThumbColor = MacrameColors.Card,
                uncheckedTrackColor = MacrameColors.Inactive, uncheckedThumbColor = MacrameColors.Muted,
                uncheckedBorderColor = MacrameColors.Inactive
            )
        )
    }
}

// ───────────── 11. gestion de productos administrador─────────────

@Composable
fun AdminProductsScreen(onNew: () -> Unit, onEdit: (Long) -> Unit) {
    val vm = macrameViewModel { AdminProductsViewModel(it.repository) }
    val state by vm.state.collectAsStateWithLifecycle()

    Column(Modifier.fillMaxSize()) {
        ScreenHeader(
            title = "Productos",
            subtitle = "${state.total} diseños en el catálogo",
            actions = { RoundIconButton(Icons.Rounded.Add, "Nuevo producto", onNew) }
        )
        AppTextField(
            value = state.query, onValueChange = vm::onQuery,
            label = "Buscar por nombre o código", leadingIcon = Icons.Rounded.Search,
            modifier = Modifier.padding(horizontal = 20.dp)
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(ProductFilter.entries.toList()) { f ->
                SageChip(f.label, selected = state.filter == f, onClick = { vm.onFilter(f) })
            }
        }
        if (state.products.isEmpty()) {
            EmptyState(Icons.Rounded.SearchOff, "Sin productos", "No hay productos con esos filtros.")
        } else {
            LazyColumn(
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.products, key = { it.id }) { product ->
                    AdminProductRow(
                        product,
                        onEdit = { onEdit(product.id) },
                        onActive = { vm.setActive(product.id, it) }
                    )
                }
                item { PrimaryButton("Nuevo producto", onNew, icon = Icons.Rounded.Add) }
            }
        }
    }
}

@Composable
private fun AdminProductRow(product: ProductEntity, onEdit: () -> Unit, onActive: (Boolean) -> Unit) {
    AppCard(onClick = onEdit) {
        Row(
            Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ProductImage(product, Modifier.size(68.dp), RoundedCornerShape(16.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(product.code, style = MaterialTheme.typography.labelSmall, color = MacrameColors.Muted)
                Text(product.name, style = MaterialTheme.typography.titleSmall, color = MacrameColors.Ink, maxLines = 1)
                Text(
                    "${Formatters.price(product.priceClp)} · ${if (product.madeToOrder) "A pedido" else "Stock ${product.stock}"}",
                    style = MaterialTheme.typography.bodySmall, color = MacrameColors.Muted
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Switch(
                    checked = product.active, onCheckedChange = onActive,
                    colors = SwitchDefaults.colors(
                        checkedTrackColor = MacrameColors.Forest, checkedThumbColor = MacrameColors.Card,
                        uncheckedTrackColor = MacrameColors.Inactive, uncheckedThumbColor = MacrameColors.Muted,
                        uncheckedBorderColor = MacrameColors.Inactive
                    )
                )
                Text(
                    if (product.active) "Activo" else "Oculto",
                    style = MaterialTheme.typography.labelSmall, color = MacrameColors.Muted
                )
            }
        }
    }
}

// ───────────── 12. formulario profucto desde la camara ─────────────

@Composable
fun ProductFormScreen(productId: Long, onBack: () -> Unit) {
    val vm = macrameViewModel(key = "product_form_$productId") { ProductFormViewModel(productId, it.repository) }
    val state by vm.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val camera = rememberPermission(Manifest.permission.CAMERA)

    var pendingUri by rememberSaveable { mutableStateOf<String?>(null) }
    var wantsPhoto by rememberSaveable { mutableStateOf(false) }

    val takePicture = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { ok ->
        if (ok) pendingUri?.let(vm::onPhoto)
        pendingUri = null
    }

    fun launchCamera() {
        val dir = File(context.filesDir, "product_photos").apply { mkdirs() }
        val file = File(dir, "lam_${System.currentTimeMillis()}.jpg")
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        pendingUri = uri.toString()
        takePicture.launch(uri)
    }

    // tras conceder el permiso se abre la cam automaticamente.
    LaunchedEffect(camera.granted) {
        if (camera.granted && wantsPhoto) {
            wantsPhoto = false
            launchCamera()
        }
    }
    LaunchedEffect(state.saved) { if (state.saved) onBack() }

    Column(Modifier.fillMaxSize()) {
        ScreenHeader(
            title = if (state.isNew) "Nuevo producto" else "Editar producto",
            subtitle = "Completa todos los campos obligatorios",
            onBack = onBack
        )
        if (state.loading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MacrameColors.Forest)
            }
            return@Column
        }
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            InlineError(state.generalError)

            // Foto del producto
            Box(
                Modifier.fillMaxWidth().height(190.dp).clip(RoundedCornerShape(22.dp))
                    .background(MacrameColors.SageTint)
            ) {
                val photo = state.photoUri
                if (photo != null) {
                    AsyncImage(photo, "Foto del producto", contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                } else {
                    ProductArt(state.category.value.ifEmpty { Category.INTERIORES }, 7L, Modifier.fillMaxSize())
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SoftButton(
                    if (state.photoUri == null) "Tomar foto" else "Cambiar foto",
                    onClick = {
                        if (camera.granted) launchCamera() else { wantsPhoto = true; camera.request() }
                    },
                    icon = Icons.Rounded.AddAPhoto, modifier = Modifier.weight(1f)
                )
                if (state.photoUri != null) {
                    SoftButton("Quitar", onClick = { vm.onPhoto(null) }, modifier = Modifier.weight(1f))
                }
            }

            AppTextField(
                state.name.value, vm::onName, "Nombre del diseño", Icons.Rounded.Title,
                error = state.name.error, capitalization = KeyboardCapitalization.Sentences,
                maxLength = 60, showValid = true
            )
            AppDropdownField(
                state.category.value, Category.all, vm::onCategory, "Categoría", Icons.Rounded.Category,
                error = state.category.error
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                AppTextField(
                    state.width.value, vm::onWidth, "Ancho (cm)", Icons.Rounded.Straighten,
                    modifier = Modifier.weight(1f), error = state.width.error, keyboardType = KeyboardType.Number
                )
                AppTextField(
                    state.height.value, vm::onHeight, "Alto (cm)", Icons.Rounded.Straighten,
                    modifier = Modifier.weight(1f), error = state.height.error, keyboardType = KeyboardType.Number
                )
            }
            AppDropdownField(
                state.material.value, FormOptions.materials, vm::onMaterial, "Material", Icons.Rounded.Layers,
                error = state.material.error
            )
            AppTextField(
                state.price.value, vm::onPrice, "Precio (CLP)", Icons.Rounded.AttachMoney,
                error = state.price.error, keyboardType = KeyboardType.Number,
                helper = "Entre \$1.000 y \$5.000.000"
            )
            AppTextField(
                state.description.value, vm::onDescription, "Descripción", Icons.Rounded.Description,
                error = state.description.error, singleLine = false, minLines = 3, maxLength = 400,
                capitalization = KeyboardCapitalization.Sentences
            )
            AppCard {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SwitchRow(
                        "Confección a pedido", "No usa stock; se teje al recibir la solicitud",
                        state.madeToOrder, vm::onMadeToOrder
                    )
                    AnimatedVisibility(visible = !state.madeToOrder) {
                        AppTextField(
                            state.stock.value, vm::onStock, "Unidades en stock", Icons.Rounded.Inventory2,
                            error = state.stock.error, keyboardType = KeyboardType.Number
                        )
                    }
                    SwitchRow("Visible en el catálogo", "Los clientes solo ven productos activos", state.active, vm::onActive)
                }
            }
            PrimaryButton(
                if (state.isNew) "Publicar producto" else "Guardar cambios",
                onClick = vm::save, loading = state.saving, modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(24.dp))
        }
    }
}

// ───────────── 13. colecciones y campaña ─────────────

@Composable
fun AdminCampaignsScreen(onNew: () -> Unit, onEdit: (Long) -> Unit) {
    val vm = macrameViewModel { CampaignsViewModel(it.repository) }
    val rows by vm.rows.collectAsStateWithLifecycle()

    Column(Modifier.fillMaxSize()) {
        ScreenHeader(
            title = "Campañas",
            subtitle = "${rows.size} colecciones",
            actions = { RoundIconButton(Icons.Rounded.Add, "Nueva campaña", onNew) }
        )
        if (rows.isEmpty()) {
            EmptyState(Icons.Rounded.Campaign, "Aún no hay campañas", "Crea una colección para avisar a tus clientes.")
        } else {
            LazyColumn(
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(rows, key = { it.campaign.id }) { row ->
                    val c = row.campaign
                    AppCard(onClick = { onEdit(c.id) }) {
                        Column(Modifier.padding(16.dp).animateContentSize(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                val (bg, fg) = when (row.phase) {
                                    CampaignPhase.ACTIVA -> MacrameColors.SuccessTint to MacrameColors.Success
                                    CampaignPhase.PROGRAMADA -> MacrameColors.WarningTint to MacrameColors.Warning
                                    CampaignPhase.BORRADOR -> MacrameColors.Inactive to MacrameColors.InactiveText
                                    CampaignPhase.FINALIZADA -> MacrameColors.Inactive to MacrameColors.Muted
                                }
                                Pill(row.phase.label, bg, fg)
                                Spacer(Modifier.weight(1f))
                                Switch(
                                    checked = c.published, onCheckedChange = { vm.setPublished(c.id, it) },
                                    colors = SwitchDefaults.colors(
                                        checkedTrackColor = MacrameColors.Forest, checkedThumbColor = MacrameColors.Card,
                                        uncheckedTrackColor = MacrameColors.Inactive, uncheckedThumbColor = MacrameColors.Muted,
                                        uncheckedBorderColor = MacrameColors.Inactive
                                    )
                                )
                            }
                            Text(c.name, style = MaterialTheme.typography.titleMedium, color = MacrameColors.Ink)
                            Text(c.theme, style = MaterialTheme.typography.bodySmall, color = MacrameColors.Forest)
                            Text(
                                "${Formatters.date(c.startDate)} – ${Formatters.date(c.endDate)} · ${row.productCount} diseños",
                                style = MaterialTheme.typography.bodySmall, color = MacrameColors.Muted
                            )
                        }
                    }
                }
                item { PrimaryButton("Nueva campaña", onNew, icon = Icons.Rounded.Add) }
            }
        }
    }
}

@Composable
fun CampaignFormScreen(campaignId: Long, onBack: () -> Unit) {
    val vm = macrameViewModel(key = "campaign_form_$campaignId") { CampaignFormViewModel(campaignId, it.repository) }
    val state by vm.state.collectAsStateWithLifecycle()
    val products by vm.products.collectAsStateWithLifecycle()

    LaunchedEffect(state.saved) { if (state.saved) onBack() }

    Column(Modifier.fillMaxSize()) {
        ScreenHeader(
            title = if (state.isNew) "Nueva campaña" else "Editar campaña",
            subtitle = "Los clientes recibirán un aviso al publicar",
            onBack = onBack
        )
        if (state.loading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MacrameColors.Forest)
            }
            return@Column
        }
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            AppTextField(
                state.name.value, vm::onName, "Nombre de la campaña", Icons.Rounded.Campaign,
                error = state.name.error, capitalization = KeyboardCapitalization.Sentences, maxLength = 50, showValid = true
            )
            AppTextField(
                state.description.value, vm::onDescription, "Descripción", Icons.Rounded.Description,
                error = state.description.error, singleLine = false, minLines = 3, maxLength = 200,
                capitalization = KeyboardCapitalization.Sentences
            )
            AppDropdownField(
                state.theme.value, FormOptions.campaignThemes, vm::onTheme, "Tema", Icons.Rounded.Palette,
                error = state.theme.error
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                DateField(state.startDate, vm::onStart, "Inicio", Modifier.weight(1f), error = state.startError)
                DateField(state.endDate, vm::onEnd, "Término", Modifier.weight(1f), error = state.endError)
            }

            Text("Diseños incluidos (${state.selected.size})", style = MaterialTheme.typography.titleMedium, color = MacrameColors.Ink)
            AppCard {
                Column(Modifier.padding(vertical = 6.dp)) {
                    products.forEach { p ->
                        val selected = p.id in state.selected
                        Row(
                            Modifier.fillMaxWidth().clickable { vm.toggleProduct(p.id) }.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            ProductImage(p, Modifier.size(44.dp), RoundedCornerShape(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(p.name, style = MaterialTheme.typography.bodyMedium, color = MacrameColors.Ink, maxLines = 1)
                                Text(p.code, style = MaterialTheme.typography.labelSmall, color = MacrameColors.Muted)
                            }
                            Icon(
                                if (selected) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked,
                                contentDescription = if (selected) "Incluido" else "No incluido",
                                tint = if (selected) MacrameColors.Forest else MacrameColors.Muted
                            )
                        }
                    }
                }
            }
            InlineError(state.productsError)

            AppCard {
                Column(Modifier.padding(16.dp)) {
                    SwitchRow("Publicar ahora", "Si está apagado queda como borrador", state.published, vm::onPublished)
                }
            }
            PrimaryButton(
                if (state.isNew) "Crear campaña" else "Guardar cambios",
                onClick = vm::save, loading = state.saving, modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(24.dp))
        }
    }
}
