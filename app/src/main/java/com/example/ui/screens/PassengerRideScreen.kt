package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.BookedRide
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel

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

    Box(modifier = modifier.fillMaxSize()) {
        // Full screen vector navigation map drawing
        MapBackgroundCanvas(
            isBookingActive = isBookingActive,
            rideType = selectedType
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 80.dp), // Height of navigation bar
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Section: Inputs floating panel
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
                    onDropoffChange = { viewModel.dropoffQuery.value = it }
                )

                // SOS Floating Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    FloatingSosButton(onClick = { viewModel.performQuickSadaqahPayment(10.0) })
                }
            }

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
                        selectedType = selectedType,
                        onTypeSelect = { viewModel.selectedRideType.value = it },
                        onBookClick = { viewModel.requestRide() }
                    )
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
    onDropoffChange: (String) -> Unit
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
                        .fillMaxWidth()
                        .testTag("pickup_input_field"),
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
                        .fillMaxWidth()
                        .testTag("dropoff_input_field"),
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
    selectedType: String,
    onTypeSelect: (String) -> Unit,
    onBookClick: () -> Unit
) {
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
            modifier = Modifier.padding(24.dp),
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
                    text = "NEARBY (4 MINS)",
                    fontSize = 10.sp,
                    color = OnSurfaceVariantText,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                RideSelectionOptionItem(
                    title = "Economy",
                    subtitle = "Affordable, everyday secure rides",
                    priceLabel = "PHP 12.50",
                    durationLabel = "3 min",
                    isSelected = selectedType == "Economy",
                    promoTag = "Value",
                    onClick = { onTypeSelect("Economy") }
                )

                RideSelectionOptionItem(
                    title = "Family (Safe)",
                    subtitle = "Spacious 6-seater, Halal vetted, absolute security",
                    priceLabel = "PHP 18.90",
                    durationLabel = "5 min",
                    isSelected = selectedType == "Family",
                    promoTag = "Family Vetted",
                    onClick = { onTypeSelect("Family") }
                )

                RideSelectionOptionItem(
                    title = "Female Driver",
                    subtitle = "Sister-vetted driver for comfort & privacy",
                    priceLabel = "PHP 16.20",
                    durationLabel = "7 min",
                    isSelected = selectedType == "Female",
                    promoTag = "Privacy",
                    onClick = { onTypeSelect("Female") }
                )

                RideSelectionOptionItem(
                    title = "Luxury",
                    subtitle = "Premium high-grade, spacious vehicles",
                    priceLabel = "PHP 24.00",
                    durationLabel = "4 min",
                    isSelected = selectedType == "Luxury",
                    promoTag = "Premium",
                    onClick = { onTypeSelect("Luxury") }
                )
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
                    text = "Book Now",
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
                Text(
                    text = "PHP ${activeRide?.price ?: 18.90}",
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
