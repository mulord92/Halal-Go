package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface HalalGoDao {
    // Booked Rides Queries
    @Query("SELECT * FROM booked_rides ORDER BY timestamp DESC")
    fun getAllRides(): Flow<List<BookedRide>>

    @Query("SELECT * FROM booked_rides WHERE status != 'COMPLETED' AND status != 'CANCELLED' LIMIT 1")
    fun getActiveRide(): Flow<BookedRide?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRide(ride: BookedRide): Long

    @Update
    suspend fun updateRide(ride: BookedRide)

    @Query("DELETE FROM booked_rides")
    suspend fun clearRides()

    // Transactions Queries
    @Query("SELECT * FROM wallet_transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<WalletTransaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: WalletTransaction): Long

    @Query("DELETE FROM wallet_transactions")
    suspend fun clearTransactions()

    // Profile Queries
    @Query("SELECT * FROM user_profiles WHERE id = 1")
    fun getUserProfile(): Flow<UserProfile?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProfile(profile: UserProfile)
}
