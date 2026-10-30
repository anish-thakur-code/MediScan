package com.anish.mediscan

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

class ScanActivity : AppCompatActivity() {

    private lateinit var previewView: PreviewView
    private var imageCapture: ImageCapture? = null

    private val requestCameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Toast.makeText(this, getString(R.string.camera_permission_granted), Toast.LENGTH_SHORT).show()
            startCamera()
        } else {
            Toast.makeText(this, getString(R.string.camera_permission_denied_msg), Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_scan)

        previewView = findViewById(R.id.previewView)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        checkAndRequestCameraPermission()

        // Back button returns to HomeActivity
        findViewById<View>(R.id.btnBack)?.setOnClickListener {
            finish()
        }

        // Shutter button triggers photo capture & ML Kit OCR
        findViewById<View>(R.id.btnCaptureShutter)?.setOnClickListener {
            takePhotoAndRecognizeText()
        }

        // Gallery button fallback
        findViewById<View>(R.id.btnGallery)?.setOnClickListener {
            navigateToResult("")
        }
    }

    private fun checkAndRequestCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera()
        } else {
            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            try {
                val cameraProvider = cameraProviderFuture.get()

                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

                imageCapture = ImageCapture.Builder().build()

                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this,
                    cameraSelector,
                    preview,
                    imageCapture
                )
            } catch (exc: Exception) {
                Log.e("ScanActivity", "CameraX binding failed", exc)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun takePhotoAndRecognizeText() {
        val capture = imageCapture ?: run {
            navigateToResult("")
            return
        }

        showLoading(true)

        capture.takePicture(
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(imageProxy: ImageProxy) {
                    processImageWithMLKit(imageProxy)
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.e("ScanActivity", "Photo capture failed: ${exception.message}", exception)
                    showLoading(false)
                    Toast.makeText(this@ScanActivity, "Capture failed. Opening default result.", Toast.LENGTH_SHORT).show()
                    navigateToResult("")
                }
            }
        )
    }

    @OptIn(ExperimentalGetImage::class)
    private fun processImageWithMLKit(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage == null) {
            imageProxy.close()
            showLoading(false)
            navigateToResult("")
            return
        }

        val inputImage = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

        recognizer.process(inputImage)
            .addOnSuccessListener { visionText ->
                imageProxy.close()
                showLoading(false)
                val rawText = visionText.text
                val extractedMedicineName = cleanExtractedText(rawText)
                navigateToResult(extractedMedicineName)
            }
            .addOnFailureListener { e ->
                imageProxy.close()
                showLoading(false)
                Log.e("ScanActivity", "Text recognition failed: ${e.message}", e)
                Toast.makeText(this, "Text recognition failed", Toast.LENGTH_SHORT).show()
                navigateToResult("")
            }
    }

    private fun cleanExtractedText(rawText: String): String {
        if (rawText.isBlank()) return ""

        val lines = rawText.lines()
            .map { it.trim() }
            .filter { line ->
                // Remove empty lines, lines that are just numbers/symbols, and very short lines (< 3 chars)
                line.isNotEmpty() &&
                !line.all { char -> char.isDigit() || char.isWhitespace() || !char.isLetterOrDigit() } &&
                line.length >= 3
            }

        // Pick the most likely medicine name as the first meaningful line of reasonable length (3 to 30 chars)
        val bestMatch = lines.firstOrNull { it.length in 3..30 } ?: lines.firstOrNull()

        return bestMatch ?: ""
    }

    private fun showLoading(isLoading: Boolean) {
        val loadingOverlay = findViewById<View>(R.id.loadingOverlay)
        loadingOverlay?.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun navigateToResult(recognizedText: String) {
        val intent = Intent(this, ResultActivity::class.java).apply {
            putExtra(EXTRA_RECOGNIZED_TEXT, recognizedText)
        }
        startActivity(intent)
    }

    companion object {
        const val EXTRA_RECOGNIZED_TEXT = "EXTRA_RECOGNIZED_TEXT"
    }
}
