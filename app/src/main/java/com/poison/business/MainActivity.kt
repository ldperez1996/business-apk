@file:OptIn(ExperimentalMaterial3Api::class)
package com.poison.business

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.poison.business.screens.BillCounterScreen
import com.poison.business.screens.MapScreen
import com.poison.business.screens.OrderListScreen
import com.poison.business.screens.ProfitabilityScreen
import com.yalantis.ucrop.UCrop
import java.io.File
import java.io.FileOutputStream
import com.poison.business.ui.theme.BusinessTheme

const val PERMISSIONS_REQUEST_CODE = 100

fun checkPermissions(context: Context): Boolean {
    return (context.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
            context.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Verificar y solicitar permisos en tiempo de ejecución
        if (!checkPermissions(this)) {
            requestPermissions(
                arrayOf(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ),
                PERMISSIONS_REQUEST_CODE
            )
        }
        setContent {
            BusinessTheme {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = "main") {
                    composable("main") { MainMenu(navController) }
                    composable("contador") { BillCounterScreen(navController) }
                    composable("rentabilidad") { ProfitabilityScreen(navController) }
                    composable("pedidos") { OrderListScreen(navController) }
                    composable("mapa") { MapScreen(navController) }
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            if ((grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED })) {
                Toast.makeText(this, "Permisos otorgados.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Los permisos son necesarios para usar esta función.", Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }
}

@Composable
fun MainMenu(navController: NavController) {
    var qrImageUri by remember { mutableStateOf<Uri?>(null) }
    var imageBitmap by remember { mutableStateOf<Bitmap?>(null) }
    val context = LocalContext.current

    // Estado para verificar si los permisos han sido otorgados
    var hasPermissions by remember { mutableStateOf(checkPermissions(context)) }

    LaunchedEffect(Unit) {
        hasPermissions = checkPermissions(context)
    }

    // Función para guardar la imagen localmente
    fun saveImageLocally(bitmap: Bitmap): Uri {
        val file = File(context.filesDir, "qr_image.png")
        FileOutputStream(file).use { stream ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        }
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }

    // Función para cargar la imagen guardada
    fun loadSavedImage(): Uri? {
        val file = File(context.filesDir, "qr_image.png")
        return if (file.exists()) {
            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        } else {
            null
        }
    }

    // Cargar la imagen guardada al iniciar
    LaunchedEffect(Unit) {
        if (hasPermissions) {
            val savedUri = loadSavedImage()
            if (savedUri != null) {
                try {
                    val inputStream = context.contentResolver.openInputStream(savedUri)
                    imageBitmap = BitmapFactory.decodeStream(inputStream)
                    qrImageUri = savedUri
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    // Lanzador para capturar el resultado de uCrop
    val cropLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        result.data?.let { intent ->
            val croppedUri = UCrop.getOutput(intent)
            croppedUri?.let { uri ->
                try {
                    val inputStream = context.contentResolver.openInputStream(uri)
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    qrImageUri = saveImageLocally(bitmap) // Guardar la imagen localmente
                    imageBitmap = bitmap // Actualizar el estado con la nueva imagen
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    // Lanzador para seleccionar una imagen desde la galería
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (hasPermissions) {
            uri?.let { inputUri ->
                // Iniciar uCrop para recortar la imagen
                val outputFile = File(context.cacheDir, "cropped_image.png")
                val intent = UCrop.of(inputUri, Uri.fromFile(outputFile))
                    .withAspectRatio(1f, 1f) // Aspecto cuadrado
                    .withMaxResultSize(512, 512) // Tamaño máximo del resultado
                    .getIntent(context) // Obtenemos el Intent de UCrop
                cropLauncher.launch(intent) // Lanzamos el Intent a través de cropLauncher
            }
        } else {
            Toast.makeText(context, "Se requieren permisos para acceder a la galería.", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Botones superiores en una cuadrícula
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .weight(1f) // Ocupa el espacio necesario para los botones superiores
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(
                        items = listOf(
                            "Contador de billetes" to Icons.Filled.AttachMoney,
                            "Mapa UCI" to Icons.Filled.Map,
                            "Rentabilidad" to Icons.Filled.TrendingUp,
                            "Lista de Pedidos" to Icons.Filled.List
                        )
                    ) { (title, icon) ->
                        ElevatedButton(
                            onClick = {
                                when (title) {
                                    "Contador de billetes" -> navController.navigate("contador")
                                    "Mapa UCI" -> navController.navigate("mapa")
                                    "Rentabilidad" -> navController.navigate("rentabilidad")
                                    "Lista de Pedidos" -> navController.navigate("pedidos")
                                }
                            },
                            colors = ButtonDefaults.elevatedButtonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = Color.White
                            ),
                            shape = MaterialTheme.shapes.large,
                            modifier = Modifier
                                .padding(8.dp)
                                .aspectRatio(1f)
                                .fillMaxWidth()
                                .shadow(
                                    elevation = 8.dp,
                                    shape = MaterialTheme.shapes.large,
                                    spotColor = MaterialTheme.colorScheme.primary
                                )
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = MaterialTheme.colorScheme.tertiary
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.titleLarge,
                                    color = Color.White,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(4.dp)
                                )
                            }
                        }
                    }
                }

                // Mostrar la imagen QR cargada
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f) // Asegura que ocupe el espacio disponible
                        .padding(16.dp)
                ) {
                    if (imageBitmap != null) {
                        Image(
                            bitmap = imageBitmap!!.asImageBitmap(),
                            contentDescription = "QR cargado",
                            modifier = Modifier
                                .widthIn(max = 300.dp) // Limita el ancho máximo de la imagen
                                .aspectRatio(1f) // Mantiene la relación de aspecto cuadrada
                                .align(Alignment.CenterHorizontally) // Centra la imagen horizontalmente
                                .padding(8.dp) // Márgenes internos opcionales
                        )
                    } else {
                        Text(
                            text = "No se ha cargado ninguna imagen.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp)) // Espacio entre la imagen y el botón
                    Button(
                        onClick = { galleryLauncher.launch("image/*") },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary,
                            contentColor = Color.White
                        ),
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text(text = "Cargar un nuevo QR")
                    }
                }
            }

            // Agregar la marca de agua
            Text(
                text = "Made by Poison",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = Color.Gray.copy(alpha = 0.6f) // Color gris semi-transparente
                ),
                modifier = Modifier
                    .align(Alignment.BottomEnd) // Alineación en la esquina inferior derecha
                    .padding(16.dp) // Espaciado interno
            )
        }
    }
}