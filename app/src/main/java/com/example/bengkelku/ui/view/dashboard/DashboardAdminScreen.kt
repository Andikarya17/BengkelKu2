package com.example.bengkelku.ui.view.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.bengkelku.data.remote.model.BookingResponse
import com.example.bengkelku.ui.view.components.BaseScaffold
import com.example.bengkelku.viewmodel.dashboard.AdminAksiState
import com.example.bengkelku.viewmodel.dashboard.AdminBookingState
import com.example.bengkelku.viewmodel.dashboard.DashboardAdminViewModel
import java.text.NumberFormat
import java.util.*

@Composable
fun DashboardAdminScreen(
    viewModel: DashboardAdminViewModel,
    onKelolaServis: () -> Unit,
    onKelolaSlotServis: () -> Unit
) {
    val bookingState by viewModel.bookingState.collectAsState()
    val aksiState by viewModel.aksiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Handle aksi state for snackbar
    LaunchedEffect(aksiState) {
        when (val state = aksiState) {
            is AdminAksiState.Success -> {
                snackbarHostState.showSnackbar(state.message)
                viewModel.resetAksiState()
            }
            is AdminAksiState.Error -> {
                snackbarHostState.showSnackbar(state.message)
                viewModel.resetAksiState()
            }
            else -> {}
        }
    }

    BaseScaffold(
        title = "Dashboard Admin",
        showBack = false
    ) { paddingValues ->
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(innerPadding)
                    .padding(16.dp)
            ) {
                // Admin Management Buttons
                Text("Manajemen", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onKelolaServis,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            Icons.Default.Build,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text("Kelola Servis")
                    }

                    OutlinedButton(
                        onClick = onKelolaSlotServis,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            Icons.Default.DateRange,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text("Kelola Slot")
                    }
                }

                Spacer(Modifier.height(24.dp))

                // Booking Monitoring Section
                when (val state = bookingState) {
                    is AdminBookingState.Loading -> {
                        Text(
                            "Monitoring Booking",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(Modifier.height(8.dp))
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                    is AdminBookingState.Success -> {
                        Text(
                            "Monitoring Booking (${state.data.size})",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(Modifier.height(8.dp))

                        if (state.data.isEmpty()) {
                            Card(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    "Belum ada booking masuk",
                                    modifier = Modifier.padding(16.dp),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(state.data, key = { it.id }) { booking ->
                                    BookingItemAdmin(
                                        booking = booking,
                                        onStatusChange = { newStatus ->
                                            viewModel.updateBookingStatus(booking.id, newStatus)
                                        },
                                        isLoading = aksiState is AdminAksiState.Loading
                                    )
                                }
                            }
                        }
                    }
                    is AdminBookingState.Error -> {
                        Text(
                            "Monitoring Booking",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(Modifier.height(8.dp))
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = state.message,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.error
                            )
                            Spacer(Modifier.height(16.dp))
                            Button(onClick = { viewModel.loadData() }) {
                                Text("Coba Lagi")
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BookingItemAdmin(
    booking: BookingResponse,
    onStatusChange: (String) -> Unit,
    isLoading: Boolean
) {
    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale("id", "ID")) }
    var expanded by remember { mutableStateOf(false) }

    // Normalize status - default to MENUNGGU if empty or unrecognized
    val normalizedStatus = booking.status.uppercase().ifBlank { "MENUNGGU" }
    
    // Status options based on current status (only 3 valid: MENUNGGU, DIPROSES, SELESAI)
    val statusOptions = when (normalizedStatus) {
        "MENUNGGU" -> listOf("MENUNGGU", "DIPROSES")
        "DIPROSES" -> listOf("DIPROSES", "SELESAI")
        "SELESAI" -> listOf("SELESAI")
        else -> listOf("MENUNGGU", "DIPROSES", "SELESAI")
    }

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(12.dp)) {
            // Customer name
            Text(
                "Customer: ${booking.namaPengguna ?: "N/A"}",
                style = MaterialTheme.typography.titleSmall
            )

            Spacer(Modifier.height(4.dp))

            // Vehicle info
            Text(
                "Kendaraan: ${booking.namaKendaraan ?: "N/A"}",
                style = MaterialTheme.typography.bodyMedium
            )

            // Service name
            Text(
                "Servis: ${booking.namaServis ?: "N/A"}",
                style = MaterialTheme.typography.bodyMedium
            )

            // Date & Time
            Text(
                "Jadwal: ${booking.tanggalServis} ${booking.jamServis}",
                style = MaterialTheme.typography.bodyMedium
            )

            // Queue number
            Text(
                "Antrian: ${booking.nomorAntrian}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Total
            Text(
                "Total: ${currencyFormat.format(booking.totalBiaya)}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(Modifier.height(8.dp))

            // Status dropdown
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Status:",
                    style = MaterialTheme.typography.labelMedium
                )

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { if (!isLoading) expanded = !expanded },
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 8.dp)
                ) {
                    OutlinedTextField(
                        value = normalizedStatus,
                        onValueChange = {},
                        readOnly = true,
                        enabled = !isLoading,
                        trailingIcon = {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        textStyle = MaterialTheme.typography.bodySmall,
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = when (normalizedStatus) {
                                "MENUNGGU" -> MaterialTheme.colorScheme.tertiaryContainer
                                "DIPROSES" -> MaterialTheme.colorScheme.primaryContainer
                                "SELESAI" -> MaterialTheme.colorScheme.secondaryContainer
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            }
                        )
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        statusOptions.forEach { status ->
                            DropdownMenuItem(
                                text = { Text(status) },
                                onClick = {
                                    onStatusChange(status)
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
