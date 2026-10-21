package com.louaraya.macrame.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Diamond
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Grass
import androidx.compose.material.icons.rounded.LocalFlorist
import androidx.compose.material.icons.rounded.Nightlight
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.lerp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.louaraya.macrame.data.ProductEntity
import com.louaraya.macrame.domain.RequestStatus
import com.louaraya.macrame.ui.theme.MacrameColors
import com.louaraya.macrame.util.Formatters

// ───────────── tarjeta y encabezados ─────────────

@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val colors = CardDefaults.cardColors(containerColor = MacrameColors.Card)
    val elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    val shape = RoundedCornerShape(22.dp)
    if (onClick != null) {
        Card(onClick = onClick, modifier = modifier, shape = shape, colors = colors, elevation = elevation, border = CardBorder, content = content)
    } else {
        Card(modifier = modifier, shape = shape, colors = colors, elevation = elevation, border = CardBorder, content = content)
    }
}

@Composable
fun RoundIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    container: Color = MacrameColors.Card,
    tint: Color = MacrameColors.Ink,
    size: Dp = 44.dp
) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = container,
        border = CardBorder,
        modifier = modifier.size(size)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = contentDescription, tint = tint, modifier = Modifier.size(22.dp))
        }
    }
}

/** encabezado de pantalla */
@Composable
fun ScreenHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (onBack != null) {
            RoundIconButton(Icons.AutoMirrored.Rounded.ArrowBack, "Volver", onBack)
        }
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.headlineSmall, color = MacrameColors.Ink, maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (subtitle != null) {
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MacrameColors.Muted)
            }
        }
        actions()
    }
}

@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier, action: (@Composable () -> Unit)? = null) {
    Row(
        modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text, style = MaterialTheme.typography.titleLarge, color = MacrameColors.Ink)
        action?.invoke()
    }
}

@Composable
fun EmptyState(icon: ImageVector, title: String, message: String, modifier: Modifier = Modifier) {
    Column(
        modifier.fillMaxWidth().padding(horizontal = 32.dp, vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            Modifier.size(72.dp).clip(CircleShape).background(MacrameColors.SageTint),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = MacrameColors.Forest, modifier = Modifier.size(32.dp))
        }
        Text(title, style = MaterialTheme.typography.titleMedium, color = MacrameColors.Ink, textAlign = TextAlign.Center)
        Text(message, style = MaterialTheme.typography.bodyMedium, color = MacrameColors.Muted, textAlign = TextAlign.Center)
    }
}

@Composable
fun Pill(text: String, background: Color, foreground: Color, modifier: Modifier = Modifier) {
    Box(
        modifier.clip(CircleShape).background(background).padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(text, style = MaterialTheme.typography.labelSmall, color = foreground)
    }
}

@Composable
fun StatusPill(status: String, modifier: Modifier = Modifier) {
    val (bg, fg) = when (status) {
        RequestStatus.PENDIENTE -> MacrameColors.WarningTint to MacrameColors.Warning
        RequestStatus.CONFIRMADO -> MacrameColors.SuccessTint to MacrameColors.Success
        RequestStatus.ENTREGADO -> MacrameColors.SageTint to MacrameColors.Forest
        RequestStatus.RECHAZADA -> MacrameColors.ErrorTint to MacrameColors.Error
        else -> MacrameColors.Inactive to MacrameColors.InactiveText
    }
    Pill(status, bg, fg, modifier)
}

// ───────────── Avatares ─────────────

private val AvatarIcons = listOf(
    Icons.Rounded.Spa, Icons.Rounded.LocalFlorist, Icons.Rounded.Grass,
    Icons.Rounded.WbSunny, Icons.Rounded.Nightlight, Icons.Rounded.Diamond
)
private val AvatarColors = listOf(
    Color(0xFF2D4030), Color(0xFF7F9885), Color(0xFF8C7B5E),
    Color(0xFF5E7F73), Color(0xFF3F5A45), Color(0xFFA3705A)
)
val AvatarCount: Int = AvatarIcons.size

@Composable
fun Avatar(index: Int, size: Dp, modifier: Modifier = Modifier, selected: Boolean = false) {
    val safe = ((index % AvatarCount) + AvatarCount) % AvatarCount
    val borderColor by animateColorAsState(
        if (selected) MacrameColors.Forest else Color.Transparent, tween(200), label = "avatarBorder"
    )
    val scale by animateFloatAsState(if (selected) 1.08f else 1f, spring(Spring.DampingRatioMediumBouncy), label = "avatarScale")
    Box(
        modifier
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .size(size)
            .border(3.dp, borderColor, CircleShape)
            .padding(4.dp)
            .clip(CircleShape)
            .background(AvatarColors[safe]),
        contentAlignment = Alignment.Center
    ) {
        Icon(AvatarIcons[safe], contentDescription = "Avatar ${safe + 1}", tint = Color.White, modifier = Modifier.size(size * 0.45f))
    }
}

// ───────────── barra de navegacion flotante ─────────────

data class NavItem(val route: String, val label: String, val icon: ImageVector)

/**
 * Barra inferior flotante en forma de píldora. El ítem activo se expande y muestra su etiqueta
 * con una animación de tamaño y color.
 */
@Composable
fun FloatingNavBar(
    items: List<NavItem>,
    currentRoute: String?,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 16.dp, vertical = 10.dp)) {
        Surface(
            shape = RoundedCornerShape(30.dp),
            color = MacrameColors.Forest,
            shadowElevation = 10.dp,
            modifier = Modifier.align(Alignment.Center)
        ) {
            Row(
                Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                items.forEach { item ->
                    val selected = item.route == currentRoute
                    val bg by animateColorAsState(
                        if (selected) Color.White.copy(alpha = 0.16f) else Color.Transparent, tween(220), label = "navBg"
                    )
                    Row(
                        Modifier
                            .clip(CircleShape)
                            .background(bg)
                            .clickable { onSelect(item.route) }
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                            .animateContentSize(tween(250)),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            item.icon, contentDescription = item.label,
                            tint = if (selected) Color.White else Color.White.copy(alpha = 0.6f),
                            modifier = Modifier.size(24.dp)
                        )
                        AnimatedVisibility(visible = selected) {
                            Text(
                                item.label,
                                color = Color.White,
                                style = MaterialTheme.typography.labelMedium,
                                maxLines = 1,
                                modifier = Modifier.padding(start = 6.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ───────────── Tarjeta de producto y favorito ─────────────

@Composable
fun FavoriteButton(favorite: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier, size: Dp = 36.dp) {
    val scale by animateFloatAsState(
        if (favorite) 1.12f else 1f, spring(Spring.DampingRatioHighBouncy, Spring.StiffnessMedium), label = "heart"
    )
    val tint by animateColorAsState(
        if (favorite) MacrameColors.Error else MacrameColors.Muted, tween(200), label = "heartTint"
    )
    Surface(
        onClick = onClick, shape = CircleShape, color = Color.White.copy(alpha = 0.92f),
        modifier = modifier.size(size).graphicsLayer { scaleX = scale; scaleY = scale }
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                if (favorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                contentDescription = if (favorite) "Quitar de favoritos" else "Guardar en favoritos",
                tint = tint, modifier = Modifier.size(size * 0.55f)
            )
        }
    }
}

/** Etiqueta de disponibilidad distingue piezas en stock de trabajos confeccionados a pedido. */
@Composable
fun AvailabilityPill(product: ProductEntity, modifier: Modifier = Modifier) {
    when {
        product.madeToOrder -> Pill("A pedido", MacrameColors.Inactive, MacrameColors.InactiveText, modifier)
        product.stock > 0 -> Pill("Disponible", MacrameColors.SuccessTint, MacrameColors.Success, modifier)
        else -> Pill("Agotado", MacrameColors.ErrorTint, MacrameColors.Error, modifier)
    }
}

@Composable
fun ProductCard(
    product: ProductEntity,
    imageAspect: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    favorite: Boolean? = null,
    onFavorite: (() -> Unit)? = null
) {
    AppCard(modifier = modifier, onClick = onClick) {
        Box {
            ProductImage(
                product,
                Modifier.fillMaxWidth().aspectRatio(imageAspect),
                RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp)
            )
            if (favorite != null && onFavorite != null) {
                FavoriteButton(favorite, onFavorite, Modifier.align(Alignment.TopEnd).padding(8.dp))
            }
        }
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                product.name, style = MaterialTheme.typography.titleSmall, color = MacrameColors.Ink,
                maxLines = 2, overflow = TextOverflow.Ellipsis
            )
            Text(Formatters.price(product.priceClp), style = MaterialTheme.typography.titleMedium, color = MacrameColors.Forest)
            Spacer(Modifier.height(2.dp))
            AvailabilityPill(product)
        }
    }
}

/** confirmacion que se dibuja con una animacion. */
@Composable
fun AnimatedCheck(modifier: Modifier = Modifier.size(96.dp)) {
    val scale = remember { Animatable(0f) }
    val progress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        scale.animateTo(1f, spring(dampingRatio = 0.5f, stiffness = Spring.StiffnessMedium))
        progress.animateTo(1f, tween(420))
    }
    Canvas(modifier.graphicsLayer { scaleX = scale.value; scaleY = scale.value }) {
        drawCircle(MacrameColors.Forest)
        val p1 = Offset(size.width * 0.28f, size.height * 0.52f)
        val p2 = Offset(size.width * 0.44f, size.height * 0.67f)
        val p3 = Offset(size.width * 0.72f, size.height * 0.36f)
        val first = (progress.value / 0.4f).coerceIn(0f, 1f)
        val second = ((progress.value - 0.4f) / 0.6f).coerceIn(0f, 1f)
        val stroke = size.width * 0.075f
        drawLine(Color.White, p1, lerp(p1, p2, first), strokeWidth = stroke, cap = StrokeCap.Round)
        if (second > 0f) drawLine(Color.White, p2, lerp(p2, p3, second), strokeWidth = stroke, cap = StrokeCap.Round)
    }
}
