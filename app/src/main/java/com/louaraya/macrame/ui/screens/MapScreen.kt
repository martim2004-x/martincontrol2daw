package com.louaraya.macrame.ui.screens

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.view.MotionEvent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Directions
import androidx.compose.material.icons.rounded.Event
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.MyLocation
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.louaraya.macrame.data.FairEntity
import com.louaraya.macrame.domain.FairType
import com.louaraya.macrame.ui.components.AppCard
import com.louaraya.macrame.ui.components.Pill
import com.louaraya.macrame.ui.components.PrimaryButton
import com.louaraya.macrame.ui.components.SageChip
import com.louaraya.macrame.ui.components.ScreenHeader
import com.louaraya.macrame.ui.components.openAppSettings
import com.louaraya.macrame.ui.components.rememberPermission
import com.louaraya.macrame.ui.theme.MacrameColors
import com.louaraya.macrame.ui.viewmodel.MapViewModel
import com.louaraya.macrame.ui.viewmodel.macrameViewModel
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

// ───────────── 7. Mapa de ferias y showrooms ─────────────

private val SantiagoCenter = GeoPoint(-33.4489, -70.6693)

@Composable
fun MapScreen(showBack: Boolean, onBack: () -> Unit) {
    val vm = macrameViewModel { MapViewModel(it.repository) }
    val state by vm.state.collectAsStateWithLifecycle()
    val location = rememberPermission(
        Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION
    )
    val context = LocalContext.current
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val lifecycle = LocalLifecycleOwner.current.lifecycle

    // cache propio
    val mapView = remember {
        Configuration.getInstance().apply {
            userAgentValue = context.packageName
            osmdroidBasePath = context.filesDir.resolve("osmdroid")
            osmdroidTileCache = context.cacheDir.resolve("osmdroid_tiles")
        }
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            controller.setZoom(12.0)
            controller.setCenter(SantiagoCenter)
            // Evita que el scroll de la pantalla robe los gestos del mapa.
            setOnTouchListener { v, event ->
                if (event.action == MotionEvent.ACTION_DOWN) v.parent?.requestDisallowInterceptTouchEvent(true)
                false
            }
        }
    }
    val locationOverlay = remember(location.granted) {
        if (location.granted) {
            MyLocationNewOverlay(GpsMyLocationProvider(context), mapView).apply { enableMyLocation() }
        } else null
    }

    DisposableEffect(lifecycle) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> { mapView.onResume(); locationOverlay?.enableMyLocation() }
                Lifecycle.Event.ON_PAUSE -> { locationOverlay?.disableMyLocation(); mapView.onPause() }
                else -> Unit
            }
        }
        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
            locationOverlay?.disableMyLocation()
            mapView.onDetach()
        }
    }

    // Reconstruye los marcadores cuando cambian los datos de feria o coleccion etc.
    LaunchedEffect(state.fairs, state.selected, locationOverlay) {
        mapView.overlays.clear()
        state.fairs.forEach { fair ->
            mapView.overlays.add(Marker(mapView).apply {
                position = GeoPoint(fair.lat, fair.lng)
                title = fair.name
                snippet = fair.place
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                setOnMarkerClickListener { _, _ -> vm.select(fair.id); true }
            })
        }
        locationOverlay?.let { mapView.overlays.add(it) }
        state.selected?.let { mapView.controller.animateTo(GeoPoint(it.lat, it.lng)) }
        mapView.invalidate()
    }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            ScreenHeader(
                title = "Ferias y showrooms",
                subtitle = "Dónde encontrar a Lou Araya",
                onBack = if (showBack) onBack else null
            )
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item { SageChip("Todas", selected = state.typeFilter == null, onClick = { vm.onTypeFilter(null) }) }
                items(FairType.all) { type ->
                    SageChip(type, selected = state.typeFilter == type, onClick = { vm.onTypeFilter(type) })
                }
            }
            Box(
                Modifier.fillMaxSize().padding(16.dp)
                    .background(MacrameColors.Card, RoundedCornerShape(24.dp))
                    .padding(4.dp)
            ) {
                AndroidView(
                    factory = { mapView },
                    modifier = Modifier.fillMaxSize()
                )
                if (!location.granted) {
                    LocationBanner(
                        onAllow = location.request,
                        onSettings = { openAppSettings(context) },
                        modifier = Modifier.align(Alignment.TopCenter).padding(8.dp)
                    )
                }
                FloatingActionButton(
                    onClick = {
                        val fix = locationOverlay?.myLocation
                        when {
                            !location.granted -> location.request()
                            fix != null -> mapView.controller.animateTo(fix)
                            else -> scope.launch { snackbar.showSnackbar("Aún no tenemos tu posición. Activa el GPS y espera unos segundos.") }
                        }
                    },
                    containerColor = MacrameColors.Forest, contentColor = MacrameColors.Card,
                    modifier = Modifier.align(Alignment.BottomEnd).padding(12.dp)
                ) { Icon(Icons.Rounded.MyLocation, contentDescription = "Centrar en mi ubicación") }

                Box(Modifier.align(Alignment.BottomStart).padding(12.dp).fillMaxWidth(0.78f)) {
                    // Llamada calificada: dentro hay Column y Box como receptores y, sin esto, el compilador se confunde.
                    androidx.compose.animation.AnimatedVisibility(
                        visible = state.selected != null,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        state.selected?.let { fair ->
                            FairCard(fair, onClose = { vm.select(null) }, onDirections = {
                                val uri = Uri.parse("geo:${fair.lat},${fair.lng}?q=${fair.lat},${fair.lng}(${Uri.encode(fair.name)})")
                                val intent = Intent(Intent.ACTION_VIEW, uri)
                                if (intent.resolveActivity(context.packageManager) != null) context.startActivity(intent)
                                else scope.launch { snackbar.showSnackbar("No hay una app de mapas instalada") }
                            })
                        }
                    }
                }
            }
        }
        SnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter).padding(16.dp))
    }
}

@Composable
private fun LocationBanner(onAllow: () -> Unit, onSettings: () -> Unit, modifier: Modifier = Modifier) {
    Surface(modifier, shape = RoundedCornerShape(16.dp), color = MacrameColors.Card, shadowElevation = 4.dp) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Activa tu ubicación", style = MaterialTheme.typography.titleSmall, color = MacrameColors.Ink)
            Text(
                "Así podemos mostrarte qué feria te queda más cerca. El mapa funciona igual sin ella.",
                style = MaterialTheme.typography.bodySmall, color = MacrameColors.Muted
            )
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                TextButton(onClick = onAllow) { Text("Permitir", color = MacrameColors.Forest) }
                TextButton(onClick = onSettings) { Text("Abrir ajustes", color = MacrameColors.Muted) }
            }
        }
    }
}

@Composable
private fun FairCard(fair: FairEntity, onClose: () -> Unit, onDirections: () -> Unit) {
    AppCard {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Pill(fair.type, MacrameColors.SageTint, MacrameColors.Forest)
                Box(Modifier.weight(1f))
                Icon(
                    Icons.Rounded.Close, contentDescription = "Cerrar",
                    tint = MacrameColors.Muted,
                    modifier = Modifier.size(22.dp).background(MacrameColors.Inactive, CircleShape)
                        .clickable(onClick = onClose).padding(3.dp)
                )
            }
            Text(fair.name, style = MaterialTheme.typography.titleMedium, color = MacrameColors.Ink)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Rounded.LocationOn, null, tint = MacrameColors.Muted, modifier = Modifier.size(16.dp))
                Text(fair.place, style = MaterialTheme.typography.bodySmall, color = MacrameColors.Muted)
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Rounded.Event, null, tint = MacrameColors.Muted, modifier = Modifier.size(16.dp))
                Text(fair.dateText, style = MaterialTheme.typography.bodySmall, color = MacrameColors.Muted)
            }
            Text(fair.description, style = MaterialTheme.typography.bodySmall, color = MacrameColors.Ink)
            PrimaryButton("Cómo llegar", onDirections, icon = Icons.Rounded.Directions)
        }
    }
}