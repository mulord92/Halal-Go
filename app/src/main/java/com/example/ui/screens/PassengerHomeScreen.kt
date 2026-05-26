package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
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
fun PassengerHomeScreen(
    viewModel: MainViewModel,
    userProfile: UserProfile?,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Hero Wallet balance card (Visual Glassmorphism)
        WalletBalanceBentoCard(
            balance = userProfile?.balance ?: 12450.85,
            onTopUpClick = { viewModel.topupFunds() },
            onHistoryClick = { viewModel.setPassengerTab("Wallet") }
        )

        // Services Action Grid (Ride, Food, Grocery, Parcel)
        QuickActionGridSection(
            onRideClick = { viewModel.setPassengerTab("Ride") },
            onFoodClick = { 
                viewModel.setPassengerTab("Delivery")
                viewModel.setDeliverySubTab("Food")
            },
            onGroceryClick = { 
                viewModel.setPassengerTab("Delivery")
                viewModel.setDeliverySubTab("Grocery")
            },
            onParcelClick = { 
                viewModel.setPassengerTab("Delivery")
                viewModel.setDeliverySubTab("Parcel")
            }
        )

        // Cinematic promotion banner card
        PromoBannerSection(onPromoClick = { viewModel.setPassengerTab("Ride") })

        // Nearby Halal food carousel
        NearbyHalalRestaurantsSection()
        
        Spacer(modifier = Modifier.height(80.dp)) // Padding to ensure scroll spacing
    }
}

@Composable
fun WalletBalanceBentoCard(
    balance: Double,
    onTopUpClick: () -> Unit,
    onHistoryClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("wallet_balance_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(PrimaryContainer, Color(0xFF1B6B51)),
                        start = Offset(0f, 0f),
                        end = Offset(1000f, 1000f)
                    )
                )
                .border(1.dp, GoldSecondary.copy(alpha = 0.3f), RoundedCornerShape(24.dp))
                .padding(24.dp)
        ) {
            // Background design circles
            Canvas(modifier = Modifier.matchParentSize()) {
                drawCircle(
                    color = GoldSecondary.copy(alpha = 0.05f),
                    radius = 350f,
                    center = Offset(size.width, 0f)
                )
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "TOTAL BALANCE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryEmerald.copy(alpha = 0.8f),
                            letterSpacing = 1.5.sp
                        )
                        Text(
                            text = "PHP ${String.format("%,.2f", balance)}",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    // Gold tier badge
                    Row(
                        modifier = Modifier
                            .background(GoldSecondary.copy(alpha = 0.2f), RoundedCornerShape(100.dp))
                            .border(1.dp, GoldSecondary.copy(alpha = 0.3f), RoundedCornerShape(100.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Gold Tier Logo",
                            tint = GoldSecondary,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "Gold Tier",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = GoldSecondary
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onTopUpClick,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("top_up_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.12f)),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(vertical = 14.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Top Up",
                                tint = GoldSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text("Top Up", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                        }
                    }

                    Button(
                        onClick = onHistoryClick,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("history_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.12f)),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(vertical = 14.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Transaction History",
                                tint = GoldSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text("History", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QuickActionGridSection(
    onRideClick: () -> Unit,
    onFoodClick: () -> Unit,
    onGroceryClick: () -> Unit,
    onParcelClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("services_action_grid"),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        QuickGridItem(icon = Icons.Default.Place, label = "Ride", onClick = onRideClick)
        QuickGridItem(icon = Icons.Default.ShoppingCart, label = "Food", onClick = onFoodClick)
        QuickGridItem(icon = Icons.Default.Favorite, label = "Grocery", onClick = onGroceryClick)
        QuickGridItem(icon = Icons.Default.Share, label = "Parcel", onClick = onParcelClick)
    }
}

@Composable
fun QuickGridItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .clickable(onClick = onClick)
            .testTag("service_item_$label")
    ) {
        Surface(
            modifier = Modifier.size(64.dp),
            shape = RoundedCornerShape(24.dp),
            color = SurfaceContainer,
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
            tonalElevation = 6.dp
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = PrimaryEmerald,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = OnSurfaceText,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun PrayerWidgetSection(
    ramadanMode: Boolean,
    onWidgetClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onWidgetClick)
            .testTag("prayer_widget"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceContainer.copy(alpha = 0.85f)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
    ) {
        Row(
            modifier = Modifier
                .drawBehind {
                    // Golden left accent stripe
                    drawRect(
                        color = GoldSecondary,
                        topLeft = Offset(0f, 0f),
                        size = Size(12f, size.height)
                    )
                }
                .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1.5f)
            ) {
                Surface(
                    color = GoldSecondary.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = "Mosque Symbol representing Prayer",
                            tint = GoldSecondary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Next Prayer: Asr",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Starts in",
                            fontSize = 13.sp,
                            color = OnSurfaceVariantText
                        )
                        Text(
                            text = "45:15",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = GoldSecondary
                        )
                        if (ramadanMode) {
                            Text(
                                text = "• Ramadan",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = GoldSecondary,
                                modifier = Modifier
                                    .background(GoldSecondary.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                }
            }

            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "3:45 PM",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = PrimaryEmerald
                )
                Text(
                    text = "Qibla: 254.2°",
                    fontSize = 11.sp,
                    color = OnSurfaceVariantText.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
fun PromoBannerSection(onPromoClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clickable(onClick = onPromoClick)
            .testTag("discount_banner"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Draw luxury abstract futuristic vehicle lines on background
            Canvas(modifier = Modifier.matchParentSize()) {
                val lineBrush = Brush.linearGradient(
                    colors = listOf(PrimaryEmerald.copy(alpha = 0.25f), Colors.Transparent),
                    start = Offset(0f, size.height),
                    end = Offset(size.width, 0f)
                )
                drawRect(
                    brush = Brush.linearGradient(
                        colors = listOf(PrimaryContainer, Color(0xFF0C1322)),
                        start = Offset(0f, 0f),
                        end = Offset(size.width, size.height)
                    )
                )
                // Diagonal speed vectors
                drawLine(
                    brush = lineBrush,
                    start = Offset(0f, size.height * 0.8f),
                    end = Offset(size.width * 0.8f, 0f),
                    strokeWidth = 12f
                )
                drawLine(
                    brush = lineBrush,
                    start = Offset(0f, size.height * 0.4f),
                    end = Offset(size.width * 0.4f, 0f),
                    strokeWidth = 6f
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "LIMITED OFFER",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.6f),
                    modifier = Modifier
                        .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "First Ride 50% Off",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Premium, secure transport, vetted for peace of mind",
                    fontSize = 12.sp,
                    color = PrimaryEmerald
                )
            }
        }
    }
}

data class FoodRestaurant(
    val name: String,
    val style: String,
    val rating: String,
    val time: String,
    val decorOffset: Float
)

@Composable
fun NearbyHalalRestaurantsSection() {
    val itemsList = listOf(
        FoodRestaurant("Al-Sultan Grill", "Middle Eastern", "4.8", "15-25 min", 0.5f),
        FoodRestaurant("Bait Al Burger", "American Gourmet", "4.6", "10-20 min", 0.2f),
        FoodRestaurant("Green Oasis Cafe", "Healthy Organic", "4.9", "8-15 min", 0.8f)
    )

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Nearby Halal Food",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "View All",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryEmerald,
                modifier = Modifier.clickable { }
            )
        }

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 8.dp)
        ) {
            items(itemsList) { restaurant ->
                RestaurantBentoCard(restaurant)
            }
        }
    }
}

@Composable
fun RestaurantBentoCard(restaurant: FoodRestaurant) {
    Card(
        modifier = Modifier
            .width(280.dp)
            .testTag("restaurant_card_${restaurant.name}"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceContainer.copy(alpha = 0.85f)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.06f))
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(128.dp)
                    .background(Color(0xFF141F32))
            ) {
                // Top-down abstract presentation of culinary flatlay photo (drawn via canvas vector)
                Canvas(modifier = Modifier.matchParentSize()) {
                    // Dark wooden plate
                    drawCircle(
                        color = Color(0xFF231F1D),
                        radius = 120f,
                        center = Offset(size.width * 0.5f, size.height * 0.5f)
                    )
                    // Nested glowing highlights inside plate represents luxury culinary setup
                    drawCircle(
                        color = GoldSecondary.copy(alpha = 0.3f),
                        radius = 100f,
                        center = Offset(size.width * 0.5f, size.height * 0.5f),
                        style = Stroke(width = 4f)
                    )
                    // Salad bowl shapes helper vectors
                    drawCircle(
                        color = TertiaryEmerald.copy(alpha = 0.4f),
                        radius = 45f,
                        center = Offset(size.width * 0.3f, size.height * 0.4f)
                    )
                    drawCircle(
                        color = GoldSecondary.copy(alpha = 0.5f),
                        radius = 35f,
                        center = Offset(size.width * 0.7f, size.height * 0.6f)
                    )
                }

                Surface(
                    modifier = Modifier
                        .padding(12.dp)
                        .align(Alignment.TopEnd),
                    color = GoldSecondary,
                    shape = RoundedCornerShape(100.dp)
                ) {
                    Text(
                        text = "HALAL CERTIFIED",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = OnSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = restaurant.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Ratings Key",
                            tint = GoldSecondary,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = restaurant.rating,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
                Text(
                    text = "${restaurant.style} • ${restaurant.time}",
                    fontSize = 13.sp,
                    color = OnSurfaceVariantText
                )
            }
        }
    }
}

// Global empty representation to ensure correct compiles
object Colors {
    val Transparent = Color(0x00FFFFFF)
}
