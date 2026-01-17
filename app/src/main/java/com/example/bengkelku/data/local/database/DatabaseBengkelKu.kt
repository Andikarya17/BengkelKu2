package com.example.bengkelku.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.bengkelku.data.local.dao.BookingDao
import com.example.bengkelku.data.local.dao.KendaraanDao
import com.example.bengkelku.data.local.dao.PenggunaDao
import com.example.bengkelku.data.local.dao.ServisDao
import com.example.bengkelku.data.local.dao.SlotServisDao
import com.example.bengkelku.data.local.entity.Booking
import com.example.bengkelku.data.local.entity.Kendaraan
import com.example.bengkelku.data.local.entity.Pengguna
import com.example.bengkelku.data.local.entity.Servis
import com.example.bengkelku.data.local.entity.SlotServis

/**
 * Room Database for BengkelKu app
 * 
 * Version 5: Status cleanup
 * - StatusBooking now only has 3 values: MENUNGGU, DIPROSES, SELESAI
 * - Removed nomorAntrian from Booking
 */
@Database(
    entities = [
        Pengguna::class,
        Kendaraan::class,
        Servis::class,
        SlotServis::class,
        Booking::class
    ],
    version = 5,  // StatusBooking cleanup
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class DatabaseBengkelKu : RoomDatabase() {

    abstract fun penggunaDao(): PenggunaDao
    abstract fun kendaraanDao(): KendaraanDao
    abstract fun servisDao(): ServisDao
    abstract fun slotServisDao(): SlotServisDao
    abstract fun bookingDao(): BookingDao

    companion object {
        @Volatile
        private var INSTANCE: DatabaseBengkelKu? = null

        fun getInstance(context: Context): DatabaseBengkelKu {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }
        }

        private fun buildDatabase(context: Context): DatabaseBengkelKu {
            return Room.databaseBuilder(
                context.applicationContext,
                DatabaseBengkelKu::class.java,
                "bengkelku_database"
            )
                .fallbackToDestructiveMigration()  // Clear DB on version mismatch
                .build()
        }
    }
}

/**
 * Type converters for Room
 */
class Converters {
    @androidx.room.TypeConverter
    fun fromStatusBooking(status: com.example.bengkelku.data.local.entity.StatusBooking): String {
        return status.name
    }

    @androidx.room.TypeConverter
    fun toStatusBooking(value: String): com.example.bengkelku.data.local.entity.StatusBooking {
        return com.example.bengkelku.data.local.entity.StatusBooking.fromString(value)
    }
    
    @androidx.room.TypeConverter
    fun fromRolePengguna(role: com.example.bengkelku.data.local.entity.RolePengguna): String {
        return role.name
    }

    @androidx.room.TypeConverter
    fun toRolePengguna(value: String): com.example.bengkelku.data.local.entity.RolePengguna {
        return com.example.bengkelku.data.local.entity.RolePengguna.fromString(value)
    }
}