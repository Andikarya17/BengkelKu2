package com.example.bengkelku.ui.view.slotservis

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.bengkelku.data.local.entity.SlotServis
import com.example.bengkelku.ui.view.components.BaseScaffold
import com.example.bengkelku.viewmodel.slotservis.AksiSlotState
import com.example.bengkelku.viewmodel.slotservis.SlotServisViewModel
import java.util.Calendar

@Composable
fun KelolaSlotServisScreen(
    viewModel: SlotServisViewModel,
    onBack: () -> Unit
) {
    val semuaSlot by viewModel.semuaSlot.collectAsState()
    val aksiState by viewModel.aksiState.collectAsState()
    val slotUntukEdit by viewModel.slotUntukEdit.collectAsState()

    var showTambahDialog by remember { mutableStateOf(false) }
    var showHapusDialog by remember { mutableStateOf<SlotServis?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(aksiState) {
        when (val state = aksiState) {
            is AksiSlotState.Berhasil -> {
                snackbarHostState.showSnackbar(state.pesan)
                viewModel.resetState()
            }
            is AksiSlotState.Gagal -> {
                snackbarHostState.showSnackbar(state.pesan)
                viewModel.resetState()
            }
            else -> {}
        }
    }

    BaseScaffold(
        title = "Kelola Slot Servis",
        showBack = true,
        onBack = onBack
    ) { paddingValues ->
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { showTambahDialog = true }
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Tambah Slot")
                }
            }
        ) { innerPadding ->
            if (semuaSlot.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Belum ada slot servis",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(innerPadding)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(semuaSlot, key = { it.id }) { slot ->
                        SlotServisItemCard(
                            slot = slot,
                            onEdit = { viewModel.pilihUntukEdit(slot) },
                            onHapus = { showHapusDialog = slot }
                        )
                    }
                }
            }
        }
    }

    // Dialog Tambah Slot
    if (showTambahDialog) {
        SlotFormDialog(
            title = "Tambah Slot",
            onDismiss = { showTambahDialog = false },
            onSimpan = { tanggal, jamMulai, jamSelesai, kapasitas ->
                viewModel.tambahSlot(tanggal, jamMulai, jamSelesai, kapasitas)
                showTambahDialog = false
            }
        )
    }

    // Dialog Edit Slot
    slotUntukEdit?.let { slot ->
        SlotFormDialog(
            title = "Edit Slot",
            initialTanggal = slot.tanggal,
            initialJamMulai = slot.jamMulai,
            initialJamSelesai = slot.jamSelesai,
            initialKapasitas = slot.kapasitas.toString(),
            onDismiss = { viewModel.batalEdit() },
            onSimpan = { tanggal, jamMulai, jamSelesai, kapasitas ->
                viewModel.updateSlot(
                    slot.copy(
                        tanggal = tanggal,
                        jamMulai = jamMulai,
                        jamSelesai = jamSelesai,
                        kapasitas = kapasitas
                    )
                )
            }
        )
    }

    // Dialog Konfirmasi Hapus
    showHapusDialog?.let { slot ->
        AlertDialog(
            onDismissRequest = { showHapusDialog = null },
            title = { Text("Hapus Slot") },
            text = {
                Text("Yakin ingin menghapus slot tanggal ${slot.tanggal} (${slot.jamMulai} - ${slot.jamSelesai})?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.hapusSlot(slot)
                        showHapusDialog = null
                    }
                ) {
                    Text("Hapus", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showHapusDialog = null }) {
                    Text("Batal")
                }
            }
        )
    }
}

@Composable
private fun SlotServisItemCard(
    slot: SlotServis,
    onEdit: () -> Unit,
    onHapus: () -> Unit
) {
    val sisaSlot = slot.kapasitas - slot.terpakai
    val isAktif = sisaSlot > 0

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    // Tanggal
                    Text(
                        text = slot.tanggal,
                        style = MaterialTheme.typography.titleMedium
                    )

                    // Jam mulai - jam selesai
                    Text(
                        text = "${slot.jamMulai} – ${slot.jamSelesai}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(Modifier.height(4.dp))

                    // Kapasitas total
                    Text(
                        text = "Kapasitas: ${slot.kapasitas}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Sisa slot
                    Text(
                        text = "Sisa: $sisaSlot",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (sisaSlot > 0)
                            MaterialTheme.colorScheme.tertiary
                        else
                            MaterialTheme.colorScheme.error
                    )
                }

                Row {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                    IconButton(onClick = onHapus) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Hapus",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // Status indicator
            Surface(
                color = if (isAktif)
                    MaterialTheme.colorScheme.primaryContainer
                else
                    MaterialTheme.colorScheme.errorContainer,
                shape = MaterialTheme.shapes.small
            ) {
                Text(
                    text = if (isAktif) "Aktif" else "Penuh",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isAktif)
                        MaterialTheme.colorScheme.onPrimaryContainer
                    else
                        MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun SlotFormDialog(
    title: String,
    initialTanggal: String = "",
    initialJamMulai: String = "",
    initialJamSelesai: String = "",
    initialKapasitas: String = "1",
    onDismiss: () -> Unit,
    onSimpan: (tanggal: String, jamMulai: String, jamSelesai: String, kapasitas: Int) -> Unit
) {
    val context = LocalContext.current
    val calendar = remember { Calendar.getInstance() }

    var tanggal by remember { mutableStateOf(initialTanggal) }
    var jamMulai by remember { mutableStateOf(initialJamMulai) }
    var jamSelesai by remember { mutableStateOf(initialJamSelesai) }
    var kapasitas by remember { mutableStateOf(initialKapasitas) }

    var tanggalError by remember { mutableStateOf(false) }
    var jamMulaiError by remember { mutableStateOf(false) }
    var jamSelesaiError by remember { mutableStateOf(false) }
    var kapasitasError by remember { mutableStateOf(false) }
    var waktuError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // ===== TANGGAL =====
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            DatePickerDialog(
                                context,
                                { _, year, month, day ->
                                    tanggal = String.format(
                                        "%04d-%02d-%02d",
                                        year, month + 1, day
                                    )
                                    tanggalError = false
                                },
                                calendar.get(Calendar.YEAR),
                                calendar.get(Calendar.MONTH),
                                calendar.get(Calendar.DAY_OF_MONTH)
                            ).show()
                        }
                ) {
                    OutlinedTextField(
                        value = tanggal,
                        onValueChange = {},
                        label = { Text("Tanggal") },
                        readOnly = true,
                        enabled = false,
                        isError = tanggalError,
                        supportingText = if (tanggalError) {
                            { Text("Tanggal wajib diisi") }
                        } else null,
                        trailingIcon = {
                            Icon(
                                Icons.Default.DateRange,
                                contentDescription = null
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // ===== JAM MULAI =====
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val hour = if (jamMulai.isNotBlank()) {
                                jamMulai.split(":").getOrNull(0)?.toIntOrNull() ?: 9
                            } else 9
                            val minute = if (jamMulai.isNotBlank()) {
                                jamMulai.split(":").getOrNull(1)?.toIntOrNull() ?: 0
                            } else 0

                            TimePickerDialog(
                                context,
                                { _, h, m ->
                                    jamMulai = String.format("%02d:%02d", h, m)
                                    jamMulaiError = false
                                    waktuError = false
                                },
                                hour, minute, true
                            ).show()
                        }
                ) {
                    OutlinedTextField(
                        value = jamMulai,
                        onValueChange = {},
                        label = { Text("Jam Mulai") },
                        readOnly = true,
                        enabled = false,
                        isError = jamMulaiError,
                        supportingText = if (jamMulaiError) {
                            { Text("Jam mulai wajib diisi") }
                        } else null,
                        trailingIcon = {
                            Icon(
                                Icons.Default.Schedule,
                                contentDescription = null
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // ===== JAM SELESAI =====
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val hour = if (jamSelesai.isNotBlank()) {
                                jamSelesai.split(":").getOrNull(0)?.toIntOrNull() ?: 10
                            } else 10
                            val minute = if (jamSelesai.isNotBlank()) {
                                jamSelesai.split(":").getOrNull(1)?.toIntOrNull() ?: 0
                            } else 0

                            TimePickerDialog(
                                context,
                                { _, h, m ->
                                    jamSelesai = String.format("%02d:%02d", h, m)
                                    jamSelesaiError = false
                                    waktuError = false
                                },
                                hour, minute, true
                            ).show()
                        }
                ) {
                    OutlinedTextField(
                        value = jamSelesai,
                        onValueChange = {},
                        label = { Text("Jam Selesai") },
                        readOnly = true,
                        enabled = false,
                        isError = jamSelesaiError || waktuError,
                        supportingText = when {
                            jamSelesaiError -> {
                                { Text("Jam selesai wajib diisi") }
                            }
                            waktuError -> {
                                { Text("Jam selesai harus lebih dari jam mulai") }
                            }
                            else -> null
                        },
                        trailingIcon = {
                            Icon(
                                Icons.Default.Schedule,
                                contentDescription = null
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // ===== KAPASITAS =====
                OutlinedTextField(
                    value = kapasitas,
                    onValueChange = {
                        kapasitas = it.filter { char -> char.isDigit() }
                        kapasitasError = false
                    },
                    label = { Text("Kapasitas") },
                    isError = kapasitasError,
                    supportingText = if (kapasitasError) {
                        { Text("Kapasitas harus lebih dari 0") }
                    } else null,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    // Validasi
                    tanggalError = tanggal.isBlank()
                    jamMulaiError = jamMulai.isBlank()
                    jamSelesaiError = jamSelesai.isBlank()
                    kapasitasError = kapasitas.toIntOrNull() == null || kapasitas.toInt() <= 0

                    // Validasi waktu: jam selesai harus > jam mulai
                    waktuError = if (jamMulai.isNotBlank() && jamSelesai.isNotBlank()) {
                        jamSelesai <= jamMulai
                    } else {
                        false
                    }

                    if (tanggalError || jamMulaiError || jamSelesaiError || kapasitasError || waktuError) {
                        return@TextButton
                    }

                    onSimpan(
                        tanggal,
                        jamMulai,
                        jamSelesai,
                        kapasitas.toInt()
                    )
                }
            ) {
                Text("Simpan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}
