package com.poison.business.screens

import androidx.compose.runtime.*
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Home
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Modifier
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.navigation.NavController

data class Order(val apartment: String, val description: String, val id: Int)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderListScreen(navController: NavController) {
    val orders = rememberSaveable(
        saver = Saver<SnapshotStateList<Order>, List<List<Any>>>(
            save = { list -> list.map { listOf(it.apartment, it.description, it.id) } },
            restore = { restored ->
                mutableStateListOf<Order>().apply {
                    addAll(restored.map { item ->
                        Order(
                            apartment = item[0] as String,
                            description = item[1] as String,
                            id = (item[2] as? Int) ?: 0
                        )
                    })
                }
            }
        )
    ) { mutableStateListOf() }
    var newApartment by rememberSaveable { mutableStateOf("") }
    var newDescription by rememberSaveable { mutableStateOf("") }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Lista de Pedidos") },
                actions = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.Home, contentDescription = "Inicio")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).padding(16.dp)) {
            OutlinedTextField(
                value = newApartment,
                onValueChange = { newValue -> newApartment = newValue },
                label = { Text("Apartamento") },
                modifier = Modifier.fillMaxWidth()
            )
            
            OutlinedTextField(
                value = newDescription,
                onValueChange = { newValue -> newDescription = newValue },
                label = { Text("Descripción") },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            )
            
            Button(
                onClick = {
                    if (newApartment.isNotBlank() && newDescription.isNotBlank()) {
                        orders.add(Order(newApartment, newDescription, orders.size))
                        newApartment = ""
                        newDescription = ""
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Agregar")
                Spacer(Modifier.width(8.dp))
                Text("Agregar Pedido")
            }
            
            LazyColumn(modifier = Modifier.padding(top = 16.dp)) {
                items(orders) { order ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Apartamento: ${order.apartment}")
                                Text("Descripción: ${order.description}")
                            }
                            IconButton(onClick = { orders.remove(order) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Eliminar")
                            }
                        }
                    }
                }
            }
        }
    }
} 