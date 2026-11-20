package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Class
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import com.example.R
import com.example.auth.attemptAutoSignIn
import com.example.auth.onGoogleSignInClicked
import com.example.ui.components.InfoPill
import com.example.ui.components.TutorDeskCard
import com.example.ui.theme.CanvasBase
import com.example.ui.theme.CoralBg
import com.example.ui.theme.CoralText
import com.example.ui.theme.DeepSlateNavy
import com.example.ui.theme.EmeraldBg
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.InteractiveBlue
import com.example.ui.theme.InteractiveBlueBg
import com.example.ui.theme.InteractiveBlueDeep
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate900
import com.example.ui.theme.SurfaceWhite

@Composable
fun AuthScreen(
    onAuthSuccess: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        attemptAutoSignIn(
            context = context,
            credentialManager = credentialManager,
            onAuthSuccess = onAuthSuccess,
            onUnauthenticated = { /* Interactive button ready */ },
            scope = scope
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CanvasBase)
            .safeDrawingPadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 520.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(DeepSlateNavy),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "TD",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = SurfaceWhite
                        )
                    }
                    Column {
                        Text(
                            text = "TutorDesk",
                            style = MaterialTheme.typography.headlineMedium,
                            color = Slate900
                        )
                        Text(
                            text = "HSC ICT Mentor Workspace",
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate500
                        )
                    }
                }
                InfoPill(text = "BDT (৳)")
            }

            Spacer(modifier = Modifier.height(20.dp))

            TutorDeskCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 16.dp,
                borderColor = Slate200,
                elevation = 2.dp
            ) {
                Image(
                    painter = painterResource(id = R.drawable.tutordesk_hero_1795162893517),
                    contentDescription = "TutorDesk HSC ICT coaching workspace illustration",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(190.dp)
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                    contentScale = ContentScale.Crop
                )
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Run your HSC ICT batches with calm clarity.",
                        style = MaterialTheme.typography.headlineLarge,
                        color = Slate900
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Built for private tutors and coaching mentors across Bangladesh. Track Class XI–XII batches, mark daily roll call with one thumb, and reconcile monthly ৳ tuition dues.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Slate700
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    FeatureHighlightRow(
                        icon = Icons.Filled.Class,
                        tint = InteractiveBlueDeep,
                        bg = InteractiveBlueBg,
                        title = "Batch & Topic Tracking",
                        subtitle = "Manage Class XI, XII & HSC Revision schedules"
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    FeatureHighlightRow(
                        icon = Icons.Filled.CheckCircle,
                        tint = EmeraldGreen,
                        bg = EmeraldBg,
                        title = "One-Thumb Roll Call",
                        subtitle = "Fast P / L / A segmented toggle per class date"
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    FeatureHighlightRow(
                        icon = Icons.Filled.AccountBalanceWallet,
                        tint = DeepSlateNavy,
                        bg = Color(0xFFF1F5F9),
                        title = "Monthly BDT (৳) Ledger",
                        subtitle = "Instant Paid, Partial & Due summaries synced to cloud"
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (!errorMessage.isNullOrBlank()) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    shape = RoundedCornerShape(10.dp),
                    color = CoralBg
                ) {
                    Text(
                        text = errorMessage ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = CoralText,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            Button(
                onClick = {
                    errorMessage = null
                    isLoading = true
                    onGoogleSignInClicked(
                        context = context,
                        credentialManager = credentialManager,
                        onAuthSuccess = {
                            isLoading = false
                            onAuthSuccess()
                        },
                        onAuthError = { msg ->
                            isLoading = false
                            errorMessage = msg
                        },
                        scope = scope,
                        onAuthCancelled = {
                            isLoading = false
                        }
                    )
                },
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("google_sign_in_button"),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DeepSlateNavy,
                    contentColor = SurfaceWhite
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = SurfaceWhite,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Signing in...", style = MaterialTheme.typography.labelLarge)
                } else {
                    Icon(
                        imageVector = Icons.Filled.Lock,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Sign in with Google",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Your batches, attendance & fee records stay backed up to your Google account.",
                style = MaterialTheme.typography.bodySmall,
                color = Slate500
            )
        }
    }
}

@Composable
private fun FeatureHighlightRow(
    icon: ImageVector,
    tint: Color,
    bg: Color,
    title: String,
    subtitle: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(bg),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(18.dp))
        }
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = Slate900
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = Slate500
            )
        }
    }
}
