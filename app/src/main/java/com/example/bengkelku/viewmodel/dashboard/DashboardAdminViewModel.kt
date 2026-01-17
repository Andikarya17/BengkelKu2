package com.example.bengkelku.viewmodel.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bengkelku.data.remote.model.BookingResponse
import com.example.bengkelku.data.remote.model.ServisResponse
import com.example.bengkelku.data.repository.RepositoryBooking
import com.example.bengkelku.data.repository.RepositoryServis
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DashboardAdminViewModel(
    private val repositoryBooking: RepositoryBooking,
    private val repositoryServis: RepositoryServis
) : ViewModel() {

    // ===== BOOKING STATE =====
    private val _bookingState = MutableStateFlow<AdminBookingState>(AdminBookingState.Loading)
    val bookingState: StateFlow<AdminBookingState> = _bookingState

    // ===== SERVIS STATE =====
    private val _servisState = MutableStateFlow<AdminServisState>(AdminServisState.Loading)
    val servisState: StateFlow<AdminServisState> = _servisState

    // ===== ACTION STATE =====
    private val _aksiState = MutableStateFlow<AdminAksiState>(AdminAksiState.Idle)
    val aksiState: StateFlow<AdminAksiState> = _aksiState

    init {
        loadData()
    }

    fun loadData() {
        loadAllBooking()
        loadAllServis()
    }

    private fun loadAllBooking() {
        viewModelScope.launch {
            _bookingState.value = AdminBookingState.Loading

            val result = repositoryBooking.getAllBookingApi()
            result.fold(
                onSuccess = { data ->
                    // Debug log to see status values
                    data.forEach { 
                        android.util.Log.d("AdminVM", "Booking ${it.id}: status='${it.status}'") 
                    }
                    _bookingState.value = AdminBookingState.Success(data)
                },
                onFailure = { error ->
                    _bookingState.value = AdminBookingState.Error(error.message ?: "Gagal memuat booking")
                }
            )
        }
    }

    private fun loadAllServis() {
        viewModelScope.launch {
            _servisState.value = AdminServisState.Loading

            val result = repositoryServis.getAllServisApi()
            result.fold(
                onSuccess = { data ->
                    _servisState.value = AdminServisState.Success(data)
                },
                onFailure = { error ->
                    _servisState.value = AdminServisState.Error(error.message ?: "Gagal memuat servis")
                }
            )
        }
    }

    /**
     * Update booking status via API
     */
    fun updateBookingStatus(bookingId: Int, newStatus: String) {
        android.util.Log.d("AdminVM", "updateBookingStatus: id=$bookingId, status='$newStatus'")
        viewModelScope.launch {
            _aksiState.value = AdminAksiState.Loading

            val result = repositoryBooking.updateBookingStatusApi(bookingId, newStatus)
            result.fold(
                onSuccess = { message ->
                    _aksiState.value = AdminAksiState.Success(message)
                    loadAllBooking() // Refresh data after update
                },
                onFailure = { error ->
                    _aksiState.value = AdminAksiState.Error(error.message ?: "Gagal update status")
                }
            )
        }
    }

    fun resetAksiState() {
        _aksiState.value = AdminAksiState.Idle
    }
}

sealed class AdminBookingState {
    object Loading : AdminBookingState()
    data class Success(val data: List<BookingResponse>) : AdminBookingState()
    data class Error(val message: String) : AdminBookingState()
}

sealed class AdminServisState {
    object Loading : AdminServisState()
    data class Success(val data: List<ServisResponse>) : AdminServisState()
    data class Error(val message: String) : AdminServisState()
}

sealed class AdminAksiState {
    object Idle : AdminAksiState()
    object Loading : AdminAksiState()
    data class Success(val message: String) : AdminAksiState()
    data class Error(val message: String) : AdminAksiState()
}
