package com.antigravity.sandadventure.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.antigravity.sandadventure.data.ProgressManager

@Composable
fun LevelSelectScreen(
    progressManager: ProgressManager,
    onLevelSelected: (Int) -> Unit,
    onBack: () -> Unit
) {
    val highestUnlocked = progressManager.highestUnlockedLevel
    val initialWorld = ((highestUnlocked - 1) / 10).coerceIn(0, 9)
    var selectedWorld by remember { mutableIntStateOf(initialWorld) }

    val worldNames = listOf(
        "🏖️ 1. Beach",
        "🍬 2. Candy",
        "🌿 3. Meadow",
        "🍧 4. Ice Cream",
        "🌈 5. Rainbow",
        "💣 6. TNT Quarry",
        "🎡 7. Carnival",
        "🏰 8. Castle",
        "🌌 9. Portals",
        "👑 10. Master"
    )

    // Cheerful sunny background
    val bgGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF38BDF8),
            Color(0xFFBAE6FD),
            Color(0xFFFEF08A),
            Color(0xFFFDE047)
        )
    )

    // Current level pulsating animation
    val infiniteTransition = rememberInfiniteTransition(label = "pulseCurrent")
    val currentPulse by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "currentPulse"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgGradient)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // TOP BAR
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 40.dp, start = 16.dp, end = 16.dp, bottom = 8.dp)
            ) {
                Row(
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
                        text = "🗺️ SELECT LEVEL",
                        color = Color(0xFF0F172A),
                        fontWeight = FontWeight.Black,
                        fontSize = 22.sp
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    // Stars pill
                    Surface(
                        color = Color.White,
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(2.dp, Color(0xFFF59E0B)),
                        shadowElevation = 4.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "⭐ ", fontSize = 14.sp)
                            Text(
                                text = "${progressManager.getTotalStars()}",
                                color = Color(0xFFB45309),
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // World Tabs
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(worldNames.size) { wIdx ->
                        val isSelected = wIdx == selectedWorld
                        val worldFirstLevel = wIdx * 10 + 1
                        val isWorldUnlocked = highestUnlocked >= worldFirstLevel

                        Surface(
                            color = if (isSelected) Color(0xFF10B981) else if (isWorldUnlocked) Color.White else Color(0x66FFFFFF),
                            shape = RoundedCornerShape(18.dp),
                            border = if (isSelected) BorderStroke(2.dp, Color(0xFF6EE7B7)) else null,
                            shadowElevation = if (isSelected) 6.dp else 2.dp,
                            modifier = Modifier.clickable {
                                selectedWorld = wIdx
                            }
                        ) {
                            Text(
                                text = worldNames[wIdx],
                                color = if (isSelected) Color.White else if (isWorldUnlocked) Color(0xFF1E293B) else Color(0xFF94A3B8),
                                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }

            // LEVEL GRID
            val startLevel = selectedWorld * 10 + 1

            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(10) { index ->
                    val levelId = startLevel + index
                    val isUnlocked = levelId <= highestUnlocked
                    val isCurrent = levelId == highestUnlocked
                    val stars = progressManager.getStarsForLevel(levelId)

                    Surface(
                        color = when {
                            isCurrent -> Color(0xFFFEF3C7) // Golden highlight
                            isUnlocked -> Color.White
                            else -> Color(0x66FFFFFF)
                        },
                        shape = RoundedCornerShape(22.dp),
                        shadowElevation = if (isUnlocked) 8.dp else 1.dp,
                        border = when {
                            isCurrent -> BorderStroke(3.dp, Color(0xFFF59E0B))
                            isUnlocked -> BorderStroke(2.dp, Color(0xFFE2E8F0))
                            else -> null
                        },
                        modifier = Modifier
                            .aspectRatio(1f)
                            .then(if (isCurrent) Modifier.scale(currentPulse) else Modifier)
                            .clickable(enabled = isUnlocked) {
                                onLevelSelected(levelId)
                            }
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(8.dp)
                        ) {
                            if (isUnlocked) {
                                Text(
                                    text = "$levelId",
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (isCurrent) Color(0xFFD97706) else Color(0xFF1E293B)
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                // Stars Row
                                Row(
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    for (s in 1..3) {
                                        val earned = s <= stars
                                        Icon(
                                            imageVector = Icons.Default.Star,
                                            contentDescription = null,
                                            tint = if (earned) Color(0xFFF59E0B) else Color(0xFFCBD5E1),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Locked",
                                    tint = Color(0xFF94A3B8),
                                    modifier = Modifier.size(26.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "$levelId",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF94A3B8)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
