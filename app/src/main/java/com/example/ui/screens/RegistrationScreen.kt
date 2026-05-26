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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrationScreen(
    viewModel: MainViewModel,
    onRegistrationSuccess: (
        name: String, 
        email: String, 
        balance: Double, 
        hlgoBalance: Double,
        ramadanMode: Boolean,
        halalFilter: Boolean,
        sadaqahRoundUp: Boolean
    ) -> Unit,
    onLoginSuccess: (
        name: String,
        email: String
    ) -> Unit
) {
    // 0 = Register Tab, 1 = Login Tab
    var activeTab by remember { mutableStateOf(0) }
    val scope = rememberCoroutineScope()

    // --- REGISTRATION STATES ---
    var nameState by remember { mutableStateOf("") }
    var emailState by remember { mutableStateOf("") }
    var balanceState by remember { mutableStateOf("12450.85") }
    var hlgoBalanceState by remember { mutableStateOf("842.15") }
    
    var ramadanModeState by remember { mutableStateOf(true) }
    var halalFilterState by remember { mutableStateOf(true) }
    var sadaqahRoundUpState by remember { mutableStateOf(true) }
    
    var termsAgreedState by remember { mutableStateOf(true) }
    
    var nameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var balanceError by remember { mutableStateOf<String?>(null) }
    var hlgoError by remember { mutableStateOf<String?>(null) }

    // --- LOGIN STATES ---
    var loginNameInput by remember { mutableStateOf("Ahmed K.") }
    var loginEmailInput by remember { mutableStateOf("ahmed.k@gmail.com") }
    var showAccountChooser by remember { mutableStateOf(false) }
    var isAuthLoading by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        // Glowing brand background aura
        Box(
            modifier = Modifier
                .size(450.dp)
                .align(Alignment.TopCenter)
                .offset(y = (-120).dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(PrimaryEmerald.copy(alpha = 0.18f), Color.Transparent)
                    )
                )
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(28.dp))

            // App Brand Frame
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(DarkSurface, RoundedCornerShape(26.dp))
                    .border(2.5.dp, GoldSecondary, RoundedCornerShape(26.dp))
                    .padding(12.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.halalgo_app_icon_1779787997419),
                    contentDescription = "Halal Go Premium Logo Symbol",
                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(16.dp))
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Halal Go",
                fontSize = 38.sp,
                fontWeight = FontWeight.ExtraBold,
                color = PrimaryEmerald,
                letterSpacing = 1.sp
            )

            Text(
                text = "Premium Shariah-compliant Transport & Food",
                fontSize = 13.sp,
                color = OnSurfaceVariantText,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Premium Custom Tabs Control (Register vs Login)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .background(PrimaryContainer.copy(alpha = 0.2f), RoundedCornerShape(24.dp))
                    .padding(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Register Tab Button
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(
                            if (activeTab == 0) PrimaryEmerald else Color.Transparent,
                            RoundedCornerShape(20.dp)
                        )
                        .clickable { activeTab = 0 }
                        .testTag("auth_tab_register_btn"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Register",
                        color = if (activeTab == 0) OnPrimary else OnSurfaceVariantText,
                        fontWeight = if (activeTab == 0) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 14.sp
                    )
                }

                // Login Tab Button
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(
                            if (activeTab == 1) PrimaryEmerald else Color.Transparent,
                            RoundedCornerShape(20.dp)
                        )
                        .clickable { activeTab = 1 }
                        .testTag("auth_tab_login_btn"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Sign In",
                        color = if (activeTab == 1) OnPrimary else OnSurfaceVariantText,
                        fontWeight = if (activeTab == 1) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            if (activeTab == 0) {
                // --- TAB 1: REGISTRATION FLOW ---
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Personal Details Card
                    Card(
                        colors = CardDefaults.cardColors(containerColor = DarkSurface),
                        border = BorderStroke(1.dp, PrimaryContainer.copy(alpha = 0.3f)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Text(
                                text = "1. PERSONAL DETAILS",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = GoldSecondary,
                                letterSpacing = 1.sp
                            )

                            OutlinedTextField(
                                value = nameState,
                                onValueChange = { 
                                    nameState = it
                                    if (it.isNotBlank()) nameError = null
                                },
                                label = { Text("Full Name", color = OnSurfaceVariantText) },
                                isError = nameError != null,
                                supportingText = nameError?.let { { Text(it, color = ErrorRed) } },
                                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = PrimaryEmerald) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = PrimaryEmerald,
                                    unfocusedBorderColor = PrimaryContainer.copy(alpha = 0.4f),
                                    focusedLabelColor = PrimaryEmerald,
                                    unfocusedLabelColor = OnSurfaceVariantText,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                modifier = Modifier.fillMaxWidth().testTag("reg_name_field")
                            )

                            OutlinedTextField(
                                value = emailState,
                                onValueChange = { 
                                    emailState = it
                                    if (it.isNotBlank() && it.contains("@")) emailError = null
                                },
                                label = { Text("Email Address", color = OnSurfaceVariantText) },
                                isError = emailError != null,
                                supportingText = emailError?.let { { Text(it, color = ErrorRed) } },
                                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = PrimaryEmerald) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = PrimaryEmerald,
                                    unfocusedBorderColor = PrimaryContainer.copy(alpha = 0.4f),
                                    focusedLabelColor = PrimaryEmerald,
                                    unfocusedLabelColor = OnSurfaceVariantText,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                modifier = Modifier.fillMaxWidth().testTag("reg_email_field")
                            )
                        }
                    }

                    // Wallet Config Card
                    Card(
                        colors = CardDefaults.cardColors(containerColor = DarkSurface),
                        border = BorderStroke(1.dp, PrimaryContainer.copy(alpha = 0.3f)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Text(
                                text = "2. WALLET PARAMETERS",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = GoldSecondary,
                                letterSpacing = 1.sp
                            )

                            OutlinedTextField(
                                value = balanceState,
                                onValueChange = { 
                                    balanceState = it
                                    if (it.toDoubleOrNull() != null) balanceError = null
                                },
                                label = { Text("Starting Balance (PHP)", color = OnSurfaceVariantText) },
                                isError = balanceError != null,
                                supportingText = balanceError?.let { { Text(it, color = ErrorRed) } },
                                leadingIcon = { Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = GoldSecondary) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = PrimaryEmerald,
                                    unfocusedBorderColor = PrimaryContainer.copy(alpha = 0.4f),
                                    focusedLabelColor = PrimaryEmerald,
                                    unfocusedLabelColor = OnSurfaceVariantText,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                modifier = Modifier.fillMaxWidth().testTag("reg_balance_field")
                            )

                            OutlinedTextField(
                                value = hlgoBalanceState,
                                onValueChange = { 
                                    hlgoBalanceState = it
                                    if (it.toDoubleOrNull() != null) hlgoError = null
                                },
                                label = { Text("Starting HLGO Balance", color = OnSurfaceVariantText) },
                                isError = hlgoError != null,
                                supportingText = hlgoError?.let { { Text(it, color = ErrorRed) } },
                                leadingIcon = { Icon(Icons.Default.Star, contentDescription = null, tint = GoldSecondary) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = PrimaryEmerald,
                                    unfocusedBorderColor = PrimaryContainer.copy(alpha = 0.4f),
                                    focusedLabelColor = PrimaryEmerald,
                                    unfocusedLabelColor = OnSurfaceVariantText,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                modifier = Modifier.fillMaxWidth().testTag("reg_hlgo_field")
                            )
                        }
                    }

                    // Shariah Toggles Card
                    Card(
                        colors = CardDefaults.cardColors(containerColor = DarkSurface),
                        border = BorderStroke(1.dp, PrimaryContainer.copy(alpha = 0.3f)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Text(
                                text = "3. SHARIAH PREFERENCES",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = GoldSecondary,
                                letterSpacing = 1.sp
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Ramadan Companion Mode", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                                    Text("Enables smart prayer calculations and fasting schedules", color = OnSurfaceVariantText, fontSize = 11.sp)
                                }
                                Switch(
                                    checked = ramadanModeState,
                                    onCheckedChange = { ramadanModeState = it },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = PrimaryEmerald,
                                        checkedTrackColor = PrimaryContainer
                                    ),
                                    modifier = Modifier.testTag("reg_switch_ramadan")
                                )
                            }

                            HorizontalDivider(color = PrimaryContainer.copy(alpha = 0.15f))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Strict Halal Merchant Filter", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                                    Text("Forces strict certificate validation checks", color = OnSurfaceVariantText, fontSize = 11.sp)
                                }
                                Switch(
                                    checked = halalFilterState,
                                    onCheckedChange = { halalFilterState = it },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = PrimaryEmerald,
                                        checkedTrackColor = PrimaryContainer
                                    ),
                                    modifier = Modifier.testTag("reg_switch_halal")
                                )
                            }

                            HorizontalDivider(color = PrimaryContainer.copy(alpha = 0.15f))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Sadaqah micro-donation Round-Up", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                                    Text("Automatically rounds rides to nearest PHP to donate", color = OnSurfaceVariantText, fontSize = 11.sp)
                                }
                                Switch(
                                    checked = sadaqahRoundUpState,
                                    onCheckedChange = { sadaqahRoundUpState = it },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = PrimaryEmerald,
                                        checkedTrackColor = PrimaryContainer
                                    ),
                                    modifier = Modifier.testTag("reg_switch_sadaqah")
                                )
                            }
                        }
                    }

                    // Shariah Terms Checkbox
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { termsAgreedState = !termsAgreedState }
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = termsAgreedState,
                            onCheckedChange = { termsAgreedState = it },
                            colors = CheckboxDefaults.colors(
                                checkedColor = PrimaryEmerald,
                                uncheckedColor = Color.Gray
                            ),
                            modifier = Modifier.testTag("reg_terms_checkbox")
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "I state this is my true individual profile & I agree to Halal Go Shariah-Compliant Terms of Service.",
                            color = OnSurfaceText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // Register Button Submit
                    Button(
                        onClick = {
                            var hasError = false
                            if (nameState.trim().isEmpty()) {
                                nameError = "Full name is required."
                                hasError = true
                            }
                            if (emailState.trim().isEmpty() || !emailState.contains("@")) {
                                emailError = "Please enter a valid email address."
                                hasError = true
                            }
                            val balanceVal = balanceState.toDoubleOrNull()
                            if (balanceVal == null || balanceVal < 0) {
                                balanceError = "Provide a valid wallet starting balance."
                                hasError = true
                            }
                            val hlgoVal = hlgoBalanceState.toDoubleOrNull()
                            if (hlgoVal == null || hlgoVal < 0) {
                                hlgoError = "Provide a valid HLGO premium balance."
                                hasError = true
                            }

                            if (!hasError && termsAgreedState) {
                                onRegistrationSuccess(
                                    nameState.trim(),
                                    emailState.trim(),
                                    balanceVal!!,
                                    hlgoVal!!,
                                    ramadanModeState,
                                    halalFilterState,
                                    sadaqahRoundUpState
                                )
                            }
                        },
                        enabled = termsAgreedState && nameState.isNotBlank() && emailState.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryEmerald,
                            disabledContainerColor = Color.Gray.copy(alpha = 0.3f),
                            contentColor = OnPrimary,
                            disabledContentColor = Color.LightGray.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .testTag("register_profile_button")
                    ) {
                        Text(
                            text = "Register & Create Profile",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            } else {
                // --- TAB 2: SIGN IN FLOW ---
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
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
                                text = "DEMO CREDENTIALS CONFIG",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = GoldSecondary,
                                letterSpacing = 1.sp
                            )

                            OutlinedTextField(
                                value = loginNameInput,
                                onValueChange = { loginNameInput = it },
                                label = { Text("Profile Name", color = OnSurfaceVariantText) },
                                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = PrimaryEmerald) },
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
                                value = loginEmailInput,
                                onValueChange = { loginEmailInput = it },
                                label = { Text("Email Address", color = OnSurfaceVariantText) },
                                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = PrimaryEmerald) },
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

                    Spacer(modifier = Modifier.height(6.dp))

                    // Premium official-looking Google Login Button
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
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .background(Color.White, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccountCircle,
                                    contentDescription = "Google Logo icon",
                                    tint = PrimaryEmerald,
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

                    // Direct Standard Login Button as an alternative
                    OutlinedButton(
                        onClick = {
                            if (loginNameInput.isNotBlank() && loginEmailInput.isNotBlank()) {
                                isAuthLoading = true
                                scope.launch {
                                    delay(1100)
                                    isAuthLoading = false
                                    onLoginSuccess(loginNameInput.trim(), loginEmailInput.trim())
                                }
                            }
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = GoldSecondary),
                        border = BorderStroke(1.2.dp, GoldSecondary.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("credentials_login_button")
                    ) {
                        Text("Sign In with Credentials", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }

                    Text(
                        text = "Accessing your Halal Go security container states that you agree to our Shariah general guidelines list.",
                        fontSize = 11.sp,
                        color = OnSurfaceVariantText.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }

        // Simulating the Google account chooser bottom sheet inside the Tabbed screen container!
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
                    Text(
                        text = "Sign in with Google",
                        color = Color.Black,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "to continue to Halal Go super service application",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // 1. Custom account option matching values typed in
                        GoogleCustomAccountRow(
                            name = loginNameInput,
                            email = loginEmailInput,
                            isCustom = true,
                            onClick = {
                                showAccountChooser = false
                                isAuthLoading = true
                                scope.launch {
                                    delay(1400)
                                    isAuthLoading = false
                                    onLoginSuccess(loginNameInput, loginEmailInput)
                                }
                            }
                        )

                        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.4f))

                        // 2. Preset: Ahmed Al-Farsi
                        GoogleCustomAccountRow(
                            name = "Ahmed Al-Farsi",
                            email = "ahmed.farsi@gmail.com",
                            isCustom = false,
                            onClick = {
                                showAccountChooser = false
                                isAuthLoading = true
                                scope.launch {
                                    delay(1400)
                                    isAuthLoading = false
                                    onLoginSuccess("Ahmed Al-Farsi", "ahmed.farsi@gmail.com")
                                }
                            }
                        )

                        // 3. Preset: Ayesha Mendoza
                        GoogleCustomAccountRow(
                            name = "Ayesha Mendoza",
                            email = "ayesha.m@gmail.com",
                            isCustom = false,
                            onClick = {
                                showAccountChooser = false
                                isAuthLoading = true
                                scope.launch {
                                    delay(1400)
                                    isAuthLoading = false
                                    onLoginSuccess("Ayesha Mendoza", "ayesha.m@gmail.com")
                                }
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }

        // Fullscreen dynamic authentic glassmorphic progress container
        if (isAuthLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.82f)),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    border = BorderStroke(1.1.dp, PrimaryEmerald.copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(26.dp),
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
fun GoogleCustomAccountRow(
    name: String,
    email: String,
    isCustom: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 10.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .background(if (isCustom) PrimaryContainer else Color.LightGray.copy(alpha = 0.4f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "User dynamic Google profile",
                tint = if (isCustom) PrimaryEmerald else Color.DarkGray,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = name,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Text(
                text = email,
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
        if (isCustom) {
            Text(
                text = "Custom",
                fontSize = 10.sp,
                color = PrimaryEmerald,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .background(PrimaryContainer, RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }
    }
}
