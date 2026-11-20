# 🏖️ Sand Adventure – Android Native (Kotlin 2.0 + Jetpack Compose + SurfaceView)

> **Zero Ads • 100% Offline • Child-Friendly • Dual-Native Architecture**  
> Inspired by the classic physics-based sand digging genre (*Sand Balls*).

---

## 🌟 Key Features

- **🎮 Dual-Layer Architecture**:
  - **Jetpack Compose (Material 3)** for high-contrast, child-friendly cartoon UI (Main Menu, 100 Level Select, Settings, HUD).
  - **Native SurfaceView + Hardware Canvas** for 60+ FPS liquid ball cascade physics and sand carving.
- **🏝️ 100 Unique Levels Across 10 Themed Worlds**:
  1. 🏖️ **Sunny Beach (1–10)**: Tutorial ramps, gentle curves, intro to 2X gates, mini windmills, pinball mushroom.
  2. 🍬 **Candy Dunes (11–20)**: Y-split fork routes, double multiplier gates, teleport portals.
  3. 🌿 **Emerald Meadow (21–30)**: Motorized rotating paddles with alternating directions (+45° / -45°).
  4. 🍧 **Ice Cream Peak (31–40)**: Multi-bumper pinball cascades with high restitution (1.8x).
  5. 🌈 **Rainbow Canyon (41–50)**: Multiplier cascades (2X / 3X) with mega ball swarms (100+ rainbow balls).
  6. 💣 **TNT Quarry (51–60)**: Explosive TNT toy crates with blast craters and celebration star bursts.
  7. 🎡 **Carnival Park (61–70)**: Dual rotating wheels, high-speed chutes, bouncy bumpers.
  8. 🏰 **Sand Castle (71–80)**: Royal castle chambers, secret teleport shortcuts.
  9. 🌌 **Space Portals (81–90)**: Interstellar criss-crossing portal networks.
  10. 👑 **Master Island (91–100)**: Grand multi-stage puzzles combining all interactive mechanics.
- **✨ Interactive Gameplay Elements**:
  - **Multiplier Gates (2X / 3X)**: Clones passing balls with momentum.
  - **Portal Pairs (Teleporters)**: Teleports balls from entrance to exit with a speed boost.
  - **Bouncy Bumpers**: Pinball mushroom caps that pulse and launch balls outward.
  - **TNT Toy Crates**: Explodes on contact, blows open a large crater in the sand terrain.
  - **Rotating Windmill Paddles**: Continuous motor speed deflecting balls.
  - **Dormant White Ball Infection**: Contacting colored balls awakens dormant balls with playful pop sounds.
- **🎵 CD-Quality Audio Experience**:
  - 19 high-fidelity audio tracks (44.1kHz 16-bit PCM WAV).
  - Dynamic world-themed background music (`bgm_gameplay_kids.wav`, `bgm_candy_world.wav`, `bgm_jungle_fun.wav`, `bgm_icecream_waltz.wav`).
  - Joyful victory fanfares, friendly truck horn, and kids cheering audio.
- **🛡️ 100% Zero Ads & Child Safe**:
  - No AdMob, Unity Ads, AppLovin, or third-party tracking SDKs.
  - No internet connection required — runs completely offline.
  - No in-app purchases or account requirements.

---

## 🛠️ Tech Stack & Requirements

- **Language**: Kotlin 2.0.0
- **Android Target**: Android 14 (API 34)
- **Minimum SDK**: Android 8.0 (API 26)
- **UI Toolkit**: Jetpack Compose 1.6.8 + Material 3
- **Rendering**: Android SurfaceView + Hardware Accelerated Canvas
- **Physics**: Custom Deterministic Physics Engine (Verlet Integration + Spatial Hash Grid + Sub-stepping)
- **Terrain Engine**: Real-time BitSet Grid Tunnel Carving + Masking
- **Build Tool**: Gradle 8.4 + Android Gradle Plugin 8.3.2

---

## 🚀 Getting Started

### 1. Clone the repository
```bash
git clone https://github.com/minhlai1123/sandball-androin.git
cd sandball-androin
```

### 2. Build the project
```bash
./gradlew assembleDebug
```
The compiled APK will be generated at:
```
app/build/outputs/apk/debug/app-debug.apk
```

### 3. Run Unit Tests
```bash
./gradlew testDebugUnitTest
```
Runs 100% of unit tests verifying all 100 level files, JSON parsing, physics, and progress saving.

### 4. Install onto Device
```bash
adb install -r -d app/build/outputs/apk/debug/app-debug.apk
```

---

## 📁 Project Structure

```
.
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/antigravity/sandadventure/
│   │   │   │   ├── data/             # LevelModel, ProgressManager, WorldTheme
│   │   │   │   ├── gameplay/         # GameEngine, GameSurfaceView, AudioManager, SceneManager, InputHandler
│   │   │   │   ├── physics/          # PhysicsEngine, Ball, CollisionBody, SpatialHashGrid, Vector2D
│   │   │   │   ├── terrain/          # SandTerrain, CapsuleCarver
│   │   │   │   └── ui/               # MainMenuScreen, LevelSelectScreen, GameHUD, GameScreen, SettingsScreen
│   │   │   ├── assets/
│   │   │   │   ├── levels/           # 100 Level JSON definitions (level_1.json to level_100.json)
│   │   │   │   └── audio/            # 19 CD-quality WAV sound effects and world BGMs
│   │   │   └── AndroidManifest.xml
│   │   └── test/java/com/antigravity/sandadventure/
│   │       ├── LevelParsingTest.kt   # Integrity testing for all 100 levels
│   │       └── PhysicsEngineTest.kt  # Physics & collision math tests
│   └── build.gradle.kts
├── gradle/
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
└── README.md
```

---

## 📜 License
Developed as a dual-native educational & casual gaming project. Free for personal and educational use. Zero tracking, zero ads.
