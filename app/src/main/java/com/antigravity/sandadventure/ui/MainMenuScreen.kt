package com.antigravity.sandadventure.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.antigravity.sandadventure.data.ProgressManager

@Composable
fun MainMenuScreen(
    progressManager: ProgressManager,
    onPlay: () -> Unit,
    onLevelSelect: () -> Unit,
    onSettings: () -> Unit
) {
    // Joyful, sunny cartoon gradient (Soft Sky Blue -> Sunny Cream -> Warm Golden Sand)
    val bgGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF38BDF8), // Cheerful Sky Blue
            Color(0xFFBAE6FD), // Light cloud blue
            Color(0xFFFEF08A), // Sunny butter cream
            Color(0xFFFDE047), // Warm golden sand
            Color(0xFFF59E0B)  // Rich sand rim
        )
    )

    // Gentle pulsating animation for PLAY button
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val playScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "playScale"
    )

    val mascotFloat by infiniteTransition.animateFloat(
        initialValue = -6f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "mascotFloat"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgGradient)
    ) {
        // Floating decorative cartoon clouds and sand hill backdrop
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Cloud 1
            drawCircle(Color(0x99FFFFFF), radius = 55.dp.toPx(), center = Offset(w * 0.18f, h * 0.10f))
            drawCircle(Color(0x99FFFFFF), radius = 45.dp.toPx(), center = Offset(w * 0.28f, h * 0.11f))
            drawCircle(Color(0x99FFFFFF), radius = 40.dp.toPx(), center = Offset(w * 0.10f, h * 0.12f))

            // Cloud 2
            drawCircle(Color(0x88FFFFFF), radius = 50.dp.toPx(), center = Offset(w * 0.82f, h * 0.18f))
            drawCircle(Color(0x88FFFFFF), radius = 40.dp.toPx(), center = Offset(w * 0.90f, h * 0.19f))
            drawCircle(Color(0x88FFFFFF), radius = 35.dp.toPx(), center = Offset(w * 0.74f, h * 0.20f))

            // Golden bottom sand dune curve
            val sandPath = Path().apply {
                moveTo(0f, h * 0.88f)
                cubicTo(w * 0.35f, h * 0.83f, w * 0.65f, h * 0.92f, w, h * 0.86f)
                lineTo(w, h)
                lineTo(0f, h)
                close()
            }
            drawPath(sandPath, Color(0xFFEAB308))
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .padding(top = 44.dp, bottom = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // TOP BAR: Stars Badge & Kid-Safe Indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Kid Safe badge
                Surface(
                    color = Color(0xDDFFFFFF),
                    shape = RoundedCornerShape(20.dp),
                    shadowElevation = 4.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "🎈 NO ADS • OFFLINE", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F766E))
                    }
                }

                // Stars count badge
                Surface(
                    color = Color(0xFFFEF3C7),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(2.dp, Color(0xFFF59E0B)),
                    shadowElevation = 4.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "⭐ ", fontSize = 14.sp)
                        Text(
                            text = "${progressManager.getTotalStars()} / 300",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFFB45309)
                        )
                    }
                }
            }

            // CENTER HERO: 3D Bubble Title & Cute Kawaii Mascot Balls
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Bubble Title 3D Card
                Surface(
                    color = Color.White,
                    shape = RoundedCornerShape(28.dp),
                    border = BorderStroke(4.dp, Color(0xFFFBBF24)),
                    shadowElevation = 10.dp,
                    modifier = Modifier.padding(horizontal = 12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 28.dp, vertical = 14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "🏖️ SAND",
                            fontSize = 42.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFFEA580C),
                            letterSpacing = 2.sp
                        )
                        Text(
                            text = "ADVENTURE",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF0284C7),
                            letterSpacing = 4.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Mascot Balls Illustration (Kawaii faces blinking & smiling)
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .height(130.dp)
                        .offset(y = mascotFloat.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val cx = size.width / 2f
                        val cy = size.height / 2f

                        // Ball 1: Red Sweetie (Left)
                        val b1X = cx - 75.dp.toPx()
                        val b1Y = cy + 10.dp.toPx()
                        val r1 = 34.dp.toPx()
                        drawCircle(Color(0xFFFF4757), radius = r1, center = Offset(b1X, b1Y))
                        drawCircle(Color(0x66FFFFFF), radius = r1 * 0.38f, center = Offset(b1X - r1 * 0.3f, b1Y - r1 * 0.3f))
                        // Kawaii eyes & smile
                        drawCircle(Color(0x77FF6B81), radius = r1 * 0.22f, center = Offset(b1X - r1 * 0.5f, b1Y + r1 * 0.15f))
                        drawCircle(Color(0x77FF6B81), radius = r1 * 0.22f, center = Offset(b1X + r1 * 0.5f, b1Y + r1 * 0.15f))
                        drawCircle(Color.White, radius = r1 * 0.25f, center = Offset(b1X - r1 * 0.32f, b1Y - r1 * 0.1f))
                        drawCircle(Color.White, radius = r1 * 0.25f, center = Offset(b1X + r1 * 0.32f, b1Y - r1 * 0.1f))
                        drawCircle(Color(0xFF1E293B), radius = r1 * 0.16f, center = Offset(b1X - r1 * 0.32f, b1Y - r1 * 0.1f))
                        drawCircle(Color(0xFF1E293B), radius = r1 * 0.16f, center = Offset(b1X + r1 * 0.32f, b1Y - r1 * 0.1f))
                        drawArc(Color(0xFF1E293B), startAngle = 20f, sweepAngle = 140f, useCenter = false,
                            topLeft = Offset(b1X - r1 * 0.28f, b1Y + r1 * 0.10f),
                            size = Size(r1 * 0.56f, r1 * 0.40f),
                            style = Stroke(width = 3.dp.toPx())
                        )

                        // Ball 2: Golden Star Mascot (Center, slightly larger)
                        val b2X = cx
                        val b2Y = cy - 8.dp.toPx()
                        val r2 = 42.dp.toPx()
                        drawCircle(Color(0xFFFFA502), radius = r2, center = Offset(b2X, b2Y))
                        drawCircle(Color(0x88FFFFFF), radius = r2 * 0.38f, center = Offset(b2X - r2 * 0.3f, b2Y - r2 * 0.3f))
                        // Sparkle eyes
                        drawCircle(Color(0x77FF6B81), radius = r2 * 0.22f, center = Offset(b2X - r2 * 0.5f, b2Y + r2 * 0.15f))
                        drawCircle(Color(0x77FF6B81), radius = r2 * 0.22f, center = Offset(b2X + r2 * 0.5f, b2Y + r2 * 0.15f))
                        drawCircle(Color.White, radius = r2 * 0.28f, center = Offset(b2X - r2 * 0.32f, b2Y - r2 * 0.1f))
                        drawCircle(Color.White, radius = r2 * 0.28f, center = Offset(b2X + r2 * 0.32f, b2Y - r2 * 0.1f))
                        drawCircle(Color(0xFF1E293B), radius = r2 * 0.18f, center = Offset(b2X - r2 * 0.30f, b2Y - r2 * 0.1f))
                        drawCircle(Color(0xFF1E293B), radius = r2 * 0.18f, center = Offset(b2X + r2 * 0.34f, b2Y - r2 * 0.1f))
                        drawCircle(Color.White, radius = r2 * 0.08f, center = Offset(b2X - r2 * 0.34f, b2Y - r2 * 0.15f))
                        drawCircle(Color.White, radius = r2 * 0.08f, center = Offset(b2X + r2 * 0.30f, b2Y - r2 * 0.15f))
                        // Cheerful open laughing mouth
                        drawArc(Color(0xFFFF4757), startAngle = 10f, sweepAngle = 160f, useCenter = true,
                            topLeft = Offset(b2X - r2 * 0.26f, b2Y + r2 * 0.12f),
                            size = Size(r2 * 0.52f, r2 * 0.45f)
                        )

                        // Ball 3: Mint/Cyan Happy (Right)
                        val b3X = cx + 75.dp.toPx()
                        val b3Y = cy + 12.dp.toPx()
                        val r3 = 34.dp.toPx()
                        drawCircle(Color(0xFF2ED573), radius = r3, center = Offset(b3X, b3Y))
                        drawCircle(Color(0x66FFFFFF), radius = r3 * 0.38f, center = Offset(b3X - r3 * 0.3f, b3Y - r3 * 0.3f))
                        // Cute closed smiling eyes (⌒ ‿ ⌒)
                        drawArc(Color(0xFF1E293B), startAngle = 190f, sweepAngle = 160f, useCenter = false,
                            topLeft = Offset(b3X - r3 * 0.50f, b3Y - r3 * 0.18f),
                            size = Size(r3 * 0.38f, r3 * 0.32f),
                            style = Stroke(width = 3.dp.toPx())
                        )
                        drawArc(Color(0xFF1E293B), startAngle = 190f, sweepAngle = 160f, useCenter = false,
                            topLeft = Offset(b3X + r3 * 0.12f, b3Y - r3 * 0.18f),
                            size = Size(r3 * 0.38f, r3 * 0.32f),
                            style = Stroke(width = 3.dp.toPx())
                        )
                        drawArc(Color(0xFF1E293B), startAngle = 20f, sweepAngle = 140f, useCenter = false,
                            topLeft = Offset(b3X - r3 * 0.24f, b3Y + r3 * 0.12f),
                            size = Size(r3 * 0.48f, r3 * 0.35f),
                            style = Stroke(width = 3.dp.toPx())
                        )
                    }
                }
            }

            // BUTTONS AREA: Big Juicy 3D Cartoon Buttons
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // 1. GIANT 3D GREEN PLAY BUTTON (Pulsating)
                Surface(
                    onClick = onPlay,
                    color = Color(0xFF10B981),
                    shape = RoundedCornerShape(32.dp),
                    border = BorderStroke(3.dp, Color(0xFF6EE7B7)),
                    shadowElevation = 12.dp,
                    modifier = Modifier
                        .scale(playScale)
                        .fillMaxWidth(0.82f)
                        .height(72.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color(0xFF34D399), Color(0xFF059669))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "▶  PLAY NOW!",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                        }
                    }
                }

                // 2. 3D ORANGE LEVEL SELECT BUTTON
                Surface(
                    onClick = onLevelSelect,
                    color = Color(0xFFF59E0B),
                    shape = RoundedCornerShape(26.dp),
                    border = BorderStroke(2.dp, Color(0xFFFDE68A)),
                    shadowElevation = 8.dp,
                    modifier = Modifier
                        .fillMaxWidth(0.82f)
                        .height(58.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color(0xFFFBBF24), Color(0xFFD97706))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "🗺️  100 LEVELS",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }
                }

                // 3. 3D BLUE SETTINGS BUTTON
                Surface(
                    onClick = onSettings,
                    color = Color(0xFF0284C7),
                    shape = RoundedCornerShape(26.dp),
                    border = BorderStroke(2.dp, Color(0xFFBAE6FD)),
                    shadowElevation = 6.dp,
                    modifier = Modifier
                        .fillMaxWidth(0.82f)
                        .height(54.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color(0xFF38BDF8), Color(0xFF0284C7))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "⚙️  SETTINGS & SOUND",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            // FOOTER: Friendly reassurance
            Text(
                text = "✨ 100 UNIQUE PUZZLES • FUN FOR ALL AGES ✨",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF78350F),
                textAlign = TextAlign.Center
            )
        }
    }
}
