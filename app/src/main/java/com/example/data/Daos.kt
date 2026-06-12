package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PlayerDao {
    @Query("SELECT * FROM player_state WHERE id = 1")
    fun getPlayerState(): Flow<PlayerState?>

    @Query("SELECT * FROM player_state WHERE id = 1")
    suspend fun getPlayerStateDirect(): PlayerState?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlayerState(playerState: PlayerState)

    // Inventory
    @Query("SELECT * FROM inventory_item")
    fun getInventoryItems(): Flow<List<InventoryItem>>

    @Query("SELECT * FROM inventory_item")
    suspend fun getInventoryItemsDirect(): List<InventoryItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInventoryItem(item: InventoryItem)

    @Update
    suspend fun updateInventoryItem(item: InventoryItem)

    @Delete
    suspend fun deleteInventoryItem(item: InventoryItem)

    // Quests
    @Query("SELECT * FROM quest ORDER BY id DESC")
    fun getQuests(): Flow<List<Quest>>

    @Query("SELECT * FROM quest ORDER BY id DESC")
    suspend fun getQuestsDirect(): List<Quest>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuest(quest: Quest)

    @Update
    suspend fun updateQuest(quest: Quest)

    @Query("DELETE FROM quest")
    suspend fun clearQuests()

    // Dungeons
    @Query("SELECT * FROM dungeon")
    fun getDungeons(): Flow<List<Dungeon>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDungeons(dungeons: List<Dungeon>)

    @Update
    suspend fun updateDungeon(dungeon: Dungeon)

    // Achievements
    @Query("SELECT * FROM achievement")
    fun getAchievements(): Flow<List<Achievement>>

    @Query("SELECT * FROM achievement")
    suspend fun getAchievementsDirect(): List<Achievement>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAchievements(achievements: List<Achievement>)

    @Update
    suspend fun updateAchievement(achievement: Achievement)

    // Guild
    @Query("SELECT * FROM guild_state WHERE id = 1")
    fun getGuildState(): Flow<GuildState?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGuildState(guildState: GuildState)
}
