package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.BookedRide
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel

enum class PinSelectorType { PICKUP, DROPOFF }

val LANDMARK_PLACE_IDS = mapOf(
    "Makati CBD" to "ChIJN1t_tDeuEmsRUsoyG83frY4",
    "Makati Central Business District" to "ChIJN1t_tDeuEmsRUsoyG83frY4",
    "SM Mall of Asia, Pasay" to "ChIJs7P-Y6m3lzMR2b76H6i0X5A",
    "Intramuros Gate, Manila" to "ChIJyXGv09vDlzMRuO4u1tW3L7Y",
    "Binondo Church, Manila" to "ChIJ5Z4eOjvFlzMRAi_789VbNis",
    "SM Megamall Main Entrance" to "ChIJRz_EGDzFlzMRg-6jA6vGf4w",
    "NAIA Terminal 3, Pasay" to "ChIJ27x5i6O3lzMR-uQY89XmD-g",
    "Ayala Triangle Gardens, Makati" to "ChIJN1t_tDeuEmsRUsoyG83frY4",
    "C5 Highway, BGC Taguig" to "ChIJN1t_tDeuEmsRUsoyG83frY4"
)

private fun resolveAddressFromOffset(width: Float, height: Float, offset: androidx.compose.ui.geometry.Offset, type: PinSelectorType): String {
    val xRatio = offset.x / width
    val yRatio = offset.y / height
    return when {
        xRatio < 0.5f && yRatio < 0.5f -> {
            if (type == PinSelectorType.PICKUP) "Intramuros Gate, Manila" else "Binondo Church, Manila"
        }
        xRatio >= 0.5f && yRatio < 0.5f -> {
            if (type == PinSelectorType.PICKUP) "SM Megamall Main Entrance" else "Ortigas Center, Pasig"
        }
        xRatio < 0.5f && yRatio >= 0.5f -> {
            if (type == PinSelectorType.PICKUP) "SM Mall of Asia, Pasay" else "NAIA Terminal 3, Pasay"
        }
        else -> {
            if (type == PinSelectorType.PICKUP) "Ayala Triangle Gardens, Makati" else "C5 Highway, BGC Taguig"
        }
    }
}

@Composable
fun PassengerRideScreen(
    viewModel: MainViewModel,
    activeRide: BookedRide?,
    isBookingActive: Boolean,
    isSearching: Boolean,
    modifier: Modifier = Modifier
) {
    val selectedType by viewModel.selectedRideType.collectAsState()
    val pickupQuery by viewModel.pickupQuery.collectAsState()
    val dropoffQuery by viewModel.dropoffQuery.collectAsState()
    val simulationMessage by viewModel.simulationMessage.collectAsState()
    val distanceKm by viewModel.travelDistanceKm.collectAsState()
    val durationMins by viewModel.travelDurationMins.collectAsState()
    val surgeMultiplier by viewModel.surgeMultiplier.collectAsState()

    val fetchedPlaceDetails by viewModel.fetchedPlaceDetails.collectAsState()
    val isFetchingPlaceDetails by viewModel.isFetchingPlaceDetails.collectAsState()
    val placeDetailsError by viewModel.placeDetailsError.collectAsState()

    var pinSelectionMode by remember { mutableStateOf<PinSelectorType?>(null) }
    var lastPinnedOffset by remember { mutableStateOf<Offset?>(null) }
    var mapSize by remember { mutableStateOf(androidx.compose.ui.unit.IntSize.Zero) }

    LaunchedEffect(pinSelectionMode) {
        viewModel.clearPlaceDetails()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .onSizeChanged { mapSize = it }
    ) {
        // Full screen vector navigation map drawing
        MapBackgroundCanvas(
            isBookingActive = isBookingActive,
            rideType = selectedType
        )

        // GestureDetector overlay when in Pinning Mode
        if (pinSelectionMode != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(pinSelectionMode) {
                        detectTapGestures { offset ->
                            lastPinnedOffset = offset
                            val width = if (mapSize.width > 0) mapSize.width.toFloat() else 1000f
                            val height = if (mapSize.height > 0) mapSize.height.toFloat() else 2000f
                            val addr = resolveAddressFromOffset(width, height, offset, pinSelectionMode!!)
                            if (pinSelectionMode == PinSelectorType.PICKUP) {
                                viewModel.pickupQuery.value = addr
                            } else {
                                viewModel.dropoffQuery.value = addr
                            }
                        }
                    }
            )
        }

        // Draw pin locator feedback elements
        if (pinSelectionMode != null) {
            val density = LocalDensity.current
            val pinned = lastPinnedOffset

            if (pinned != null) {
                // Draw locator pin directly on top of the map background at tapped offset
                val offsetX = with(density) { pinned.x.toDp() }
                val offsetY = with(density) { pinned.y.toDp() }

                Box(
                    modifier = Modifier
                        .absoluteOffset(
                            x = offsetX - 20.dp,
                            y = offsetY - 40.dp
                        )
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Pinned Spot",
                            tint = if (pinSelectionMode == PinSelectorType.PICKUP) PrimaryEmerald else GoldSecondary,
                            modifier = Modifier.size(40.dp)
                        )
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(
                                    color = (if (pinSelectionMode == PinSelectorType.PICKUP) PrimaryEmerald else GoldSecondary).copy(alpha = 0.5f),
                                    shape = CircleShape
                                )
                        )
                    }
                }
            } else {
                // Central Map Pin (if they haven\\'t tapped any exact location)
                Box(
                    modifier = Modifier.align(Alignment.Center)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Center Map Pin",
                            tint = if (pinSelectionMode == PinSelectorType.PICKUP) PrimaryEmerald else GoldSecondary,
                            modifier = Modifier
                                .size(44.dp)
                                .offset(y = (-16).dp)
                        )
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(
                                    color = (if (pinSelectionMode == PinSelectorType.PICKUP) PrimaryEmerald else GoldSecondary).copy(alpha = 0.3f),
                                    shape = CircleShape
                                )
                        )
                    }
                }
            }
        }

        // Central target/crosshair feedback element in Pinning Mode (so they drag the map under it!)
        if (pinSelectionMode != null) {
            Box(
                modifier = Modifier.align(Alignment.Center)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Center Map Pin",
                        tint = if (pinSelectionMode == PinSelectorType.PICKUP) PrimaryEmerald else GoldSecondary,
                        modifier = Modifier
                            .size(44.dp)
                            .offset(y = (-16).dp)
                    )
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(
                                color = (if (pinSelectionMode == PinSelectorType.PICKUP) PrimaryEmerald else GoldSecondary).copy(alpha = 0.3f),
                                shape = CircleShape
                            )
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 80.dp), // Height of navigation bar
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Section: Inputs floating panel or Pin banner
            if (pinSelectionMode == null) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FloatingRouteInputCard(
                        pickup = pickupQuery,
                        dropoff = dropoffQuery,
                        onPickupChange = { viewModel.pickupQuery.value = it },
                        onDropoffChange = { viewModel.dropoffQuery.value = it },
                        onPinPickupClick = {
                            pinSelectionMode = PinSelectorType.PICKUP
                            lastPinnedOffset = null
                            if (pickupQuery.isEmpty()) {
                                viewModel.pickupQuery.value = "Makati Central Business District"
                            }
                        },
                        onPinDropoffClick = {
                            pinSelectionMode = PinSelectorType.DROPOFF
                            lastPinnedOffset = null
                            if (dropoffQuery.isEmpty()) {
                                viewModel.dropoffQuery.value = "SM Mall of Asia, Pasay"
                            }
                        }
                    )

                    // SOS Floating Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        FloatingSosButton(onClick = { viewModel.performQuickSadaqahPayment(10.0) })
                    }
                }
            } else {
                // Pinning top instructions banner
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceContainer.copy(alpha = 0.95f)),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.5.dp, GoldSecondary.copy(alpha = 0.4f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Place,
                            contentDescription = "Pin Mode",
                            tint = if (pinSelectionMode == PinSelectorType.PICKUP) PrimaryEmerald else GoldSecondary,
                            modifier = Modifier.size(24.dp)
                        )
                        Column {
                            Text(
                                text = if (pinSelectionMode == PinSelectorType.PICKUP) "Select Pickup Pin" else "Select Destination Pin",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Tap any spot on the map below to drop pin",
                                color = OnSurfaceVariantText,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            if (pinSelectionMode == null) {
                // Bottom Section: Interactive sheet representing ride details
                AnimatedContent(
                    targetState = when {
                        isSearching -> "SEARCHING"
                        isBookingActive -> "BOOKED"
                        else -> "SELECT_RIDE"
                    },
                    transitionSpec = {
                        slideInVertically(initialOffsetY = { it }) togetherWith slideOutVertically(targetOffsetY = { it })
                    },
                    label = "BookingStateAnimation"
                ) { state ->
                    when (state) {
                        "SEARCHING" -> SearchingDriverSheet(simulationMessage = simulationMessage)
                        "BOOKED" -> BookedStatusSheet(
                            activeRide = activeRide,
                            simulationMessage = simulationMessage,
                            onArrivedClick = { viewModel.completePassengerRide() },
                            onCancelClick = { viewModel.cancelPassengerRide() }
                        )
                        else -> SelectRideSheet(
                            viewModel = viewModel,
                            selectedType = selectedType,
                            distanceKm = distanceKm,
                            durationMins = durationMins,
                            surgeMultiplier = surgeMultiplier,
                            onTypeSelect = { viewModel.selectedRideType.value = it },
                            onBookClick = { viewModel.requestRide() }
                        )
                    }
                }
            } else {
                // Pin Confirmation Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .testTag("pin_confirmation_card"),
                    colors = CardDefaults.cardColors(containerColor = SurfaceContainer.copy(alpha = 0.95f)),
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "PINNED LOCATION DETAILS",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = GoldSecondary,
                                letterSpacing = 0.5.sp
                            )
                            IconButton(
                                onClick = { pinSelectionMode = null },
                                modifier = Modifier.size(24.dp).testTag("cancel_pin_icon")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Cancel Pinning",
                                    tint = OnSurfaceVariantText
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White.copy(alpha = 0.03f), RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Icon(
                                imageVector = if (pinSelectionMode == PinSelectorType.PICKUP) Icons.Default.Place else Icons.Default.LocationOn,
                                contentDescription = "Active Pin Icon",
                                tint = if (pinSelectionMode == PinSelectorType.PICKUP) PrimaryEmerald else GoldSecondary,
                                modifier = Modifier.size(24.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (pinSelectionMode == PinSelectorType.PICKUP) "Starting point" else "Destination point",
                                    fontSize = 11.sp,
                                    color = OnSurfaceVariantText
                                )
                                Text(
                                    text = if (pinSelectionMode == PinSelectorType.PICKUP) pickupQuery else dropoffQuery,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    maxLines = 2
                                )
                            }
                        }

                        // Place Details API Section (RapidAPI implementation)
                        if (isFetchingPlaceDetails) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.White.copy(alpha = 0.04f), RoundedCornerShape(12.dp))
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = GoldSecondary,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Retrieving Place Details on RapidAPI...",
                                    fontSize = 11.sp,
                                    color = OnSurfaceVariantText
                                )
                            }
                        } else if (fetchedPlaceDetails != null) {
                            val details = fetchedPlaceDetails!!
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(GoldSecondary.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                                    .border(1.dp, GoldSecondary.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = "Verified Rating",
                                        tint = GoldSecondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "VERIFIED DETAILS (RAPIDAPI)",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = GoldSecondary,
                                        letterSpacing = 0.5.sp
                                    )
                                }
                                
                                Text(
                                    text = details.name,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (details.rating != null) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = "Rating: ",
                                                fontSize = 11.sp,
                                                color = OnSurfaceVariantText
                                            )
                                            Text(
                                                text = "${details.rating} ★",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = GoldSecondary
                                            )
                                        }
                                    }
                                    
                                    if (!details.formatted_phone_number.isNullOrBlank()) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Phone,
                                                contentDescription = "Contact Phone",
                                                tint = OnSurfaceVariantText,
                                                modifier = Modifier.size(12.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = details.formatted_phone_number,
                                                fontSize = 11.sp,
                                                color = OnSurfaceVariantText
                                            )
                                        }
                                    }
                                }
                            }
                        } else {
                            if (placeDetailsError != null) {
                                Text(
                                    text = placeDetailsError!!,
                                    color = Color.Red.copy(alpha = 0.8f),
                                    fontSize = 10.sp,
                                    modifier = Modifier.padding(horizontal = 4.dp).fillMaxWidth()
                                )
                            }
                            
                            OutlinedButton(
                                onClick = {
                                    val currentQuery = if (pinSelectionMode == PinSelectorType.PICKUP) pickupQuery else dropoffQuery
                                    val placeId = LANDMARK_PLACE_IDS[currentQuery] ?: "ChIJN1t_tDeuEmsRUsoyG83frY4"
                                    viewModel.fetchPlaceDetails(placeId)
                                },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = GoldSecondary),
                                border = BorderStroke(1.dp, GoldSecondary.copy(alpha = 0.5f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(38.dp)
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = "Search Details",
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "Verify Location Details (RapidAPI)",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }

                        Button(
                            onClick = { pinSelectionMode = null },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryContainer),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("confirm_pin_location_btn")
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Check, contentDescription = "Confirm", tint = OnPrimaryContainer)
                                Text(
                                    text = "Confirm & Proceed",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = OnPrimaryContainer
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MapBackgroundCanvas(
    isBookingActive: Boolean,
    rideType: String
) {
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // Draw elegant futuristic map road network grid
        val gridColor = Color.White.copy(alpha = 0.04f)
        val roadColor = Color(0xFF1B2333)

        // Horizontal highway road bands
        drawRect(color = roadColor, topLeft = Offset(0f, size.height * 0.3f), size = androidx.compose.ui.geometry.Size(size.width, 100f))
        drawRect(color = roadColor, topLeft = Offset(0f, size.height * 0.7f), size = androidx.compose.ui.geometry.Size(size.width, 80f))
        // Vertical road band
        drawRect(color = roadColor, topLeft = Offset(size.width * 0.35f, 0f), size = androidx.compose.ui.geometry.Size(120f, size.height))

        // Compass grid lines representation
        for (i in 0..size.width.toInt() step 60) {
            drawLine(
                color = gridColor,
                start = Offset(i.toFloat(), 0f),
                end = Offset(i.toFloat(), size.height),
                strokeWidth = 2f
            )
        }
        for (i in 0..size.height.toInt() step 60) {
            drawLine(
                color = gridColor,
                start = Offset(0f, i.toFloat()),
                end = Offset(size.width, i.toFloat()),
                strokeWidth = 2f
            )
        }

        // Draw active navigation course (Pulsing route line)
        if (isBookingActive) {
            val routeBrush = Brush.linearGradient(
                colors = listOf(PrimaryEmerald, GoldSecondary),
                start = Offset(size.width * 0.15f, size.height * 0.82f),
                end = Offset(size.width * 0.72f, size.height * 0.22f)
            )

            drawLine(
                brush = routeBrush,
                start = Offset(size.width * 0.15f, size.height * 0.82f),
                end = Offset(size.width * 0.42f, size.height * 0.52f),
                strokeWidth = 14f,
                cap = androidx.compose.ui.graphics.StrokeCap.Round
            )
            drawLine(
                brush = routeBrush,
                start = Offset(size.width * 0.42f, size.height * 0.52f),
                end = Offset(size.width * 0.72f, size.height * 0.22f),
                strokeWidth = 14f,
                cap = androidx.compose.ui.graphics.StrokeCap.Round
            )

            // Outer pulse glow
            drawCircle(
                color = PrimaryEmerald.copy(alpha = 0.25f),
                radius = 32f,
                center = Offset(size.width * 0.15f, size.height * 0.82f)
            )
            // Pickup anchor dot
            drawCircle(
                color = PrimaryEmerald,
                radius = 16f,
                center = Offset(size.width * 0.15f, size.height * 0.82f)
            )

            // Outer dropoff target gold pulse
            drawCircle(
                color = GoldSecondary.copy(alpha = 0.25f),
                radius = 32f,
                center = Offset(size.width * 0.72f, size.height * 0.22f)
            )
            drawCircle(
                color = GoldSecondary,
                radius = 16f,
                center = Offset(size.width * 0.72f, size.height * 0.22f)
            )
            
            // Moveable vehicle indicator along route
            drawCircle(
                color = GoldSecondary,
                radius = 12f,
                center = Offset(size.width * 0.35f, size.height * 0.6f)
            )
        } else {
            // Draw scatter point vehicle drivers nearby (simulating taxi overlay)
            drawCircle(color = PrimaryEmerald, radius = 8f, center = Offset(size.width * 0.25f, size.height * 0.45f))
            drawCircle(color = PrimaryEmerald.copy(alpha = 0.5f), radius = 6f, center = Offset(size.width * 0.65f, size.height * 0.32f))
            drawCircle(color = PrimaryEmerald, radius = 8f, center = Offset(size.width * 0.48f, size.height * 0.62f))
        }
    }
}

@Composable
fun FloatingRouteInputCard(
    pickup: String,
    dropoff: String,
    onPickupChange: (String) -> Unit,
    onDropoffChange: (String) -> Unit,
    onPinPickupClick: () -> Unit,
    onPinDropoffClick: () -> Unit
) {
    Surface(
        color = SurfaceContainer.copy(alpha = 0.9f),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
        tonalElevation = 8.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Multi-layered bullet
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .background(PrimaryEmerald, CircleShape)
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(modifier = Modifier.fillMaxSize().background(Color.White, CircleShape))
                }

                OutlinedTextField(
                    value = pickup,
                    onValueChange = onPickupChange,
                    placeholder = { Text("Starting position...") },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("pickup_input_field"),
                    trailingIcon = {
                        IconButton(
                            onClick = onPinPickupClick,
                            modifier = Modifier.testTag("pin_pickup_on_map_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Place,
                                contentDescription = "Pin Pickup on Map",
                                tint = PrimaryEmerald
                            )
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedBorderColor = PrimaryEmerald,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.12f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .background(GoldSecondary, CircleShape)
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(modifier = Modifier.fillMaxSize().background(Color.White, CircleShape))
                }

                OutlinedTextField(
                    value = dropoff,
                    onValueChange = onDropoffChange,
                    placeholder = { Text("Destination details...") },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("dropoff_input_field"),
                    trailingIcon = {
                        IconButton(
                            onClick = onPinDropoffClick,
                            modifier = Modifier.testTag("pin_dropoff_on_map_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = "Pin Dropoff on Map",
                                tint = GoldSecondary
                            )
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedBorderColor = GoldSecondary,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.12f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }
    }
}

@Composable
fun FloatingSosButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = ErrorContainerRed),
        border = BorderStroke(1.dp, ErrorRed.copy(alpha = 0.3f)),
        shape = CircleShape,
        contentPadding = PaddingValues(0.dp),
        modifier = Modifier
            .size(54.dp)
            .testTag("sos_trigger_key")
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add, // Using standard representation
                contentDescription = "Quick emergency help",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
            Text("Sadaqah", fontSize = 8.sp, color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun SelectRideSheet(
    viewModel: MainViewModel,
    selectedType: String,
    distanceKm: Double,
    durationMins: Int,
    surgeMultiplier: Double,
    onTypeSelect: (String) -> Unit,
    onBookClick: () -> Unit
) {
    val economyBreakdown = viewModel.getFareBreakdown("Economy", distanceKm, durationMins, surgeMultiplier)
    val familyBreakdown = viewModel.getFareBreakdown("Family", distanceKm, durationMins, surgeMultiplier)
    val femaleBreakdown = viewModel.getFareBreakdown("Female", distanceKm, durationMins, surgeMultiplier)
    val luxuryBreakdown = viewModel.getFareBreakdown("Luxury", distanceKm, durationMins, surgeMultiplier)

    val selectedBreakdown = when (selectedType) {
        "Economy" -> economyBreakdown
        "Family" -> familyBreakdown
        "Female" -> femaleBreakdown
        "Luxury" -> luxuryBreakdown
        else -> economyBreakdown
    }

    Surface(
        color = SurfaceContainer.copy(alpha = 0.95f),
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
        tonalElevation = 16.dp,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("select_ride_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .width(48.dp)
                    .height(5.dp)
                    .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(100.dp))
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Select Ride",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryEmerald
                )
                Text(
                    text = "%,.1f KM • %d MINS".format(distanceKm, durationMins),
                    fontSize = 11.sp,
                    color = GoldSecondary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }

            // Dynamics surge controller
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.03f)),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Surge factor",
                                tint = GoldSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Simulate Traffic Surge",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }
                        Text(
                            text = "%,.2fx".format(surgeMultiplier),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = GoldSecondary
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    Slider(
                        value = surgeMultiplier.toFloat(),
                        onValueChange = { viewModel.surgeMultiplier.value = it.toDouble() },
                        valueRange = 1.0f..2.0f,
                        colors = SliderDefaults.colors(
                            thumbColor = GoldSecondary,
                            activeTrackColor = GoldSecondary,
                            inactiveTrackColor = Color.White.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier.fillMaxWidth().height(24.dp).testTag("surge_slider")
                    )
                    
                    Text(
                        text = "Capped at legally mandated LTFRB 2.0x limit",
                        fontSize = 9.sp,
                        color = OnSurfaceVariantText.copy(alpha = 0.6f),
                        style = androidx.compose.ui.text.TextStyle(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                RideSelectionOptionItem(
                    title = "Economy",
                    subtitle = "Grab Hatchback Pattern • ₱55 Base",
                    priceLabel = "₱%,.2f".format(economyBreakdown.totalFare),
                    durationLabel = "3 min",
                    isSelected = selectedType == "Economy",
                    promoTag = "Value",
                    onClick = { onTypeSelect("Economy") }
                )

                RideSelectionOptionItem(
                    title = "Family (Safe)",
                    subtitle = "Grab SUV Pattern • ₱75 Base",
                    priceLabel = "₱%,.2f".format(familyBreakdown.totalFare),
                    durationLabel = "5 min",
                    isSelected = selectedType == "Family",
                    promoTag = "6-Seater",
                    onClick = { onTypeSelect("Family") }
                )

                RideSelectionOptionItem(
                    title = "Female Driver",
                    subtitle = "Grab Sedan Pattern • ₱65 Base",
                    priceLabel = "₱%,.2f".format(femaleBreakdown.totalFare),
                    durationLabel = "7 min",
                    isSelected = selectedType == "Female",
                    promoTag = "Privacy",
                    onClick = { onTypeSelect("Female") }
                )

                RideSelectionOptionItem(
                    title = "Luxury",
                    subtitle = "Grab Premium • ₱165 Base • ₱4/min",
                    priceLabel = "₱%,.2f".format(luxuryBreakdown.totalFare),
                    durationLabel = "4 min",
                    isSelected = selectedType == "Luxury",
                    promoTag = "Premium",
                    onClick = { onTypeSelect("Luxury") }
                )
            }

            // Upfront Agreement Card (Aqd-compliant transparent details)
            Card(
                colors = CardDefaults.cardColors(containerColor = PrimaryEmerald.copy(alpha = 0.05f)),
                border = BorderStroke(1.dp, PrimaryEmerald.copy(alpha = 0.25f)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "COMPLIANT UPFRONT CONTRACT FARE",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryEmerald,
                            letterSpacing = 0.5.sp
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Compliant logo",
                                tint = PrimaryEmerald,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = "Zero Extra Fee",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryEmerald
                            )
                        }
                    }

                    HorizontalDivider(color = PrimaryEmerald.copy(alpha = 0.15f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Base Flag-down Fee", fontSize = 11.sp, color = OnSurfaceVariantText)
                        Text("₱%,.2f".format(selectedBreakdown.baseFare), fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Distance Charge (₱15/km)", fontSize = 11.sp, color = OnSurfaceVariantText)
                        Text("₱%,.2f".format(selectedBreakdown.distanceFare), fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val minRate = if (selectedType == "Luxury") 4 else 2
                        Text("Duration Charge (₱%d/min)".format(minRate), fontSize = 11.sp, color = OnSurfaceVariantText)
                        Text("₱%,.2f".format(selectedBreakdown.timeFare), fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
                    }

                    if (selectedBreakdown.surgeMultiplier > 1.0) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("LTFRB Regulated Surge Modifier", fontSize = 11.sp, color = OnSurfaceVariantText)
                            Text("+₱%,.2f (%,.2fx)".format(selectedBreakdown.surgeComponent, selectedBreakdown.surgeMultiplier), fontSize = 12.sp, color = GoldSecondary, fontWeight = FontWeight.Bold)
                        }
                    }

                    HorizontalDivider(color = PrimaryEmerald.copy(alpha = 0.15f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Final Bound Contract Price",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "₱%,.2f".format(selectedBreakdown.totalFare),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryEmerald
                        )
                    }
                }
            }

            Button(
                onClick = onBookClick,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryContainer),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("book_now_button")
            ) {
                Text(
                    text = "Agree & Book Upfront",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = OnPrimaryContainer
                )
            }
        }
    }
}

@Composable
fun RideSelectionOptionItem(
    title: String,
    subtitle: String,
    priceLabel: String,
    durationLabel: String,
    isSelected: Boolean,
    promoTag: String?,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) PrimaryEmerald else Color.White.copy(alpha = 0.05f)
    val bgColor = if (isSelected) PrimaryContainer.copy(alpha = 0.15f) else SurfaceContainerLow

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(bgColor, RoundedCornerShape(16.dp))
            .border(if (isSelected) 2.dp else 1.dp, borderColor, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(16.dp)
            .testTag("ride_option_$title"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(3f)
        ) {
            Surface(
                color = if (isSelected) PrimaryEmerald.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.04f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.size(54.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Place, // Simulated car
                        contentDescription = "Ride",
                        tint = if (isSelected) PrimaryEmerald else OnSurfaceVariantText,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) PrimaryEmerald else Color.White
                    )
                    if (promoTag != null) {
                        Text(
                            text = promoTag.uppercase(),
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) PrimaryEmerald else GoldSecondary,
                            modifier = Modifier
                                .background(
                                    color = if (isSelected) PrimaryEmerald.copy(alpha = 0.15f) else GoldSecondary.copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                }
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = OnSurfaceVariantText,
                    lineHeight = 14.sp
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.End,
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = priceLabel,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryEmerald
            )
            Text(
                text = durationLabel,
                fontSize = 11.sp,
                color = OnSurfaceVariantText.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
fun SearchingDriverSheet(simulationMessage: String) {
    Surface(
        color = SurfaceContainer,
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
        tonalElevation = 16.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            CircularProgressIndicator(color = PrimaryEmerald)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Finding Your Ride...",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = simulationMessage,
                    fontSize = 14.sp,
                    color = OnSurfaceVariantText,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun BookedStatusSheet(
    activeRide: BookedRide?,
    simulationMessage: String,
    onArrivedClick: () -> Unit,
    onCancelClick: () -> Unit
) {
    Surface(
        color = SurfaceContainer,
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
        tonalElevation = 16.dp,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("booked_driver_dashboard")
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Driver is En Route",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Vetted category: ${activeRide?.type ?: "Family"}",
                        fontSize = 13.sp,
                        color = PrimaryEmerald
                    )
                }
                val displayedPrice = activeRide?.price?.let { "₱%,.2f".format(it) } ?: "₱18.90"
                Text(
                    text = displayedPrice,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = GoldSecondary
                )
            }

            Divider(color = Color.White.copy(alpha = 0.08f))

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(PrimaryContainer, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Ayesha Driver Avatar Slot",
                        tint = Color.White
                    )
                }

                Column {
                    Text(text = "Sister Ayesha • Vetted Female Rider", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text(text = "Nissan Patrol (Silver) • Rating: 4.98", fontSize = 12.sp, color = OnSurfaceVariantText)
                }
            }

            Text(
                text = simulationMessage,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = GoldSecondary,
                lineHeight = 16.sp,
                modifier = Modifier
                    .background(GoldSecondary.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                    .padding(12.dp)
                    .fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onCancelClick,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorContainerRed),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Cancel Ride", fontSize = 14.sp, color = ErrorRed)
                }

                Button(
                    onClick = onArrivedClick,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryContainer),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Trigger Arrived", fontSize = 14.sp, color = OnPrimaryContainer)
                }
            }
        }
    }
}
