package com.example.bengkelku.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Booking entity - stores bookings from backend
 * PrimaryKey is from backend (not autoGenerate)
 * 
 * ForeignKeys reference parent tables that MUST exist before insert
 */
@Entity(
    tableName = "booking",
    foreignKeys = [
        ForeignKey(
            entity = Pengguna::class,
            parentColumns = ["id"],
            childColumns = ["penggunaId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Kendaraan::class,
            parentColumns = ["id"],
            childColumns = ["kendaraanId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Servis::class,
            parentColumns = ["id"],
            childColumns = ["servisId"],
            onDelete = ForeignKey.CASCADE  // Changed from RESTRICT to CASCADE
        ),
        ForeignKey(
            entity = SlotServis::class,
            parentColumns = ["id"],
            childColumns = ["slotServisId"],
            onDelete = ForeignKey.CASCADE  // Changed from RESTRICT to CASCADE
        )
    ],
    indices = [
        Index("penggunaId"),
        Index("kendaraanId"),
        Index("servisId"),
        Index("slotServisId")
    ]
)
data class Booking(

    @PrimaryKey
    val id: Int,  // From backend, not autoGenerate

    val penggunaId: Int,      // Pelanggan yang booking

    val kendaraanId: Int,     // Kendaraan yang diservis

    val servisId: Int,        // Jenis servis

    val slotServisId: Int,    // Slot waktu servis

    val tanggalServis: String, // Format: yyyy-MM-dd (from slot_servis.tanggal)

    val jamServis: String,     // Format: HH:mm (from slot_servis.jam_mulai)

    val nomorAntrian: Int,     // Changed from String to Int (matches backend)

    val status: StatusBooking = StatusBooking.MENUNGGU,

    val totalBiaya: Int        // From jenis_servis.harga
)

/**
 * Status pengerjaan booking servis
 * Only 3 valid statuses: MENUNGGU, DIPROSES, SELESAI
 */
enum class StatusBooking {
    MENUNGGU,
    DIPROSES,
    SELESAI;
    
    companion object {
        fun fromString(value: String): StatusBooking {
            return when (value.uppercase()) {
                "MENUNGGU" -> MENUNGGU
                "DIPROSES" -> DIPROSES
                "SELESAI" -> SELESAI
                else -> MENUNGGU
            }
        }
    }
}
