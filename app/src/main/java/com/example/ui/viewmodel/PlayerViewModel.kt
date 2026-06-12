package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.random.Random

class PlayerViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = PlayerRepository(db.playerDao())

    val playerState: StateFlow<PlayerState?> = repository.playerState
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val inventoryItems: StateFlow<List<InventoryItem>> = repository.inventoryItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val quests: StateFlow<List<Quest>> = repository.quests
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val dungeons: StateFlow<List<Dungeon>> = repository.dungeons
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val achievements: StateFlow<List<Achievement>> = repository.achievements
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val guildState: StateFlow<GuildState?> = repository.guildState
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // UI States
    private val _isLootBoxOpening = MutableStateFlow(false)
    val isLootBoxOpening: StateFlow<Boolean> = _isLootBoxOpening.asStateFlow()

    private val _lootBoxResult = MutableStateFlow<LootBoxReward?>(null)
    val lootBoxResult: StateFlow<LootBoxReward?> = _lootBoxResult.asStateFlow()

    // Active Dungeon Tracker
    private val _activeDungeon = MutableStateFlow<Dungeon?>(null)
    val activeDungeon: StateFlow<Dungeon?> = _activeDungeon.asStateFlow()

    private val _activeDungeonTimeLeft = MutableStateFlow(0)
    val activeDungeonTimeLeft: StateFlow<Int> = _activeDungeonTimeLeft.asStateFlow()

    private val _activeDungeonStepsProgress = MutableStateFlow<List<Boolean>>(emptyList())
    val activeDungeonStepsProgress: StateFlow<List<Boolean>> = _activeDungeonStepsProgress.asStateFlow()

    private var dungeonTimerJob: Job? = null

    // Hidden Quests Notifications
    private val _hiddenQuestMessage = MutableStateFlow<String?>(null)
    val hiddenQuestMessage: StateFlow<String?> = _hiddenQuestMessage.asStateFlow()

    init {
        viewModelScope.launch {
            repository.initializeDatabaseIfEmpty()
            repository.checkDailyPenalty()
        }
    }

    // Player Intentions
    fun awakenPlayer(nameInput: String) {
        viewModelScope.launch {
            val current = playerState.value ?: return@launch
            val updated = current.copy(
                name = nameInput.ifBlank { "نامشخص" },
                isAwakened = true,
                lastActiveDate = repository.getTodayDateString()
            )
            repository.updatePlayerState(updated)
            // Seed a starter Guild with the player name!
            repository.updatePlayerState(updated)
        }
    }

    fun renouncePlayer() {
        // Will trigger the GAME OVER loop if player chooses "NO" in intro
    }

    fun distributeStatPoint(statName: String) {
        viewModelScope.launch {
            repository.distributeStatPoint(statName)
        }
    }

    fun completeQuestItem(quest: Quest) {
        viewModelScope.launch {
            val player = playerState.value ?: return@launch
            repository.completeQuest(quest, player)
            // Trigger 10% chance of triggering a random Hidden Quest immediately!
            if (Random.nextFloat() < 0.25f && quest.type == "Daily") {
                triggerRandomHiddenQuest()
            }
        }
    }

    private fun triggerRandomHiddenQuest() {
        viewModelScope.launch {
            val activeQuests = quests.value
            val alreadyHasHidden = activeQuests.any { it.type == "Hidden" && !it.isCompleted }
            if (alreadyHasHidden) return@launch

            val hiddenQuestsPool = listOf(
                Pair("امروز به یک دوست یا غریبه بدون چشم‌داشت کمک کن.", "امروز به یک نفر کمک کن"),
                Pair("بدن شما گنج شماست؛ همین امروز ۳ لیتر آب سالم بنوشید.", "امروز ۳ لیتر آب بنوش"),
                Pair("از جایتان برخیزید و امروز بیش از ۱۰ هزار قدم بردارید.", "امروز ۱۰ هزار قدم راه برو")
            )
            val selected = hiddenQuestsPool.random()
            val newHidden = Quest(
                title = selected.second,
                description = selected.first,
                type = "Hidden",
                xpReward = 500,
                goldReward = 1000,
                difficulty = "Hard",
                date = repository.getTodayDateString()
            )
            db.playerDao().insertQuest(newHidden)
            _hiddenQuestMessage.value = "هشدار! یک مأموریت مخفی (Hidden Quest) کشف شد: ${selected.second}"
        }
    }

    fun dismissHiddenQuestWarning() {
        _hiddenQuestMessage.value = null
    }

    fun dismissPenalty() {
        viewModelScope.launch {
            repository.dismissPenalty()
        }
    }

    fun resetDailyQuestsManually() {
        viewModelScope.launch {
            repository.generateDailyQuests()
        }
    }

    // Dungeon Functions
    fun startDungeon(dungeon: Dungeon) {
        dungeonTimerJob?.cancel()
        _activeDungeon.value = dungeon
        _activeDungeonTimeLeft.value = dungeon.timerSeconds
        val stepCount = dungeon.questSteps.split(";").size
        _activeDungeonStepsProgress.value = List(stepCount) { false }

        dungeonTimerJob = viewModelScope.launch {
            while (_activeDungeonTimeLeft.value > 0) {
                delay(1000)
                _activeDungeonTimeLeft.value -= 1
            }
            // Timer finished, failed if not completed
            if (_activeDungeon.value != null) {
                failDungeon()
            }
        }
    }

    fun toggleDungeonStep(index: Int) {
        val currentProgress = _activeDungeonStepsProgress.value.toMutableList()
        if (index in currentProgress.indices) {
            currentProgress[index] = !currentProgress[index]
            _activeDungeonStepsProgress.value = currentProgress

            // Check if all steps are completed
            if (currentProgress.all { it }) {
                completeActiveDungeon()
            }
        }
    }

    private fun completeActiveDungeon() {
        val dungeon = _activeDungeon.value ?: return
        dungeonTimerJob?.cancel()
        viewModelScope.launch {
            repository.completeDungeon(dungeon)
            _activeDungeon.value = null
        }
    }

    fun failDungeon() {
        dungeonTimerJob?.cancel()
        _activeDungeon.value = null
    }

    // Store & Loot Box Opening
    fun purchaseStoreProduct(productName: String, price: Int, type: String = "Potion"): Boolean {
        var purchased = false
        val player = playerState.value ?: return false
        if (player.gold < price) return false

        viewModelScope.launch {
            val item = when (type) {
                "Potion" -> {
                    InventoryItem(
                        name = productName,
                        type = "Potion",
                        iconName = "ic_potion",
                        quantity = 1,
                        rarity = if (productName.contains("ویژه")) "Rare" else "Common",
                        description = "معجون بازیابی قدرت خریداری شده از فروشگاه."
                    )
                }
                "Booster" -> {
                    InventoryItem(
                        name = productName,
                        type = "Booster",
                        iconName = "ic_booster",
                        quantity = 1,
                        rarity = "Rare",
                        description = "تقویت‌کننده دو برابری XP برای تمرینات."
                    )
                }
                "Equipable" -> {
                    val bonus = if (productName.contains("تاج")) "تمام مشخصه‌ها +۲۵" else "قدرت +۱۵"
                    InventoryItem(
                        name = productName,
                        type = "Equipable",
                        iconName = if (productName.contains("تاج")) "ic_crown" else "ic_gear",
                        quantity = 1,
                        rarity = if (productName.contains("تاج")) "Mythic" else "Legendary",
                        attributeBonus = bonus,
                        bonusValue = if (productName.contains("تاج")) 25 else 15,
                        description = "تجهیزات گرانبها از انبار شاهانه سیستم."
                    )
                }
                else -> return@launch
            }
            purchased = repository.buyStoreItem(item, price)
        }
        return true
    }

    fun purchaseLootBoxTicket(ticketName: String, price: Int) {
        val player = playerState.value ?: return
        if (player.gold < price) return

        viewModelScope.launch {
            val item = InventoryItem(
                name = ticketName,
                type = "Ticket",
                iconName = "ic_ticket",
                quantity = 1,
                rarity = when {
                    ticketName.contains("اساطیری") -> "Mythic"
                    ticketName.contains("طلایی") -> "Legendary"
                    else -> "Rare"
                },
                description = "بلیط ویژه برای شبیه‌سازی شانس لوت باکس کالاها."
            )
            repository.buyStoreItem(item, price)
        }
    }

    fun openLootBoxWithChance(ticket: InventoryItem) {
        if (ticket.quantity <= 0) return
        _isLootBoxOpening.value = true
        _lootBoxResult.value = null

        viewModelScope.launch {
            // Deduct ticket
            if (ticket.quantity == 1) {
                db.playerDao().deleteInventoryItem(ticket)
            } else {
                db.playerDao().updateInventoryItem(ticket.copy(quantity = ticket.quantity - 1))
            }

            delay(2200) // Animate glow suspension for 2.2 seconds

            val rand = Random.nextInt(100)
            val reward = when {
                rand < 2 -> { // 2% Mythic
                    LootBoxReward(
                        title = "تاج گرانبهای اساطیری (Mythic Crown)",
                        subtitle = "یک آیتم فوق تکرار نشدنی و شاهانه!",
                        rarity = "Mythic",
                        colorHex = 0xFFFF0055,
                        bonusDescription = "تمام ویژگی‌ها +۲۵",
                        itemToReceive = InventoryItem(
                            name = "تاج اساطیری (Mythic Crown)",
                            type = "Equipable",
                            iconName = "ic_crown",
                            rarity = "Mythic",
                            attributeBonus = "تمام مشخصه‌ها +۲۵",
                            bonusValue = 25,
                            description = "تاجی مجلل که شایسته تنها یک پادشاه لولینگ واقعی است."
                        )
                    )
                }
                rand < 10 -> { // 8% Legendary
                    LootBoxReward(
                        title = "لقب افسانه‌ای: فاتح بی‌رحم (Dungeon Conqueror)",
                        subtitle = "لقب تالار افتخارات شکارچیان برتر",
                        rarity = "Legendary",
                        colorHex = 0xFFFF9D00,
                        bonusDescription = "افزایش دائمی +۱۰ به قدرت و استقامت",
                        itemToReceive = InventoryItem(
                            name = "فاتح بی‌رحم (Conqueror)",
                            type = "Title",
                            iconName = "ic_title",
                            rarity = "Legendary",
                            attributeBonus = "قدرت +۱۰، استقامت +۱۰",
                            isEquipped = false,
                            description = "لقبی باشکوه برای فاتحین واقعی دخمه‌های عمیق."
                        )
                    )
                }
                rand < 25 -> { // 15% Epic
                    LootBoxReward(
                        title = "زعفران و معجون بازیابی ویژه امپراطور",
                        subtitle = "معجونی شاهانه برای ارتقای بدنی",
                        rarity = "Epic",
                        colorHex = 0xFF9D00FF,
                        bonusDescription = "دریافت ۳۰۰ سکه طلا و ۲۰۰ امتیاز XP",
                        onClaimAction = {
                            repository.addRewardsAndCheckLevelUp(200, 300)
                        }
                    )
                }
                rand < 50 -> { // 25% Rare
                    LootBoxReward(
                        title = "کلید کریستالی طلایی تاریک",
                        subtitle = "کلید کمیاب برای تصرف سیاه‌چال‌ها",
                        rarity = "Rare",
                        colorHex = 0xFF0070F3,
                        bonusDescription = "دریافت ۱ کلید تاریکی اساطیری در وسایل شما",
                        itemToReceive = InventoryItem(
                            name = "کلید تاریکی اساطیری",
                            type = "Key",
                            iconName = "ic_key",
                            rarity = "Rare",
                            description = "کلید لازم برای ورود به معبد سیاه‌چال فرمانروای سایه‌ها."
                        )
                    )
                }
                else -> { // 50% Common
                    LootBoxReward(
                        title = "پوشن قرمز سلامتی و ۵۰ سکه",
                        subtitle = "یک جایزه عمومی و کاربردی",
                        rarity = "Common",
                        colorHex = 0xFF00D2FF,
                        bonusDescription = "دریافت ۵۰ سکه طلا و ۱ معجون سلامتی",
                        itemToReceive = InventoryItem(
                            name = "معجون قرمز سلامتی",
                            type = "Potion",
                            iconName = "ic_potion",
                            quantity = 1,
                            rarity = "Common",
                            description = "یک معجون ساده برای بازیابی نیروی جسمانی."
                        ),
                        onClaimAction = {
                            repository.addRewardsAndCheckLevelUp(20, 50)
                        }
                    )
                }
            }

            _lootBoxResult.value = reward
            _isLootBoxOpening.value = false
        }
    }

    fun claimLootBoxReward(reward: LootBoxReward) {
        viewModelScope.launch {
            if (reward.itemToReceive != null) {
                val currentItems = db.playerDao().getInventoryItemsDirect()
                val existing = currentItems.find { it.name == reward.itemToReceive.name && it.type == reward.itemToReceive.type }
                if (existing != null) {
                    db.playerDao().updateInventoryItem(existing.copy(quantity = existing.quantity + reward.itemToReceive.quantity))
                } else {
                    db.playerDao().insertInventoryItem(reward.itemToReceive)
                }
            }
            reward.onClaimAction?.invoke()
            _lootBoxResult.value = null
        }
    }

    fun toggleEquipItem(item: InventoryItem) {
        viewModelScope.launch {
            repository.toggleEquipItem(item)
        }
    }

    fun usePotion(item: InventoryItem) {
        viewModelScope.launch {
            repository.usePotion(item)
        }
    }

    // Boss fight actions
    fun defeatBoss(bossName: String, xp: Int, gold: Int, nextRankRule: String) {
        viewModelScope.launch {
            val player = playerState.value ?: return@launch
            repository.addRewardsAndCheckLevelUp(xp, gold)

            // Special Title drop on Boss Defeat
            val droppedTitle = when (bossName) {
                "شاه کاهلی و تنبلی (King of Laziness)" -> InventoryItem(
                    name = "بدن آهنین (Iron Body)",
                    type = "Title",
                    iconName = "ic_title",
                    rarity = "Rare",
                    attributeBonus = "استقامت +۶",
                    isEquipped = false,
                    description = "عنوانی باشکوه برای کسی که ۷ روز بدون تنبلی بر نفس خود پیروز شد."
                )
                "ارباب ضعف بدنی (Lord of Weakness)" -> InventoryItem(
                    name = "جنگجوی سایه (Shadow Warrior)",
                    type = "Title",
                    iconName = "ic_title",
                    rarity = "Epic",
                    attributeBonus = "قدرت +۱۲، چابکی +۱۲",
                    isEquipped = false,
                    description = "لقب دریافتی از ارباب ضعف بعد از ۳۰ روز فعالیت بدنی پیوسته."
                )
                "غول سایه غول‌پیکر (Shadow Giant)" -> InventoryItem(
                    name = "فرمانروای سایه‌ها (Shadow Monarch)",
                    type = "Title",
                    iconName = "ic_title",
                    rarity = "Mythic",
                    attributeBonus = "تمام ویژگی‌ها +۲۰، دریافت XP دوبرابر (+۱۰۰٪)",
                    isEquipped = false,
                    description = "معتبرترین و بزرگ‌ترین لقب سیستم سولو!"
                )
                else -> null
            }

            if (droppedTitle != null) {
                db.playerDao().insertInventoryItem(droppedTitle)
            }
        }
    }

    // Guild custom actions
    fun createOrUpgradeGuild(name: String) {
        viewModelScope.launch {
            val player = playerState.value ?: return@launch
            if (player.gold < 1000) return@launch

            // Deduct gold
            db.playerDao().insertPlayerState(player.copy(gold = player.gold - 1000, guildName = name, guildRole = "رهبر صنف (Leader)"))

            val currentGuild = db.playerDao().getPlayerStateDirect()
            db.playerDao().insertGuildState(
                GuildState(
                    id = 1,
                    name = name,
                    level = 2,
                    xp = 50,
                    memberCount = 5,
                    multiplier = 1.15
                )
            )

            // Adjust Achievement progress
            repository.adjustAchievementProgress("ach_level_10", 1)
        }
    }

    // Skill tree levels
    fun upgradeSkillNode(nodeName: String) {
        viewModelScope.launch {
            val player = playerState.value ?: return@launch
            if (player.statPoints < 2) return@launch // needs 2 level statPoints for skill tree

            val updatedPlayer = when (nodeName) {
                "Warrior" -> player.copy(activeSkillWarriorLevel = player.activeSkillWarriorLevel + 1, statPoints = player.statPoints - 2, strength = player.strength + 3)
                "Athlete" -> player.copy(activeSkillAthleteLevel = player.activeSkillAthleteLevel + 1, statPoints = player.statPoints - 2, agility = player.agility + 3, endurance = player.endurance + 3)
                "Leader" -> player.copy(activeSkillLeaderLevel = player.activeSkillLeaderLevel + 1, statPoints = player.statPoints - 2, charisma = player.charisma + 4)
                "Scholar" -> player.copy(activeSkillScholarLevel = player.activeSkillScholarLevel + 1, statPoints = player.statPoints - 2, knowledge = player.knowledge + 4)
                "Shadow" -> player.copy(activeSkillShadowLevel = player.activeSkillShadowLevel + 1, statPoints = player.statPoints - 2, mentalPower = player.mentalPower + 4)
                else -> player
            }
            db.playerDao().insertPlayerState(updatedPlayer)
        }
    }

    // Change system theme color preference
    fun selectTheme(themeName: String) {
        viewModelScope.launch {
            val player = playerState.value ?: return@launch
            db.playerDao().insertPlayerState(player.copy(selectedTheme = themeName))
        }
    }
}

data class LootBoxReward(
    val title: String,
    val subtitle: String,
    val rarity: String,
    val colorHex: Long,
    val bonusDescription: String,
    val itemToReceive: InventoryItem? = null,
    val onClaimAction: (suspend () -> Unit)? = null
)
