package com.example.data

import com.example.data.BookedRide
import com.example.data.WalletTransaction
import com.example.data.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.coroutineScope

class HalalGoRepository(private val dao: HalalGoDao) {

    val allRides: Flow<List<BookedRide>> = dao.getAllRides()
    val activeRide: Flow<BookedRide?> = dao.getActiveRide()
    val allTransactions: Flow<List<WalletTransaction>> = dao.getAllTransactions()
    val userProfile: Flow<UserProfile?> = dao.getUserProfile()

    suspend fun populateInitialDataIfEmpty() {
        val currentProfile = dao.getUserProfile().firstOrNull()
        if (currentProfile == null) {
            // Populate extremely clean empty profile for the custom Registration flow
            val defaultProfile = UserProfile(
                id = 1,
                name = "",
                balance = 0.0,
                hlgoBalance = 0.0,
                zakatDue = 0.0,
                isSadaqahRoundUp = true,
                isHalalFilter = false,
                ramadanModeEnabled = true,
                profilePicture = null,
                email = "",
                isLoggedIn = false
            )
            dao.insertOrUpdateProfile(defaultProfile)
        }
    }

    suspend fun createRide(pickup: String, dropoff: String, price: Double, type: String): Long {
        val ride = BookedRide(
            pickup = pickup,
            dropoff = dropoff,
            price = price,
            type = type,
            status = "REQUESTED"
        )
        return dao.insertRide(ride)
    }

    suspend fun updateRideStatus(ride: BookedRide, status: String) {
        dao.updateRide(ride.copy(status = status))
    }

    suspend fun cancelActiveRide(ride: BookedRide) {
        dao.updateRide(ride.copy(status = "CANCELLED"))
    }

    suspend fun addTransaction(title: String, amount: Double, category: String, dateTime: String) {
        val tx = WalletTransaction(
            category = category,
            title = title,
            amount = amount,
            dateTimeString = dateTime,
            statusString = "COMPLETED"
        )
        dao.insertTransaction(tx)

        // Update profile balance too
        val profile = dao.getUserProfile().firstOrNull() ?: UserProfile()
        dao.insertOrUpdateProfile(profile.copy(balance = profile.balance + amount))
    }

    suspend fun updateProfile(profile: UserProfile) {
        dao.insertOrUpdateProfile(profile)
    }

    suspend fun clearAll() {
        dao.clearRides()
        dao.clearTransactions()
    }
}
