package com.example.ui.components

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Surface
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.R

@Composable
fun OnboardingScreen(
    onDismiss: () -> Unit,
    onComplete: () -> Unit
) {
    val context = LocalContext.current
    var currentStep by remember { mutableIntStateOf(0) }
    val totalSteps = 4

    fun hasPermission(perm: String) =
        ContextCompat.checkSelfPermission(context, perm) == PackageManager.PERMISSION_GRANTED

    val hasLocation = hasPermission(Manifest.permission.ACCESS_FINE_LOCATION) ||
            hasPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
    val hasNotifications = android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.TIRAMISU ||
            hasPermission(Manifest.permission.POST_NOTIFICATIONS)

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .testTag("onboarding_screen"),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .widthIn(max = 500.dp)
                    .padding(vertical = 16.dp),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Stepper indicators
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        repeat(totalSteps) { idx ->
                            Box(
                                modifier = Modifier
                                    .size(if (idx == currentStep) 24.dp else 8.dp, 8.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (idx == currentStep) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.outlineVariant
                                    )
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp))

                    AnimatedContent(
                        targetState = currentStep,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label = "onboarding_step",
                        modifier = Modifier.weight(1f, fill = false)
                    ) { step ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            when (step) {
                                0 -> {
                                    // Welcome Step
                                    Box(
                                        modifier = Modifier
                                            .size(64.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primaryContainer),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AutoAwesome,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(36.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = stringResource(R.string.onboarding_welcome_title),
                                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = stringResource(R.string.onboarding_welcome_desc),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                                        shape = RoundedCornerShape(16.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Shield,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = stringResource(R.string.onboarding_privacy_note),
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }
                                }

                                1 -> {
                                    // Triggers & Rules Step
                                    Box(
                                        modifier = Modifier
                                            .size(64.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.secondaryContainer),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Speed,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.secondary,
                                            modifier = Modifier.size(36.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = stringResource(R.string.onboarding_how_title),
                                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = stringResource(R.string.onboarding_how_desc),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Column(
                                        verticalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        OnboardingFeatureRow(icon = Icons.Default.LocationOn, title = "Location (GPS)", desc = "Arrive or leave Home, Work, or Gym")
                                        OnboardingFeatureRow(icon = Icons.Default.Security, title = "Wi-Fi & Bluetooth", desc = "Connect to specific SSIDs or Bluetooth audio")
                                        OnboardingFeatureRow(icon = Icons.Default.BatteryChargingFull, title = "Battery & Power", desc = "Auto power-saving & charging state triggers")
                                    }
                                }

                                2 -> {
                                    // Permissions Step
                                    Box(
                                        modifier = Modifier
                                            .size(64.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.tertiaryContainer),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Notifications,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.tertiary,
                                            modifier = Modifier.size(36.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = stringResource(R.string.onboarding_perm_title),
                                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = stringResource(R.string.onboarding_perm_desc),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Column(
                                        verticalArrangement = Arrangement.spacedBy(10.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        OnboardingPermissionCard(
                                            title = "Location Access (Geofence Triggers)",
                                            isGranted = hasLocation,
                                            onGrant = {
                                                try {
                                                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                                        data = Uri.fromParts("package", context.packageName, null)
                                                    }
                                                    context.startActivity(intent)
                                                } catch (e: Exception) {
                                                    e.printStackTrace()
                                                }
                                            }
                                        )

                                        OnboardingPermissionCard(
                                            title = "Notification Permission",
                                            isGranted = hasNotifications,
                                            onGrant = {
                                                try {
                                                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                                        data = Uri.fromParts("package", context.packageName, null)
                                                    }
                                                    context.startActivity(intent)
                                                } catch (e: Exception) {
                                                    e.printStackTrace()
                                                }
                                            }
                                        )
                                    }
                                }

                                3 -> {
                                    // Ready Step
                                    Box(
                                        modifier = Modifier
                                            .size(64.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primaryContainer),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(36.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = stringResource(R.string.onboarding_ready_title),
                                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = stringResource(R.string.onboarding_ready_desc),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Navigation Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (currentStep > 0) {
                            TextButton(onClick = { currentStep-- }) {
                                Text(stringResource(R.string.onboarding_btn_back))
                            }
                        } else {
                            TextButton(onClick = onDismiss) {
                                Text(stringResource(R.string.onboarding_btn_skip))
                            }
                        }

                        Button(
                            onClick = {
                                if (currentStep < totalSteps - 1) {
                                    currentStep++
                                } else {
                                    onComplete()
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("onboarding_next_btn")
                        ) {
                            Text(if (currentStep == totalSteps - 1) stringResource(R.string.onboarding_btn_get_started) else stringResource(R.string.onboarding_btn_continue))
                            if (currentStep < totalSteps - 1) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(Icons.Default.ChevronRight, contentDescription = null, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OnboardingDialog(
    onDismiss: () -> Unit,
    onComplete: () -> Unit
) {
    OnboardingScreen(onDismiss = onDismiss, onComplete = onComplete)
}

@Composable
private fun OnboardingFeatureRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    desc: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
            Text(desc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun OnboardingPermissionCard(
    title: String,
    isGranted: Boolean,
    onGrant: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isGranted) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
            else MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                Text(
                    text = if (isGranted) "Granted ✓" else "Tap to Grant",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isGranted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (!isGranted) {
                OutlinedButton(onClick = onGrant, shape = RoundedCornerShape(10.dp)) {
                    Text("Grant", style = MaterialTheme.typography.labelSmall)
                }
            } else {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
            }
        }
    }
}
