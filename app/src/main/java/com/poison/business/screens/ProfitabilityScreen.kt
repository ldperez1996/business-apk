package com.poison.business.screens

import androidx.compose.runtime.*
import androidx.compose.material3.*
import androidx.compose.foundation.layout.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.navigation.NavController
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfitabilityScreen(navController: NavController) {
    var purchasePrice by rememberSaveable { mutableStateOf("") }
    var salePrice by rememberSaveable { mutableStateOf("") }
    val profit = remember { derivedStateOf {
        val cost = purchasePrice.toDoubleOrNull() ?: 0.0
        val sale = salePrice.toDoubleOrNull() ?: 0.0
        if (cost > 0) ((sale - cost) / cost) * 1000 else 0.0
    }}
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Rentabilidad") },
                actions = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.Home, contentDescription = "Inicio")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            OutlinedTextField(
                value = purchasePrice,
                onValueChange = { newValue -> purchasePrice = newValue },
                label = { Text("Precio de compra") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            
            OutlinedTextField(
                value = salePrice,
                onValueChange = { newValue -> salePrice = newValue },
                label = { Text("Precio de venta") },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            
            Text(
                text = "Ganancia por 1000 pesos: ${"%.2f".format(profit.value)}",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(vertical = 16.dp)
            )
        }
    }
} 