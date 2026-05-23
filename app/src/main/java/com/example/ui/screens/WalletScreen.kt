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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.UserProfile
import com.example.data.WalletTransaction
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel

@Composable
fun WalletScreen(
    viewModel: MainViewModel,
    userProfile: UserProfile?,
    transactionsList: List<WalletTransaction>,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val showScanner by viewModel.showQrScanner.collectAsState()
    val scannerMsg by viewModel.qrScanCompleteMessage.collectAsState()

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Balance card showing AED
            WalletBalanceBentoCard(
                balance = userProfile?.balance ?: 4280.50,
                onTopUpClick = { viewModel.topupFunds() },
                onHistoryClick = { viewModel.withdrawFunds() }
            )

            // Dynamic weekly analytics indicators (Bento)
            WeeklyAnalyticsGrid()

            // Islamic digital assets tracker (HLGO Assets)
            HlgoDigitalAssetCard(
                hlgoBalance = userProfile?.hlgoBalance ?: 842.15,
                onCalculateZakatClick = {
                    viewModel.zakatInputWealth.value = "25000"
                    viewModel.showZakatCalculator.value = true
                }
            )

            // Switch triggers for payment configurations
            PaymentSettingsRowSection(
                isSadaqahRoundup = userProfile?.isSadaqahRoundUp ?: true,
                isHalalFilter = userProfile?.isHalalFilter ?: false,
                onSadaqahToggle = { viewModel.toggleSadaqahRoundUp() },
                onHalalToggle = { viewModel.toggleHalalFilter() }
            )

            // Recent activity lists
            RecentActivitySection(
                transactions = transactionsList,
                onQrClick = { viewModel.triggerMockScanner() }
            )

            Spacer(modifier = Modifier.height(100.dp))
        }

        // Animated toast scanner trigger completion notification
        if (scannerMsg.isNotEmpty()) {
            Snackbar(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 96.dp, start = 16.dp, end = 16.dp),
                containerColor = PrimaryContainer,
                contentColor = OnPrimaryContainer,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(text = scannerMsg, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            }
        }

        // QR Scanner overlay mockup screen representer
        if (showScanner) {
            MockQrScannerOverlay(onClose = { viewModel.showQrScanner.value = false })
        }
    }
}

@Composable
fun WeeklyAnalyticsGrid() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("weekly_analytics_grid"),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // This Week
        Surface(
            color = SurfaceContainer.copy(alpha = 0.5f),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.weight(1f)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("This Week", fontSize = 12.sp, color = OnSurfaceVariantText)
                    Text("+12%", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TertiaryEmerald)
                }
                Text("1,840 AED", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                
                // Visual bars representation
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(32.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    Box(modifier = Modifier.weight(1f).height(12.dp).background(PrimaryEmerald.copy(alpha = 0.2f), RoundedCornerShape(2.dp)))
                    Box(modifier = Modifier.weight(1f).height(20.dp).background(PrimaryEmerald.copy(alpha = 0.4f), RoundedCornerShape(2.dp)))
                    Box(modifier = Modifier.weight(1f).height(32.dp).background(PrimaryEmerald, RoundedCornerShape(2.dp)))
                    Box(modifier = Modifier.weight(1f).height(24.dp).background(PrimaryEmerald.copy(alpha = 0.6f), RoundedCornerShape(2.dp)))
                    Box(modifier = Modifier.weight(1f).height(10.dp).background(PrimaryEmerald.copy(alpha = 0.3f), RoundedCornerShape(2.dp)))
                }
            }
        }

        // Last Week
        Surface(
            color = SurfaceContainer.copy(alpha = 0.5f),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.weight(1f)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Last Week", fontSize = 12.sp, color = OnSurfaceVariantText)
                    Text("-4%", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ErrorRed)
                }
                Text("1,625 AED", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                
                // Visual greyed bars
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(32.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    Box(modifier = Modifier.weight(1f).height(16.dp).background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(2.dp)))
                    Box(modifier = Modifier.weight(1f).height(10.dp).background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(2.dp)))
                    Box(modifier = Modifier.weight(1f).height(24.dp).background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(2.dp)))
                    Box(modifier = Modifier.weight(1f).height(18.dp).background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(2.dp)))
                    Box(modifier = Modifier.weight(1f).height(28.dp).background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(2.dp)))
                }
            }
        }
    }
}

@Composable
fun HlgoDigitalAssetCard(
    hlgoBalance: Double,
    onCalculateZakatClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("hlgo_assets_tracker"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceContainerHigh.copy(alpha = 0.6f)),
        border = BorderStroke(1.dp, GoldSecondary.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = GoldSecondary.copy(alpha = 0.15f),
                        shape = CircleShape,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Home, // Mosque crypto outline indicator
                                contentDescription = "HLGO brand logo",
                                tint = GoldSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Column {
                        Text("HLGO Assets", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = GoldSecondary)
                        Text("HALAL CRYPTO ECOSYSTEM", fontSize = 10.sp, color = OnSurfaceVariantText, letterSpacing = 1.sp)
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text("${String.format("%.2f", hlgoBalance)} HLGO", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text("≈ $1,245.50 USD", fontSize = 12.sp, color = TertiaryEmerald, fontWeight = FontWeight.SemiBold)
                }
            }

            // Halal crypto milestone loading progress
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                LinearProgressIndicator(
                    progress = { 0.65f },
                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(100.dp)),
                    color = GoldSecondary,
                    trackColor = Color.White.copy(alpha = 0.05f),
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Zakat Threshold", fontSize = 11.sp, color = OnSurfaceVariantText)
                    Text("65% reached", fontSize = 11.sp, color = GoldSecondary, fontWeight = FontWeight.Bold)
                }
            }

            Button(
                onClick = onCalculateZakatClick,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                contentPadding = PaddingValues(0.dp),
                modifier = Modifier.height(24.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Calculate Zakat Due", fontSize = 13.sp, color = PrimaryEmerald, fontWeight = FontWeight.Bold)
                    Icon(
                        imageVector = Icons.Default.ArrowBack, // rotating standard back to act as right Chevron
                        contentDescription = "Zakat dues results calculator navigation",
                        tint = PrimaryEmerald,
                        modifier = Modifier.size(14.dp).rotate(180f)
                    )
                }
            }
        }
    }
}

@Composable
fun PaymentSettingsRowSection(
    isSadaqahRoundup: Boolean,
    isHalalFilter: Boolean,
    onSadaqahToggle: () -> Unit,
    onHalalToggle: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Payment Settings", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)

        Surface(
            color = SurfaceContainer,
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Sadaqah Item
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onSadaqahToggle)
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(3f)
                    ) {
                        Surface(
                            color = PrimaryContainer.copy(alpha = 0.2f),
                            shape = CircleShape,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Favorite,
                                    contentDescription = "Charity option",
                                    tint = PrimaryEmerald,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Column {
                            Text("Sadaqah Round-Up", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Text("Donate spare change directly to charity", fontSize = 12.sp, color = OnSurfaceVariantText)
                        }
                    }

                    Switch(
                        checked = isSadaqahRoundup,
                        onCheckedChange = { onSadaqahToggle() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = OnPrimary,
                            checkedTrackColor = PrimaryEmerald
                        )
                    )
                }

                Divider(color = Color.White.copy(alpha = 0.05f), modifier = Modifier.padding(horizontal = 16.dp))

                // Halal Vendor Filter
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onHalalToggle)
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(3f)
                    ) {
                        Surface(
                            color = GoldSecondary.copy(alpha = 0.1f),
                            shape = CircleShape,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Verified Vendor checks",
                                    tint = GoldSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Column {
                            Text("Halal Merchant Filter", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Text("Auto-approve only strictly verified vendors", fontSize = 12.sp, color = OnSurfaceVariantText)
                        }
                    }

                    Switch(
                        checked = isHalalFilter,
                        onCheckedChange = { onHalalToggle() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = OnPrimary,
                            checkedTrackColor = PrimaryEmerald
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun RecentActivitySection(
    transactions: List<WalletTransaction>,
    onQrClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Recent Activity", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onQrClick,
                    modifier = Modifier
                        .size(36.dp)
                        .background(PrimaryContainer.copy(alpha = 0.3f), CircleShape)
                        .border(1.dp, PrimaryEmerald.copy(alpha = 0.3f), CircleShape)
                        .testTag("qr_trigger_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add, // Standard Scanner analogy representation
                        contentDescription = "Scan to Pay QR code scanner",
                        tint = PrimaryEmerald,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Text("View All", fontSize = 12.sp, color = PrimaryEmerald, fontWeight = FontWeight.Bold)
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            for (tx in transactions.take(3)) {
                TransactionHistoryRow(tx)
            }
        }
    }
}

@Composable
fun TransactionHistoryRow(transaction: WalletTransaction) {
    val isDeduction = transaction.amount < 0
    val amountSymbol = if (isDeduction) "-" else "+"
    val amountColor = if (isDeduction) Color.White else PrimaryEmerald

    Surface(
        color = SurfaceContainer.copy(alpha = 0.5f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
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
                Surface(
                    color = when (transaction.category) {
                        "FOOD" -> GoldSecondary.copy(alpha = 0.1f)
                        "RIDE" -> PrimaryEmerald.copy(alpha = 0.1f)
                        else -> TertiaryEmerald.copy(alpha = 0.1f)
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = when (transaction.category) {
                                "FOOD" -> Icons.Default.ShoppingCart
                                "RIDE" -> Icons.Default.Place
                                else -> Icons.Default.Check
                            },
                            contentDescription = transaction.category,
                            tint = when (transaction.category) {
                                "FOOD" -> GoldSecondary
                                "RIDE" -> PrimaryEmerald
                                else -> TertiaryEmerald
                            }
                        )
                    }
                }

                Column {
                    Text(
                        text = transaction.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = transaction.dateTimeString,
                        fontSize = 12.sp,
                        color = OnSurfaceVariantText
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "$amountSymbol$${String.format("%.2f", Math.abs(transaction.amount))}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = amountColor
                )
                Text(
                    text = transaction.statusString,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (transaction.statusString == "COMPLETED") PrimaryEmerald else GoldSecondary,
                    modifier = Modifier
                        .background(
                            if (transaction.statusString == "COMPLETED") PrimaryEmerald.copy(alpha = 0.12f) else GoldSecondary.copy(alpha = 0.12f),
                            RoundedCornerShape(100.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
fun MockQrScannerOverlay(onClose: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.9f))
            .clickable(enabled = false) {}
            .testTag("qr_mock_overlay_scanner")
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            Text(
                text = "Scannig QR Code...",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryEmerald
            )

            // Dynamic targeted scanner borders
            Box(
                modifier = Modifier
                    .size(240.dp)
                    .border(2.dp, PrimaryEmerald.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
                    .padding(24.dp)
            ) {
                // Diagonal markers drawn inside
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val side = 40f
                    // Top-Left marker
                    drawLine(color = PrimaryEmerald, start = Offset(0f, 0f), end = Offset(side, 0f), strokeWidth = 8f)
                    drawLine(color = PrimaryEmerald, start = Offset(0f, 0f), end = Offset(0f, side), strokeWidth = 8f)

                    // Top-Right marker
                    drawLine(color = PrimaryEmerald, start = Offset(size.width, 0f), end = Offset(size.width - side, 0f), strokeWidth = 8f)
                    drawLine(color = PrimaryEmerald, start = Offset(size.width, 0f), end = Offset(size.width, side), strokeWidth = 8f)

                    // Bottom-Left marker
                    drawLine(color = PrimaryEmerald, start = Offset(0f, size.height), end = Offset(side, size.height), strokeWidth = 8f)
                    drawLine(color = PrimaryEmerald, start = Offset(0f, size.height), end = Offset(0f, size.height - side), strokeWidth = 8f)

                    // Bottom-Right marker
                    drawLine(color = PrimaryEmerald, start = Offset(size.width, size.height), end = Offset(size.width - side, size.height), strokeWidth = 8f)
                    drawLine(color = PrimaryEmerald, start = Offset(size.width, size.height), end = Offset(size.width, size.height - side), strokeWidth = 8f)
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add, // simulated code center
                        contentDescription = "Target matrix pointer animation",
                        tint = PrimaryEmerald.copy(alpha = 0.3f),
                        modifier = Modifier.size(64.dp)
                    )
                }
            }

            Text(
                text = "Position target code within parameters to verify Halal certificates and transfer funds.",
                fontSize = 13.sp,
                color = OnSurfaceVariantText,
                textAlign = TextAlign.Center
            )

            Button(
                onClick = onClose,
                colors = ButtonDefaults.buttonColors(containerColor = SurfaceContainerHigh),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Cancel Scan", color = Color.White)
            }
        }
    }
}
