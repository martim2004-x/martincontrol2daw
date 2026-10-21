package com.louaraya.macrame.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Campaign
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.QrCodeScanner
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.louaraya.macrame.data.RequestEntity
import com.louaraya.macrame.data.UserEntity
import com.louaraya.macrame.domain.Category
import com.louaraya.macrame.domain.Role
import com.louaraya.macrame.ui.components.AppCard
import com.louaraya.macrame.ui.components.Avatar
import com.louaraya.macrame.ui.components.EmptyState
import com.louaraya.macrame.ui.components.Pill
import com.louaraya.macrame.ui.components.ProductArt
import com.louaraya.macrame.ui.components.ProductCard
import com.louaraya.macrame.ui.components.RoundIconButton
import com.louaraya.macrame.ui.components.SectionTitle
import com.louaraya.macrame.ui.components.StatusPill
import com.louaraya.macrame.ui.navigation.Routes
import com.louaraya.macrame.ui.theme.MacrameColors
import com.louaraya.macrame.ui.viewmodel.AdminStats
import com.louaraya.macrame.ui.viewmodel.FeaturedCollection
import com.louaraya.macrame.ui.viewmodel.HomeViewModel
import com.louaraya.macrame.ui.viewmodel.macrameViewModel
import com.louaraya.macrame.util.Formatters

// ───────────── 4. Principal dashboard─────────────

@Composable
fun HomeScreen(
    onNavigate: (String) -> Unit,
    onProduct: (Long) -> Unit,
    onNotifications: () -> Unit,
    onRequestDetail: () -> Unit
) {
    val vm = macrameViewModel { HomeViewModel(it.repository, it.session) }
    val user by vm.user.collectAsStateWithLifecycle()
    val unread by vm.unread.collectAsStateWithLifecycle()
    val current = user

    if (current == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = MacrameColors.Forest)
        }
        return
    }

    if (current.role == Role.ADMIN) {
        val stats by vm.adminStats.collectAsStateWithLifecycle()
        AdminHome(current, unread, stats, onNavigate, onNotifications, onRequestDetail)
    } else {
        val featured by vm.featured.collectAsStateWithLifecycle()
        val products by vm.products.collectAsStateWithLifecycle()
        val favorites by vm.favoritesCount.collectAsStateWithLifecycle()
        ClientHome(current, unread, featured, products.take(8), favorites, onNavigate, onProduct, onNotifications)
    }
}

@Composable
private fun HomeHeader(user: UserEntity, greeting: String, unread: Int, onNotifications: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Avatar(index = user.avatarIndex, size = 52.dp)
        Column(Modifier.weight(1f)) {
            Text(greeting, style = MaterialTheme.typography.bodySmall, color = MacrameColors.Muted)
            Text(
                user.fullName.substringBefore(' '),
                style = MaterialTheme.typography.headlineSmall, color = MacrameColors.Ink,
                maxLines = 1, overflow = TextOverflow.Ellipsis
            )
        }
        Box {
            RoundIconButton(Icons.Rounded.Notifications, "Notificaciones", onNotifications)
            if (unread > 0) {
                Box(
                    Modifier
                        .align(Alignment.TopEnd)
                        .size(13.dp)
                        .background(MacrameColors.Error, CircleShape)
                        .border(2.dp, MacrameColors.Ash, CircleShape)
                )
            }
        }
    }
}

// ───────────── vista del cliente ─────────────

@Composable
private fun ClientHome(
    user: UserEntity,
    unread: Int,
    featured: FeaturedCollection?,
    products: List<com.louaraya.macrame.data.ProductEntity>,
    favorites: Int,
    onNavigate: (String) -> Unit,
    onProduct: (Long) -> Unit,
    onNotifications: () -> Unit
) {
    val appear = remember { MutableTransitionState(false).apply { targetState = true } }
    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item { HomeHeader(user, "Hola, qué lindo tenerte aquí", unread, onNotifications) }

        item {
            Surface(
                onClick = { onNavigate(Routes.CATALOG) },
                shape = RoundedCornerShape(18.dp), color = MacrameColors.Card,
                border = com.louaraya.macrame.ui.components.CardBorder,
                modifier = Modifier.padding(horizontal = 20.dp).fillMaxWidth()
            ) {
                Row(
                    Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(Icons.Rounded.Search, contentDescription = null, tint = MacrameColors.Muted)
                    Text("Buscar piezas, materiales o códigos", color = MacrameColors.Muted, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        item {
            AnimatedVisibility(
                visibleState = appear,
                enter = fadeIn(tween(600)) + slideInVertically(tween(600)) { it / 6 }
            ) {
                FeaturedHero(featured, onClick = { onNavigate(Routes.CATALOG) })
            }
        }

        item {
            Column(Modifier.padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SectionTitle("Accesos rápidos")
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    QuickTile(Icons.Rounded.Category, "Catálogo", null, Modifier.weight(1f)) { onNavigate(Routes.CATALOG) }
                    QuickTile(Icons.Rounded.QrCodeScanner, "Escáner QR", null, Modifier.weight(1f)) { onNavigate(Routes.SCANNER) }
                    QuickTile(Icons.Rounded.Map, "Mapa", null, Modifier.weight(1f)) { onNavigate(Routes.MAP) }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    QuickTile(Icons.Rounded.Favorite, "Favoritos", if (favorites > 0) favorites.toString() else null, Modifier.weight(1f)) { onNavigate(Routes.FAVORITES) }
                    QuickTile(Icons.Rounded.ReceiptLong, "Solicitudes", null, Modifier.weight(1f)) { onNavigate(Routes.REQUESTS) }
                    QuickTile(Icons.Rounded.Person, "Perfil", null, Modifier.weight(1f)) { onNavigate(Routes.PROFILE) }
                }
            }
        }

        item {
            Column(Modifier.padding(horizontal = 20.dp)) {
                SectionTitle(
                    "Piezas destacadas",
                    action = {
                        TextButton(onClick = { onNavigate(Routes.CATALOG) }) {
                            Text("Ver todo", color = MacrameColors.Forest, style = MaterialTheme.typography.labelLarge)
                        }
                    }
                )
            }
        }

        item {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(products, key = { it.id }) { product ->
                    ProductCard(
                        product = product, imageAspect = 0.85f,
                        onClick = { onProduct(product.id) },
                        modifier = Modifier.width(168.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun FeaturedHero(featured: FeaturedCollection?, onClick: () -> Unit) {
    val shape = RoundedCornerShape(30.dp)
    Box(
        Modifier
            .padding(horizontal = 20.dp)
            .fillMaxWidth()
            .height(210.dp)
            .clip(shape)
            .clickable(onClick = onClick)
    ) {
        val campaign = featured?.campaign
        ProductArt(campaign?.theme ?: Category.INTERIORES, (campaign?.id ?: 1L) + 2, Modifier.fillMaxSize())
        Box(
            Modifier.fillMaxSize().background(
                Brush.verticalGradient(listOf(Color.Transparent, MacrameColors.Forest.copy(alpha = 0.88f)))
            )
        )
        Column(Modifier.align(Alignment.BottomStart).padding(20.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Pill(
                if (campaign != null) "Colección destacada" else "Catálogo",
                Color.White.copy(alpha = 0.92f), MacrameColors.Forest
            )
            Text(
                campaign?.name ?: "Explora todas las piezas",
                style = MaterialTheme.typography.titleLarge, color = Color.White
            )
            Text(
                if (campaign != null && featured != null)
                    "${featured.products.size} piezas · hasta ${Formatters.date(campaign.endDate)}"
                else "Diseños en fibras naturales hechos a mano",
                style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.85f)
            )
        }
    }
}

@Composable
private fun QuickTile(
    icon: ImageVector,
    label: String,
    badge: String?,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    AppCard(modifier = modifier, onClick = onClick) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Box(
                    Modifier.size(40.dp).clip(CircleShape).background(MacrameColors.SageTint),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = MacrameColors.Forest, modifier = Modifier.size(22.dp))
                }
                if (badge != null) Pill(badge, MacrameColors.Forest, Color.White)
            }
            Text(label, style = MaterialTheme.typography.labelLarge, color = MacrameColors.Ink, maxLines = 1)
        }
    }
}

// ───────────── vista admin ─────────────

@Composable
private fun AdminHome(
    user: UserEntity,
    unread: Int,
    stats: AdminStats,
    onNavigate: (String) -> Unit,
    onNotifications: () -> Unit,
    onRequestDetail: () -> Unit
) {
    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item { HomeHeader(user, "Panel de administración", unread, onNotifications) }

        item {
            Row(
                Modifier.padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(stats.pending, "Solicitudes pendientes", Modifier.weight(1f))
                StatCard(stats.activeProducts, "Productos activos", Modifier.weight(1f))
                StatCard(stats.activeCampaigns, "Campañas activas", Modifier.weight(1f))
            }
        }

        item {
            Column(Modifier.padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SectionTitle("Gestión")
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    QuickTile(Icons.Rounded.Inventory2, "Productos", null, Modifier.weight(1f)) { onNavigate(Routes.ADMIN_PRODUCTS) }
                    QuickTile(Icons.Rounded.Campaign, "Campañas", null, Modifier.weight(1f)) { onNavigate(Routes.ADMIN_CAMPAIGNS) }
                    QuickTile(Icons.Rounded.ReceiptLong, "Solicitudes", if (stats.pending > 0) stats.pending.toString() else null, Modifier.weight(1f)) { onNavigate(Routes.REQUESTS) }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    QuickTile(Icons.Rounded.Category, "Catálogo", null, Modifier.weight(1f)) { onNavigate(Routes.CATALOG) }
                    QuickTile(Icons.Rounded.Map, "Mapa", null, Modifier.weight(1f)) { onNavigate(Routes.MAP) }
                    QuickTile(Icons.Rounded.QrCodeScanner, "Escáner QR", null, Modifier.weight(1f)) { onNavigate(Routes.SCANNER) }
                }
            }
        }

        item {
            Column(Modifier.padding(horizontal = 20.dp)) {
                SectionTitle("Pendientes de revisión")
            }
        }

        if (stats.pendingRequests.isEmpty()) {
            item {
                EmptyState(
                    Icons.Rounded.ReceiptLong, "Todo al día",
                    "No hay solicitudes pendientes por revisar en este momento."
                )
            }
        } else {
            items(stats.pendingRequests, key = { it.id }) { request ->
                PendingRequestRow(request, onRequestDetail, Modifier.padding(horizontal = 20.dp))
            }
        }
    }
}

@Composable
private fun StatCard(value: Int, label: String, modifier: Modifier = Modifier) {
    val animated by animateIntAsState(value, tween(700), label = "stat")
    AppCard(modifier) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(animated.toString(), style = MaterialTheme.typography.headlineMedium, color = MacrameColors.Forest)
            Text(label, style = MaterialTheme.typography.bodySmall, color = MacrameColors.Muted, maxLines = 2)
        }
    }
}

@Composable
private fun PendingRequestRow(request: RequestEntity, onClick: () -> Unit, modifier: Modifier = Modifier) {
    AppCard(modifier = modifier.fillMaxWidth(), onClick = onClick) {
        Row(
            Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    "${Formatters.requestCode(request.id)} · ${request.clientName}",
                    style = MaterialTheme.typography.titleSmall, color = MacrameColors.Ink,
                    maxLines = 1, overflow = TextOverflow.Ellipsis
                )
                Text(
                    request.productName ?: "Diseño personalizado",
                    style = MaterialTheme.typography.bodySmall, color = MacrameColors.Muted
                )
                Text(
                    Formatters.relative(request.createdAt),
                    style = MaterialTheme.typography.bodySmall, color = MacrameColors.Muted
                )
            }
            StatusPill(request.status)
            Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = MacrameColors.Muted)
        }
    }
}
