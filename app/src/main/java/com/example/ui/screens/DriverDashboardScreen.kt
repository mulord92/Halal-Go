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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel

@Composable
fun DriverDashboardScreen(
    viewModel: MainViewModel,
    isOnline: Boolean,
    showNewRequest: Boolean,
    popupTimerCount: Int,
    isDriverOnActiveRide: Boolean,
    activeDriverRideStatus: String,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        if (isDriverOnActiveRide) {
            // Live Satellite turn-by-turn navigation overlay (Screen 8)
            DriverLiveNavigationOverlay(
                activeStatus = activeDriverRideStatus,
                onArriveClick = { viewModel.completeDriverActiveRide() }
            )
        } else {
            // Base dashboard layout (Screen 5)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Top section: Ahmed K's earnings summaries card
                DriverDailyEarningsCard(earnings = 450.00, rides = 12, rating = 4.9)

                // Central Go Online tactile action (shown only when Offline)
                if (!isOnline) {
                    DriverOfflineModeCentralAction(onClick = { viewModel.toggleDriverOnline() })
                } else {
                    DriverOnlineModeWaitingState(onClick = { viewModel.toggleDriverOnline() })
                }
            }
        }

        // Slide-up ride request invitation modal overlay (Screen 7)
        AnimatedVisibility(
            visible = showNewRequest && isOnline && !isDriverOnActiveRide,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        ) {
            IncomingRideRequestPopup(
                secondsRemaining = popupTimerCount,
                onAccept = { viewModel.acceptRideInvitation() },
                onDecline = { viewModel.declineRideInvitation() }
            )
        }
    }
}

@Composable
fun DriverDailyEarningsCard(
    earnings: Double,
    rides: Int,
    rating: Double
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("driver_earnings_dashboard_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceContainer.copy(alpha = 0.85f)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
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
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "TODAY'S EARNINGS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryEmerald,
                        letterSpacing = 1.5.sp
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(text = "PHP", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = GoldSecondary)
                        Text(text = String.format("%.2f", earnings), fontSize = 36.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }

                Surface(
                    color = PrimaryEmerald.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Star, // simple upward trend proxy
                            contentDescription = "Trending statistics",
                            tint = PrimaryEmerald
                        )
                    }
                }
            }

            Divider(color = Color.White.copy(alpha = 0.08f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Total Rides bento box
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .background(Color.White.copy(alpha = 0.04f), RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        color = Color.White.copy(alpha = 0.05f),
                        shape = CircleShape,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Place, // ride
                                contentDescription = "Rides Completed",
                                tint = PrimaryEmerald,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Column {
                        Text("Rides", fontSize = 11.sp, color = OnSurfaceVariantText)
                        Text("$rides", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }

                // Average Rating bento box
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .background(Color.White.copy(alpha = 0.04f), RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        color = Color.White.copy(alpha = 0.05f),
                        shape = CircleShape,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Star, // ratings star
                                contentDescription = "Lifetime average ratings",
                                tint = GoldSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Column {
                        Text("Rating", fontSize = 11.sp, color = OnSurfaceVariantText)
                        Text("$rating", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun DriverOfflineModeCentralAction(onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Glowing animated power button
        Button(
            onClick = onClick,
            modifier = Modifier
                .size(160.dp)
                .testTag("go_online_button_tactile"),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            shape = CircleShape,
            contentPadding = PaddingValues(0.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(PrimaryContainer, Color(0xFF1B6B51)),
                            radius = 240f
                        ),
                        shape = CircleShape
                    )
                    .border(2.dp, PrimaryEmerald, CircleShape)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Menu, // central power toggle indicator proxy
                        contentDescription = "Tactile Go Online link trigger",
                        tint = Color.White,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "GO ONLINE",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        letterSpacing = 1.sp
                    )
                }
            }
        }

        Text(
            text = "Tap to start receiving Halal-certified ride requests.",
            fontSize = 13.sp,
            color = OnSurfaceVariantText,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(200.dp)
        )
    }
}

@Composable
fun DriverOnlineModeWaitingState(onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        CircularProgressIndicator(color = GoldSecondary)
        
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "Waiting for Requests...",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = GoldSecondary
            )
            Text(
                text = "Online in Makati CBD. Top priority active.",
                fontSize = 13.sp,
                color = OnSurfaceVariantText
            )
        }

        Button(
            onClick = onClick,
            colors = ButtonDefaults.buttonColors(containerColor = ErrorContainerRed),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("GO OFFLINE", color = ErrorRed, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun IncomingRideRequestPopup(
    secondsRemaining: Int,
    onAccept: () -> Unit,
    onDecline: () -> Unit
) {
    Surface(
        color = SurfaceContainerHighest.copy(alpha = 0.95f),
        border = BorderStroke(2.dp, GoldSecondary.copy(alpha = 0.3f)),
        shape = RoundedCornerShape(24.dp),
        tonalElevation = 24.dp,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("driver_incoming_request_card")
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("New Ride Request", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Row(
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .background(GoldSecondary.copy(alpha = 0.15f), RoundedCornerShape(100.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Favorite, // shield verified proxy
                            contentDescription = "Safe indicators",
                            tint = GoldSecondary,
                            modifier = Modifier.size(12.dp)
                        )
                        Text("Family Safe Vetted", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = GoldSecondary)
                    }
                }

                // Countdown radial indicator (Screen 7)
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(64.dp)
                ) {
                    CircularProgressIndicator(
                        progress = { secondsRemaining / 15f },
                        color = PrimaryEmerald,
                        trackColor = Color.White.copy(alpha = 0.08f),
                        strokeWidth = 4.dp,
                        modifier = Modifier.fillMaxSize()
                    )
                    Text(
                        text = "$secondsRemaining",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Estimated Earnings bento
                Surface(
                    color = Color.White.copy(alpha = 0.03f),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Estimated Earnings", fontSize = 11.sp, color = OnSurfaceVariantText)
                        Text("PHP 45.00", fontSize = 15.sp, color = PrimaryEmerald, fontWeight = FontWeight.Bold)
                    }
                }

                // Distance bento
                Surface(
                    color = Color.White.copy(alpha = 0.03f),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Distance & Time", fontSize = 11.sp, color = OnSurfaceVariantText)
                        Text("4.2 km (12 mins)", fontSize = 14.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            // Route endpoints details (Screen 7)
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top,
                modifier = Modifier
                    .background(Color.White.copy(alpha = 0.03f), RoundedCornerShape(16.dp))
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.height(60.dp)
                ) {
                    Box(modifier = Modifier.size(10.dp).background(PrimaryEmerald, CircleShape))
                    Box(modifier = Modifier.width(1.dp).height(32.dp).background(Color.White.copy(alpha = 0.2f)))
                    Box(modifier = Modifier.size(10.dp).background(GoldSecondary, CircleShape))
                }

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Column {
                        Text("PICKUP", fontSize = 10.sp, color = OnSurfaceVariantText, fontWeight = FontWeight.Bold)
                        Text("SM Mall of Asia, Pasay", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                    }
                    Column {
                        Text("DROP-OFF", fontSize = 10.sp, color = OnSurfaceVariantText, fontWeight = FontWeight.Bold)
                        Text("BGC High Street, Taguig", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onDecline,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceContainerHigh),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Decline", color = Color.White)
                }

                Button(
                    onClick = onAccept,
                    modifier = Modifier.weight(1.5f),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryContainer),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Accept Ride", color = OnPrimaryContainer, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun DriverLiveNavigationOverlay(
    activeStatus: String,
    onArriveClick: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Map Satellite Background
        MapBackgroundCanvas(isBookingActive = true, rideType = "Luxury")

        // Top Navigation direction bar (Screen 8)
        Surface(
            color = SurfaceContainer.copy(alpha = 0.9f),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, start = 16.dp, end = 16.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(3f)
                ) {
                    Surface(
                        color = PrimaryEmerald,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.size(54.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack, // sligh left turn proxy
                                contentDescription = "Turn indicator",
                                tint = OnPrimary,
                                modifier = Modifier.size(28.dp).rotate(45f)
                            )
                        }
                    }

                    Column {
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.Bottom) {
                            Text("300", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = PrimaryEmerald)
                            Text("METERS", fontSize = 10.sp, color = PrimaryEmerald, fontWeight = FontWeight.Bold)
                        }
                        Text("Turn Left in 300m", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text("onto Sheikh Zayed Road", fontSize = 12.sp, color = OnSurfaceVariantText)
                    }
                }

                IconButton(onClick = {}) {
                    Icon(imageVector = Icons.Default.Notifications, contentDescription = "Volume guidelines", tint = OnSurfaceVariantText)
                }
            }
        }

        // Bottom navigation statistics sheet floating (Screen 8)
        Surface(
            color = SurfaceContainer.copy(alpha = 0.95f),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Trip progression metrics
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text("ESTIMATED ARRIVAL", fontSize = 10.sp, color = OnSurfaceVariantText, fontWeight = FontWeight.Bold)
                        Text("12:45 PM", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                        Column(horizontalAlignment = Alignment.End) {
                            Text("TIME", fontSize = 10.sp, color = OnSurfaceVariantText, fontWeight = FontWeight.Bold)
                            Text("14 min", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = PrimaryEmerald)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("DISTANCE", fontSize = 10.sp, color = OnSurfaceVariantText, fontWeight = FontWeight.Bold)
                            Text("8.2 km", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = GoldSecondary)
                        }
                    }
                }

                // Completion status percentage progress loading bar
                val progressFraction = when (activeStatus) {
                    "PICKUP" -> 0.25f
                    "ARRIVED" -> 0.70f
                    else -> 1.0f
                }
                LinearProgressIndicator(
                    progress = { progressFraction },
                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(100.dp)),
                    color = PrimaryEmerald,
                    trackColor = Color.White.copy(alpha = 0.05f)
                )

                // Navigation controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = onArriveClick,
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryContainer),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(3f)
                            .height(54.dp)
                            .testTag("driver_arrived_action_button")
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = when (activeStatus) {
                                    "PICKUP" -> "En Route to Pickup"
                                    "ARRIVED" -> "Arrived at Pickup"
                                    else -> "Arrived at Destination"
                                },
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = OnPrimaryContainer
                            )
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Navigation milestone complete",
                                tint = OnPrimaryContainer
                            )
                        }
                    }

                    // Messaging buttons
                    IconButton(
                        onClick = {},
                        modifier = Modifier
                            .size(54.dp)
                            .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                    ) {
                        Icon(imageVector = Icons.Default.Call, contentDescription = "Call Passenger directly", tint = Color.White)
                    }

                    IconButton(
                        onClick = {},
                        modifier = Modifier
                            .size(54.dp)
                            .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                    ) {
                        Icon(imageVector = Icons.Default.Send, contentDescription = "Chat with Passenger", tint = Color.White)
                    }
                }
            }
        }
    }
}
