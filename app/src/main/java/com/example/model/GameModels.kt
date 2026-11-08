package com.example.model

import androidx.compose.ui.graphics.Color

enum class BreadType(
    val id: String,
    val displayName: String,
    val description: String,
    val crustColor: Color,
    val crumbColor: Color,
    val toastedCrumbColor: Color,
    val fragility: Float,      // Higher = easier to rip
    val spreadFriction: Float, // Resistance when spreading butter (0.5 to 1.5)
    val unlockStars: Int,
    val unlockCoins: Int
) {
    WHITE(
        id = "white",
        displayName = "Classic White",
        description = "Standard breakfast toast. Balanced friction & moderate softness.",
        crustColor = Color(0xFF8B4513),
        crumbColor = Color(0xFFFFF8DC),
        toastedCrumbColor = Color(0xFFDEB887),
        fragility = 0.30f,
        spreadFriction = 0.9f,
        unlockStars = 0,
        unlockCoins = 0
    ),
    BRIOCHE(
        id = "brioche",
        displayName = "Golden Brioche",
        description = "Delicate & light. Very fragile; melts butter rapidly but tears easily.",
        crustColor = Color(0xFF6E260E),
        crumbColor = Color(0xFFFFECC0),
        toastedCrumbColor = Color(0xFFE89A3C),
        fragility = 0.70f,
        spreadFriction = 0.6f,
        unlockStars = 3,
        unlockCoins = 150
    ),
    SOURDOUGH(
        id = "sourdough",
        displayName = "Rustic Sourdough",
        description = "Chewy with deep air pockets. Takes firm knife pressure to butter.",
        crustColor = Color(0xFF5D2E12),
        crumbColor = Color(0xFFFDF5E6),
        toastedCrumbColor = Color(0xFFD2A679),
        fragility = 0.20f,
        spreadFriction = 1.25f,
        unlockStars = 7,
        unlockCoins = 300
    ),
    RYE(
        id = "rye",
        displayName = "Hearty Dark Rye",
        description = "Dense grain bread. High friction, but virtually tear-proof.",
        crustColor = Color(0xFF3E2723),
        crumbColor = Color(0xFF8D6E63),
        toastedCrumbColor = Color(0xFF6D4C41),
        fragility = 0.10f,
        spreadFriction = 1.4f,
        unlockStars = 12,
        unlockCoins = 500
    ),
    BAGEL(
        id = "bagel",
        displayName = "Toasted Bagel",
        description = "Round bagel half with center hole. Smooth, glossy crust edge.",
        crustColor = Color(0xFF795548),
        crumbColor = Color(0xFFFFF9E6),
        toastedCrumbColor = Color(0xFFE0B07A),
        fragility = 0.15f,
        spreadFriction = 1.0f,
        unlockStars = 18,
        unlockCoins = 750
    ),
    WAFFLE(
        id = "waffle",
        displayName = "Crispy Waffle",
        description = "Square pocket grid. High butter holding capacity.",
        crustColor = Color(0xFF964B00),
        crumbColor = Color(0xFFFFE082),
        toastedCrumbColor = Color(0xFFFFB300),
        fragility = 0.35f,
        spreadFriction = 1.1f,
        unlockStars = 24,
        unlockCoins = 1000
    ),
    CERAMIC_OVAL(
        id = "ceramic_oval",
        displayName = "Ceramic Open-Oval Plate",
        description = "Artisanal glazed porcelain serving ring. Smooth curved rim with a center cutout and bottom opening gap.",
        crustColor = Color(0xFFCBD5E1),
        crumbColor = Color(0xFFF8FAFC),
        toastedCrumbColor = Color(0xFFE2E8F0),
        fragility = 0.05f,
        spreadFriction = 0.70f,
        unlockStars = 30,
        unlockCoins = 1200
    );

    companion object {
        fun fromId(id: String): BreadType = entries.firstOrNull { it.id == id } ?: WHITE
    }
}

enum class ButterType(
    val id: String,
    val displayName: String,
    val description: String,
    val solidColor: Color,
    val meltedColor: Color,
    val shineColor: Color,
    val hasFlecks: Boolean = false,
    val fleckColor: Color = Color.Transparent
) {
    SALTED_CLASSIC(
        id = "salted",
        displayName = "Farmhouse Salted",
        description = "Golden cream butter with fine sea salt crystals.",
        solidColor = Color(0xFFFFE066),
        meltedColor = Color(0xFFFFC107),
        shineColor = Color(0xFFFFF9C4)
    ),
    GARLIC_HERB(
        id = "garlic",
        displayName = "Garlic Herb Butter",
        description = "Infused with crushed garlic cloves and fresh parsley.",
        solidColor = Color(0xFFE8F5E9),
        meltedColor = Color(0xFFC8E6C9),
        shineColor = Color(0xFFF1F8E9),
        hasFlecks = true,
        fleckColor = Color(0xFF2E7D32)
    ),
    HONEY_CINNAMON(
        id = "honey",
        displayName = "Honey Cinnamon",
        description = "Whipped clover honey and ground Ceylon cinnamon.",
        solidColor = Color(0xFFFFD54F),
        meltedColor = Color(0xFFFFB74D),
        shineColor = Color(0xFFFFF3E0),
        hasFlecks = true,
        fleckColor = Color(0xFF8D6E63)
    ),
    SWEET_BERRY(
        id = "berry",
        displayName = "Whipped Strawberry",
        description = "Pink summer strawberry purée churned into fluffy butter.",
        solidColor = Color(0xFFFF80AB),
        meltedColor = Color(0xFFF06292),
        shineColor = Color(0xFFFCE4EC),
        hasFlecks = true,
        fleckColor = Color(0xFFC2185B)
    ),
    TRUFFLE_LUXE(
        id = "truffle",
        displayName = "Black Truffle Gold",
        description = "French black summer truffle butter for gourmet toasts.",
        solidColor = Color(0xFFFFF8E1),
        meltedColor = Color(0xFFFFE082),
        shineColor = Color(0xFFFFFDE7),
        hasFlecks = true,
        fleckColor = Color(0xFF212121)
    ),
    BANANA_MAYO(
        id = "banana_mayo",
        displayName = "Creamy Mayo & Banana Glaze",
        description = "Vintage Southern classic: rich creamy mayonnaise with sweet banana slices.",
        solidColor = Color(0xFFFFFEE4),
        meltedColor = Color(0xFFFFF9C4),
        shineColor = Color(0xFFFFFFFF),
        hasFlecks = true,
        fleckColor = Color(0xFFFDD835)
    ),
    AVOCADO_CREAM(
        id = "avocado_cream",
        displayName = "Zesty Avocado Mash",
        description = "Whipped Haas avocado with lime zest and cracked pepper.",
        solidColor = Color(0xFFC5E1A5),
        meltedColor = Color(0xFFAED581),
        shineColor = Color(0xFFDCEDC8),
        hasFlecks = true,
        fleckColor = Color(0xFF33691E)
    ),
    VEGEMITE(
        id = "vegemite",
        displayName = "Aussie Vegemite Spread",
        description = "Thick, glossy dark brown savoury yeast extract paste. Bold aroma and smooth spreadability.",
        solidColor = Color(0xFF2E1C0C),
        meltedColor = Color(0xFF1E1106),
        shineColor = Color(0xFF4E3620),
        hasFlecks = true,
        fleckColor = Color(0xFF5D4037)
    );

    companion object {
        fun fromId(id: String): ButterType = entries.firstOrNull { it.id == id } ?: SALTED_CLASSIC
    }
}

enum class KnifeSkin(
    val id: String,
    val displayName: String,
    val description: String,
    val bladeLength: Float,   // Affects reach & blade contact sweep
    val bladeWidth: Float,    // Affects coverage area per stroke
    val controlFactor: Float, // Affects response and tearing resistance
    val bladeColor: Color,
    val handleColor: Color,
    val accentColor: Color,
    val unlockStars: Int,
    val unlockCoins: Int
) {
    CLASSIC_SILVER(
        id = "classic",
        displayName = "Bistro Butter Knife",
        description = "Reliable stainless steel knife with standard rounded spreader tip.",
        bladeLength = 135f,
        bladeWidth = 26f,
        controlFactor = 1.0f,
        bladeColor = Color(0xFFE0E0E0),
        handleColor = Color(0xFF5D4037),
        accentColor = Color(0xFFB0BEC5),
        unlockStars = 0,
        unlockCoins = 0
    ),
    WOODEN_SPREADER(
        id = "wood",
        displayName = "Artisan Beechwood Spreader",
        description = "Extra-wide curved paddle. Huge coverage per swipe, gentle on bread.",
        bladeLength = 165f,
        bladeWidth = 46f,
        controlFactor = 1.35f,
        bladeColor = Color(0xFFD7CCC8),
        handleColor = Color(0xFF8D6E63),
        accentColor = Color(0xFFA1887F),
        unlockStars = 4,
        unlockCoins = 200
    ),
    FRENCH_PALETTE(
        id = "french",
        displayName = "Offset Pastry Spatula",
        description = "Extra-long flexible blade. Effortless even glide across large toasts.",
        bladeLength = 190f,
        bladeWidth = 28f,
        controlFactor = 1.40f,
        bladeColor = Color(0xFFECEFF1),
        handleColor = Color(0xFF212121),
        accentColor = Color(0xFF90A4AE),
        unlockStars = 9,
        unlockCoins = 400
    ),
    PRECISION_NEEDLE(
        id = "needle",
        displayName = "Precision Palette",
        description = "Slim tapered tip for surgical buttering into waffle pockets & corners.",
        bladeLength = 150f,
        bladeWidth = 18f,
        controlFactor = 1.50f,
        bladeColor = Color(0xFFCFD8DC),
        handleColor = Color(0xFF37474F),
        accentColor = Color(0xFF78909C),
        unlockStars = 15,
        unlockCoins = 650
    ),
    GOLDEN_LUXE(
        id = "gold",
        displayName = "Royal 24k Gold",
        description = "Heated wide gold blade melts butter +25% faster for glistening perfection.",
        bladeLength = 165f,
        bladeWidth = 36f,
        controlFactor = 1.45f,
        bladeColor = Color(0xFFFFD700),
        handleColor = Color(0xFF3E2723),
        accentColor = Color(0xFFFFF176),
        unlockStars = 22,
        unlockCoins = 900
    ),
    RAINBOW_CHROME(
        id = "rainbow",
        displayName = "Prismatic Titanium",
        description = "Wide iridescent titanium finish. Grants +50% bonus coins per level!",
        bladeLength = 175f,
        bladeWidth = 40f,
        controlFactor = 1.60f,
        bladeColor = Color(0xFF80DEEA),
        handleColor = Color(0xFF7E57C2),
        accentColor = Color(0xFFFF4081),
        unlockStars = 28,
        unlockCoins = 1200
    );

    companion object {
        fun fromId(id: String): KnifeSkin = entries.firstOrNull { it.id == id } ?: CLASSIC_SILVER
    }
}

enum class PowerUpType(
    val id: String,
    val displayName: String,
    val description: String,
    val durationSeconds: Float,
    val iconColor: Color,
    val shopCost: Int
) {
    WIDE_KNIFE(
        id = "wide_knife",
        displayName = "Wide Butter Knife",
        description = "Massively expands knife width by 1.8x to cover huge toast surface!",
        durationSeconds = 12f,
        iconColor = Color(0xFFFFB300),
        shopCost = 75
    ),
    AUTO_SPREAD(
        id = "auto_spread",
        displayName = "Auto-Spread Magic",
        description = "Magically swirls and glides butter across unbuttered toast automatically!",
        durationSeconds = 7f,
        iconColor = Color(0xFF4CAF50),
        shopCost = 100
    ),
    INFINITE_BUTTER(
        id = "infinite_butter",
        displayName = "Endless Butter Flow",
        description = "Blade flows with non-stop golden butter for 10s — never runs dry!",
        durationSeconds = 10f,
        iconColor = Color(0xFFFFD700),
        shopCost = 90
    ),
    NO_MESS_SHIELD(
        id = "no_mess_shield",
        displayName = "No-Mess Shield",
        description = "Aura prevents point deductions for edge spills, tears, or dropped knives!",
        durationSeconds = 15f,
        iconColor = Color(0xFF00BCD4),
        shopCost = 80
    ),
    GOLDEN_MELT(
        id = "golden_melt",
        displayName = "Golden Sizzler",
        description = "Warms butter immediately, turning stiff butter into silky golden glaze!",
        durationSeconds = 10f,
        iconColor = Color(0xFFFF5722),
        shopCost = 70
    ),
    BUTTER_MAGNET(
        id = "butter_magnet",
        displayName = "Butter Magnet",
        description = "Attracts floating tokens and draws butter smoothly into empty toast spots!",
        durationSeconds = 12f,
        iconColor = Color(0xFFE040FB),
        shopCost = 85
    );

    companion object {
        fun fromId(id: String): PowerUpType = entries.firstOrNull { it.id == id } ?: WIDE_KNIFE
    }
}

data class Level(
    val id: Int,
    val title: String,
    val subtitle: String,
    val hint: String,
    val breadType: BreadType,
    val butterType: ButterType,
    val patCount: Int,
    val butterHardness: Float, // 0.0 (soft) to 1.0 (cold/hard)
    val toastTemp: Float,      // 0.0 (cool) to 1.0 (piping hot)
    val targetCoverage: Float, // e.g. 0.65 = 65%
    val targetEvenness: Float, // e.g. 0.50 = 50%
    val maxTearsAllowed: Int,
    val timeLimitSeconds: Int = 45,
    val star1Coverage: Float = 0.50f,
    val star2Coverage: Float = 0.70f,
    val star3Coverage: Float = 0.85f,
    val isHapticOnly: Boolean = false,
    val isBusRide: Boolean = false,
    val hasBananas: Boolean = false,
    val isSmallBoard: Boolean = false,
    val isCeramicPlate: Boolean = false
)

data class PointUpgrade(
    val id: String,
    val name: String,
    val category: String, // "Blade Mastery", "Technique", "Perks", "Currency Exchange"
    val description: String,
    val pointCost: Int,
    val isRepeatable: Boolean = false
) {
    companion object {
        val ALL_UPGRADES: List<PointUpgrade> = listOf(
            PointUpgrade(
                id = "blade_hone",
                name = "Diamond Blade Hone",
                category = "Blade Mastery",
                description = "+25% knife control & precision stroke stability on all knives",
                pointCost = 1200
            ),
            PointUpgrade(
                id = "thermal_edge",
                name = "Thermal Edge",
                category = "Blade Mastery",
                description = "Warms cold butter +30% faster during spreading",
                pointCost = 2000
            ),
            PointUpgrade(
                id = "wide_sweep",
                name = "Wide Sweep Technique",
                category = "Technique",
                description = "Expands active butter deposit swath by +20%",
                pointCost = 2800
            ),
            PointUpgrade(
                id = "gentle_touch",
                name = "Velvet Crumb Shield",
                category = "Technique",
                description = "Reduces bread tear sensitivity by 50% on delicate toasts",
                pointCost = 3500
            ),
            PointUpgrade(
                id = "butter_reservoir",
                name = "Deep Butter Reservoir",
                category = "Blade Mastery",
                description = "Knife edge holds up to 1.15x butter volume per pat slice",
                pointCost = 4500
            ),
            PointUpgrade(
                id = "coin_multiplier",
                name = "Golden Toast Mastery",
                category = "Perks",
                description = "Permanently doubles all bakery coins earned from level completions!",
                pointCost = 6000
            ),
            PointUpgrade(
                id = "coin_pack_small",
                name = "Coin Exchange (150 Coins)",
                category = "Currency Exchange",
                description = "Exchange 600 score points for 150 Bakery Coins",
                pointCost = 600,
                isRepeatable = true
            ),
            PointUpgrade(
                id = "coin_pack_large",
                name = "Coin Exchange (500 Coins)",
                category = "Currency Exchange",
                description = "Exchange 1,800 score points for 500 Bakery Coins",
                pointCost = 1800,
                isRepeatable = true
            ),
            PointUpgrade(
                id = "powerup_bundle",
                name = "Chef's Power-Up Box (2x Random)",
                category = "Perks",
                description = "Receive 2 random power-ups to use during levels",
                pointCost = 800,
                isRepeatable = true
            ),
            PointUpgrade(
                id = "steady_hand",
                name = "Steady Hand Grip",
                category = "Blade Mastery",
                description = "Completely eliminates score penalties from dropping the knife off the board",
                pointCost = 2400
            ),
            PointUpgrade(
                id = "speed_spreader",
                name = "Speed Spreader",
                category = "Technique",
                description = "Slows score decay over time by 50% across all levels",
                pointCost = 3200
            ),
            PointUpgrade(
                id = "golden_pat_luck",
                name = "Golden Pat Magnetism",
                category = "Perks",
                description = "Spawns bonus floating power-up tokens 50% more frequently",
                pointCost = 4000
            ),
            PointUpgrade(
                id = "coin_pack_mega",
                name = "Mega Coin Exchange (1,200 Coins)",
                category = "Currency Exchange",
                description = "Exchange 3,500 score points for 1,200 Bakery Coins",
                pointCost = 3500,
                isRepeatable = true
            )
        )
    }
}

data class PowerUpToken(
    val id: Long,
    val type: PowerUpType,
    var x: Float, // Normalized 0..1 in toast space
    var y: Float,
    var pulse: Float = 0f,
    var collected: Boolean = false
)

data class ScoreFloatingPopup(
    val id: Long,
    val text: String,
    val x: Float,
    val y: Float,
    val isPositive: Boolean,
    var alpha: Float = 1.0f,
    var offsetY: Float = 0f
)

data class GameResult(
    val levelId: Int,
    val finalScore: Int,
    val initialScore: Int,
    val timeScoreLoss: Int,
    val boundaryPenalties: Int,
    val tearPenalties: Int,
    val dropPenalties: Int,
    val perfectionBonus: Int,
    val coverage: Float,
    val evenness: Float,
    val tearsCount: Int,
    val stars: Int,
    val coinsEarned: Int,
    val passed: Boolean,
    val ratingTitle: String,
    val feedbackMessage: String
)
