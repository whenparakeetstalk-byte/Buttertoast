package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.BreadType
import com.example.model.KnifeSkin
import com.example.model.PointUpgrade
import com.example.model.PowerUpType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class GamePreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("butter_toast_prefs", Context.MODE_PRIVATE)

    private val _highestUnlockedLevel = MutableStateFlow(prefs.getInt(KEY_UNLOCKED_LEVEL, 1))
    val highestUnlockedLevel: StateFlow<Int> = _highestUnlockedLevel.asStateFlow()

    private val _coins = MutableStateFlow(prefs.getInt(KEY_COINS, 200)) // Starting bonus 200 coins
    val coins: StateFlow<Int> = _coins.asStateFlow()

    private val _spendablePoints = MutableStateFlow(loadInitialPoints())
    val spendablePoints: StateFlow<Int> = _spendablePoints.asStateFlow()

    private val _unlockedUpgrades = MutableStateFlow(loadUnlockedSet(KEY_UNLOCKED_UPGRADES, emptySet()))
    val unlockedUpgrades: StateFlow<Set<String>> = _unlockedUpgrades.asStateFlow()

    private val _selectedKnifeId = MutableStateFlow(prefs.getString(KEY_SELECTED_KNIFE, "classic") ?: "classic")
    val selectedKnifeId: StateFlow<String> = _selectedKnifeId.asStateFlow()

    private val _selectedBreadId = MutableStateFlow(prefs.getString(KEY_SELECTED_BREAD, "white") ?: "white")
    val selectedBreadId: StateFlow<String> = _selectedBreadId.asStateFlow()

    private val _soundEnabled = MutableStateFlow(prefs.getBoolean(KEY_SOUND_ENABLED, true))
    val soundEnabled: StateFlow<Boolean> = _soundEnabled.asStateFlow()

    private val _hapticsEnabled = MutableStateFlow(prefs.getBoolean(KEY_HAPTICS_ENABLED, true))
    val hapticsEnabled: StateFlow<Boolean> = _hapticsEnabled.asStateFlow()

    private val _totalStars = MutableStateFlow(calculateTotalStars())
    val totalStars: StateFlow<Int> = _totalStars.asStateFlow()

    private val _unlockedKnives = MutableStateFlow(loadUnlockedSet(KEY_UNLOCKED_KNIVES, setOf("classic")))
    val unlockedKnives: StateFlow<Set<String>> = _unlockedKnives.asStateFlow()

    private val _unlockedBreads = MutableStateFlow(loadUnlockedSet(KEY_UNLOCKED_BREADS, setOf("white")))
    val unlockedBreads: StateFlow<Set<String>> = _unlockedBreads.asStateFlow()

    private val _powerUpInventory = MutableStateFlow(loadPowerUpInventory())
    val powerUpInventory: StateFlow<Map<String, Int>> = _powerUpInventory.asStateFlow()

    private fun loadInitialPoints(): Int {
        val stored = prefs.getInt(KEY_SPENDABLE_POINTS, -1)
        if (stored >= 0) return stored
        // Backfill points from historical best scores so existing players have currency to spend right away!
        var total = 500
        for (level in LevelRepository.levels) {
            total += prefs.getInt("best_score_level_${level.id}", 0)
        }
        prefs.edit().putInt(KEY_SPENDABLE_POINTS, total).apply()
        return total
    }

    private fun loadUnlockedSet(key: String, default: Set<String>): Set<String> {
        val stored = prefs.getStringSet(key, null)
        return if (stored.isNullOrEmpty()) default else (stored + default)
    }

    private fun loadPowerUpInventory(): Map<String, Int> {
        val map = mutableMapOf<String, Int>()
        for (type in PowerUpType.entries) {
            // Give 1 free powerup of each to start the fun!
            val count = prefs.getInt("powerup_${type.id}", 1)
            map[type.id] = count
        }
        return map
    }

    fun getStarsForLevel(levelId: Int): Int {
        return prefs.getInt("stars_level_$levelId", 0)
    }

    fun getBestCoverageForLevel(levelId: Int): Float {
        return prefs.getFloat("best_coverage_level_$levelId", 0f)
    }

    fun getBestScoreForLevel(levelId: Int): Int {
        return prefs.getInt("best_score_level_$levelId", 0)
    }

    fun saveLevelResult(levelId: Int, stars: Int, coverage: Float, score: Int, coinsAwarded: Int) {
        val currentStars = getStarsForLevel(levelId)
        val currentBestCoverage = getBestCoverageForLevel(levelId)
        val currentBestScore = getBestScoreForLevel(levelId)

        val editor = prefs.edit()
        if (stars > currentStars) {
            editor.putInt("stars_level_$levelId", stars)
        }
        if (coverage > currentBestCoverage) {
            editor.putFloat("best_coverage_level_$levelId", coverage)
        }
        if (score > currentBestScore) {
            editor.putInt("best_score_level_$levelId", score)
        }

        // Add earned coins (doubled if Golden Toast Mastery is unlocked)
        val finalCoinsAwarded = if (hasUpgrade("coin_multiplier")) coinsAwarded * 2 else coinsAwarded
        val newCoins = _coins.value + finalCoinsAwarded
        editor.putInt(KEY_COINS, newCoins)
        _coins.value = newCoins

        // Add earned score points to spendable bank
        val newPoints = _spendablePoints.value + score
        editor.putInt(KEY_SPENDABLE_POINTS, newPoints)
        _spendablePoints.value = newPoints

        // Unlock next level if passed (at least 1 star)
        if (stars >= 1) {
            val nextLevel = levelId + 1
            val maxLevelId = LevelRepository.levels.maxOfOrNull { it.id } ?: 10
            if (nextLevel > _highestUnlockedLevel.value && nextLevel <= maxLevelId) {
                editor.putInt(KEY_UNLOCKED_LEVEL, nextLevel)
                _highestUnlockedLevel.value = nextLevel
            }
        }

        editor.apply()
        _totalStars.value = calculateTotalStars()

        // Auto-check unlock thresholds based on stars
        checkStarUnlocks()
    }

    private fun checkStarUnlocks() {
        val stars = _totalStars.value
        val editor = prefs.edit()
        var knivesChanged = false
        var breadsChanged = false

        val currentKnives = _unlockedKnives.value.toMutableSet()
        for (knife in KnifeSkin.entries) {
            if (knife.unlockStars > 0 && stars >= knife.unlockStars && !currentKnives.contains(knife.id)) {
                currentKnives.add(knife.id)
                knivesChanged = true
            }
        }

        val currentBreads = _unlockedBreads.value.toMutableSet()
        for (bread in BreadType.entries) {
            if (bread.unlockStars > 0 && stars >= bread.unlockStars && !currentBreads.contains(bread.id)) {
                currentBreads.add(bread.id)
                breadsChanged = true
            }
        }

        if (knivesChanged) {
            editor.putStringSet(KEY_UNLOCKED_KNIVES, currentKnives)
            _unlockedKnives.value = currentKnives
        }
        if (breadsChanged) {
            editor.putStringSet(KEY_UNLOCKED_BREADS, currentBreads)
            _unlockedBreads.value = currentBreads
        }
        editor.apply()
    }

    fun unlockKnifeWithCoins(knife: KnifeSkin): Boolean {
        if (_coins.value >= knife.unlockCoins) {
            val newCoins = _coins.value - knife.unlockCoins
            val currentKnives = _unlockedKnives.value.toMutableSet()
            currentKnives.add(knife.id)

            prefs.edit()
                .putInt(KEY_COINS, newCoins)
                .putStringSet(KEY_UNLOCKED_KNIVES, currentKnives)
                .apply()

            _coins.value = newCoins
            _unlockedKnives.value = currentKnives
            setSelectedKnife(knife.id)
            return true
        }
        return false
    }

    fun unlockBreadWithCoins(bread: BreadType): Boolean {
        if (_coins.value >= bread.unlockCoins) {
            val newCoins = _coins.value - bread.unlockCoins
            val currentBreads = _unlockedBreads.value.toMutableSet()
            currentBreads.add(bread.id)

            prefs.edit()
                .putInt(KEY_COINS, newCoins)
                .putStringSet(KEY_UNLOCKED_BREADS, currentBreads)
                .apply()

            _coins.value = newCoins
            _unlockedBreads.value = currentBreads
            setSelectedBread(bread.id)
            return true
        }
        return false
    }

    fun buyPowerUp(powerUp: PowerUpType): Boolean {
        if (_coins.value >= powerUp.shopCost) {
            val newCoins = _coins.value - powerUp.shopCost
            val currentCount = _powerUpInventory.value[powerUp.id] ?: 0
            val newCount = currentCount + 1

            prefs.edit()
                .putInt(KEY_COINS, newCoins)
                .putInt("powerup_${powerUp.id}", newCount)
                .apply()

            _coins.value = newCoins
            val newMap = _powerUpInventory.value.toMutableMap()
            newMap[powerUp.id] = newCount
            _powerUpInventory.value = newMap
            return true
        }
        return false
    }

    fun consumePowerUp(powerUp: PowerUpType): Boolean {
        val currentCount = _powerUpInventory.value[powerUp.id] ?: 0
        if (currentCount > 0) {
            val newCount = currentCount - 1
            prefs.edit().putInt("powerup_${powerUp.id}", newCount).apply()
            val newMap = _powerUpInventory.value.toMutableMap()
            newMap[powerUp.id] = newCount
            _powerUpInventory.value = newMap
            return true
        }
        return false
    }

    fun setSelectedKnife(knifeId: String) {
        prefs.edit().putString(KEY_SELECTED_KNIFE, knifeId).apply()
        _selectedKnifeId.value = knifeId
    }

    fun setSelectedBread(breadId: String) {
        prefs.edit().putString(KEY_SELECTED_BREAD, breadId).apply()
        _selectedBreadId.value = breadId
    }

    fun setSoundEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_SOUND_ENABLED, enabled).apply()
        _soundEnabled.value = enabled
    }

    fun setHapticsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_HAPTICS_ENABLED, enabled).apply()
        _hapticsEnabled.value = enabled
    }

    private fun calculateTotalStars(): Int {
        var total = 0
        for (level in LevelRepository.levels) {
            total += prefs.getInt("stars_level_${level.id}", 0)
        }
        return total
    }

    fun hasUpgrade(id: String): Boolean = _unlockedUpgrades.value.contains(id)

    fun buyUpgrade(upgrade: PointUpgrade): Boolean {
        if (_spendablePoints.value >= upgrade.pointCost) {
            val newPoints = _spendablePoints.value - upgrade.pointCost
            val editor = prefs.edit().putInt(KEY_SPENDABLE_POINTS, newPoints)
            _spendablePoints.value = newPoints

            if (upgrade.id == "coin_pack_small") {
                val newCoins = _coins.value + 150
                editor.putInt(KEY_COINS, newCoins)
                _coins.value = newCoins
            } else if (upgrade.id == "coin_pack_large") {
                val newCoins = _coins.value + 500
                editor.putInt(KEY_COINS, newCoins)
                _coins.value = newCoins
            } else if (upgrade.id == "coin_pack_mega") {
                val newCoins = _coins.value + 1200
                editor.putInt(KEY_COINS, newCoins)
                _coins.value = newCoins
            } else if (upgrade.id == "powerup_bundle") {
                // Grant 2 random power-ups
                val types = PowerUpType.entries.shuffled().take(2)
                val newMap = _powerUpInventory.value.toMutableMap()
                for (type in types) {
                    val count = (newMap[type.id] ?: 0) + 1
                    editor.putInt("powerup_${type.id}", count)
                    newMap[type.id] = count
                }
                _powerUpInventory.value = newMap
            } else if (!upgrade.isRepeatable) {
                val set = _unlockedUpgrades.value.toMutableSet()
                set.add(upgrade.id)
                editor.putStringSet(KEY_UNLOCKED_UPGRADES, set)
                _unlockedUpgrades.value = set
            }

            editor.apply()
            return true
        }
        return false
    }

    fun unlockKnifeWithPoints(knife: KnifeSkin, pointCost: Int): Boolean {
        if (_spendablePoints.value >= pointCost) {
            val newPoints = _spendablePoints.value - pointCost
            val currentKnives = _unlockedKnives.value.toMutableSet()
            currentKnives.add(knife.id)

            prefs.edit()
                .putInt(KEY_SPENDABLE_POINTS, newPoints)
                .putStringSet(KEY_UNLOCKED_KNIVES, currentKnives)
                .apply()

            _spendablePoints.value = newPoints
            _unlockedKnives.value = currentKnives
            setSelectedKnife(knife.id)
            return true
        }
        return false
    }

    fun unlockBreadWithPoints(bread: BreadType, pointCost: Int): Boolean {
        if (_spendablePoints.value >= pointCost) {
            val newPoints = _spendablePoints.value - pointCost
            val currentBreads = _unlockedBreads.value.toMutableSet()
            currentBreads.add(bread.id)

            prefs.edit()
                .putInt(KEY_SPENDABLE_POINTS, newPoints)
                .putStringSet(KEY_UNLOCKED_BREADS, currentBreads)
                .apply()

            _spendablePoints.value = newPoints
            _unlockedBreads.value = currentBreads
            setSelectedBread(bread.id)
            return true
        }
        return false
    }

    companion object {
        private const val KEY_UNLOCKED_LEVEL = "key_unlocked_level"
        private const val KEY_COINS = "key_coins"
        private const val KEY_SPENDABLE_POINTS = "key_spendable_points"
        private const val KEY_UNLOCKED_UPGRADES = "key_unlocked_upgrades"
        private const val KEY_SELECTED_KNIFE = "key_selected_knife"
        private const val KEY_SELECTED_BREAD = "key_selected_bread"
        private const val KEY_UNLOCKED_KNIVES = "key_unlocked_knives"
        private const val KEY_UNLOCKED_BREADS = "key_unlocked_breads"
        private const val KEY_SOUND_ENABLED = "key_sound_enabled"
        private const val KEY_HAPTICS_ENABLED = "key_haptics_enabled"
    }
}
