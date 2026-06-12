package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "player_state")
data class PlayerState(
    @PrimaryKey val id: Int = 1,
    val name: String = "نامشخص",
    val isAwakened: Boolean = false,
    val level: Int = 1,
    val xp: Int = 0,
    val gold: Int = 1000,
    val rank: String = "E Rank",
    val statPoints: Int = 5,
    val strength: Int = 10,
    val agility: Int = 10,
    val endurance: Int = 10,
    val discipline: Int = 10,
    val knowledge: Int = 10,
    val charisma: Int = 10,
    val mentalPower: Int = 10,
    val streak: Int = 0,
    val lastActiveDate: String = "",
    val showPenaltyScreen: Boolean = false,
    val currentAvatarSkin: String = "ردا و لباس شکارچی ساده",
    val equippedGloves: String? = null,
    val equippedShoes: String? = null,
    val equippedBelt: String? = null,
    val equippedCrown: String? = null,
    val selectedTitle: String? = "شکارچی تازه‌کار",
    val selectedTheme: String = "DarkCosmic",
    val guildName: String? = null,
    val guildRole: String? = null,
    val activeSkillWarriorLevel: Int = 0,
    val activeSkillAthleteLevel: Int = 0,
    val activeSkillLeaderLevel: Int = 0,
    val activeSkillScholarLevel: Int = 0,
    val activeSkillShadowLevel: Int = 0
)

@Entity(tableName = "inventory_item")
data class InventoryItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val type: String, // "Equipable", "Key", "Ticket", "Potion", "Booster", "Title"
    val iconName: String,
    val quantity: Int = 1,
    val rarity: String = "Common", // "Common", "Rare", "Epic", "Legendary", "Mythic"
    val attributeBonus: String? = null,
    val bonusValue: Int = 0,
    val isEquipped: Boolean = false,
    val description: String = ""
)

@Entity(tableName = "quest")
data class Quest(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val type: String, // "Daily", "Hidden"
    val xpReward: Int,
    val goldReward: Int,
    val difficulty: String, // "Easy", "Medium", "Hard"
    val isCompleted: Boolean = false,
    val isPenaltyActivated: Boolean = false,
    val date: String = ""
)

@Entity(tableName = "dungeon")
data class Dungeon(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val type: String, // "Strength", "Discipline", "Mental", "Shadow", "Training"
    val levelRequired: Int,
    val keyTypeRequired: String?, // keys name or null
    val timerSeconds: Int,
    val isCleared: Boolean = false,
    val questSteps: String, // semi-colon separated Persian step list
    val xpReward: Int,
    val goldReward: Int,
    val customSuccessMessage: String = ""
)

@Entity(tableName = "achievement")
data class Achievement(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val progress: Int = 0,
    val maxProgress: Int,
    val isUnlocked: Boolean = false,
    val rewardGold: Int = 200,
    val rewardTitle: String? = null
)

@Entity(tableName = "guild_state")
data class GuildState(
    @PrimaryKey val id: Int = 1,
    val name: String = "بدون صنف",
    val level: Int = 1,
    val xp: Int = 0,
    val memberCount: Int = 1,
    val multiplier: Double = 1.0
)
