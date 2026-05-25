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
            // Populate default user profile matching screenshots
            val defaultProfile = UserProfile(
                id = 1,
                name = "Ahmed K.",
                balance = 12450.85,
                hlgoBalance = 842.15,
                zakatDue = 311.27,
                isSadaqahRoundUp = true,
                isHalalFilter = false,
                ramadanModeEnabled = true,
                profilePicture = null,
                email = "ahmed.k@example.com",
                isLoggedIn = false
            )
            dao.insertOrUpdateProfile(defaultProfile)
        }

        val allTx = dao.getAllTransactions().firstOrNull()
        if (allTx.isNullOrEmpty()) {
            // Populate dummy transactions matching screenshots exactly
            val transactions = listOf(
                WalletTransaction(
                    category = "FOOD",
                    title = "Green Oasis Cafe",
                    amount = -24.50,
                    dateTimeString = "Today, 12:45 PM",
                    statusString = "COMPLETED"
                ),
                WalletTransaction(
                    category = "RIDE",
                    title = "Halal Go Ride",
                    amount = -18.20,
                    dateTimeString = "Yesterday, 08:30 PM",
                    statusString = "COMPLETED"
                ),
                WalletTransaction(
                    category = "BANK",
                    title = "Top up via Bank",
                    amount = 500.00,
                    dateTimeString = "2 days ago, 10:15 AM",
                    statusString = "COMPLETED"
                ),
                WalletTransaction(
                    category = "WITHDRAWAL",
                    title = "Withdrawal to Bank",
                    amount = -1200.00,
                    dateTimeString = "Yesterday, 09:15 AM",
                    statusString = "PENDING"
                ),
                WalletTransaction(
                    category = "BONUS",
                    title = "Weekly Bonus",
                    amount = 250.00,
                    dateTimeString = "Oct 24, 2023",
                    statusString = "COMPLETED"
                )
            )
            for (tx in transactions) {
                dao.insertTransaction(tx)
            }
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
