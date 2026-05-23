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
import androidx.compose.ui.draw.rotate
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
fun PrayerScreen(
    viewModel: MainViewModel,
    ramadanMode: Boolean,
    compassRotation: Float,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Ramadan mode switcher and Hijri date labels
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "TODAY",
                    fontSize = 11.sp,
                    color = OnSurfaceVariantText,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "14 Ramadan, 1445 AH",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            // Ramadan mode interactive trigger tag
            Row(
                modifier = Modifier
                    .background(
                        if (ramadanMode) GoldSecondary.copy(alpha = 0.2f) else SurfaceContainer,
                        RoundedCornerShape(100.dp)
                    )
                    .border(
                        1.dp,
                        if (ramadanMode) GoldSecondary.copy(alpha = 0.4f) else Color.White.copy(alpha = 0.1f),
                        RoundedCornerShape(100.dp)
                    )
                    .clickable { viewModel.toggleRamadanMode() }
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Favorite, // Simple crescent analogy
                    contentDescription = "Ramadan Mode switch",
                    tint = if (ramadanMode) GoldSecondary else OnSurfaceVariantText,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "RAMADAN MODE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (ramadanMode) GoldSecondary else OnSurfaceVariantText
                )
            }
        }

        // Qibla Compass and countdown active clock (Main Hero Card)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("prayer_compass_card"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceContainer.copy(alpha = 0.5f)),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "NEXT PRAYER: MAGHRIB",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryEmerald,
                        letterSpacing = 1.5.sp
                    )
                    Text(
                        text = "08:32:25",
                        fontSize = 52.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        lineHeight = 56.sp
                    )
                    Text(
                        text = "Time to Iftar / Sunset",
                        fontSize = 13.sp,
                        color = OnSurfaceVariantText
                    )
                }

                // Rotary compass representation
                Box(
                    modifier = Modifier
                        .size(180.dp)
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Standard dial outline
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawCircle(
                            color = Color.White.copy(alpha = 0.06f),
                            style = Stroke(width = 4f)
                        )
                        drawCircle(
                            color = Color.White.copy(alpha = 0.03f),
                            style = Stroke(width = 1f)
                        )
                    }

                    // Rotating arrow pointer
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .rotate(compassRotation),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Place,
                            contentDescription = "Qibla pointer pointing Mecca",
                            tint = GoldSecondary,
                            modifier = Modifier
                                .size(36.dp)
                                .offset(y = (-20).dp)
                        )
                    }

                    // Central glowing pin
                    Surface(
                        color = OnSurfaceText,
                        border = BorderStroke(4.dp, DarkBackground),
                        shape = CircleShape,
                        modifier = Modifier.size(16.dp)
                    ) {}

                    // Directions labels
                    Box(modifier = Modifier.matchParentSize()) {
                        Text("N", fontSize = 11.sp, color = OnSurfaceVariantText, modifier = Modifier.align(Alignment.TopCenter))
                        Text("S", fontSize = 11.sp, color = OnSurfaceVariantText, modifier = Modifier.align(Alignment.BottomCenter))
                        Text("W", fontSize = 11.sp, color = OnSurfaceVariantText, modifier = Modifier.align(Alignment.CenterStart))
                        Text("E", fontSize = 11.sp, color = OnSurfaceVariantText, modifier = Modifier.align(Alignment.CenterEnd))
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(Color.White.copy(alpha = 0.03f), RoundedCornerShape(12.dp))
                            .border(1.dp, Color.White.copy(alpha = 0.04f), RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Qibla", fontSize = 11.sp, color = OnSurfaceVariantText)
                            Text("145° SE", fontSize = 16.sp, color = PrimaryEmerald, fontWeight = FontWeight.Bold)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(Color.White.copy(alpha = 0.03f), RoundedCornerShape(12.dp))
                            .border(1.dp, Color.White.copy(alpha = 0.04f), RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Location", fontSize = 11.sp, color = OnSurfaceVariantText)
                            Text("Dubai, UAE", fontSize = 16.sp, color = PrimaryEmerald, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Quick charity pay bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(colors = listOf(SecondaryContainer, GoldSecondary)),
                    shape = RoundedCornerShape(24.dp)
                )
                .clickable {
                    viewModel.zakatInputWealth.value = "25000"
                    viewModel.showZakatCalculator.value = true
                }
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = Color.Black.copy(alpha = 0.12f),
                    shape = CircleShape,
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "Zakat and Sadaqah Charity Option",
                            tint = OnSecondaryContainer,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                Column {
                    Text(
                        text = "Zakat & Sadaqah",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = OnSecondaryContainer
                    )
                    Text(
                        text = "Quick pay for Ramadan blessings",
                        fontSize = 12.sp,
                        color = OnSecondaryContainer.copy(alpha = 0.8f)
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.ArrowBack, // simple pointing arrow rotation analogy
                contentDescription = "Pay Zakat",
                tint = OnSecondaryContainer,
                modifier = Modifier
                    .size(20.dp)
                    .rotate(180f)
            )
        }

        // Prayer Schedule Sections
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Daily Schedule",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Event details schedule",
                    tint = PrimaryEmerald
                )
            }

            // Schedule table items
            PrayerScheduleRow(name = "Fajr", details = "Dawn", time = "05:12", alertEnabled = false)
            PrayerScheduleRow(name = "Dhuhr", details = "Noon", time = "12:34", alertEnabled = true)
            PrayerScheduleRow(name = "Asr", details = "Afternoon", time = "15:58", alertEnabled = true)
            PrayerScheduleRow(name = "Maghrib", details = "Sunset • Next", time = "18:32", alertEnabled = true, activeState = true)
            PrayerScheduleRow(name = "Isha", details = "Night", time = "19:48", alertEnabled = false)
        }

        // Atmosphere visual indicators bento grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AtmosphereMetricsBox(
                icon = Icons.Default.Star, // Sunset time indicator
                label = "Sunset Time",
                value = "18:32 PM",
                modifier = Modifier.weight(1f)
            )

            AtmosphereMetricsBox(
                icon = Icons.Default.Send, // Humidity air metric indicator
                label = "Humidity",
                value = "42%",
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}

@Composable
fun PrayerScheduleRow(
    name: String,
    details: String,
    time: String,
    alertEnabled: Boolean,
    activeState: Boolean = false
) {
    val containerBg = if (activeState) PrimaryContainer.copy(alpha = 0.2f) else SurfaceContainer.copy(alpha = 0.5f)
    val containerBorder = if (activeState) BorderStroke(2.dp, PrimaryEmerald.copy(alpha = 0.4f)) else BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    val textColor = if (activeState) PrimaryEmerald else Color.White

    Surface(
        color = containerBg,
        border = containerBorder,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("prayer_row_$name")
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (name == "Maghrib") Icons.Default.Favorite else Icons.Default.Home,
                    contentDescription = name,
                    tint = if (activeState) PrimaryEmerald else OnSurfaceVariantText
                )

                Column {
                    Text(
                        text = name,
                        fontSize = 16.sp,
                        fontWeight = if (activeState) FontWeight.Bold else FontWeight.Medium,
                        color = textColor
                    )
                    Text(
                        text = details,
                        fontSize = 12.sp,
                        color = OnSurfaceVariantText
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = time,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )

                Icon(
                    imageVector = if (alertEnabled) Icons.Default.Notifications else Icons.Default.Close,
                    contentDescription = "Alert toggles",
                    tint = if (activeState) PrimaryEmerald else OnSurfaceVariantText.copy(alpha = 0.6f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun AtmosphereMetricsBox(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        color = SurfaceContainer.copy(alpha = 0.5f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = GoldSecondary
            )
            Text(
                text = label,
                fontSize = 12.sp,
                color = OnSurfaceVariantText
            )
            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}
