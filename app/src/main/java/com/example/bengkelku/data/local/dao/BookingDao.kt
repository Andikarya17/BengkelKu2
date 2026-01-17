package com.example.bengkelku.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.bengkelku.data.local.entity.Booking
import com.example.bengkelku.data.local.entity.StatusBooking
import kotlinx.coroutines.flow.Flow

/**
 * Data class untuk menampilkan booking dengan detail relasi
 */
data class BookingWithDetails(
    val id: Int,
    val penggunaId: Int,
    val namaPengguna: String,
    val kendaraanId: Int,
    val merkKendaraan: String,
    val modelKendaraan: String,
    val nomorPlat: String,
    val servisId: Int,
    val namaServis: String,
    val hargaServis: Int,
    val tanggalServis: String,
    val jamServis: String,
    val nomorAntrian: Int,
    val status: StatusBooking,
    val totalBiaya: Int
)

@Dao
interface BookingDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(booking: Booking)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(bookingList: List<Booking>)

    @Update
    suspend fun update(booking: Booking)

    @Delete
    suspend fun delete(booking: Booking)
    
    @Query("DELETE FROM booking WHERE id = :id")
    suspend fun deleteById(id: Int)

    // Booking aktif pelanggan
    @Query("""
        SELECT * FROM booking
        WHERE penggunaId = :penggunaId
          AND status IN ('MENUNGGU', 'DIPROSES')
        ORDER BY tanggalServis ASC, jamServis ASC
    """)
    fun getBookingAktif(penggunaId: Int): Flow<List<Booking>>

    // Riwayat booking pelanggan
    @Query("""
        SELECT * FROM booking
        WHERE penggunaId = :penggunaId
          AND status = 'SELESAI'
        ORDER BY tanggalServis DESC
    """)
    fun getRiwayatBooking(penggunaId: Int): Flow<List<Booking>>
    
    // All bookings for a customer
    @Query("""
        SELECT * FROM booking
        WHERE penggunaId = :penggunaId
        ORDER BY tanggalServis DESC, jamServis DESC
    """)
    fun getBookingByPengguna(penggunaId: Int): Flow<List<Booking>>

    // Semua booking (Admin)
    @Query("""
        SELECT * FROM booking
        ORDER BY tanggalServis DESC, jamServis DESC
    """)
    fun getAllBooking(): Flow<List<Booking>>

    // Semua booking dengan detail (Admin) - JOIN query
    @Query("""
        SELECT 
            b.id,
            b.penggunaId,
            p.nama AS namaPengguna,
            b.kendaraanId,
            k.merk AS merkKendaraan,
            k.model AS modelKendaraan,
            k.nomorPlat,
            b.servisId,
            s.namaServis,
            s.harga AS hargaServis,
            b.tanggalServis,
            b.jamServis,
            b.nomorAntrian,
            b.status,
            b.totalBiaya
        FROM booking b
        INNER JOIN pengguna p ON b.penggunaId = p.id
        INNER JOIN kendaraan k ON b.kendaraanId = k.id
        INNER JOIN servis s ON b.servisId = s.id
        ORDER BY b.tanggalServis DESC, b.jamServis DESC
    """)
    fun getAllBookingWithDetails(): Flow<List<BookingWithDetails>>

    // Get booking by ID
    @Query("SELECT * FROM booking WHERE id = :bookingId")
    suspend fun getBookingById(bookingId: Int): Booking?
    
    // Clear all bookings (for sync)
    @Query("DELETE FROM booking")
    suspend fun deleteAll()
}
