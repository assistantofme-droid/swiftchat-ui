package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.TelegramAccent
import com.example.ui.theme.TelegramDarkBg
import com.example.ui.theme.TelegramPrimary
import com.example.ui.theme.TelegramTextMuted
import com.example.ui.theme.TelegramTextPrimary
import com.example.ui.theme.TelegramTextSecondary
import kotlinx.coroutines.delay

enum class AuthStep {
    PHONE_INPUT,
    OTP_VERIFY
}

@Composable
fun LoginScreen(
    isLoading: Boolean,
    errorMessage: String?,
    onSendOtp: (String) -> Unit,
    onVerifyOtp: (String, String) -> Unit,
    onSkipLogin: () -> Unit,
    modifier: Modifier = Modifier
) {
    var step by remember { mutableStateOf(AuthStep.PHONE_INPUT) }
    var countryCode by remember { mutableStateOf("+98") }
    var phoneNumber by remember { mutableStateOf("9121234567") }
    var otpCode by remember { mutableStateOf("") }
    var timerSeconds by remember { mutableIntStateOf(60) }
    val focusManager = LocalFocusManager.current

    // Countdown timer for OTP resend
    LaunchedEffect(step) {
        if (step == AuthStep.OTP_VERIFY) {
            timerSeconds = 60
            while (timerSeconds > 0) {
                delay(1000)
                timerSeconds--
            }
        }
    }

    val fullPhone = remember(countryCode, phoneNumber) {
        val cleanCode = countryCode.removePrefix("+").trim()
        val cleanNum = phoneNumber.trim().removePrefix("0")
        "$cleanCode$cleanNum"
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(TelegramDarkBg)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Navigation Bar / Back button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (step == AuthStep.OTP_VERIFY) {
                    IconButton(onClick = {
                        step = AuthStep.PHONE_INPUT
                        otpCode = ""
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                }
                Spacer(modifier = Modifier.weight(1f))
                TextButton(onClick = onSkipLogin) {
                    Text(
                        text = "Skip / Guest",
                        color = TelegramPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Telegram Logo / Graphic
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(TelegramAccent, TelegramPrimary)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Telegram",
                    tint = Color.White,
                    modifier = Modifier.size(46.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            AnimatedContent(
                targetState = step,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "AuthStepTransition"
            ) { currentStep ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    when (currentStep) {
                        AuthStep.PHONE_INPUT -> {
                            Text(
                                text = "Your Phone Number",
                                color = TelegramTextPrimary,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Please enter your phone number to sign in or register with 7eve9chat.",
                                color = TelegramTextSecondary,
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center,
                                lineHeight = 20.sp,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )

                            Spacer(modifier = Modifier.height(32.dp))

                            // Phone Input Box
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Country code
                                OutlinedTextField(
                                    value = countryCode,
                                    onValueChange = { countryCode = it },
                                    label = { Text("Code", color = TelegramTextMuted) },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = TelegramPrimary,
                                        unfocusedBorderColor = Color(0xFF2C3E50),
                                        focusedTextColor = TelegramTextPrimary,
                                        unfocusedTextColor = TelegramTextPrimary,
                                        cursorColor = TelegramPrimary
                                    ),
                                    modifier = Modifier.width(90.dp)
                                )

                                Spacer(modifier = Modifier.width(12.dp))

                                // Number field
                                OutlinedTextField(
                                    value = phoneNumber,
                                    onValueChange = { phoneNumber = it },
                                    label = { Text("Phone Number", color = TelegramTextMuted) },
                                    placeholder = { Text("912 123 4567", color = TelegramTextMuted) },
                                    singleLine = true,
                                    trailingIcon = {
                                        if (phoneNumber.isNotEmpty()) {
                                            IconButton(onClick = { phoneNumber = "" }) {
                                                Icon(
                                                    imageVector = Icons.Default.Close,
                                                    contentDescription = "Clear",
                                                    tint = TelegramTextSecondary
                                                )
                                            }
                                        }
                                    },
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Phone,
                                        imeAction = ImeAction.Done
                                    ),
                                    keyboardActions = KeyboardActions(
                                        onDone = {
                                            focusManager.clearFocus()
                                            if (phoneNumber.isNotBlank() && !isLoading) {
                                                onSendOtp(fullPhone)
                                                step = AuthStep.OTP_VERIFY
                                            }
                                        }
                                    ),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = TelegramPrimary,
                                        unfocusedBorderColor = Color(0xFF2C3E50),
                                        focusedTextColor = TelegramTextPrimary,
                                        unfocusedTextColor = TelegramTextPrimary,
                                        cursorColor = TelegramPrimary
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            // Error display
                            AnimatedVisibility(visible = !errorMessage.isNullOrBlank()) {
                                Text(
                                    text = errorMessage.orEmpty(),
                                    color = Color(0xFFFF5252),
                                    fontSize = 13.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(top = 12.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(28.dp))

                            // Send Code Button
                            Button(
                                onClick = {
                                    focusManager.clearFocus()
                                    if (phoneNumber.isNotBlank() && !isLoading) {
                                        onSendOtp(fullPhone)
                                        step = AuthStep.OTP_VERIFY
                                    }
                                },
                                enabled = phoneNumber.isNotBlank() && !isLoading,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = TelegramPrimary,
                                    disabledContainerColor = TelegramPrimary.copy(alpha = 0.5f)
                                ),
                                shape = RoundedCornerShape(24.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(
                                        color = Color.White,
                                        strokeWidth = 2.dp,
                                        modifier = Modifier.size(22.dp)
                                    )
                                } else {
                                    Text(
                                        text = "Next / دریافت کد",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Demo / Quick Test info box
                            Surface(
                                color = Color(0x332AABEE),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        phoneNumber = "9121234567"
                                    }
                                    .padding(vertical = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Phone,
                                        contentDescription = null,
                                        tint = TelegramPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "Default Test Phone: 989121234567",
                                            color = Color.White,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Text(
                                            text = "Tap here to auto-fill sample phone",
                                            color = TelegramTextSecondary,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }
                        }

                        AuthStep.OTP_VERIFY -> {
                            Text(
                                text = "Enter Code",
                                color = TelegramTextPrimary,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "Code sent to +$fullPhone",
                                    color = TelegramTextSecondary,
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                IconButton(
                                    onClick = {
                                        step = AuthStep.PHONE_INPUT
                                        otpCode = ""
                                    },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Edit phone",
                                        tint = TelegramPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(28.dp))

                            // OTP Code Input Field
                            OutlinedTextField(
                                value = otpCode,
                                onValueChange = {
                                    if (it.length <= 6) {
                                        otpCode = it
                                        if (it.length >= 4 && !isLoading) {
                                            onVerifyOtp(fullPhone, it)
                                        }
                                    }
                                },
                                label = { Text("Confirmation Code (کد تأیید)", color = TelegramTextMuted) },
                                placeholder = { Text("1234", color = TelegramTextMuted) },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.NumberPassword,
                                    imeAction = ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(
                                    onDone = {
                                        focusManager.clearFocus()
                                        if (otpCode.isNotBlank() && !isLoading) {
                                            onVerifyOtp(fullPhone, otpCode)
                                        }
                                    }
                                ),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = TelegramPrimary,
                                    unfocusedBorderColor = Color(0xFF2C3E50),
                                    focusedTextColor = TelegramTextPrimary,
                                    unfocusedTextColor = TelegramTextPrimary,
                                    cursorColor = TelegramPrimary
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )

                            // Error display
                            AnimatedVisibility(visible = !errorMessage.isNullOrBlank()) {
                                Text(
                                    text = errorMessage.orEmpty(),
                                    color = Color(0xFFFF5252),
                                    fontSize = 13.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(top = 12.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            // Verify Button
                            Button(
                                onClick = {
                                    focusManager.clearFocus()
                                    if (otpCode.isNotBlank() && !isLoading) {
                                        onVerifyOtp(fullPhone, otpCode)
                                    }
                                },
                                enabled = otpCode.isNotBlank() && !isLoading,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = TelegramPrimary,
                                    disabledContainerColor = TelegramPrimary.copy(alpha = 0.5f)
                                ),
                                shape = RoundedCornerShape(24.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(
                                        color = Color.White,
                                        strokeWidth = 2.dp,
                                        modifier = Modifier.size(22.dp)
                                    )
                                } else {
                                    Text(
                                        text = "Confirm & Sign In",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(18.dp))

                            // Resend Code timer
                            if (timerSeconds > 0) {
                                Text(
                                    text = "Resend code in 0:${if (timerSeconds < 10) "0$timerSeconds" else "$timerSeconds"}",
                                    color = TelegramTextMuted,
                                    fontSize = 13.sp
                                )
                            } else {
                                TextButton(onClick = {
                                    timerSeconds = 60
                                    onSendOtp(fullPhone)
                                }) {
                                    Text(
                                        text = "Resend SMS / ارسال مجدد پیامک",
                                        color = TelegramPrimary,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Fast test auto-fill button
                            TextButton(onClick = {
                                otpCode = "1234"
                                onVerifyOtp(fullPhone, "1234")
                            }) {
                                Text(
                                    text = "⚡ Test Code: 1234",
                                    color = Color(0xFF64B5F6),
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
