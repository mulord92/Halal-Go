package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.UserProfile
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PassengerDeliveryScreen(
    viewModel: MainViewModel,
    userProfile: UserProfile?,
    modifier: Modifier = Modifier
) {
    val activeTab by viewModel.currentDeliverySubTab.collectAsState()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // State managers for popups & checkouts
    var selectedFoodItem by remember { mutableStateOf<FoodMenuItem?>(null) }
    var selectedRestaurant by remember { mutableStateOf<FoodRestaurantDetails?>(null) }
    
    var groceryBasket by remember { mutableStateOf<Map<GroceryItem, Int>>(emptyMap()) }
    var showGroceryCheckout by remember { mutableStateOf(false) }

    // Parcel booking states
    var parcelSender by remember { mutableStateOf(userProfile?.name ?: "") }
    var parcelReceiver by remember { mutableStateOf("") }
    var parcelPhone by remember { mutableStateOf("") }
    var parcelWeightState by remember { mutableStateOf("1.5") }
    var parcelCategory by remember { mutableStateOf("Document") } // Document, Food, Packaged, Fragile
    var parcelTrackingState by remember { mutableStateOf<String?>(null) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = DarkBackground,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Elegant Premium App Bar Title
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkSurface)
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "HALAL SERVICES",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = GoldSecondary,
                            letterSpacing = 1.5.sp
                        )
                        Text(
                            text = "Vetted Delivery Hub",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    // Balance Display Indicator
                    Surface(
                        color = PrimaryContainer.copy(alpha = 0.3f),
                        border = BorderStroke(1.dp, PrimaryEmerald.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.ShoppingCart, contentDescription = "Balance Indicator", tint = GoldSecondary, modifier = Modifier.size(14.dp))
                            Text(
                                text = "PHP ${String.format("%,.2f", userProfile?.balance ?: 0.0)}",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Custom Material 3 Service Picker Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkSurface)
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                listOf("Food", "Grocery", "Parcel").forEach { tab ->
                    val isActive = activeTab.equals(tab, ignoreCase = true)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 4.dp)
                            .height(44.dp)
                            .background(
                                if (isActive) PrimaryEmerald else SurfaceContainer.copy(alpha = 0.5f),
                                RoundedCornerShape(12.dp)
                            )
                            .clickable { viewModel.setDeliverySubTab(tab) }
                            .testTag("delivery_tab_$tab"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val tabIcon = when (tab) {
                                "Food" -> Icons.Default.ShoppingCart
                                "Grocery" -> Icons.Default.Favorite
                                else -> Icons.Default.Share
                            }
                            Icon(
                                imageVector = tabIcon,
                                contentDescription = tab,
                                tint = if (isActive) OnPrimary else OnSurfaceVariantText,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = tab,
                                color = if (isActive) OnPrimary else OnSurfaceText,
                                fontSize = 13.sp,
                                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // Tabs Router Content
            Box(modifier = Modifier.weight(1f)) {
                when (activeTab) {
                    "Food" -> FoodDeliveryTab(
                        onItemSelect = { restaurant, item ->
                            selectedRestaurant = restaurant
                            selectedFoodItem = item
                        }
                    )
                    "Grocery" -> GroceryDeliveryTab(
                        basket = groceryBasket,
                        onUpdateQty = { item, delta ->
                            val currentQty = groceryBasket[item] ?: 0
                            val newQty = currentQty + delta
                            groceryBasket = if (newQty <= 0) {
                                groceryBasket - item
                            } else {
                                groceryBasket + (item to newQty)
                            }
                        },
                        onCheckoutClick = { showGroceryCheckout = true }
                    )
                    "Parcel" -> ParcelDeliveryTab(
                        sender = parcelSender,
                        receiver = parcelReceiver,
                        phone = parcelPhone,
                        weight = parcelWeightState,
                        category = parcelCategory,
                        trackingState = parcelTrackingState,
                        onSenderChange = { parcelSender = it },
                        onReceiverChange = { parcelReceiver = it },
                        onPhoneChange = { parcelPhone = it },
                        onWeightChange = { parcelWeightState = it },
                        onCategorySelect = { parcelCategory = it },
                        onBookClick = {
                            val weightVal = parcelWeightState.toDoubleOrNull() ?: 1.0
                            val basePrice = 80.0 + (weightVal * 25.0)
                            if (parcelReceiver.isNotBlank()) {
                                viewModel.bookParcelDelivery(
                                    sender = parcelSender,
                                    receiver = parcelReceiver,
                                    typeString = parcelCategory,
                                    price = basePrice,
                                    hasSadaqahRoundUp = userProfile?.isSadaqahRoundUp ?: true
                                )
                                parcelTrackingState = "DEL_TRK_${(100000..999999).random()}"
                                scope.launch {
                                    snackbarHostState.showSnackbar("Alhamdulillah! Parcel Delivery booked. Tracking ID: $parcelTrackingState")
                                }
                            }
                        },
                        onClearTracking = { parcelTrackingState = null }
                    )
                }
            }
        }

        // --- FOOD CHECKOUT DIALOG / BOTTOM MEETS ---
        selectedFoodItem?.let { item ->
            val restaurant = selectedRestaurant ?: return@let
            val isRoundUpActive = userProfile?.isSadaqahRoundUp ?: true
            val roundedValue = if (isRoundUpActive) Math.ceil(item.price / 10.0) * 10.0 else item.price
            val roundUpDiff = roundedValue - item.price

            AlertDialog(
                onDismissRequest = { selectedFoodItem = null },
                containerColor = DarkSurface,
                title = {
                    Text(
                        text = "Vetted Halal Checkout",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "Confirming your instant order from ${restaurant.name} using secure Shariah credentials.",
                            color = OnSurfaceVariantText,
                            fontSize = 13.sp
                        )

                        Card(
                            colors = CardDefaults.cardColors(containerColor = SurfaceContainer),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(item.name, color = Color.White, fontWeight = FontWeight.Bold)
                                    Text("PHP ${String.format("%.2f", item.price)}", color = Color.White, fontWeight = FontWeight.Bold)
                                }
                                Text(item.desc, color = OnSurfaceVariantText, fontSize = 11.sp)
                                
                                HorizontalDivider(color = Color.White.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 4.dp))
                                
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("Sadaqah Micro-donation Roundup", color = GoldSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        Text("Calculated round-up to nearest PHP 10.00", color = OnSurfaceVariantText.copy(alpha = 0.6f), fontSize = 10.sp)
                                    }
                                    Text(
                                        text = if (isRoundUpActive) "+ PHP ${String.format("%.2f", roundUpDiff)}" else "Disabled",
                                        color = if (isRoundUpActive) PrimaryEmerald else Color.Gray,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Total Charge:", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text(
                                text = "PHP ${String.format("%.2f", if (isRoundUpActive) roundedValue else item.price)}",
                                color = PrimaryEmerald,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryEmerald),
                        onClick = {
                            viewModel.placeFoodOrder(
                                restaurantName = restaurant.name,
                                itemName = item.name,
                                price = item.price,
                                hasSadaqahRoundUp = isRoundUpActive
                            )
                            selectedFoodItem = null
                            scope.launch {
                                snackbarHostState.showSnackbar("Order placed! ${item.name} is being prepared by certified kitchen.")
                            }
                        }
                    ) {
                        Text("Confirm Order", fontWeight = FontWeight.Bold, color = OnPrimary)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { selectedFoodItem = null }) {
                        Text("Cancel", color = OnSurfaceVariantText)
                    }
                }
            )
        }

        // --- GROCERY CHECKOUT DIALOG ---
        if (showGroceryCheckout) {
            val basketTotal = groceryBasket.entries.sumOf { it.key.price * it.value }
            val isRoundUpActive = userProfile?.isSadaqahRoundUp ?: true
            val roundedValue = if (isRoundUpActive) Math.ceil(basketTotal / 10.0) * 10.0 else basketTotal
            val roundUpDiff = roundedValue - basketTotal

            AlertDialog(
                onDismissRequest = { showGroceryCheckout = false },
                containerColor = DarkSurface,
                title = {
                    Text(
                        text = "Vetted Halal Grocery Checkout",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "Please review your certified food selection before paying with clean Shariah reserves.",
                            color = OnSurfaceVariantText,
                            fontSize = 13.sp
                        )

                        Card(
                            colors = CardDefaults.cardColors(containerColor = SurfaceContainer),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(14.dp)
                                    .heightIn(max = 160.dp)
                                    .verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                groceryBasket.forEach { (item, qty) ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("${item.name} (x$qty)", color = Color.White, fontSize = 12.sp)
                                        Text("PHP ${String.format("%.2f", item.price * qty)}", color = Color.White, fontSize = 12.sp)
                                    }
                                }
                            }
                        }

                        Card(
                            colors = CardDefaults.cardColors(containerColor = SurfaceContainer.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Micro-Sadaqah Roundup Contribution", color = GoldSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Text("Nearest PHP 10.00 micro-donation stream", color = OnSurfaceVariantText.copy(alpha = 0.6f), fontSize = 9.sp)
                                }
                                Text(
                                    text = if (isRoundUpActive) "PHP ${String.format("%.2f", roundUpDiff)}" else "Disabled",
                                    color = if (isRoundUpActive) PrimaryEmerald else Color.Gray,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Total Charge:", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text(
                                text = "PHP ${String.format("%.2f", if (isRoundUpActive) roundedValue else basketTotal)}",
                                color = PrimaryEmerald,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryEmerald),
                        onClick = {
                            viewModel.placeGroceryOrder(
                                storeName = "Shariah Pure Market",
                                itemsCount = groceryBasket.values.sum(),
                                price = basketTotal,
                                hasSadaqahRoundUp = isRoundUpActive
                            )
                            groceryBasket = emptyMap()
                            showGroceryCheckout = false
                            scope.launch {
                                snackbarHostState.showSnackbar("Alhamdulillah! Grocery order placed successfully.")
                            }
                        }
                    ) {
                        Text("Pay Now", fontWeight = FontWeight.Bold, color = OnPrimary)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showGroceryCheckout = false }) {
                        Text("Cancel", color = OnSurfaceVariantText)
                    }
                }
            )
        }
    }
}

// --- SUBTAB: FOOD DELIVERY ---
@Composable
fun FoodDeliveryTab(
    onItemSelect: (FoodRestaurantDetails, FoodMenuItem) -> Unit
) {
    val itemsList = listOf(
        FoodRestaurantDetails(
            name = "Al-Sultan Grill",
            style = "Middle Eastern • Vetted V0125",
            rating = "4.8",
            time = "15-25 mins",
            menu = listOf(
                FoodMenuItem("Mandhy Chicken Plate", "Fragrant saffron rice paired with slow-roasted certified chicken breast half.", 320.00),
                FoodMenuItem("Shish Kebab Shawarma Wrap", "Vetted beef flank grilled with seasoned garlic sauce and parsley garnishment.", 210.00),
                FoodMenuItem("Hummus Pure Plate with Pita", "Pristine crushed chickpea dip drizzled with olive oil extract and fresh flatbread.", 140.00)
            )
        ),
        FoodRestaurantDetails(
            name = "Bait Al Burger",
            style = "Gourmet Halal • Cert B481",
            rating = "4.6",
            time = "10-20 mins",
            menu = listOf(
                FoodMenuItem("Golden Truffle Burger", "Shariah permissible pure certified beef smash with truffle aioli on brioche.", 280.00),
                FoodMenuItem("Smoked Saffron Crispy Wing Pack", "Crunchy deep fried wings hand-tossed in lemon saffron reduction marinade.", 220.00)
            )
        ),
        FoodRestaurantDetails(
            name = "Green Oasis Cafe",
            style = "Healthy Healthy organic • ID 9924",
            rating = "4.9",
            time = "8-15 mins",
            menu = listOf(
                FoodMenuItem("Medina Dates Energizer Shake", "Organic local dates pureed with hand-milked cream and honey sweetener.", 180.00),
                FoodMenuItem("Fig and Toasted Walnut Salad bowl", "Ripe soft figs topped with wild greens, olive glaze and roasted walnuts.", 190.00)
            )
        )
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(itemsList) { restaurant ->
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.dp, PrimaryContainer.copy(alpha = 0.2f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Title section
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(restaurant.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                            Text(restaurant.style, color = OnSurfaceVariantText, fontSize = 11.sp)
                        }

                        // Rating element
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .background(GoldSecondary.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.Star, contentDescription = "Restaurant Rating Badge", tint = GoldSecondary, modifier = Modifier.size(13.dp))
                            Text(restaurant.rating, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text("Vetted Certified Halal Menu:", color = GoldSecondary, fontWeight = FontWeight.Bold, fontSize = 11.sp, letterSpacing = 0.5.sp)
                    HorizontalDivider(color = Color.White.copy(alpha = 0.06f), modifier = Modifier.padding(vertical = 6.dp))

                    // Menu item rows
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        restaurant.menu.forEach { item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onItemSelect(restaurant, item) }
                                    .background(SurfaceContainer.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                                    Text(item.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text(item.desc, color = OnSurfaceVariantText, fontSize = 10.sp, lineHeight = 13.sp)
                                }
                                Box(
                                    modifier = Modifier
                                        .background(PrimaryEmerald, RoundedCornerShape(8.dp))
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = "PHP ${item.price.toInt()}",
                                        color = OnPrimary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

data class FoodRestaurantDetails(
    val name: String,
    val style: String,
    val rating: String,
    val time: String,
    val menu: List<FoodMenuItem>
)

data class FoodMenuItem(
    val name: String,
    val desc: String,
    val price: Double
)

// --- SUBTAB: GROCERY DELIVERY ---
@Composable
fun GroceryDeliveryTab(
    basket: Map<GroceryItem, Int>,
    onUpdateQty: (GroceryItem, Int) -> Unit,
    onCheckoutClick: () -> Unit
) {
    val commodities = listOf(
        GroceryItem("Medina Premium Organic Dates", "Fleshy and delicious premium desert import. Ideal for instant fasting companion break.", 180.00, "Pack"),
        GroceryItem("Shariah Certified Organic Lamb Cutlet", "Premium halal clean mutton cutlets, 500g storage freshness ensured.", 490.00, "Pack"),
        GroceryItem("Pristine Hand-Pressed Extra Olive Oil", "Fresh cold-milled first extraction premium culinary oil 500ml container.", 320.00, "Bottle"),
        GroceryItem("Medina Fragrant Jasmine Rice (2kg Bag)", "Wholesome natural grains, vetted clean milling standards.", 190.00, "Bag"),
        GroceryItem("Wild Mountain Pure Raw Organic Honey", "Raw honey harvested with ethical bee preservation practices.", 260.00, "Jar")
    )

    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "HALAL CO-OP FRESH MARKET",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = GoldSecondary,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
            }

            items(commodities) { item ->
                val countInBasket = basket[item] ?: 0
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, PrimaryContainer.copy(alpha = 0.15f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                            Text(item.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(item.desc, color = OnSurfaceVariantText, fontSize = 10.sp, lineHeight = 14.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("PHP ${String.format("%.2f", item.price)} / ${item.unit}", color = GoldSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        // Counter Controls
                        if (countInBasket == 0) {
                            Button(
                                onClick = { onUpdateQty(item, 1) },
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryEmerald),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text("Add", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(30.dp)
                                        .background(PrimaryContainer, CircleShape)
                                        .clickable { onUpdateQty(item, -1) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Reduce qty", tint = Color.White, modifier = Modifier.size(14.dp))
                                }

                                Text(
                                    text = countInBasket.toString(),
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                Box(
                                    modifier = Modifier
                                        .size(30.dp)
                                        .background(PrimaryEmerald, CircleShape)
                                        .clickable { onUpdateQty(item, 1) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "Raise qty", tint = OnPrimary, modifier = Modifier.size(14.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        // Drawer footer bottom checkout banner
        if (basket.isNotEmpty()) {
            val totalAmount = basket.entries.sumOf { it.key.price * it.value }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkSurface)
                    .border(BorderStroke(1.1.dp, PrimaryEmerald.copy(alpha = 0.2f)))
                    .padding(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("BASKET TOTAL", color = OnSurfaceVariantText, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text("PHP ${String.format("%,.2f", totalAmount)}", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryEmerald),
                        shape = RoundedCornerShape(12.dp),
                        onClick = onCheckoutClick,
                        modifier = Modifier
                            .height(44.dp)
                            .testTag("grocery_checkout_btn")
                    ) {
                        Text("Proceed to Checkout", fontWeight = FontWeight.Bold, color = OnPrimary, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

data class GroceryItem(
    val name: String,
    val desc: String,
    val price: Double,
    val unit: String
)

// --- SUBTAB: PARCEL DELIVERY ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParcelDeliveryTab(
    sender: String,
    receiver: String,
    phone: String,
    weight: String,
    category: String,
    trackingState: String?,
    onSenderChange: (String) -> Unit,
    onReceiverChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onWeightChange: (String) -> Unit,
    onCategorySelect: (String) -> Unit,
    onBookClick: () -> Unit,
    onClearTracking: () -> Unit
) {
    val calculatedPrice = (weight.toDoubleOrNull() ?: 1.0) * 25.0 + 80.0

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (trackingState == null) {
            item {
                Text(
                    text = "SECURE PARCEL DISPATCH FORM",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = GoldSecondary,
                    letterSpacing = 1.2.sp
                )
            }

            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, PrimaryContainer.copy(alpha = 0.2f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        OutlinedTextField(
                            value = sender,
                            onValueChange = onSenderChange,
                            label = { Text("Sender Name (Vetted)", color = OnSurfaceVariantText) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryEmerald,
                                unfocusedBorderColor = PrimaryContainer.copy(alpha = 0.4f),
                                focusedLabelColor = PrimaryEmerald,
                                unfocusedLabelColor = OnSurfaceVariantText,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            modifier = Modifier.fillMaxWidth().testTag("parcel_sender_input")
                        )

                        OutlinedTextField(
                            value = receiver,
                            onValueChange = onReceiverChange,
                            label = { Text("Receiver Full Name", color = OnSurfaceVariantText) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryEmerald,
                                unfocusedBorderColor = PrimaryContainer.copy(alpha = 0.4f),
                                focusedLabelColor = PrimaryEmerald,
                                unfocusedLabelColor = OnSurfaceVariantText,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            modifier = Modifier.fillMaxWidth().testTag("parcel_receiver_input")
                        )

                        OutlinedTextField(
                            value = phone,
                            onValueChange = onPhoneChange,
                            label = { Text("Receiver Mobile Number", color = OnSurfaceVariantText) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryEmerald,
                                unfocusedBorderColor = PrimaryContainer.copy(alpha = 0.4f),
                                focusedLabelColor = PrimaryEmerald,
                                unfocusedLabelColor = OnSurfaceVariantText,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            modifier = Modifier.fillMaxWidth().testTag("parcel_phone_input")
                        )

                        OutlinedTextField(
                            value = weight,
                            onValueChange = onWeightChange,
                            label = { Text("Estimated Weight (kg)", color = OnSurfaceVariantText) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryEmerald,
                                unfocusedBorderColor = PrimaryContainer.copy(alpha = 0.4f),
                                focusedLabelColor = PrimaryEmerald,
                                unfocusedLabelColor = OnSurfaceVariantText,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            modifier = Modifier.fillMaxWidth().testTag("parcel_weight_input")
                        )
                    }
                }
            }

            item {
                Text(
                    text = "Vetted Cargo Category",
                    color = GoldSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                // Row of parcel Categories (M3 Chips style)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Document", "Food", "Packaged", "Fragile").forEach { item ->
                        val isSel = item == category
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(
                                    if (isSel) PrimaryEmerald else SurfaceContainer,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { onCategorySelect(item) }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = item,
                                color = if (isSel) OnPrimary else Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            item {
                Button(
                    onClick = onBookClick,
                    enabled = receiver.isNotBlank() && phone.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryEmerald),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("parcel_submit_btn")
                ) {
                    Text(
                        text = "Estimate & Book (PHP ${String.format("%.2f", calculatedPrice)})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        } else {
            // Live active tracking monitor
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, GoldSecondary.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .background(PrimaryEmerald.copy(alpha = 0.12f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Share, contentDescription = "Active Track", tint = PrimaryEmerald, modifier = Modifier.size(32.dp))
                        }

                        Text(
                            text = "PARCEL IN TRANSIT V77",
                            color = PrimaryEmerald,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            letterSpacing = 1.5.sp
                        )

                        Text(
                            text = trackingState,
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp
                        )

                        Text(
                            text = "Vetted dispatch rider is on the way to sender pickup spot.",
                            color = OnSurfaceVariantText,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )

                        HorizontalDivider(color = Color.White.copy(alpha = 0.08f))

                        // Stepper progress layout
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            TrackStepItem("Manifest Registered", "Done", true)
                            TrackStepItem("vetted Courier assigned", "En Route", true)
                            TrackStepItem("At pickup site", "Awaiting", false)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedButton(
                            onClick = onClearTracking,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = GoldSecondary),
                            border = BorderStroke(1.dp, GoldSecondary.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Send Another Parcel")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TrackStepItem(title: String, desc: String, isDone: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .background(if (isDone) PrimaryEmerald else Color.DarkGray, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (isDone) {
                    Icon(Icons.Default.Check, contentDescription = "Finished Checkmark", tint = OnPrimary, modifier = Modifier.size(10.dp))
                }
            }
            Text(title, color = if (isDone) Color.White else Color.Gray, fontSize = 13.sp)
        }
        Text(desc, color = if (isDone) PrimaryEmerald else Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}
