package com.poison.business.screens

import android.content.Context
import android.os.Bundle
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Polygon
import org.json.JSONObject
import java.io.File

@Composable
fun MapScreen(navController: NavController) {
    val context = LocalContext.current

    // Inicializar OSMDroid
    Configuration.getInstance().load(context, context.getSharedPreferences("osmdroid", 0))

    // Usa AndroidView para integrar MapView en Jetpack Compose
    AndroidView(
        factory = { ctx ->
            MapView(ctx).apply {
                setTileSource(TileSourceFactory.MAPNIK) // Fuente de mapas de OpenStreetMap
                setBuiltInZoomControls(true) // Habilitar controles de zoom
                setMultiTouchControls(true) // Habilitar multitouch

                // Centrar el mapa en la UCI
                val uciLocation = GeoPoint(22.990278, -82.465833) // Coordenadas de la UCI
                controller.setCenter(uciLocation)
                controller.setZoom(17.0)

                // Cargar el archivo GeoJSON desde los assets
                val geoJsonString = ctx.assets.open("export.geojson").bufferedReader().use { it.readText() }
                val jsonObject = JSONObject(geoJsonString)
                val features = jsonObject.getJSONArray("features")

                for (i in 0 until features.length()) {
                    val feature = features.getJSONObject(i)
                    val geometry = feature.getJSONObject("geometry")
                    val type = geometry.getString("type")
                    val coordinates = geometry.getJSONArray("coordinates")

                    when (type) {
                        "Polygon" -> {
                            val polygonCoordinates = mutableListOf<GeoPoint>()
                            val polygonArray = coordinates.getJSONArray(0)
                            for (j in 0 until polygonArray.length()) {
                                val point = polygonArray.getJSONArray(j)
                                val lat = point.getDouble(1)
                                val lon = point.getDouble(0)
                                polygonCoordinates.add(GeoPoint(lat, lon))
                            }

                            // Dibujar el polígono en el mapa
                            val polygon = Polygon()
                            polygon.points = polygonCoordinates
                            polygon.fillColor = 0x50FF0000 // Color rojo con transparencia
                            polygon.strokeWidth = 2f
                            overlays.add(polygon)
                        }
                        "LineString" -> {
                            val lineCoordinates = mutableListOf<GeoPoint>()
                            for (j in 0 until coordinates.length()) {
                                val point = coordinates.getJSONArray(j)
                                val lat = point.getDouble(1)
                                val lon = point.getDouble(0)
                                lineCoordinates.add(GeoPoint(lat, lon))
                            }

                            // Dibujar la línea en el mapa
                            val line = org.osmdroid.views.overlay.Polyline()
                            line.setPoints(lineCoordinates)
                            line.color = 0xFF0000FF.toInt() // Color azul
                            line.width = 5f
                            overlays.add(line)
                        }
                    }
                }

                invalidate() // Actualizar el mapa
            }
        },
        modifier = androidx.compose.ui.Modifier.fillMaxSize()
    )
}