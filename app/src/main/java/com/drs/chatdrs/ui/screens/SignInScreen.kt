package com.drs.chatdrs.ui.screens

import android.app.Activity
import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import com.drs.chatdrs.R
import com.drs.chatdrs.auth.AuthManager
import com.drs.chatdrs.auth.AuthManager.findActivity
import com.drs.chatdrs.ui.components.LuminousEdgeStrip
import com.drs.chatdrs.ui.components.ProfileImagePicker
import com.drs.chatdrs.ui.theme.CairoFont
import com.drs.chatdrs.ui.theme.ErrorRed
import com.drs.chatdrs.ui.theme.PurpleGradientEnd
import com.drs.chatdrs.ui.theme.PurpleGradientStart
import com.drs.chatdrs.ui.theme.PurplePrimary
import com.google.firebase.auth.FirebaseUser

@Composable
fun SignInScreen(
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    onSignInSuccess: (FirebaseUser) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var selectedBitmap by remember { mutableStateOf<Bitmap?>(null) }

    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val mutedColor = MaterialTheme.colorScheme.onSurfaceVariant
    val surfaceColor = MaterialTheme.colorScheme.surface

    // Attempt silent auto-sign in on initial composition
    LaunchedEffect(Unit) {
        AuthManager.attemptAutoSignIn(
            context = context,
            credentialManager = credentialManager,
            onAuthSuccess = onSignInSuccess,
            onUnauthenticated = { /* Keep sign in UI displayed */ },
            scope = coroutineScope
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Topmost Animated Iridescent Crown Edge Strip
        LuminousEdgeStrip(
            height = 3.5.dp,
            isActive = true,
            modifier = Modifier.align(Alignment.TopCenter)
        )

        // Decorative background glowing ambient circles
        Box(
            modifier = Modifier
                .size(320.dp)
                .align(Alignment.TopCenter)
                .background(
                    Brush.radialGradient(
                        listOf(
                            PurpleGradientStart.copy(alpha = if (isDarkTheme) 0.18f else 0.10f),
                            Color.Transparent
                        )
                    )
                )
        )

        // Theme Toggle at Top Corner
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.End
        ) {
            IconButton(
                onClick = onToggleTheme,
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(surfaceColor)
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), CircleShape)
                    .testTag("auth_theme_toggle")
            ) {
                Icon(
                    imageVector = if (isDarkTheme) Icons.Outlined.LightMode else Icons.Outlined.DarkMode,
                    contentDescription = "تبديل المظهر",
                    tint = if (isDarkTheme) Color(0xFFFDCB6E) else PurplePrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Scrollable Auth Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .statusBarsPadding()
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(30.dp))

            // ChatDrs Main Logo with luxury glow
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(
                                PurpleGradientStart.copy(alpha = 0.2f),
                                PurpleGradientEnd.copy(alpha = 0.08f)
                            )
                        )
                    )
                    .border(
                        1.5.dp,
                        Brush.linearGradient(
                            listOf(
                                PurpleGradientStart.copy(alpha = 0.5f),
                                PurpleGradientEnd.copy(alpha = 0.2f)
                            )
                        ),
                        RoundedCornerShape(28.dp)
                    )
                    .shadow(
                        elevation = 12.dp,
                        shape = RoundedCornerShape(28.dp),
                        ambientColor = PurplePrimary.copy(alpha = 0.3f),
                        spotColor = PurplePrimary.copy(alpha = 0.3f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.chatdrs_logo),
                    contentDescription = "شعار ChatDrs",
                    modifier = Modifier
                        .size(68.dp)
                        .clip(RoundedCornerShape(20.dp))
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // App Wordmark: ChatDrs
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Chat",
                    style = TextStyle(
                        fontFamily = CairoFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 32.sp,
                        color = onSurfaceColor
                    )
                )
                Text(
                    text = "Drs",
                    style = TextStyle(
                        fontFamily = CairoFont,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 32.sp,
                        brush = Brush.linearGradient(
                            listOf(PurpleGradientStart, PurpleGradientEnd)
                        )
                    )
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "مراسلة عربية حديثة • خصوصية مطلقة • فخامة في التفاصيل",
                fontFamily = CairoFont,
                fontWeight = FontWeight.Normal,
                fontSize = 13.sp,
                color = mutedColor,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Profile Picture Selection (Complete Circular Clipping)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                        RoundedCornerShape(20.dp)
                    )
                    .padding(vertical = 16.dp, horizontal = 12.dp)
            ) {
                ProfileImagePicker(
                    selectedImageUri = selectedImageUri,
                    selectedBitmap = selectedBitmap,
                    onImageSelected = { uri, bitmap ->
                        selectedImageUri = uri
                        selectedBitmap = bitmap
                    },
                    size = 104.dp
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = if (selectedImageUri != null || selectedBitmap != null) "تم اختيار صورتك الشخصية" else "صورة الملف الشخصي",
                    fontFamily = CairoFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = if (selectedImageUri != null || selectedBitmap != null) PurplePrimary else onSurfaceColor
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "انقر على الصورة للاختيار من المعرض أو التقاط صورة بالكاميرا",
                    fontFamily = CairoFont,
                    fontWeight = FontWeight.Normal,
                    fontSize = 11.sp,
                    color = mutedColor,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Feature Highlights Cards
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FeatureBadge(
                    icon = Icons.Outlined.Lock,
                    title = "تشفير تام وحماية قصوى",
                    description = "بياناتك ومحادثاتك محمية بقواعد أمان معزولة"
                )
                FeatureBadge(
                    icon = Icons.Outlined.Bolt,
                    title = "تزامن لحظي فائق السرعة",
                    description = "تخزين سحابي مباشر على Firebase مع تحديث فوري"
                )
                FeatureBadge(
                    icon = Icons.Outlined.Palette,
                    title = "واجهة عربية راقية",
                    description = "دعم كامل للاتجاه RTL وألوان مريحة للعين"
                )
            }

            Spacer(modifier = Modifier.height(34.dp))

            // Error Message Banner (if any)
            AnimatedVisibility(
                visible = errorMessage != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                errorMessage?.let { errorText ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(ErrorRed.copy(alpha = 0.12f))
                            .border(1.dp, ErrorRed.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.ErrorOutline,
                                contentDescription = null,
                                tint = ErrorRed,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = errorText,
                                fontFamily = CairoFont,
                                fontWeight = FontWeight.Medium,
                                fontSize = 13.sp,
                                color = ErrorRed
                            )
                        }
                    }
                }
            }

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Google Sign-In Action Button using Credential Manager API
            GoogleSignInButton(
                isLoading = isLoading,
                onSignInClick = {
                    if (isLoading) return@GoogleSignInButton
                    val activity = context.findActivity()
                    if (activity == null) {
                        errorMessage = "تعذر العثور على نافذة التطبيق لتسجيل الدخول."
                        return@GoogleSignInButton
                    }
                    isLoading = true
                    errorMessage = null
                    AuthManager.signInWithGoogle(
                        activity = activity,
                        credentialManager = credentialManager,
                        onAuthSuccess = { user ->
                            isLoading = false
                            onSignInSuccess(user)
                        },
                        onAuthError = { error ->
                            isLoading = false
                            errorMessage = error
                        },
                        onCancelled = {
                            isLoading = false
                        },
                        scope = coroutineScope
                    )
                }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Terms & Privacy Note
            Text(
                text = "بتسجيل دخولك، أنت توافق على شروط الخدمة وسياسة الخصوصية الخاصة بتطبيق ChatDrs المشفّر.",
                fontFamily = CairoFont,
                fontWeight = FontWeight.Normal,
                fontSize = 11.sp,
                color = mutedColor.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
                lineHeight = 17.sp
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

/**
 * Reusable Google Sign-In button component powered by Jetpack Credential Manager.
 */
@Composable
fun GoogleSignInButton(
    isLoading: Boolean,
    onSignInClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onSignInClick,
        shape = RoundedCornerShape(22.dp),
        color = Color.Transparent,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(
                Brush.linearGradient(
                    listOf(PurpleGradientStart, PurpleGradientEnd)
                )
            )
            .testTag("google_sign_in_button")
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.5.dp
                )
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    // Google "G" icon container
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "G",
                            fontFamily = CairoFont,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 17.sp,
                            color = PurplePrimary
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = "المتابعة باستخدام حساب Google",
                        fontFamily = CairoFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun FeatureBadge(
    icon: ImageVector,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
            .border(
                1.dp,
                MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(PurplePrimary.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = PurplePrimary,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column {
            Text(
                text = title,
                fontFamily = CairoFont,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = description,
                fontFamily = CairoFont,
                fontWeight = FontWeight.Normal,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
