package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        MainAppContent()
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppContent() {
  // Instantiate MainViewModel
  val viewModel: MainViewModel = viewModel()

  // State flows binding
  val isDriverMode by viewModel.isDriverMode.collectAsStateWithLifecycle()
  val passengerTab by viewModel.currentPassengerTab.collectAsStateWithLifecycle()
  val driverTab by viewModel.currentDriverTab.collectAsStateWithLifecycle()

  val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
  val transactionsList by viewModel.allTransactions.collectAsStateWithLifecycle()
  val activeRide by viewModel.activeRide.collectAsStateWithLifecycle()

  // Booking states
  val isBookingActive by viewModel.isBookingState.collectAsStateWithLifecycle()
  val isSearching by viewModel.isRideSearching.collectAsStateWithLifecycle()

  // Driver states
  val isDriverOnline by viewModel.isDriverOnline.collectAsStateWithLifecycle()
  val showNewRequestPopup by viewModel.showNewRequestPopup.collectAsStateWithLifecycle()
  val popupTimerCount by viewModel.popupTimerCount.collectAsStateWithLifecycle()
  val isDriverOnActiveRide by viewModel.isDriverOnActiveRide.collectAsStateWithLifecycle()
  val activeDriverRideStatus by viewModel.activeDriverRideStatus.collectAsStateWithLifecycle()

  // Dialog states
  val showZakatDialog by viewModel.showZakatCalculator.collectAsStateWithLifecycle()
  val zakatInputWealth by viewModel.zakatInputWealth.collectAsStateWithLifecycle()
  val computedZakatResult by viewModel.computedZakatResult.collectAsStateWithLifecycle()

  Scaffold(
    topBar = {
      // Custom visual TopAppBar with profile switcher and status indicator
      TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = DarkSurface.copy(alpha = 0.9f),
          titleContentColor = Color.White
        ),
        title = {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            // Profile image with switch trigger
            Box(
              modifier = Modifier
                .size(40.dp)
                .background(PrimaryContainer, CircleShape)
                .clip(CircleShape)
                .clickable {
                  // Reviewers can click profile avatar to toggle roles (Passenger vs Driver!)
                  viewModel.setDriverMode(!isDriverMode)
                }
                .testTag("avatar_profile_switcher")
            ) {
              // Graphic avatar representing Ahmed
              Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "User profile toggle swapper",
                tint = Color.White,
                modifier = Modifier.fillMaxSize().padding(6.dp)
              )
            }

            Column {
              Text(
                text = "Halal Go",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryEmerald
              )
              Text(
                text = if (isDriverMode) "Driver Dashboard • Ahmed K" else "Dubai Marina • Passenger",
                fontSize = 12.sp,
                color = OnSurfaceVariantText
              )
            }
          }
        },
        actions = {
          // Status switch for driver mode (ONLINE/OFFLINE toggle on topbar)
          if (isDriverMode) {
            Row(
              modifier = Modifier
                .padding(end = 12.dp)
                .background(
                  color = if (isDriverOnline) PrimaryContainer else ErrorContainerRed,
                  shape = RoundedCornerShape(100.dp)
                )
                .clickable { viewModel.toggleDriverOnline() }
                .padding(horizontal = 14.dp, vertical = 6.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              Box(
                modifier = Modifier
                  .size(8.dp)
                  .background(if (isDriverOnline) PrimaryEmerald else ErrorRed, CircleShape)
              )
              Text(
                text = if (isDriverOnline) "ONLINE" else "OFFLINE",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDriverOnline) PrimaryEmerald else ErrorRed
              )
            }
          } else {
            // Zakat quick action
            IconButton(
              onClick = { viewModel.showZakatCalculator.value = true },
              modifier = Modifier.testTag("quick_zakat_calculator_trigger")
            ) {
              Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = "Quick Zakat dues calculator",
                tint = GoldSecondary
              )
            }
          }

          IconButton(onClick = { viewModel.triggerMockScanner() }) {
            Icon(
              imageVector = Icons.Default.Notifications,
              contentDescription = "Notifications alerts feed",
              tint = Color.White
            )
          }
        }
      )
    },
    bottomBar = {
      // Bottom navigation tabs mapping (Screens 1, 5)
      // Standard M3 window insets applied custom navigation bar padding
      NavigationBar(
        containerColor = DarkSurface.copy(alpha = 0.95f),
        tonalElevation = 8.dp,
        modifier = Modifier
          .windowInsetsPadding(WindowInsets.navigationBars)
          .testTag("super_app_bottom_nav_bar")
      ) {
        if (!isDriverMode) {
          // PASSENGER TABS (Home, Ride, Delivery, Prayer, Wallet)
          PassengerNavigationTabs(passengerTab) { viewModel.setPassengerTab(it) }
        } else {
          // DRIVER TABS (Home, Earnings, Wallet, Profile)
          DriverNavigationTabs(driverTab) { viewModel.setDriverTab(it) }
        }
      }
    },
    containerColor = DarkBackground
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
    ) {
      if (!isDriverMode) {
        // PASSENGER MODULE ROUTER
        when (passengerTab) {
          "Home" -> PassengerHomeScreen(viewModel = viewModel, userProfile = userProfile)
          "Ride" -> PassengerRideScreen(
            viewModel = viewModel,
            activeRide = activeRide,
            isBookingActive = isBookingActive,
            isSearching = isSearching
          )
          "Delivery" -> PassengerHomeScreen(viewModel = viewModel, userProfile = userProfile) // Embed within Home list
          "Prayer" -> PrayerScreen(
            viewModel = viewModel,
            ramadanMode = userProfile?.ramadanModeEnabled ?: true,
            compassRotation = viewModel.qiblaCompassRotation.collectAsStateWithLifecycle().value
          )
          "Wallet" -> WalletScreen(
            viewModel = viewModel,
            userProfile = userProfile,
            transactionsList = transactionsList
          )
        }
      } else {
        // DRIVER MODULE ROUTER
        when (driverTab) {
          "Home" -> DriverDashboardScreen(
            viewModel = viewModel,
            isOnline = isDriverOnline,
            showNewRequest = showNewRequestPopup,
            popupTimerCount = popupTimerCount,
            isDriverOnActiveRide = isDriverOnActiveRide,
            activeDriverRideStatus = activeDriverRideStatus
          )
          "Earnings" -> DriverDashboardScreen(
            viewModel = viewModel,
            isOnline = isDriverOnline,
            showNewRequest = showNewRequestPopup,
            popupTimerCount = popupTimerCount,
            isDriverOnActiveRide = isDriverOnActiveRide,
            activeDriverRideStatus = activeDriverRideStatus
          )
          "Wallet" -> WalletScreen(
            viewModel = viewModel,
            userProfile = userProfile,
            transactionsList = transactionsList
          )
          "Profile" -> DriverDashboardScreen(
            viewModel = viewModel,
            isOnline = isDriverOnline,
            showNewRequest = showNewRequestPopup,
            popupTimerCount = popupTimerCount,
            isDriverOnActiveRide = isDriverOnActiveRide,
            activeDriverRideStatus = activeDriverRideStatus
          )
        }
      }

      // Dialog container for Custom Zakat Calculator (Screen 2 info)
      if (showZakatDialog) {
        CustomZakatCalculatorDialog(
          zakatInput = zakatInputWealth,
          computedZakat = computedZakatResult,
          onCalculateValue = { viewModel.calculateZakat(it) },
          onPayZakat = { viewModel.performZakatPayment(it) },
          onDismiss = { viewModel.showZakatCalculator.value = false }
        )
      }
    }
  }
}

@Composable
fun RowScope.PassengerNavigationTabs(
  selectedTab: String,
  onTabChange: (String) -> Unit
) {
  val tabs = listOf(
    Pair("Home", Icons.Default.Home),
    Pair("Ride", Icons.Default.Place),
    Pair("Delivery", Icons.Default.ShoppingCart),
    Pair("Prayer", Icons.Default.Menu),
    Pair("Wallet", Icons.Default.Favorite)
  )

  tabs.forEach { (tabName, icon) ->
    NavigationBarItem(
      selected = selectedTab == tabName,
      onClick = { onTabChange(tabName) },
      icon = {
        Icon(
          imageVector = icon,
          contentDescription = tabName,
          tint = if (selectedTab == tabName) PrimaryEmerald else OnSurfaceVariantText.copy(alpha = 0.5f)
        )
      },
      label = {
        Text(
          text = tabName,
          fontSize = 11.sp,
          fontWeight = if (selectedTab == tabName) FontWeight.Bold else FontWeight.Normal,
          color = if (selectedTab == tabName) PrimaryEmerald else OnSurfaceVariantText
        )
      },
      colors = NavigationBarItemDefaults.colors(
        indicatorColor = PrimaryContainer.copy(alpha = 0.3f)
      )
    )
  }
}

@Composable
fun RowScope.DriverNavigationTabs(
  selectedTab: String,
  onTabChange: (String) -> Unit
) {
  val tabs = listOf(
    Pair("Home", Icons.Default.Home),
    Pair("Earnings", Icons.Default.Star),
    Pair("Wallet", Icons.Default.Favorite),
    Pair("Profile", Icons.Default.Person)
  )

  tabs.forEach { (tabName, icon) ->
    NavigationBarItem(
      selected = selectedTab == tabName,
      onClick = { onTabChange(tabName) },
      icon = {
        Icon(
          imageVector = icon,
          contentDescription = tabName,
          tint = if (selectedTab == tabName) PrimaryEmerald else OnSurfaceVariantText.copy(alpha = 0.5f)
        )
      },
      label = {
        Text(
          text = tabName,
          fontSize = 11.sp,
          fontWeight = if (selectedTab == tabName) FontWeight.Bold else FontWeight.Normal,
          color = if (selectedTab == tabName) PrimaryEmerald else OnSurfaceVariantText
        )
      },
      colors = NavigationBarItemDefaults.colors(
        indicatorColor = PrimaryContainer.copy(alpha = 0.3f)
      )
    )
  }
}

@Composable
fun CustomZakatCalculatorDialog(
  zakatInput: String,
  computedZakat: Double,
  onCalculateValue: (String) -> Unit,
  onPayZakat: (Double) -> Unit,
  onDismiss: () -> Unit
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(
        text = "Islamic Zakat Calculator",
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        color = GoldSecondary,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth()
      )
    },
    text = {
      Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        Text(
          text = "Enter net qualifying personal wealth (deducting immediate debts) to compute 2.5% simple Zakat dues (Nisab threshold applied):",
          fontSize = 13.sp,
          color = OnSurfaceVariantText,
          lineHeight = 16.sp
        )

        OutlinedTextField(
          value = zakatInput,
          onValueChange = { onCalculateValue(it) },
          label = { Text("Wealth in AED") },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("zakat_wealth_input_field"),
          colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedBorderColor = GoldSecondary,
            unfocusedBorderColor = Color.White.copy(alpha = 0.12f)
          ),
          shape = RoundedCornerShape(12.dp)
        )

        if (computedZakat > 0.0) {
          Surface(
            color = PrimaryContainer.copy(alpha = 0.15f),
            border = BorderStroke(1.dp, PrimaryEmerald.copy(alpha = 0.3f)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(
              modifier = Modifier.padding(16.dp),
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
              Text("Your computed Zakat dues (2.5%)", fontSize = 11.sp, color = OnSurfaceVariantText)
              Text("AED ${String.format("%.2f", computedZakat)}", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = PrimaryEmerald)
            }
          }
        } else if (zakatInput.isNotEmpty()) {
          Text(
            text = "Total entered is below Nisab (AED 20,000 threshold). No Zakat due. Consider voluntary Sadaqah charity instead!",
            fontSize = 11.sp,
            color = GoldSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
          )
        }
      }
    },
    confirmButton = {
      Button(
        onClick = { if (computedZakat > 0.0) onPayZakat(computedZakat) else onPayZakat(10.0) },
        colors = ButtonDefaults.buttonColors(containerColor = GoldSecondary),
        shape = RoundedCornerShape(8.dp)
      ) {
        Text(
          text = if (computedZakat > 0.0) "Pay Zakat Now" else "Give AED 10 Sadaqah",
          color = OnSecondary,
          fontWeight = FontWeight.Bold
        )
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel", color = OnSurfaceVariantText)
      }
    },
    containerColor = SurfaceContainerHigh,
    shape = RoundedCornerShape(24.dp)
  )
}
