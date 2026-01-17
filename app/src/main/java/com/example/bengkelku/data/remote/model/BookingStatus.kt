package com.example.bengkelku.data.remote.model

/**
 * Enum for booking status with safe parsing.
 * Only 3 valid statuses: MENUNGGU, DIPROSES, SELESAI
 */
enum class BookingStatus(val value: String) {
    MENUNGGU("MENUNGGU"),
    DIPROSES("DIPROSES"),
    SELESAI("SELESAI");

    companion object {
        /**
         * Safely parse status string from backend.
         * Case-insensitive, returns MENUNGGU for unrecognized/empty values.
         */
        fun fromString(status: String?): BookingStatus {
            if (status.isNullOrBlank()) return MENUNGGU
            
            return when (status.uppercase().trim()) {
                "MENUNGGU" -> MENUNGGU
                "DIPROSES", "DALAM_PROSES" -> DIPROSES
                "SELESAI" -> SELESAI
                else -> MENUNGGU
            }
        }

        /**
         * Check if status is considered "active" (not finished)
         */
        fun isActive(status: BookingStatus): Boolean {
            return status == MENUNGGU || status == DIPROSES
        }

        /**
         * Check if status is considered "history" (finished)
         */
        fun isHistory(status: BookingStatus): Boolean {
            return status == SELESAI
        }
    }
}
