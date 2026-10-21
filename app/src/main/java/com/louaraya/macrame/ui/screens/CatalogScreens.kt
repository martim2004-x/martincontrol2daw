package com.louaraya.macrame.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Layers
import androidx.compose.material.icons.rounded.QrCodeScanner
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SearchOff
import androidx.compose.material.icons.rounded.Straighten
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.louaraya.macrame.domain.Category
import com.louaraya.macrame.ui.components.AppCard
import com.louaraya.macrame.ui.components.AppTextField
import com.louaraya.macrame.ui.components.AvailabilityPill
import com.louaraya.macrame.ui.components.EmptyState
import com.louaraya.macrame.ui.components.FavoriteButton
import com.louaraya.macrame.ui.components.Pill
import com.louaraya.macrame.ui.components.PrimaryButton
import com.louaraya.macrame.ui.components.ProductCard
import com.louaraya.macrame.ui.components.ProductImage
import com.louaraya.macrame.ui.components.RoundIconButton
import com.louaraya.macrame.ui.components.SageChip
import com.louaraya.macrame.ui.components.ScreenHeader
import com.louaraya.macrame.ui.theme.MacrameColors
import com.louaraya.macrame.ui.viewmodel.CatalogViewModel
import com.louaraya.macrame.ui.viewmodel.FavoritesViewModel
import com.louaraya.macrame.ui.viewmodel.ProductDetailViewModel
import com.louaraya.macrame.ui.viewmodel.macrameViewModel
import com.louaraya.macrame.util.Formatters

private val CardAspects = listOf(0.8f, 1f, 0.9f, 1.1f)

// ───────────── 6. catalogo diseños─────────────

@Composable
fun CatalogScreen(
    showBack: Boolean,
    onBack: () -> Unit,
    onProduct: (Long) -> Unit,
    onFavorites: () -> Unit,
    onScan: () -> Unit
) {
    val vm = macrameViewModel { CatalogViewModel(it.repository, it.session) }
    val state by vm.state.collectAsStateWithLifecycle()
    val focus = LocalFocusManager.current

    Column(Modifier.fillMaxSize()) {
        ScreenHeader(
            title = "Catálogo",
            subtitle = "${state.products.size} diseños artesanales",
            onBack = if (showBack) onBack else null,
            actions = {
                RoundIconButton(Icons.Rounded.QrCodeScanner, "Escanear código QR", onScan)
                if (!state.isAdmin) {
                    RoundIconButton(Icons.Rounded.Favorite, "Ver favoritos", onFavorites, tint = MacrameColors.Error)
                }
            }
        )
        AppTextField(
            value = state.query,
            onValueChange = vm::onQuery,
            label = "Buscar diseños",
            leadingIcon = Icons.Rounded.Search,
            imeAction = ImeAction.Search,
            keyboardActions = KeyboardActions(onSearch = { focus.clearFocus() }),
            modifier = Modifier.padding(horizontal = 20.dp)
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(Category.all) { category ->
                SageChip(category, selected = state.category == category, onClick = { vm.onCategory(category) })
            }
        }

        if (state.products.isEmpty() && !state.loading) {
            EmptyState(
                Icons.Rounded.SearchOff, "Sin resultados",
                "No encontramos diseños con esos filtros. Prueba con otra búsqueda o categoría."
            )
        } else {
            LazyVerticalStaggeredGrid(
                columns = StaggeredGridCells.Fixed(2),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 24.dp),
                verticalItemSpacing = 12.dp,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                itemsIndexed(state.products, key = { _, product -> product.id }) { index, product ->
                    ProductCard(
                        product = product,
                        imageAspect = CardAspects[index % CardAspects.size],
                        onClick = { onProduct(product.id) },
                        favorite = if (state.isAdmin) null else product.id in state.favorites,
                        onFavorite = if (state.isAdmin) null else ({ vm.toggleFavorite(product.id) }),
                        modifier = Modifier.animateItem()
                    )
                }
            }
        }
    }
}

// ───────────── 7. Detalle del producto ─────────────

@Composable
fun ProductDetailScreen(
    productId: Long,
    onBack: () -> Unit,
    onRequest: (Long) -> Unit,
    onEdit: (Long) -> Unit
) {
    val vm = macrameViewModel(key = "product-$productId") {
        ProductDetailViewModel(productId, it.repository, it.session)
    }
    val state by vm.state.collectAsStateWithLifecycle()
    val product = state.product

    if (state.loading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = MacrameColors.Forest)
        }
        return
    }
    if (product == null) {
        Column(Modifier.fillMaxSize()) {
            ScreenHeader("Producto", onBack = onBack)
            EmptyState(Icons.Rounded.SearchOff, "Producto no disponible", "Esta pieza ya no existe en el catálogo.")
        }
        return
    }

    Column(Modifier.fillMaxSize()) {
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Box(
                Modifier
                    .padding(horizontal = 20.dp, vertical = 12.dp)
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(32.dp))
            ) {
                ProductImage(product, Modifier.fillMaxSize())
                RoundIconButton(
                    Icons.AutoMirrored.Rounded.ArrowBack, "Volver", onBack,
                    modifier = Modifier.align(Alignment.TopStart).padding(14.dp)
                )
                if (!state.isAdmin) {
                    FavoriteButton(
                        favorite = state.isFavorite, onClick = vm::toggleFavorite,
                        modifier = Modifier.align(Alignment.TopEnd).padding(14.dp), size = 44.dp
                    )
                }
            }

            Column(Modifier.padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        product.name, style = MaterialTheme.typography.headlineSmall,
                        color = MacrameColors.Ink, modifier = Modifier.weight(1f)
                    )
                    Pill(product.category, MacrameColors.SageTint, MacrameColors.Forest, Modifier.padding(top = 6.dp))
                }
                Text(Formatters.price(product.priceClp), style = MaterialTheme.typography.headlineMedium, color = MacrameColors.Forest)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    AvailabilityPill(product)
                    Text(
                        when {
                            product.madeToOrder -> "Se confecciona según tu pedido"
                            product.stock > 0 -> "${product.stock} unidades en stock"
                            else -> "Sin stock por ahora"
                        },
                        style = MaterialTheme.typography.bodySmall, color = MacrameColors.Muted
                    )
                    if (!product.active) Pill("Inactivo", MacrameColors.WarningTint, MacrameColors.Warning)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    SpecItem(Icons.Rounded.Straighten, "Dimensiones", Formatters.dimensions(product.widthCm, product.heightCm), Modifier.weight(1f))
                    SpecItem(Icons.Rounded.Layers, "Material", product.material, Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    SpecItem(Icons.Rounded.Category, "Categoría", product.category, Modifier.weight(1f))
                    SpecItem(Icons.Rounded.QrCodeScanner, "Código QR", product.code, Modifier.weight(1f))
                }

                Text("Descripción", style = MaterialTheme.typography.titleLarge, color = MacrameColors.Ink)
                Text(product.description, style = MaterialTheme.typography.bodyLarge, color = MacrameColors.InactiveText)
                Spacer(Modifier.height(8.dp))
            }
        }

        Surface(color = MacrameColors.Card, shadowElevation = 8.dp) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (state.isAdmin) {
                    PrimaryButton("Editar producto", onClick = { onEdit(product.id) }, icon = Icons.Rounded.Edit)
                } else {
                    PrimaryButton(
                        if (product.madeToOrder) "Solicitar confección" else "Cotizar o personalizar",
                        onClick = { onRequest(product.id) }, icon = Icons.Rounded.Edit
                    )
                }
            }
        }
    }
}

@Composable
private fun SpecItem(icon: ImageVector, label: String, value: String, modifier: Modifier = Modifier) {
    AppCard(modifier) {
        Row(
            Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                Modifier.size(36.dp).clip(CircleShape).background(MacrameColors.SageTint),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = MacrameColors.Forest, modifier = Modifier.size(20.dp))
            }
            Column {
                Text(label, style = MaterialTheme.typography.bodySmall, color = MacrameColors.Muted)
                Text(value, style = MaterialTheme.typography.titleSmall, color = MacrameColors.Ink, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

// ───────────── 9. Coleccion para los fav ─────────────

@Composable
fun FavoritesScreen(
    onBack: () -> Unit,
    onProduct: (Long) -> Unit,
    onRequest: (Long) -> Unit
) {
    val vm = macrameViewModel { FavoritesViewModel(it.repository, it.session) }
    val state by vm.state.collectAsStateWithLifecycle()

    Column(Modifier.fillMaxSize()) {
        ScreenHeader("Mis favoritos", subtitle = "${state.total} diseños guardados", onBack = onBack)
        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(Category.all) { category ->
                SageChip(category, selected = state.category == category, onClick = { vm.onCategory(category) })
            }
        }
        if (state.products.isEmpty()) {
            EmptyState(
                Icons.Rounded.FavoriteBorder,
                if (state.total == 0) "Aún no tienes favoritos" else "Sin favoritos en esta categoría",
                "Toca el corazón en cualquier diseño del catálogo para guardarlo aquí. Tus favoritos son solo de tu perfil."
            )
        } else {
            LazyColumn(
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.products, key = { it.id }) { product ->
                    AppCard(Modifier.fillMaxWidth().animateItem(), onClick = { onProduct(product.id) }) {
                        Row(
                            Modifier.padding(10.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ProductImage(product, Modifier.size(92.dp), RoundedCornerShape(16.dp))
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                Text(
                                    product.name, style = MaterialTheme.typography.titleSmall, color = MacrameColors.Ink,
                                    maxLines = 2, overflow = TextOverflow.Ellipsis
                                )
                                Text(product.category, style = MaterialTheme.typography.bodySmall, color = MacrameColors.Muted)
                                Text(Formatters.price(product.priceClp), style = MaterialTheme.typography.titleMedium, color = MacrameColors.Forest)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    TextButton(onClick = { onRequest(product.id) }, contentPadding = PaddingValues(horizontal = 0.dp)) {
                                        Text("Cotizar", color = MacrameColors.Forest, style = MaterialTheme.typography.labelLarge)
                                    }
                                }
                            }
                            RoundIconButton(Icons.Rounded.Delete, "Quitar de favoritos", { vm.remove(product.id) }, size = 40.dp, tint = MacrameColors.Error)
                        }
                    }
                }
            }
        }
    }
}
