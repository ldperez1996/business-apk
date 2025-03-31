package com.poison.business.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.material3.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.Alignment
import androidx.compose.runtime.saveable.mapSaver
import androidx.navigation.NavController
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Refresh

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillCounterScreen(navController: NavController) {
    val denominations = listOf(5, 10, 20, 50, 100, 200, 500, 1000)
    val counts = rememberSaveable(
        saver = mapSaver(
            save = { map -> mapOf("keys" to map.keys.toList(), "values" to map.values.toList()) },
            restore = { restored ->
                @Suppress("UNCHECKED_CAST")
                val keys = restored["keys"] as List<Int>
                val values = restored["values"] as List<Int>
                keys.zip(values).toMap().toMutableMap()
            }
        )
    ) { mutableStateMapOf<Int, Int>() }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Contador de billetes", 
                        style = MaterialTheme.typography.headlineSmall.copy(color = MaterialTheme.colorScheme.onPrimary)
                    ) 
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                actions = {
                    IconButton(onClick = { counts.clear() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Reiniciar")
                    }
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.Home, contentDescription = "Inicio")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            Text(
                text = "Total: ${calculateTotal(denominations, counts)} pesos",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(16.dp)
            )
            
            LazyColumn {
                items(denominations) { denomination ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Billetes de $denomination:", modifier = Modifier.weight(1f))
                        OutlinedTextField(
                            value = counts[denomination]?.takeIf { it > 0 }?.toString() ?: "",
                            onValueChange = {
                                val newValue = if (it.isBlank()) 0 else it.toIntOrNull()?.coerceAtLeast(0) ?: 0
                                counts[denomination] = newValue
                            },
                            modifier = Modifier.width(100.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }
                }
            }
        }
    }
}

private fun calculateTotal(denominations: List<Int>, counts: Map<Int, Int>): Int {
    return denominations.sumOf { denomination ->
        (counts[denomination] ?: 0) * denomination
    }
} 