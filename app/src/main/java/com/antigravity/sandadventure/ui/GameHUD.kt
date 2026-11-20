package com.antigravity.sandadventure.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.antigravity.sandadventure.data.getWorldTheme
import com.antigravity.sandadventure.gameplay.GameEngine

@Composable
fun GameHUD(
    gameEngine: GameEngine,
    onRestart: () -> Unit,
    onMenu: () -> Unit,
    onNextLevel: () -> Unit
) {
    val level = gameEngine.currentLevel
    val theme = getWorldTheme(gameEngine.currentWorldId)
    val levelNumber = gameEngine.currentLevelId
    val worldStep = ((levelNumber - 1) % 10) + 1

    Box(modifier = Modifier.fillMaxSize()) {
        // Top HUD Bar
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 36.dp, start = 16.dp, end = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Home / Menu bubble button
                IconButton(
                    onClick = onMenu,
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color.White, CircleShape)
                ) {
                    Icon(Icons.Default.Home, contentDescription = "Menu", tint = Color(0xFF0F172A))
                }

                // Level & World Pill Banner
                Surface(
                    color = Color.White,
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(2.5.dp, Color(0xFFF59E0B)),
                    shadowElevation = 6.dp,
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 22.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "⭐ LEVEL $levelNumber ⭐",
                            color = Color(0xFFD97706),
                            fontWeight = FontWeight.Black,
                            fontSize = 19.sp
                        )
                        Text(
                            text = "${theme.emoji} ${theme.name} • $worldStep/10",
                            color = Color(0xFF475569),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }

                // Restart bubble button
                IconButton(
                    onClick = onRestart,
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color.White, CircleShape)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Restart", tint = Color(0xFF0F172A))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Live Quota Tracker Badge
            Surface(
                color = if (gameEngine.isVictory) Color(0xFF10B981) else Color(0xEEFFFFFF),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.5.dp, if (gameEngine.isVictory) Color(0xFF6EE7B7) else Color(0xFFCBD5E1)),
                shadowElevation = 3.dp,
                modifier = Modifier.padding(horizontal = 12.dp)
            ) {
                Text(
                    text = if (gameEngine.isVictory) "🎉 TRUCK FULL! ROUND CLEAR! 🎉" else "🎯 Collected: ${gameEngine.collectedCount} / ${level?.quota ?: 10}",
                    color = if (gameEngine.isVictory) Color.White else Color(0xFF1E293B),
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 5.dp)
                )
            }
        }

        // Victory Dialog Overlay
        if (gameEngine.isVictory) {
            Surface(
                color = Color.White,
                shape = RoundedCornerShape(32.dp),
                border = BorderStroke(4.dp, Color(0xFFFBBF24)),
                shadowElevation = 16.dp,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(24.dp)
                    .fillMaxWidth(0.90f)
            ) {
                Column(
                    modifier = Modifier.padding(26.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "🎉 VICTORY! 🎉",
                        color = Color(0xFFEA580C),
                        fontWeight = FontWeight.Black,
                        fontSize = 28.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Star Rating Display
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        for (i in 1..3) {
                            val isEarned = i <= gameEngine.earnedStars
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Star $i",
                                tint = if (isEarned) Color(0xFFF59E0B) else Color(0xFFE2E8F0),
                                modifier = Modifier.size(52.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Balls in Truck: ${gameEngine.collectedCount} / ${level?.quota ?: 10}",
                        color = Color(0xFF1E293B),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )

                    val remainingSec = (gameEngine.autoAdvanceDelay - gameEngine.victoryTimer).coerceAtLeast(0.1f).toInt() + 1
                    Text(
                        text = "Auto advancing to next level in ${remainingSec}s...",
                        color = Color(0xFF64748B),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(22.dp))

                    // Big Juicy Next Level Button
                    Button(
                        onClick = {
                            gameEngine.audioManager.playButtonTap()
                            gameEngine.advanceToNextLevel()
                            onNextLevel()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        shape = RoundedCornerShape(26.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(58.dp)
                    ) {
                        Text(
                            text = "NEXT LEVEL ➔",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Replay Button
                    OutlinedButton(
                        onClick = onRestart,
                        shape = RoundedCornerShape(24.dp),
                        border = BorderStroke(1.5.dp, Color(0xFF94A3B8)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "Replay Level", color = Color(0xFF475569), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
