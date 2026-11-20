package com.antigravity.sandadventure.data

data class WorldTheme(
    val worldId: Int,
    val name: String,
    val emoji: String,
    val bgTopColor: Int,
    val bgBottomColor: Int,
    val sandColor: Int,
    val sandBorderColor: Int,
    val obstacleColor: Int,
    val obstacleStripeColor: Int = 0xFFFFFFFF.toInt()
)

fun getWorldTheme(worldId: Int): WorldTheme {
    return when (worldId) {
        1 -> WorldTheme(
            worldId = 1,
            name = "Sunny Beach",
            emoji = "🏖️",
            bgTopColor = 0xFFFFF8E7.toInt(),       // Sunny warm cream
            bgBottomColor = 0xFFFFECC2.toInt(),    // Warm peach cream
            sandColor = 0xFFFFD152.toInt(),        // Radiant golden beach sand
            sandBorderColor = 0xFFF39C12.toInt(),  // Golden amber rim
            obstacleColor = 0xFFFF4757.toInt()     // Candy watermelon red
        )
        2 -> WorldTheme(
            worldId = 2,
            name = "Candy Dunes",
            emoji = "🍬",
            bgTopColor = 0xFFFFF0F5.toInt(),       // Cotton candy lavender
            bgBottomColor = 0xFFFFE4E1.toInt(),    // Pastel marshmallow pink
            sandColor = 0xFFFF99C8.toInt(),        // Bubblegum strawberry sand
            sandBorderColor = 0xFFE056FD.toInt(),  // Violet candy rim
            obstacleColor = 0xFF00D2D3.toInt()     // Mint turquoise
        )
        3 -> WorldTheme(
            worldId = 3,
            name = "Emerald Meadow",
            emoji = "🌿",
            bgTopColor = 0xFFF0FFF4.toInt(),       // Fresh morning mint
            bgBottomColor = 0xFFE6FFFA.toInt(),    // Pastel jade
            sandColor = 0xFF7BED9F.toInt(),        // Bright lime grass sand
            sandBorderColor = 0xFF2ED573.toInt(),  // Emerald rim
            obstacleColor = 0xFFFFA502.toInt()     // Tangerine orange
        )
        4 -> WorldTheme(
            worldId = 4,
            name = "Ice Cream Peak",
            emoji = "🍧",
            bgTopColor = 0xFFF0F8FF.toInt(),       // Pastel ice blue
            bgBottomColor = 0xFFE6F2FF.toInt(),    // Soft sky blue
            sandColor = 0xFF70A1FF.toInt(),        // Blueberry ice cream sand
            sandBorderColor = 0xFF3742FA.toInt(),  // Deep sapphire rim
            obstacleColor = 0xFFFF6B81.toInt()     // Strawberry swirl
        )
        5 -> WorldTheme(
            worldId = 5,
            name = "Rainbow Canyon",
            emoji = "🌈",
            bgTopColor = 0xFFFAF5FF.toInt(),       // Pastel purple
            bgBottomColor = 0xFFF3E8FF.toInt(),    // Violet cream
            sandColor = 0xFFFFA502.toInt(),        // Warm sunset amber
            sandBorderColor = 0xFFFF6348.toInt(),  // Coral rim
            obstacleColor = 0xFF9B59B6.toInt()     // Candy violet
        )
        6 -> WorldTheme(
            worldId = 6,
            name = "TNT Quarry",
            emoji = "💣",
            bgTopColor = 0xFFFFF5F5.toInt(),       // Warm blush cream
            bgBottomColor = 0xFFFED7D7.toInt(),    // Soft terra cream
            sandColor = 0xFFE17055.toInt(),        // Terracotta quarry sand
            sandBorderColor = 0xFFD63031.toInt(),  // Deep blast rim
            obstacleColor = 0xFFE67E22.toInt()     // Wooden orange
        )
        7 -> WorldTheme(
            worldId = 7,
            name = "Carnival Park",
            emoji = "🎡",
            bgTopColor = 0xFFFEF9C3.toInt(),       // Joyful butter yellow
            bgBottomColor = 0xFFBAE6FD.toInt(),    // Sky breeze blue
            sandColor = 0xFFF39C12.toInt(),        // Warm festival gold sand
            sandBorderColor = 0xFFD35400.toInt(),  // Sunset rim
            obstacleColor = 0xFF1ABC9C.toInt()     // Carnival teal
        )
        8 -> WorldTheme(
            worldId = 8,
            name = "Sand Castle",
            emoji = "🏰",
            bgTopColor = 0xFFF1F5F9.toInt(),       // Slate stone cream
            bgBottomColor = 0xFFFEF3C7.toInt(),    // Castle courtyard amber
            sandColor = 0xFFE59866.toInt(),        // Fort sand
            sandBorderColor = 0xFF2980B9.toInt(),  // Royal sapphire rim
            obstacleColor = 0xFF3498DB.toInt()     // Royal blue
        )
        9 -> WorldTheme(
            worldId = 9,
            name = "Space Portals",
            emoji = "🌌",
            bgTopColor = 0xFFEEF2FF.toInt(),       // Nebula twilight
            bgBottomColor = 0xFFC7D2FE.toInt(),    // Starlight purple
            sandColor = 0xFF8E44AD.toInt(),        // Cosmic amethyst sand
            sandBorderColor = 0xFF00CEC9.toInt(),  // Neon cyan glow rim
            obstacleColor = 0xFF00D2D3.toInt()     // Neon portal cyan
        )
        else -> WorldTheme(
            worldId = 10,
            name = "Master Island",
            emoji = "👑",
            bgTopColor = 0xFFFEF08A.toInt(),       // Royal gold cream
            bgBottomColor = 0xFFFEF9C3.toInt(),    // Crown yellow
            sandColor = 0xFFF59E0B.toInt(),        // Imperial gold sand
            sandBorderColor = 0xFF8E44AD.toInt(),  // Royal purple rim
            obstacleColor = 0xFF8E44AD.toInt()     // Crown violet
        )
    }
}
