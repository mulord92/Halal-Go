package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "booked_rides")
data class BookedRide(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val pickup: String,
    val dropoff: String,
    val price: Double,
    val type: String, // "Economy", "Family", "Female", "Luxury", "Delivery"
    val status: String, // "REQUESTED", "ACCEPTED", "ARRIVED", "COMPLETED", "CANCELLED"
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "wallet_transactions")
data class WalletTransaction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val category: String, // "RIDE", "DELIVERY", "BANK", "WITHDRAWAL", "BONUS"
    val title: String, // e.g., "Green Oasis Cafe", "Halal Go Ride", "Top up via Bank"
    val amount: Double, // positive or negative
    val dateTimeString: String, // "Today, 12:45 PM", "Yesterday, 08:30 PM", etc.
    val statusString: String, // "COMPLETED", "PENDING"
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "user_profiles")
data class UserProfile(
    @PrimaryKey val id: Int = 1,
    val name: String = "",
    val balance: Double = 0.0,
    val hlgoBalance: Double = 0.0,
    val zakatDue: Double = 0.0,
    val isSadaqahRoundUp: Boolean = true,
    val isHalalFilter: Boolean = false,
    val ramadanModeEnabled: Boolean = true,
    val profilePicture: String? = null,
    val email: String = "",
    val isLoggedIn: Boolean = false
)
