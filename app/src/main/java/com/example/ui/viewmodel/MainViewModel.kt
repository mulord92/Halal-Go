package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class RideFareBreakdown(
    val baseFare: Double,
    val distanceFare: Double,
    val timeFare: Double,
    val standardFare: Double,
    val surgeMultiplier: Double,
    val surgeComponent: Double,
    val totalFare: Double
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = HalalGoRepository(db.halalGoDao())

    // App Roles & Screens Navigation States
    private val _isDriverMode = MutableStateFlow(false)
    val isDriverMode: StateFlow<Boolean> = _isDriverMode.asStateFlow()

    private val _currentPassengerTab = MutableStateFlow("Home") // Home, Ride, Delivery, Prayer, Wallet
    val currentPassengerTab: StateFlow<String> = _currentPassengerTab.asStateFlow()

    private val _currentDriverTab = MutableStateFlow("Home") // Home, Earnings, Wallet, Profile
    val currentDriverTab: StateFlow<String> = _currentDriverTab.asStateFlow()

    // Combined Profiles info
    val userProfile: StateFlow<UserProfile?> = repository.userProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allTransactions: StateFlow<List<WalletTransaction>> = repository.allTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeRide: StateFlow<BookedRide?> = repository.activeRide
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Passenger Mode UI States
    val selectedRideType = MutableStateFlow("Family") // Economy, Family, Female, Luxury
    val pickupQuery = MutableStateFlow("Makati CBD")
    val dropoffQuery = MutableStateFlow("SM Mall of Asia, Pasay")
    val locationSearchFocus = MutableStateFlow(false)

    val surgeMultiplier = MutableStateFlow(1.2) // Range 1.0 to 2.0 (COULD BE ADJUSTED LIVE!)

    // Dynamic distance calculation based on pickup/dropoff query length and content
    val travelDistanceKm: StateFlow<Double> = combine(pickupQuery, dropoffQuery) { pickup, dropoff ->
        val jointLength = (pickup.trim().length + dropoff.trim().length).toDouble()
        if (jointLength == 0.0) {
            6.5
        } else {
            val calc = (jointLength % 12.0) + 3.0
            kotlin.math.round(calc * 10.0) / 10.0
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 6.5)

    // Dynamic travel duration calculation based on distance
    val travelDurationMins: StateFlow<Int> = travelDistanceKm.map { distance ->
        val mins = (distance * 2.2).toInt() + 6
        mins.coerceIn(5, 75)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 18)

    fun getBaseFareForType(type: String): Double {
        return when (type) {
            "Economy" -> 55.0  // Hatchback
            "Female" -> 65.0   // Sedan (Standard GrabCar)
            "Family" -> 75.0   // AUV / SUV
            "Luxury" -> 165.0  // Premium
            else -> 55.0
        }
    }

    fun getTimeRateForType(type: String): Double {
        return if (type == "Luxury") 4.0 else 2.0
    }

    fun getFareBreakdown(type: String, distance: Double, duration: Int, surge: Double): RideFareBreakdown {
        val base = getBaseFareForType(type)
        val distanceFare = distance * 15.0
        val timeFare = duration * getTimeRateForType(type)
        val standardFare = base + distanceFare + timeFare
        val surgeComponent = standardFare * (surge - 1.0)
        val totalFare = standardFare * surge
        
        return RideFareBreakdown(
            baseFare = base,
            distanceFare = distanceFare,
            timeFare = timeFare,
            standardFare = standardFare,
            surgeMultiplier = surge,
            surgeComponent = surgeComponent,
            totalFare = kotlin.math.round(totalFare * 100.0) / 100.0
        )
    }

    // Booking flows and details
    private val _isBookingState = MutableStateFlow(false) // Whether booking is active
    val isBookingState: StateFlow<Boolean> = _isBookingState.asStateFlow()

    private val _isRideSearching = MutableStateFlow(false)
    val isRideSearching: StateFlow<Boolean> = _isRideSearching.asStateFlow()

    // Simulation statuses
    private val _simulationMessage = MutableStateFlow("")
    val simulationMessage: StateFlow<String> = _simulationMessage.asStateFlow()

    // Driver Mode UI States
    private val _isDriverOnline = MutableStateFlow(false)
    val isDriverOnline: StateFlow<Boolean> = _isDriverOnline.asStateFlow()

    private val _showNewRequestPopup = MutableStateFlow(false)
    val showNewRequestPopup: StateFlow<Boolean> = _showNewRequestPopup.asStateFlow()

    private val _popupTimerCount = MutableStateFlow(15)
    val popupTimerCount: StateFlow<Int> = _popupTimerCount.asStateFlow()

    // Active Driver Ride navigation state
    private val _isDriverOnActiveRide = MutableStateFlow(false)
    val isDriverOnActiveRide: StateFlow<Boolean> = _isDriverOnActiveRide.asStateFlow()

    private val _activeDriverRideStatus = MutableStateFlow("") // "PICKUP", "ARRIVED", "DROPOFF", "COMPLETED"
    val activeDriverRideStatus: StateFlow<String> = _activeDriverRideStatus.asStateFlow()

    // Dialog & overlay controls
    val showZakatCalculator = MutableStateFlow(false)
    val zakatInputWealth = MutableStateFlow("")
    val computedZakatResult = MutableStateFlow(0.0)

    val showQrScanner = MutableStateFlow(false)
    val qrScanCompleteMessage = MutableStateFlow("")

    // Qibla orientation state
    private val _qiblaCompassRotation = MutableStateFlow(145f)
    val qiblaCompassRotation: StateFlow<Float> = _qiblaCompassRotation.asStateFlow()

    // Place Details via RapidAPI
    private val _fetchedPlaceDetails = MutableStateFlow<PlaceResult?>(null)
    val fetchedPlaceDetails: StateFlow<PlaceResult?> = _fetchedPlaceDetails.asStateFlow()

    private val _isFetchingPlaceDetails = MutableStateFlow(false)
    val isFetchingPlaceDetails: StateFlow<Boolean> = _isFetchingPlaceDetails.asStateFlow()

    private val _placeDetailsError = MutableStateFlow<String?>(null)
    val placeDetailsError: StateFlow<String?> = _placeDetailsError.asStateFlow()

    fun fetchPlaceDetails(placeId: String = "ChIJN1t_tDeuEmsRUsoyG83frY4") {
        viewModelScope.launch {
            _isFetchingPlaceDetails.value = true
            _placeDetailsError.value = null
            try {
                val response = PlaceDetailsApi.service.getPlaceDetails(
                    placeId = placeId,
                    fields = "name,rating,formatted_phone_number",
                    host = "google-map-place-api.p.rapidapi.com",
                    apiKey = "7f5b0129a4msh804b0a4c8cfa1e9p157b17jsn9456e7726948"
                )
                if (response.status == "OK" && response.result != null) {
                    _fetchedPlaceDetails.value = response.result
                } else {
                    _placeDetailsError.value = "Status: ${response.status}"
                }
            } catch (e: Exception) {
                _placeDetailsError.value = "Error: ${e.localizedMessage}"
            } finally {
                _isFetchingPlaceDetails.value = false
            }
        }
    }

    fun clearPlaceDetails() {
        _fetchedPlaceDetails.value = null
        _placeDetailsError.value = null
    }

    init {
        // Prepare database and populates dummy parameters if missing
        viewModelScope.launch {
            repository.populateInitialDataIfEmpty()
        }

        // Qibla alignment swaying animation simulation
        viewModelScope.launch {
            var direction = 1
            while (true) {
                delay(300)
                val current = _qiblaCompassRotation.value
                val offset = (Math.random() - 0.5) * 1.5
                _qiblaCompassRotation.value = (current + offset).toFloat()
            }
        }
    }

    // Role switcher
    fun setDriverMode(enabled: Boolean) {
        _isDriverMode.value = enabled
        if (enabled) {
            _currentDriverTab.value = "Home"
        } else {
            _currentPassengerTab.value = "Home"
        }
    }

    fun setPassengerTab(tab: String) {
        _currentPassengerTab.value = tab
    }

    fun setDriverTab(tab: String) {
        _currentDriverTab.value = tab
    }

    // Wallet settings toggles
    fun toggleSadaqahRoundUp() {
        viewModelScope.launch {
            val profile = userProfile.value ?: UserProfile()
            repository.updateProfile(profile.copy(isSadaqahRoundUp = !profile.isSadaqahRoundUp))
        }
    }

    fun toggleHalalFilter() {
        viewModelScope.launch {
            val profile = userProfile.value ?: UserProfile()
            repository.updateProfile(profile.copy(isHalalFilter = !profile.isHalalFilter))
        }
    }

    fun toggleRamadanMode() {
        viewModelScope.launch {
            val profile = userProfile.value ?: UserProfile()
            repository.updateProfile(profile.copy(ramadanModeEnabled = !profile.ramadanModeEnabled))
        }
    }

    // Interactive booking flows (Passenger)
    fun requestRide() {
        viewModelScope.launch {
            _isRideSearching.value = true
            _simulationMessage.value = "Sending request to nearby vetted drivers..."
            delay(2500)
            
            // Generate simulated active ride with Grab calculation rules
            val dist = travelDistanceKm.value
            val dur = travelDurationMins.value
            val surge = surgeMultiplier.value
            val type = selectedRideType.value
            val finalPrice = getFareBreakdown(type, dist, dur, surge).totalFare

            repository.createRide(
                pickup = pickupQuery.value,
                dropoff = dropoffQuery.value,
                price = finalPrice,
                type = selectedRideType.value
            )
            _isRideSearching.value = false
            _isBookingState.value = true
            _simulationMessage.value = "Halal vetted driver found! Assigned and en route."

            // Automatically simulate driver progression for interactive high-fidelity feedback!
            simulateDriverPassengerRide()
        }
    }

    private fun simulateDriverPassengerRide() {
        viewModelScope.launch {
            delay(4000)
            val ride = repository.activeRide.firstOrNull()
            if (ride != null) {
                repository.updateRideStatus(ride, "ACCEPTED")
                _simulationMessage.value = "Sister driver Ayesha (Female Driver category) accepted! ETA 4 min."
            }

            delay(4000)
            val currentRide = repository.activeRide.firstOrNull()
            if (currentRide != null) {
                repository.updateRideStatus(currentRide, "ARRIVED")
                _simulationMessage.value = "Your driver has arrived outside!"
            }
        }
    }

    fun completePassengerRide() {
        viewModelScope.launch {
            val ride = repository.activeRide.firstOrNull()
            if (ride != null) {
                repository.updateRideStatus(ride, "COMPLETED")
                // Record local wallet transaction deduct
                repository.addTransaction(
                    title = "Halal Ride - ${ride.type}",
                    amount = -ride.price,
                    category = "RIDE",
                    dateTime = "Just now"
                )
            }
            _isBookingState.value = false
            _currentPassengerTab.value = "Wallet" // Guide them to wallet to see logs
        }
    }

    fun cancelPassengerRide() {
        viewModelScope.launch {
            val ride = repository.activeRide.firstOrNull()
            if (ride != null) {
                repository.cancelActiveRide(ride)
            }
            _isBookingState.value = false
            _isRideSearching.value = false
        }
    }

    // Driver online/offline toggles
    fun toggleDriverOnline() {
        _isDriverOnline.value = !_isDriverOnline.value
        if (_isDriverOnline.value) {
            // Simulate receiving a premium ride invitation in 3 seconds!
            viewModelScope.launch {
                delay(2000)
                if (_isDriverOnline.value) {
                    triggerNewRequestInvitation()
                }
            }
        } else {
            _showNewRequestPopup.value = false
            _isDriverOnActiveRide.value = false
        }
    }

    private fun triggerNewRequestInvitation() {
        _popupTimerCount.value = 15
        _showNewRequestPopup.value = true
        _isDriverOnActiveRide.value = false

        // Launch invitations timer decrement countdown
        viewModelScope.launch {
            while (_popupTimerCount.value > 0 && _showNewRequestPopup.value) {
                delay(1000)
                _popupTimerCount.value -= 1
            }
            if (_showNewRequestPopup.value && _popupTimerCount.value == 0) {
                // Auto decline invitation upon timeout
                _showNewRequestPopup.value = false
            }
        }
    }

    fun acceptRideInvitation() {
        _showNewRequestPopup.value = false
        _isDriverOnActiveRide.value = true
        _activeDriverRideStatus.value = "PICKUP"
        
        // Simulating sequence
        viewModelScope.launch {
            delay(3000)
            if (_isDriverOnActiveRide.value) {
                _activeDriverRideStatus.value = "ARRIVED"
            }
            delay(3000)
            if (_isDriverOnActiveRide.value) {
                _activeDriverRideStatus.value = "DROPOFF"
            }
        }
    }

    fun declineRideInvitation() {
        _showNewRequestPopup.value = false
    }

    fun completeDriverActiveRide() {
        _isDriverOnActiveRide.value = false
        // Insert earned driver transition credits (e.g., +45.00 PHP)
        viewModelScope.launch {
            repository.addTransaction(
                title = "Earned Ride: Mall of Asia to BGC",
                amount = 45.00,
                category = "RIDE",
                dateTime = "Today, just now"
            )
            val profile = userProfile.value ?: UserProfile()
            // Add earnings to driver's balance
            repository.updateProfile(profile.copy(balance = profile.balance + 45.00))
            _currentDriverTab.value = "Earnings"
        }
    }

    // Wallet and dialog utilities
    fun calculateZakat(amountStr: String) {
        zakatInputWealth.value = amountStr
        val wealth = amountStr.toDoubleOrNull() ?: 0.0
        // Nisab threshold is typically equivalent to ~85g of Gold, e.g. around PHP 20,000.
        // If wealth exceeds Nisab, Zakat is 2.5% simple calculating rate
        val result = if (wealth >= 20000.0) {
            wealth * 0.025
        } else {
            0.0
        }
        computedZakatResult.value = result
    }

    fun performZakatPayment(customZakatAmt: Double) {
        if (customZakatAmt > 0.0) {
            viewModelScope.launch {
                repository.addTransaction(
                    title = "Ramadan Zakat & Charity",
                    amount = -customZakatAmt,
                    category = "WITHDRAWAL",
                    dateTime = "Just now"
                )
                val profile = userProfile.value ?: UserProfile()
                repository.updateProfile(profile.copy(
                    balance = profile.balance - customZakatAmt,
                    zakatDue = maxOf(0.0, profile.zakatDue - customZakatAmt)
                ))
                showZakatCalculator.value = false
                qrScanCompleteMessage.value = "Alhamdulillah! Zakat payment of PHP $customZakatAmt completed."
                delay(3000)
                qrScanCompleteMessage.value = ""
            }
        }
    }

    fun performQuickSadaqahPayment(amount: Double) {
        viewModelScope.launch {
            repository.addTransaction(
                title = "Sadaqah Contribution",
                amount = -amount,
                category = "WITHDRAWAL",
                dateTime = "Just now"
            )
            val profile = userProfile.value ?: UserProfile()
            repository.updateProfile(profile.copy(balance = profile.balance - amount))
            qrScanCompleteMessage.value = "Alhamdulillah! Sadaqah collection of PHP $amount received."
            delay(3000)
            qrScanCompleteMessage.value = ""
        }
    }

    fun triggerMockScanner() {
        showQrScanner.value = true
        viewModelScope.launch {
            delay(2500)
            showQrScanner.value = false
            qrScanCompleteMessage.value = "Merchant verified! Scanned Green Oasis Cafe (Halal Certified)."
            delay(4000)
            qrScanCompleteMessage.value = ""
        }
    }

    fun topupFunds() {
        viewModelScope.launch {
            repository.addTransaction(
                title = "Top up via Card",
                amount = 250.00,
                category = "BANK",
                dateTime = "Just now"
            )
        }
    }

    fun withdrawFunds() {
        viewModelScope.launch {
            repository.addTransaction(
                title = "Withdrawal Transfer initiated",
                amount = -500.00,
                category = "WITHDRAWAL",
                dateTime = "Just now"
            )
        }
    }

    fun googleLogin(name: String, email: String) {
        viewModelScope.launch {
            val profile = userProfile.value ?: UserProfile()
            repository.updateProfile(profile.copy(
                name = name,
                email = email,
                isLoggedIn = true
            ))
        }
    }

    fun logout() {
        viewModelScope.launch {
            val profile = userProfile.value ?: UserProfile()
            repository.updateProfile(profile.copy(
                isLoggedIn = false
            ))
        }
    }

    fun updateProfileNameAndEmail(name: String, email: String) {
        viewModelScope.launch {
            val profile = userProfile.value ?: UserProfile()
            repository.updateProfile(profile.copy(
                name = name,
                email = email
            ))
        }
    }

    fun updateProfilePicture(imagePath: String?) {
        viewModelScope.launch {
            val profile = userProfile.value ?: UserProfile()
            repository.updateProfile(profile.copy(
                profilePicture = imagePath
            ))
        }
    }
}
