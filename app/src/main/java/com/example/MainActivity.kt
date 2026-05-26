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
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.net.Uri
import java.io.File
import android.content.Context
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.graphics.Brush
import com.example.data.UserProfile
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

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

  if (userProfile == null) {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(DarkBackground),
      contentAlignment = Alignment.Center
    ) {
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
      ) {
         Box(
           modifier = Modifier
             .size(80.dp)
             .background(DarkSurface, RoundedCornerShape(20.dp))
             .border(1.5.dp, GoldSecondary.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
             .padding(8.dp)
         ) {
           androidx.compose.foundation.Image(
             painter = painterResource(id = R.drawable.halalgo_app_icon_1779787997419),
             contentDescription = "Halal Go App Icon",
             modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp))
           )
         }
         Text(
           text = "Halal Go",
           fontSize = 24.sp,
           fontWeight = FontWeight.Bold,
           color = PrimaryEmerald
         )
         CircularProgressIndicator(color = PrimaryEmerald, strokeWidth = 3.dp)
      }
    }
  } else if (!userProfile!!.isLoggedIn) {
    RegistrationScreen(
      viewModel = viewModel,
      onRegistrationSuccess = { name, email, balance, hlgoBalance, ramadanMode, halalFilter, sadaqahRoundUp ->
        viewModel.registerProfile(
          name = name,
          email = email,
          balance = balance,
          hlgoBalance = hlgoBalance,
          ramadanMode = ramadanMode,
          halalFilter = halalFilter,
          sadaqahRoundUp = sadaqahRoundUp
        )
      },
      onLoginSuccess = { name, email ->
        viewModel.googleLogin(name, email)
      }
    )
  } else {
    var showProfileDialog by remember { mutableStateOf(false) }

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
            // App Logo
            androidx.compose.foundation.Image(
              painter = painterResource(id = R.drawable.halalgo_app_icon_1779787997419),
              contentDescription = "Halal Go App Icon",
              modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(10.dp))
                .border(2.dp, GoldSecondary.copy(alpha = 0.8f), RoundedCornerShape(10.dp))
            )

            // App Name
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
              Text(
                text = "Halal",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = PrimaryEmerald,
                letterSpacing = 0.5.sp
              )
              Text(
                text = "Go",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = GoldSecondary,
                letterSpacing = 0.5.sp
              )
            }
          }
        },
        actions = {
          // Status switch for driver mode (ONLINE/OFFLINE toggle on topbar)
          if (isDriverMode) {
            Row(
              modifier = Modifier
                .background(
                  color = if (isDriverOnline) PrimaryContainer else ErrorContainerRed,
                  shape = RoundedCornerShape(100.dp)
                )
                .clickable { viewModel.toggleDriverOnline() }
                .padding(horizontal = 10.dp, vertical = 6.dp),
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

          // Custom Vertical divider
          Spacer(modifier = Modifier.width(4.dp))
          Box(
            modifier = Modifier
              .height(24.dp)
              .width(1.dp)
              .background(Color.White.copy(alpha = 0.15f))
          )
          Spacer(modifier = Modifier.width(4.dp))

          // Profile area (click to configure profile picture or sign out)
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
              .clip(RoundedCornerShape(8.dp))
              .clickable { showProfileDialog = true }
              .padding(horizontal = 8.dp, vertical = 4.dp)
              .testTag("avatar_profile_switcher")
          ) {
            Column(horizontalAlignment = Alignment.End) {
              Text(
                text = userProfile?.name ?: "",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1
              )
              Text(
                text = if (isDriverMode) "Driver" else "Passenger",
                fontSize = 9.sp,
                color = OnSurfaceVariantText,
                maxLines = 1
              )
            }

            Box(
              modifier = Modifier
                .size(32.dp)
                .background(PrimaryContainer, CircleShape)
                .clip(CircleShape)
            ) {
              val profilePic = userProfile?.profilePicture
              if (profilePic != null) {
                AsyncImage(
                  model = profilePic,
                  contentDescription = "User profile picture",
                  modifier = Modifier.fillMaxSize(),
                  contentScale = ContentScale.Crop
                )
              } else {
                Icon(
                  imageVector = Icons.Default.Person,
                  contentDescription = "User profile toggle swapper",
                  tint = Color.White,
                  modifier = Modifier.fillMaxSize().padding(4.dp)
                )
              }
            }
          }

          Spacer(modifier = Modifier.width(8.dp))

          // Profile bottom popover dialog
          if (showProfileDialog) {
            UserProfileAndSettingsDialog(
              userProfile = userProfile,
              isDriverMode = isDriverMode,
              onDismiss = { showProfileDialog = false },
              onSaveChanges = { name, email ->
                viewModel.updateProfileNameAndEmail(name, email)
              },
              onProfilePictureSelected = { path ->
                viewModel.updateProfilePicture(path)
              },
              onToggleDriverMode = { mode ->
                viewModel.setDriverMode(mode)
              },
              onLogout = {
                viewModel.logout()
              }
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
          "Delivery" -> PassengerDeliveryScreen(viewModel = viewModel, userProfile = userProfile)
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
          label = { Text("Wealth in PHP") },
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
              Text("PHP ${String.format("%.2f", computedZakat)}", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = PrimaryEmerald)
            }
          }
        } else if (zakatInput.isNotEmpty()) {
          Text(
            text = "Total entered is below Nisab (PHP 20,000 threshold). No Zakat due. Consider voluntary Sadaqah charity instead!",
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
          text = if (computedZakat > 0.0) "Pay Zakat Now" else "Give PHP 10 Sadaqah",
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

fun copyUriToInternalStorage(context: Context, uri: Uri): String? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return null
        val file = File(context.filesDir, "profile_pic_${System.currentTimeMillis()}.png")
        file.outputStream().use { outputStream ->
            inputStream.use { it.copyTo(outputStream) }
        }
        file.absolutePath
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoogleSignInScreen(
    viewModel: MainViewModel,
    onLoginSuccess: (name: String, email: String) -> Unit
) {
    var nameInput by remember { mutableStateOf("") }
    var emailInput by remember { mutableStateOf("") }
    var showAccountChooser by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        // Decorative glowing circles / gradients
        Box(
            modifier = Modifier
                .size(400.dp)
                .align(Alignment.TopCenter)
                .offset(y = (-100).dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(PrimaryEmerald.copy(alpha = 0.15f), Color.Transparent)
                    )
                )
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())
        ) {
            // App Icon
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .background(DarkSurface, RoundedCornerShape(24.dp))
                    .border(1.5.dp, GoldSecondary.copy(alpha = 0.4f), RoundedCornerShape(24.dp))
                    .padding(8.dp)
            ) {
                androidx.compose.foundation.Image(
                    painter = painterResource(id = R.drawable.halalgo_app_icon_1779787997419),
                    contentDescription = "Halal Go App Icon",
                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(16.dp))
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Halal Go",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryEmerald,
                letterSpacing = 1.sp
            )

            Text(
                text = "Your Premium Halal Companion Super App",
                fontSize = 14.sp,
                color = OnSurfaceVariantText,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Sub-card with inputs for customized testing
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = BorderStroke(1.dp, PrimaryContainer.copy(alpha = 0.2f)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Demo Credentials Config",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = GoldSecondary,
                        letterSpacing = 1.sp
                    )

                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        label = { Text("Your Name", color = OnSurfaceVariantText) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryEmerald,
                            unfocusedBorderColor = PrimaryContainer.copy(alpha = 0.4f),
                            focusedLabelColor = PrimaryEmerald,
                            unfocusedLabelColor = OnSurfaceVariantText,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("auth_name_field")
                    )

                    OutlinedTextField(
                        value = emailInput,
                        onValueChange = { emailInput = it },
                        label = { Text("Your Email", color = OnSurfaceVariantText) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryEmerald,
                            unfocusedBorderColor = PrimaryContainer.copy(alpha = 0.4f),
                            focusedLabelColor = PrimaryEmerald,
                            unfocusedLabelColor = OnSurfaceVariantText,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("auth_email_field")
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Google Login Button styled with official brand look
            Button(
                onClick = { showAccountChooser = true },
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("google_login_button"),
                border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Modern styled mockup Google icon
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "Google Logo symbol",
                            tint = Color.Gray,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Continue with Google",
                        color = Color.Black,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Text(
                text = "By signing in, you agree to our Shariah-compliant Terms.",
                fontSize = 11.sp,
                color = OnSurfaceVariantText.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )
        }

        // Beautiful simulated Google OAuth Credential Bottom Sheet Sheet
        if (showAccountChooser) {
            ModalBottomSheet(
                onDismissRequest = { showAccountChooser = false },
                containerColor = Color.White,
                dragHandle = { BottomSheetDefaults.DragHandle(color = Color.LightGray) }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 12.dp)
                        .navigationBarsPadding(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Sign in with Google",
                            color = Color.Black,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = "to continue to Halal Go application",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Account options
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // User specified custom configured credential option
                        GoogleAccountRow(
                            name = nameInput,
                            email = emailInput,
                            isCustom = true,
                            onClick = {
                                showAccountChooser = false
                                isLoading = true
                                scope.launch {
                                    delay(1500)
                                    isLoading = false
                                    onLoginSuccess(nameInput, emailInput)
                                }
                            }
                        )

                        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.4f))

                        // Secondary mock account options
                        GoogleAccountRow(
                            name = "Ahmed Al-Farsi",
                            email = "ahmed.farsi@gmail.com",
                            isCustom = false,
                            onClick = {
                                showAccountChooser = false
                                isLoading = true
                                scope.launch {
                                    delay(1500)
                                    isLoading = false
                                    onLoginSuccess("Ahmed Al-Farsi", "ahmed.farsi@gmail.com")
                                }
                            }
                        )

                        GoogleAccountRow(
                            name = "Ayesha Mendoza",
                            email = "ayesha.m@gmail.com",
                            isCustom = false,
                            onClick = {
                                showAccountChooser = false
                                isLoading = true
                                scope.launch {
                                    delay(1500)
                                    isLoading = false
                                    onLoginSuccess("Ayesha Mendoza", "ayesha.m@gmail.com")
                                }
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }

        // Fullscreen glassmorphic loading screen during login transition
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.75f)),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    border = BorderStroke(1.dp, PrimaryEmerald.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator(color = PrimaryEmerald, strokeWidth = 3.dp)
                        Text(
                            text = "Securing Google Session...",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GoogleAccountRow(
    name: String,
    email: String,
    isCustom: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(if (isCustom) PrimaryEmerald.copy(alpha = 0.1f) else Color.LightGray.copy(alpha = 0.5f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = name.take(1).uppercase(),
                color = if (isCustom) PrimaryEmerald else Color.Black,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = name,
                color = Color.Black,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = email,
                color = Color.Gray,
                fontSize = 12.sp
            )
        }

        Icon(
            imageVector = Icons.Default.KeyboardArrowRight,
            contentDescription = "Select account",
            tint = Color.Gray
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileAndSettingsDialog(
    userProfile: UserProfile?,
    isDriverMode: Boolean,
    onDismiss: () -> Unit,
    onSaveChanges: (name: String, email: String) -> Unit,
    onProfilePictureSelected: (imagePath: String?) -> Unit,
    onToggleDriverMode: (Boolean) -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    var nameState by remember { mutableStateOf(userProfile?.name ?: "") }
    var emailState by remember { mutableStateOf(userProfile?.email ?: "") }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val path = copyUriToInternalStorage(context, it)
            onProfilePictureSelected(path)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        dragHandle = { BottomSheetDefaults.DragHandle(color = OnSurfaceVariantText.copy(alpha = 0.4f)) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp)
                .windowInsetsPadding(WindowInsets.navigationBars)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Shariah Profile Settings",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = GoldSecondary,
                letterSpacing = 0.5.sp
            )

            // Dynamic uploaded photo display frame with trigger action
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .background(PrimaryContainer, CircleShape)
                    .border(2.dp, GoldSecondary, CircleShape)
                    .clip(CircleShape)
                    .clickable { imagePickerLauncher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (userProfile?.profilePicture != null) {
                    AsyncImage(
                        model = userProfile.profilePicture,
                        contentDescription = "User uploaded profile graphic photo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Default profile image slot placeholder",
                        tint = Color.White,
                        modifier = Modifier.fillMaxSize().padding(16.dp)
                    )
                }

                // Camera overlay badge triggered to pick pictures
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(PrimaryEmerald, CircleShape)
                        .border(1.5.dp, DarkSurface, CircleShape)
                        .align(Alignment.BottomEnd)
                        .padding(6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Create,
                        contentDescription = "Edit photo symbol",
                        tint = Color.White,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Text(
                text = "Tap circle to change profile picture",
                fontSize = 12.sp,
                color = OnSurfaceVariantText,
                textAlign = TextAlign.Center
            )

            // User basic credential attributes fields
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = nameState,
                    onValueChange = { nameState = it },
                    label = { Text("Display Name", color = OnSurfaceVariantText) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryEmerald,
                        unfocusedBorderColor = PrimaryContainer.copy(alpha = 0.4f),
                        focusedLabelColor = PrimaryEmerald,
                        unfocusedLabelColor = OnSurfaceVariantText,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = emailState,
                    onValueChange = { emailState = it },
                    label = { Text("Email Address", color = OnSurfaceVariantText) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryEmerald,
                        unfocusedBorderColor = PrimaryContainer.copy(alpha = 0.4f),
                        focusedLabelColor = PrimaryEmerald,
                        unfocusedLabelColor = OnSurfaceVariantText,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Role Switch bento box button
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkBackground),
                border = BorderStroke(1.dp, PrimaryContainer.copy(alpha = 0.2f)),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth().clickable {
                    onToggleDriverMode(!isDriverMode)
                    onDismiss()
                }
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(GoldSecondary.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isDriverMode) Icons.Default.Home else Icons.Default.Build,
                            contentDescription = "Role option marker icon",
                            tint = GoldSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(11f)) {
                        Text(
                            text = if (isDriverMode) "Switch to Passenger Mode" else "Switch to Driver Dashboard",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = if (isDriverMode) "Request rides, food delivery in PHP" else "Accept rides, gain dynamic extra earnings",
                            fontSize = 11.sp,
                            color = OnSurfaceVariantText
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Action Buttons (Save vs Sign Out)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Secondary Sign out
                Button(
                    onClick = {
                        onLogout()
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorContainerRed),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f).height(46.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share, // represent sign-out/exit symbol
                        contentDescription = "Logout icon",
                        tint = ErrorRed,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Sign Out", fontWeight = FontWeight.Bold, color = ErrorRed, fontSize = 13.sp)
                }

                // Primary Save Changes
                Button(
                    onClick = {
                        onSaveChanges(nameState, emailState)
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryEmerald),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1.2f).height(46.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Save checkmark",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Save Changes", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}
