package com.drs.chatdrs

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.credentials.CredentialManager
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.drs.chatdrs.auth.AuthManager
import com.drs.chatdrs.data.preferences.ThemePreferences
import com.drs.chatdrs.data.preferences.ThemePreferencesRepository
import com.drs.chatdrs.ui.screens.ChatListScreen
import com.drs.chatdrs.ui.screens.SignInScreen
import com.drs.chatdrs.ui.theme.ChatDrsTheme
import com.drs.chatdrs.viewmodel.ChatViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            val coroutineScope = rememberCoroutineScope()
            val systemDark = isSystemInDarkTheme()
            val themePreferencesRepository = remember { ThemePreferencesRepository(context) }

            val themePrefs by themePreferencesRepository.themePreferencesFlow.collectAsStateWithLifecycle(
                initialValue = ThemePreferences(isDarkMode = null, useDynamicColor = true)
            )

            val isDarkTheme = themePrefs.isDarkMode ?: systemDark

            ChatDrsTheme(
                darkTheme = isDarkTheme,
                dynamicColor = themePrefs.useDynamicColor
            ) {
                // Enforce 100% Arabic RTL layout direction across the entire app
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    AppContent(
                        isDarkTheme = isDarkTheme,
                        onToggleTheme = {
                            coroutineScope.launch {
                                themePreferencesRepository.toggleDarkMode(isDarkTheme)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun AppContent(
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }
    var currentUser by remember { mutableStateOf(Firebase.auth.currentUser) }

    // Real-time listener for Firebase Auth session changes
    DisposableEffect(Unit) {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            currentUser = auth.currentUser
        }
        Firebase.auth.addAuthStateListener(listener)
        onDispose {
            Firebase.auth.removeAuthStateListener(listener)
        }
    }

    if (currentUser == null) {
        // Unauthenticated state: Elegant ChatDrs Sign-In Screen
        SignInScreen(
            isDarkTheme = isDarkTheme,
            onToggleTheme = onToggleTheme,
            onSignInSuccess = { user ->
                currentUser = user
            }
        )
    } else {
        val currentUserId = currentUser!!.uid
        val chatViewModel: ChatViewModel = viewModel(
            key = currentUserId,
            factory = ChatViewModel.provideFactory(currentUserId)
        )

        // Authenticated session: ChatDrs Main Conversations List
        ChatListScreen(
            isDarkTheme = isDarkTheme,
            onToggleTheme = onToggleTheme,
            chatViewModel = chatViewModel,
            onSignOut = {
                AuthManager.signOut(
                    context = context,
                    credentialManager = credentialManager,
                    onComplete = {
                        currentUser = null
                    },
                    scope = coroutineScope
                )
            }
        )
    }
}
