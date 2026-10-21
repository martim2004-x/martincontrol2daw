package com.louaraya.macrame.ui.screens

import android.Manifest
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.FlashOff
import androidx.compose.material.icons.rounded.FlashOn
import androidx.compose.material.icons.rounded.QrCode2
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.louaraya.macrame.ui.components.AppTextField
import com.louaraya.macrame.ui.components.InlineError
import com.louaraya.macrame.ui.components.PrimaryButton
import com.louaraya.macrame.ui.components.RoundIconButton
import com.louaraya.macrame.ui.components.SoftButton
import com.louaraya.macrame.ui.components.openAppSettings
import com.louaraya.macrame.ui.components.rememberPermission
import com.louaraya.macrame.ui.theme.MacrameColors
import com.louaraya.macrame.ui.viewmodel.ScanOutcome
import com.louaraya.macrame.ui.viewmodel.ScannerViewModel
import com.louaraya.macrame.ui.viewmodel.macrameViewModel
import java.util.concurrent.Executors

// ───────────── 8 escaner qr ─────────────

@Composable
fun ScannerScreen(onBack: () -> Unit, onProduct: (Long) -> Unit) {
    val vm = macrameViewModel { ScannerViewModel(it.repository, it.session) }
    val outcome by vm.outcome.collectAsStateWithLifecycle()
    val camera = rememberPermission(Manifest.permission.CAMERA)
    val context = LocalContext.current
    val focus = LocalFocusManager.current
    var torchOn by rememberSaveable { mutableStateOf(false) }
    var manualCode by rememberSaveable { mutableStateOf("") }

    // Se abre detalle edl producto y abre el qr si el codigo existe.
    LaunchedEffect(outcome) {
        val result = outcome
        if (result is ScanOutcome.Found) {
            onProduct(result.product.id)
            vm.consume()
        }
    }

    Box(Modifier.fillMaxSize().background(Color.Black)) {
        if (camera.granted) {
            CameraPreview(torchOn = torchOn, onCode = vm::onCode)
            ScanFrameOverlay()
        } else {
            PermissionNeeded(onRequest = camera.request, onSettings = { openAppSettings(context) })
        }

        Row(
            Modifier.fillMaxWidth().padding(16.dp).align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            RoundIconButton(Icons.AutoMirrored.Rounded.ArrowBack, "Volver", onBack)
            if (camera.granted) {
                RoundIconButton(
                    if (torchOn) Icons.Rounded.FlashOn else Icons.Rounded.FlashOff,
                    if (torchOn) "Apagar linterna" else "Encender linterna",
                    { torchOn = !torchOn }
                )
            }
        }

        Surface(
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth(),
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            color = MacrameColors.Ash
        ) {
            Column(
                Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Escanea un código QR", style = MaterialTheme.typography.titleLarge, color = MacrameColors.Ink)
                Text(
                    "Enfoca la etiqueta de la pieza o del stand para abrir su ficha.",
                    style = MaterialTheme.typography.bodyMedium, color = MacrameColors.Muted
                )

                val notFound = outcome as? ScanOutcome.NotFound
                AnimatedVisibility(
                    visible = notFound != null,
                    enter = fadeIn(tween(200)) + expandVertically(tween(250)),
                    exit = fadeOut(tween(150)) + shrinkVertically(tween(200))
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        InlineError("No encontramos un diseño para «${notFound?.scanned.orEmpty()}». Revisa el código e inténtalo de nuevo.")
                        SoftButton("Reintentar", onClick = vm::consume)
                    }
                }

                AppTextField(
                    value = manualCode,
                    onValueChange = { manualCode = it.uppercase() },
                    label = "O escribe el código (ej. LAM-0001)",
                    leadingIcon = Icons.Rounded.QrCode2,
                    imeAction = ImeAction.Search,
                    keyboardActions = KeyboardActions(onSearch = {
                        focus.clearFocus()
                        vm.onCode(manualCode)
                    })
                )
                PrimaryButton(
                    "Buscar código",
                    onClick = { focus.clearFocus(); vm.onCode(manualCode) },
                    enabled = manualCode.isNotBlank(),
                    icon = Icons.Rounded.Search
                )
            }
        }
    }
}

@Composable
private fun PermissionNeeded(onRequest: () -> Unit, onSettings: () -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(horizontal = 32.dp).padding(bottom = 120.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            Modifier.size(80.dp).background(Color.White.copy(alpha = 0.12f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Rounded.CameraAlt, contentDescription = null, tint = Color.White, modifier = Modifier.size(36.dp))
        }
        Text(
            "Necesitamos tu cámara", style = MaterialTheme.typography.titleLarge, color = Color.White,
            modifier = Modifier.padding(top = 16.dp)
        )
        Text(
            "Solo se usa para leer códigos QR de las piezas. Si ya la rechazaste, actívala desde los ajustes de la app.",
            style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.8f),
            textAlign = TextAlign.Center, modifier = Modifier.padding(top = 8.dp, bottom = 20.dp)
        )
        PrimaryButton("Permitir cámara", onRequest)
        TextButton(onClick = onSettings) {
            Text("Abrir ajustes", color = Color.White, style = MaterialTheme.typography.labelLarge)
        }
    }
}

/** Vista previa(solo códigos QR). */
@Composable
private fun CameraPreview(torchOn: Boolean, onCode: (String) -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val latestOnCode by rememberUpdatedState(onCode)
    val previewView = remember {
        PreviewView(context).apply {
            scaleType = PreviewView.ScaleType.FILL_CENTER
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
        }
    }
    var camera by remember { mutableStateOf<Camera?>(null) }

    DisposableEffect(lifecycleOwner) {
        val executor = Executors.newSingleThreadExecutor()
        val scanner = BarcodeScanning.getClient(
            BarcodeScannerOptions.Builder().setBarcodeFormats(Barcode.FORMAT_QR_CODE).build()
        )
        val providerFuture = ProcessCameraProvider.getInstance(context)
        providerFuture.addListener({
            val provider = providerFuture.get()
            val preview = Preview.Builder().build().also { it.setSurfaceProvider(previewView.surfaceProvider) }
            val analysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also { it.setAnalyzer(executor) { proxy -> analyze(proxy, scanner) { code -> latestOnCode(code) } } }
            provider.unbindAll()
            camera = provider.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, analysis)
        }, ContextCompat.getMainExecutor(context))

        onDispose {
            runCatching { if (providerFuture.isDone) providerFuture.get().unbindAll() }
            executor.shutdown()
            scanner.close()
        }
    }

    LaunchedEffect(torchOn, camera) {
        camera?.cameraControl?.enableTorch(torchOn)
    }

    AndroidView(factory = { previewView }, modifier = Modifier.fillMaxSize())
}

@androidx.annotation.OptIn(ExperimentalGetImage::class)
private fun analyze(proxy: ImageProxy, scanner: BarcodeScanner, onCode: (String) -> Unit) {
    val media = proxy.image
    if (media == null) {
        proxy.close()
        return
    }
    val image = InputImage.fromMediaImage(media, proxy.imageInfo.rotationDegrees)
    scanner.process(image)
        .addOnSuccessListener { codes -> codes.firstOrNull()?.rawValue?.let(onCode) }
        .addOnCompleteListener { proxy.close() }
}

/** enfoque con esquinas redondeadas. */
@Composable
private fun ScanFrameOverlay() {
    val transition = rememberInfiniteTransition(label = "scanLine")
    val progress by transition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1800, easing = LinearEasing), RepeatMode.Reverse),
        label = "scanProgress"
    )
    Canvas(Modifier.fillMaxSize().graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }) {
        val frame = size.minDimension * 0.62f
        val left = (size.width - frame) / 2f
        val top = (size.height - frame) / 2f - size.height * 0.1f
        val corner = CornerRadius(44f, 44f)
        drawRect(Color.Black.copy(alpha = 0.55f))
        drawRoundRect(
            color = Color.Transparent, topLeft = Offset(left, top), size = Size(frame, frame),
            cornerRadius = corner, blendMode = BlendMode.Clear
        )
        drawRoundRect(
            color = Color.White, topLeft = Offset(left, top), size = Size(frame, frame),
            cornerRadius = corner, style = Stroke(width = 6f)
        )
        val y = top + 28f + (frame - 56f) * progress
        drawLine(
            color = MacrameColors.SageTint, start = Offset(left + 28f, y), end = Offset(left + frame - 28f, y),
            strokeWidth = 5f, cap = StrokeCap.Round
        )
    }
}
