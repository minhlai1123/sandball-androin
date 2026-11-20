package com.antigravity.sandadventure.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.antigravity.sandadventure.data.ProgressManager

@Composable
fun SettingsScreen(
    progressManager: ProgressManager,
    onBack: () -> Unit
) {
    var soundEnabled by remember { mutableStateOf(progressManager.soundEnabled) }
    var musicEnabled by remember { mutableStateOf(progressManager.musicEnabled) }
    var hapticsEnabled by remember { mutableStateOf(progressManager.hapticsEnabled) }
    var showResetDialog by remember { mutableStateOf(false) }

    val bgGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF38BDF8),
            Color(0xFFBAE6FD),
            Color(0xFFFEF08A),
            Color(0xFFFDE047)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgGradient)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 40.dp, start = 20.dp, end = 20.dp, bottom = 20.dp)
        ) {
            // TOP BAR
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(46.dp)
                        .background(Color.White, CircleShape)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color(0xFF0F172A)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = "⚙️ SETTINGS",
                    color = Color(0xFF0F172A),
                    fontWeight = FontWeight.Black,
                    fontSize = 24.sp
                )
            }

            // Audio & Controls Card
            Surface(
                color = Color.White,
                shape = RoundedCornerShape(24.dp),
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    // Sound Effects
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🔊 Sound Effects",
                            color = Color(0xFF1E293B),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Switch(
                            checked = soundEnabled,
                            onCheckedChange = {
                                soundEnabled = it
                                progressManager.soundEnabled = it
                            }
                        )
                    }

                    HorizontalDivider(color = Color(0xFFE2E8F0))

                    // Music
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🎵 Background Music",
                            color = Color(0xFF1E293B),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Switch(
                            checked = musicEnabled,
                            onCheckedChange = {
                                musicEnabled = it
                                progressManager.musicEnabled = it
                            }
                        )
                    }

                    HorizontalDivider(color = Color(0xFFE2E8F0))

                    // Haptics
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "📳 Haptic Vibrations",
                            color = Color(0xFF1E293B),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Switch(
                            checked = hapticsEnabled,
                            onCheckedChange = {
                                hapticsEnabled = it
                                progressManager.hapticsEnabled = it
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Zero-Ads & Kid Safety Card
            Surface(
                color = Color(0xFFECFDF5),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(2.dp, Color(0xFFA7F3D0)),
                shadowElevation = 4.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "🛡️ 100% KID SAFE & ZERO ADS",
                        color = Color(0xFF047857),
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Sand Adventure is completely free, runs 100% offline, and contains zero ads, tracking, or in-app purchases. Pure safe fun for kids and family!",
                        color = Color(0xFF065F46),
                        fontSize = 13.sp,
                        lineHeight = 19.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Reset Data Button
            Surface(
                onClick = { showResetDialog = true },
                color = Color(0xFFFEE2E2),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.5.dp, Color(0xFFFCA5A5)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🗑️ Reset All Progress",
                        color = Color(0xFFDC2626),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text(text = "Reset Progress?") },
            text = { Text(text = "This will reset all unlocked levels and earned stars. This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        progressManager.resetProgress()
                        showResetDialog = false
                    }
                ) {
                    Text(text = "Reset", color = Color(0xFFDC2626))
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text(text = "Cancel")
                }
            }
        )
    }
}
