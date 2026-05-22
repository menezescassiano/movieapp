@file:Suppress("COMPOSE_APPLIER_CALL_MISMATCH")

package com.example.movieapp.screens.qrcode

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.ImageProxy
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.movieapp.ui.theme.AccentPurple
import com.example.movieapp.ui.theme.AppBackground
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QrCodeReaderScreen(
    navController: NavController,
    onQrDetected: (String) -> Unit,
    viewModel: QrCodeReaderViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA,
            ) == PackageManager.PERMISSION_GRANTED,
        )
    }

    val showCheck by viewModel.showCheck.collectAsStateWithLifecycle()
    val showError by viewModel.showError.collectAsStateWithLifecycle()
    val scanResult by viewModel.scanResult.collectAsStateWithLifecycle()

    val permissionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
            onResult = { granted -> hasCameraPermission = granted },
        )

    LaunchedEffect(scanResult) {
        when (val result = scanResult) {
            is QrCodeReaderUiState.Success -> {
                delay(1_000)
                viewModel.reset()
                onQrDetected(result.content)
            }

            is QrCodeReaderUiState.NotFound -> {
                delay(1_000)
                viewModel.reset()
            }

            else -> {
                Unit
            }
        }
    }

    LaunchedEffect(hasCameraPermission) {
        viewModel.onCameraPermissionChanged(hasCameraPermission)
    }

    val controller =
        remember {
            LifecycleCameraController(context).apply {
                setEnabledUseCases(CameraController.IMAGE_ANALYSIS or CameraController.IMAGE_CAPTURE)
            }
        }

    val barcodeScanner =
        remember {
            val options =
                BarcodeScannerOptions
                    .Builder()
                    .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                    .build()
            BarcodeScanning.getClient(options)
        }

    DisposableEffect(controller, barcodeScanner, hasCameraPermission) {
        if (hasCameraPermission) {
            controller.setImageAnalysisAnalyzer(
                ContextCompat.getMainExecutor(context),
            ) { imageProxy ->
                processFrame(
                    imageProxy = imageProxy,
                    scanner = barcodeScanner,
                    onResult = { result ->
                        viewModel.onQrDecoded(result)
                    },
                )
            }
            controller.bindToLifecycle(lifecycleOwner)
        }

        onDispose {
            controller.clearImageAnalysisAnalyzer()
            barcodeScanner.close()
        }
    }

    Scaffold(
        containerColor = AppBackground,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Scan QR code") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                        )
                    }
                },
                colors =
                    TopAppBarDefaults.topAppBarColors(
                        containerColor = AppBackground,
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White,
                    ),
            )
        },
    ) { innerPadding ->
        if (!hasCameraPermission) {
            PermissionRationale(
                onRequestAgain = {
                    permissionLauncher.launch(Manifest.permission.CAMERA)
                },
                modifier =
                    Modifier
                        .padding(innerPadding)
                        .fillMaxSize(),
            )
        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
            ) {
                AndroidView(
                    modifier =
                        Modifier
                            .padding(innerPadding)
                            .fillMaxSize(),
                    factory = { ctx ->
                        PreviewView(ctx).apply {
                            scaleType = PreviewView.ScaleType.FILL_CENTER
                            this.controller = controller
                        }
                    },
                    update = { preview ->
                        preview.controller = controller
                    },
                )
                // QR guide overlay
                QrScannerOverlay(
                    modifier = Modifier.fillMaxSize(),
                    borderColor = AccentPurple,
                )

                Box(
                    modifier = Modifier.align(Alignment.Center),
                ) {
                    if (showCheck) {
                        StatusBubble(
                            backgroundColor = AccentPurple,
                            icon = Icons.Default.Check,
                            contentDescription = "QR code read successfully",
                            message = "Success",
                        )
                    } else if (showError) {
                        StatusBubble(
                            backgroundColor = Color(0xFFCF6679),
                            icon = Icons.Default.Error,
                            contentDescription = "QR code not found",
                            message = "Not Found",
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PermissionRationale(
    onRequestAgain: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.background(AppBackground),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Camera permission is required to scan QR codes.",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onRequestAgain,
            colors =
                ButtonDefaults.buttonColors(
                    containerColor = AccentPurple,
                    contentColor = Color.White,
                ),
        ) {
            Text("Grant permission")
        }
    }
}

@SuppressLint("UnsafeOptInUsageError")
private fun processFrame(
    imageProxy: ImageProxy,
    scanner: BarcodeScanner,
    onResult: (String) -> Unit,
) {
    val mediaImage = imageProxy.image
    if (mediaImage == null) {
        imageProxy.close()
        return
    }

    val inputImage =
        InputImage.fromMediaImage(
            mediaImage,
            imageProxy.imageInfo.rotationDegrees,
        )

    scanner
        .process(inputImage)
        .addOnSuccessListener { barcodes ->
            val value = barcodes.firstOrNull()?.rawValue
            if (!value.isNullOrEmpty()) {
                onResult(value)
            }
        }.addOnFailureListener { /* ignore */ }
        .addOnCompleteListener { imageProxy.close() }
}
