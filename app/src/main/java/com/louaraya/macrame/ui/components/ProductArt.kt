package com.louaraya.macrame.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.RectangleShape
import coil.compose.AsyncImage
import com.louaraya.macrame.data.ProductEntity
import com.louaraya.macrame.domain.Category
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

private class ArtPalette(val bg: Color, val strand: Color, val accent: Color, val leaf: Color)

private val Palettes = listOf(
    ArtPalette(Color(0xFFDDE6DF), Color(0xFFF8F3E6), Color(0xFF8A6A4B), Color(0xFF6F8F76)),
    ArtPalette(Color(0xFFE9E2D2), Color(0xFFFFFFFF), Color(0xFF6B5440), Color(0xFF8FA595)),
    ArtPalette(Color(0xFF2D4030), Color(0xFFEFE7D6), Color(0xFFC9B58F), Color(0xFF8FA595)),
    ArtPalette(Color(0xFFC9D4CB), Color(0xFFFFFFFF), Color(0xFF5A4636), Color(0xFF3F5A45))
)

/**
 * (datos ficticios) cuando el administrador todavía no tomó una foto con la cámara.
 * La forma depende de la categoría y la paleta del identificador del producto.
 */
@Composable
fun ProductArt(category: String, seed: Long, modifier: Modifier = Modifier) {
    val palette = Palettes[((seed % Palettes.size) + Palettes.size).toInt() % Palettes.size]
    Canvas(modifier.background(palette.bg)) {
        when (category) {
            Category.MATRIMONIOS -> drawArch(palette)
            Category.FESTIVALES -> drawDreamCatcher(palette)
            else -> drawFringe(palette, seed)
        }
    }
}

/** Foto real si existe o la ilustración generada. */
@Composable
fun ProductImage(
    product: ProductEntity,
    modifier: Modifier = Modifier,
    shape: Shape = RectangleShape
) {
    Box(modifier.clip(shape)) {
        if (product.imageUri != null) {
            AsyncImage(
                model = product.imageUri,
                contentDescription = product.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            ProductArt(product.category, product.id, Modifier.fillMaxSize())
        }
    }
}

private fun DrawScope.drawFringe(p: ArtPalette, seed: Long) {
    val w = size.width
    val h = size.height
    val rodY = h * 0.2f
    drawLine(p.accent, Offset(w * 0.14f, rodY), Offset(w * 0.86f, rodY), strokeWidth = h * 0.014f, cap = StrokeCap.Round)
    val strands = 11
    for (i in 0 until strands) {
        val x = w * 0.2f + i * (w * 0.6f / (strands - 1))
        val wave = (sin(i * 0.9 + seed) + 1.0).toFloat() / 2f
        val length = h * (0.34f + 0.26f * wave)
        drawLine(p.strand, Offset(x, rodY), Offset(x, rodY + length), strokeWidth = w * 0.014f, cap = StrokeCap.Round)
        if (i % 2 == 0) {
            drawCircle(p.leaf, radius = w * 0.022f, center = Offset(x, rodY + length * 0.35f))
        }
        drawCircle(p.strand, radius = w * 0.012f, center = Offset(x, rodY + length))
    }
}

private fun DrawScope.drawArch(p: ArtPalette) {
    val w = size.width
    val h = size.height
    val cx = w * 0.5f
    val radius = w * 0.32f
    val cy = h * 0.52f
    drawArc(
        color = p.accent, startAngle = 180f, sweepAngle = 180f, useCenter = false,
        topLeft = Offset(cx - radius, cy - radius), size = Size(radius * 2, radius * 2),
        style = Stroke(width = w * 0.022f, cap = StrokeCap.Round)
    )
    val strands = 9
    for (k in 0 until strands) {
        val t = k / (strands - 1f)
        val angle = PI + t * PI
        val px = cx + radius * cos(angle).toFloat()
        val py = cy + radius * sin(angle).toFloat()
        val length = h * (0.16f + 0.14f * abs(t - 0.5f) * 2f)
        drawLine(p.strand, Offset(px, py), Offset(px, py + length), strokeWidth = w * 0.012f, cap = StrokeCap.Round)
        drawCircle(p.leaf, radius = w * 0.026f, center = Offset(px, py))
    }
    drawLine(p.accent, Offset(cx - radius, cy), Offset(cx - radius, h * 0.88f), strokeWidth = w * 0.022f, cap = StrokeCap.Round)
    drawLine(p.accent, Offset(cx + radius, cy), Offset(cx + radius, h * 0.88f), strokeWidth = w * 0.022f, cap = StrokeCap.Round)
}

private fun DrawScope.drawDreamCatcher(p: ArtPalette) {
    val w = size.width
    val h = size.height
    val cx = w * 0.5f
    val cy = h * 0.38f
    val radius = w * 0.24f
    drawCircle(p.accent, radius = radius, center = Offset(cx, cy), style = Stroke(width = w * 0.02f))
    for (ring in 1..3) {
        drawCircle(
            p.strand, radius = radius * (1f - ring * 0.24f), center = Offset(cx, cy),
            style = Stroke(width = w * 0.006f)
        )
    }
    for (k in 0 until 8) {
        val angle = k * PI / 4
        drawLine(
            p.strand,
            Offset(cx, cy),
            Offset(cx + radius * cos(angle).toFloat(), cy + radius * sin(angle).toFloat()),
            strokeWidth = w * 0.006f
        )
    }
    val tails = listOf(-0.14f to 0.30f, 0f to 0.40f, 0.14f to 0.32f)
    tails.forEach { (dx, len) ->
        val x = cx + w * dx
        val y0 = cy + radius
        drawLine(p.strand, Offset(x, y0), Offset(x, y0 + h * len), strokeWidth = w * 0.01f, cap = StrokeCap.Round)
        drawCircle(p.leaf, radius = w * 0.03f, center = Offset(x, y0 + h * len))
    }
}
