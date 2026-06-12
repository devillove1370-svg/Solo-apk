package com.example.data

import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PlayerRepository(private val playerDao: PlayerDao) {

    val playerState: Flow<PlayerState?> = playerDao.getPlayerState()
    val inventoryItems: Flow<List<InventoryItem>> = playerDao.getInventoryItems()
    val quests: Flow<List<Quest>> = playerDao.getQuests()
    val dungeons: Flow<List<Dungeon>> = playerDao.getDungeons()
    val achievements: Flow<List<Achievement>> = playerDao.getAchievements()
    val guildState: Flow<GuildState?> = playerDao.getGuildState()

    suspend fun getPlayerStateDirect(): PlayerState? = playerDao.getPlayerStateDirect()

    suspend fun updatePlayerState(state: PlayerState) {
        playerDao.insertPlayerState(state)
    }

    suspend fun initializeDatabaseIfEmpty() {
        val current = playerDao.getPlayerStateDirect()
        if (current == null) {
            // Seed base player state
            val seedPlayer = PlayerState(
                id = 1,
                name = "سونگ جین‌وو",
                isAwakened = false,
                level = 1,
                xp = 0,
                gold = 1000,
                rank = "E Rank",
                statPoints = 5,
                strength = 10,
                agility = 10,
                endurance = 10,
                discipline = 10,
                knowledge = 10,
                charisma = 10,
                mentalPower = 10,
                streak = 0,
                lastActiveDate = getTodayDateString(),
                showPenaltyScreen = false
            )
            playerDao.insertPlayerState(seedPlayer)

            // Seed default inventory items
            val seedItems = listOf(
                InventoryItem(
                    name = "معجون قرمز سلامتی",
                    type = "Potion",
                    iconName = "ic_potion",
                    quantity = 3,
                    rarity = "Common",
                    description = "یک معجون ساده برای بازیابی نیروی جسمانی."
                ),
                InventoryItem(
                    name = "بلیط بخت‌ آزمایی برنزی",
                    type = "Ticket",
                    iconName = "ic_ticket",
                    quantity = 2,
                    rarity = "Common",
                    description = "استفاده برای کشیدن لوت باکس ساده."
                ),
                InventoryItem(
                    name = "کلید سیاه‌چال تمرینی",
                    type = "Key",
                    iconName = "ic_key",
                    quantity = 1,
                    rarity = "Common",
                    description = "کلید ورود به سیاه‌چال تمرینی اول."
                ),
                InventoryItem(
                    name = "شکارچی تازه‌کار",
                    type = "Title",
                    iconName = "ic_title",
                    quantity = 1,
                    rarity = "Common",
                    attributeBonus = "قدرت +۱، استقامت +۱",
                    isEquipped = true,
                    description = "لقبی برای کسانی که تازه سیستم سولو را فعال کرده‌اند."
                )
            )
            for (item in seedItems) {
                playerDao.insertInventoryItem(item)
            }

            // Seed dungeons
            val seedDungeons = listOf(
                Dungeon(
                    id = "dungeon_beginner",
                    name = "سیاه‌چال مبتدی (Beginner Dungeon)",
                    description = "اولین آزمایش برای سنجش آمادگی جسمانی شما به عنوان یک شکارچی.",
                    type = "Training",
                    levelRequired = 1,
                    keyTypeRequired = null,
                    timerSeconds = 1200,
                    xpReward = 150,
                    goldReward = 300,
                    questSteps = "۵ دقیقه نرمش بدنی;۱۰ شنای استاندارد;۱۵ اسکوات موازی;۲ دقیقه پلانک تمرکزی",
                    customSuccessMessage = "سیاه‌چال با موفقیت تصرف شد! انرژی درونی شما بیدار شد."
                ),
                Dungeon(
                    id = "dungeon_strength",
                    name = "مقبره قدرت (Strength Dungeon)",
                    description = "ورود به این معبد نیاز به بدنی فولادین و بازوان ورزیده دارد.",
                    type = "Strength",
                    levelRequired = 3,
                    keyTypeRequired = "کلید سیاه‌چال قدرتی",
                    timerSeconds = 1800,
                    xpReward = 300,
                    goldReward = 800,
                    questSteps = "۴۰ شنای سوئدی;۴۰ اسکوات عمیق;۳ دقیقه پلانک;۲۰ بارفیکس یا حرکت بازو",
                    customSuccessMessage = "تبریک! بدن شما اکنون در ردیف رزمندگان برتر قرار دارد."
                ),
                Dungeon(
                    id = "dungeon_discipline",
                    name = "دخمه انضباط (Discipline Dungeon)",
                    description = "آیینی دشوار برای غلبه بر سستی و تنبلی روزمره.",
                    type = "Discipline",
                    levelRequired = 5,
                    keyTypeRequired = null,
                    timerSeconds = 900,
                    xpReward = 400,
                    goldReward = 1000,
                    questSteps = "بیدار شدن قبل ساعت ۷ صبح;عدم لمس گوشی تا ۱ ساعت بعد بیداری;۳۰ دقیقه ورزش پرانرژی صبحگاهی",
                    customSuccessMessage = "با غلبه بر سستی نفس، اراده خود را به رخ سیستم کشیدید!"
                ),
                Dungeon(
                    id = "dungeon_mental",
                    name = "معبد تمرکز و روح (Mental Dungeon)",
                    description = "سیاه‌چالی هولناک که درون ذهن و روحیات شما را به چالش می‌کشد.",
                    type = "Mental",
                    levelRequired = 4,
                    keyTypeRequired = null,
                    timerSeconds = 3600,
                    xpReward = 250,
                    goldReward = 600,
                    questSteps = "۴۵ دقیقه مطالعه عمیق مداوم;۱۵ دقیقه مدیتیشن تنفسی سولو;یادداشت روزانه ۳ هدف اصلی روز",
                    customSuccessMessage = "آرامش ذهن شما حصار سیاه‌چال را درهم شکست."
                ),
                Dungeon(
                    id = "dungeon_shadow",
                    name = "قلعه سایه‌ها (Shadow Dungeon)",
                    description = "مرز نهایی برای احضار اولین سربازان سایه و اثبات شایستگی فرمانروایی سایه‌ها.",
                    type = "Shadow",
                    levelRequired = 10,
                    keyTypeRequired = "کلید تاریکی اساطیری",
                    timerSeconds = 2400,
                    xpReward = 1000,
                    goldReward = 2500,
                    questSteps = "۱ ساعت پیاده‌روی سریع شبانه;۵۰ شنای تک‌ضرب;۸۰ اسکوات پرشی;۵۰ درازونشست",
                    customSuccessMessage = "تبریک فرمانروا! اولین سربازان سایه در خدمت شما هستند (برخیزید: ARISE)."
                )
            )
            playerDao.insertDungeons(seedDungeons)

            // Seed Achievements
            val seedAchievements = listOf(
                Achievement("ach_level_10", "برخاستن ابدی", "رسیدن به سطح ۱۰ بازی", 1, 10, false, 500),
                Achievement("ach_pushups", "ضربات آتشین", "بیش از ۳۰۰ حرکت شنای ثبت‌شده", 0, 300, false, 400),
                Achievement("ach_dungeons", "فاتح مقبره‌ها", "تکمیل ۵ غول سیاه‌چال بزرگ", 0, 5, false, 1000),
                Achievement("ach_gold", "اندوخته طلایی شکارچی", "ذخیره بیش از ۵۰۰۰ سکه طلا", 1000, 5000, false, 300),
                Achievement("ach_knowledge", "دانش‌پژوه تاریک", "بیش از ۵ ساعت (۳۰۰ دقیقه) مطالعه و ارتقای ذهن", 0, 300, false, 500),
                Achievement("ach_streak_7", "ثبات بی‌رحمانه", "دستیابی به ۷ روز متوالی تمرین روزانه بدون وقفه", 0, 7, false, 600)
            )
            playerDao.insertAchievements(seedAchievements)

            // Seed Guild
            val defaultGuild = GuildState(
                id = 1,
                name = "صنف سایه‌های برخاسته",
                level = 1,
                xp = 10,
                memberCount = 1,
                multiplier = 1.0
            )
            playerDao.insertGuildState(defaultGuild)

            // Seed Quests
            generateDailyQuests()
        }
    }

    suspend fun generateDailyQuests() {
        playerDao.clearQuests()
        val today = getTodayDateString()
        val defaultDailyme = listOf(
            Quest(
                title = "۱۰ حرکت شنا (Push-ups)",
                description = "افزایش ویژگی Strength (قدرت بدنی). تمرین واقعی و تیک زدن پس از تکمیل.",
                type = "Daily",
                xpReward = 20,
                goldReward = 100,
                difficulty = "Easy",
                date = today
            ),
            Quest(
                title = "۲۰ اسکوات (Squats)",
                description = "افزایش ویژگی Agility (چابکی) و پایداری پاها.",
                type = "Daily",
                xpReward = 40,
                goldReward = 150,
                difficulty = "Easy",
                date = today
            ),
            Quest(
                title = "۱۵ دقیقه مطالعه کتاب یا یادگیری مهارت جدید",
                description = "افزایش ویژگی Knowledge (دانش روزمره و قدرت ذهنی).",
                type = "Daily",
                xpReward = 100,
                goldReward = 200,
                difficulty = "Medium",
                date = today
            ),
            Quest(
                title = "نوشیدن ۲ لیتر آب خالص",
                description = "افزایش ویژگی Endurance (استقامت عمومی بدن).",
                type = "Daily",
                xpReward = 50,
                goldReward = 100,
                difficulty = "Easy",
                date = today
            ),
            Quest(
                title = "۳۰ دقیقه پیاده‌روی سریع یا ورزش مداوم",
                description = "افزایش ویژگی Discipline (نظم تیمی) و استقامت قلبی.",
                type = "Daily",
                xpReward = 150,
                goldReward = 300,
                difficulty = "Medium",
                date = today
            )
        )
        for (q in defaultDailyme) {
            playerDao.insertQuest(q)
        }
    }

    // Quest Actions
    suspend fun completeQuest(quest: Quest, player: PlayerState) {
        val updatedQuest = quest.copy(isCompleted = true)
        playerDao.insertQuest(updatedQuest)

        // Attribute increases based on quest description/type
        var strPlus = 0
        var agiPlus = 0
        var endPlus = 0
        var dscPlus = 0
        var knwPlus = 0
        var menPlus = 0

        val titleLower = quest.title.lowercase()
        if (titleLower.contains("شنا") || titleLower.contains("push-up")) {
            strPlus = 2
            adjustAchievementProgress("ach_pushups", 10)
        } else if (titleLower.contains("اسکوات") || titleLower.contains("squat")) {
            agiPlus = 2
        } else if (titleLower.contains("مطالعه") || titleLower.contains("کتاب")) {
            knwPlus = 3
            menPlus = 1
            adjustAchievementProgress("ach_knowledge", 15)
        } else if (titleLower.contains("آب") || titleLower.contains("water")) {
            endPlus = 2
        } else if (titleLower.contains("ورزش") || titleLower.contains("پیاده")) {
            dscPlus = 2
            endPlus = 1
        }

        // Give Rewards
        addRewardsAndCheckLevelUp(
            xpAmount = quest.xpReward,
            goldAmount = quest.goldReward,
            strengthPlus = strPlus,
            agilityPlus = agiPlus,
            endurancePlus = endPlus,
            disciplinePlus = dscPlus,
            knowledgePlus = knwPlus,
            mentalPlus = menPlus
        )
    }

    suspend fun addRewardsAndCheckLevelUp(
        xpAmount: Int,
        goldAmount: Int,
        strengthPlus: Int = 0,
        agilityPlus: Int = 0,
        endurancePlus: Int = 0,
        disciplinePlus: Int = 0,
        knowledgePlus: Int = 0,
        charismaPlus: Int = 0,
        mentalPlus: Int = 0
    ) {
        val player = playerDao.getPlayerStateDirect() ?: return

        var newXp = player.xp + xpAmount
        var newLevel = player.level
        var statPointsGained = 0

        // Level Up calculation: 100 XP per level
        var requiredXp = getXpForNextLevel(newLevel)
        while (newXp >= requiredXp) {
            newXp -= requiredXp
            newLevel++
            statPointsGained += 5
            requiredXp = getXpForNextLevel(newLevel)
        }

        // Adjust Rank based on level
        val calculatedRank = calculateRankFromLevel(newLevel)

        val updatedPlayer = player.copy(
            level = newLevel,
            xp = newXp,
            gold = player.gold + goldAmount,
            rank = calculatedRank,
            statPoints = player.statPoints + statPointsGained,
            strength = player.strength + strengthPlus,
            agility = player.agility + agilityPlus,
            endurance = player.endurance + endurancePlus,
            discipline = player.discipline + disciplinePlus,
            knowledge = player.knowledge + knowledgePlus,
            charisma = player.charisma + charismaPlus,
            mentalPower = player.mentalPower + mentalPlus
        )
        playerDao.insertPlayerState(updatedPlayer)

        // Check Gold achievement
        updateAchievementDirectProgress("ach_gold", updatedPlayer.gold)
        updateAchievementDirectProgress("ach_level_10", updatedPlayer.level)
    }

    suspend fun distributeStatPoint(statName: String) {
        val player = playerDao.getPlayerStateDirect() ?: return
        if (player.statPoints <= 0) return

        val updated = when (statName) {
            "strength" -> player.copy(strength = player.strength + 1, statPoints = player.statPoints - 1)
            "agility" -> player.copy(agility = player.agility + 1, statPoints = player.statPoints - 1)
            "endurance" -> player.copy(endurance = player.endurance + 1, statPoints = player.statPoints - 1)
            "discipline" -> player.copy(discipline = player.discipline + 1, statPoints = player.statPoints - 1)
            "knowledge" -> player.copy(knowledge = player.knowledge + 1, statPoints = player.statPoints - 1)
            "charisma" -> player.copy(charisma = player.charisma + 1, statPoints = player.statPoints - 1)
            "mentalPower" -> player.copy(mentalPower = player.mentalPower + 1, statPoints = player.statPoints - 1)
            else -> player
        }
        playerDao.insertPlayerState(updated)
    }

    suspend fun buyStoreItem(item: InventoryItem, price: Int): Boolean {
        val player = playerDao.getPlayerStateDirect() ?: return false
        if (player.gold < price) return false

        // Charge gold
        playerDao.insertPlayerState(player.copy(gold = player.gold - price))

        // Check if item already exists to increase quantity
        val currentItems = playerDao.getInventoryItemsDirect()
        val existing = currentItems.find { it.name == item.name && it.type == item.type }
        if (existing != null) {
            playerDao.updateInventoryItem(existing.copy(quantity = existing.quantity + item.quantity))
        } else {
            playerDao.insertInventoryItem(item)
        }
        return true
    }

    suspend fun toggleEquipItem(item: InventoryItem) {
        val items = playerDao.getInventoryItemsDirect()
        val player = playerDao.getPlayerStateDirect() ?: return

        if (item.type != "Equipable" && item.type != "Title") return

        if (item.type == "Title") {
            // Un-equip all titles first
            items.filter { it.type == "Title" }.forEach {
                playerDao.updateInventoryItem(it.copy(isEquipped = false))
            }
            // Equip selected
            val nextEquippedState = !item.isEquipped
            playerDao.updateInventoryItem(item.copy(isEquipped = nextEquippedState))
            playerDao.insertPlayerState(
                player.copy(selectedTitle = if (nextEquippedState) item.name else null)
            )
            return
        }

        // It is Equipable. Equip/Unequip based on slot
        val slot = when {
            item.name.contains("دستکش") -> "gloves"
            item.name.contains("کفش") -> "shoes"
            item.name.contains("کمربند") -> "belt"
            item.name.contains("تاج") -> "crown"
            else -> "unknown"
        }

        val updatedEquippedState = !item.isEquipped

        // Un-equip other items in same category
        items.filter { it.type == "Equipable" && it.name.contains(item.name.take(3)) }.forEach {
            playerDao.updateInventoryItem(it.copy(isEquipped = false))
        }

        playerDao.updateInventoryItem(item.copy(isEquipped = updatedEquippedState))

        // Modify player state
        val updatedPlayer = when (slot) {
            "gloves" -> player.copy(equippedGloves = if (updatedEquippedState) item.name else null)
            "shoes" -> player.copy(equippedShoes = if (updatedEquippedState) item.name else null)
            "belt" -> player.copy(equippedBelt = if (updatedEquippedState) item.name else null)
            "crown" -> player.copy(equippedCrown = if (updatedEquippedState) item.name else null)
            else -> player
        }
        playerDao.insertPlayerState(updatedPlayer)
    }

    suspend fun usePotion(item: InventoryItem): Boolean {
        if (item.quantity <= 0) return false
        val player = getPlayerStateDirect() ?: return false

        // Remove 1 quantity
        if (item.quantity == 1) {
            playerDao.deleteInventoryItem(item)
        } else {
            playerDao.updateInventoryItem(item.copy(quantity = item.quantity - 1))
        }

        // Double check potion effects
        if (item.name.contains("کمیاب") || item.name.contains("ویژه")) {
            // Boost all attributes
            addRewardsAndCheckLevelUp(
                xpAmount = 100,
                goldAmount = 100,
                strengthPlus = 1,
                agilityPlus = 1,
                endurancePlus = 1,
                disciplinePlus = 1,
                knowledgePlus = 1,
                charismaPlus = 1,
                mentalPlus = 1
            )
        } else {
            // Standard potion gains XP
            addRewardsAndCheckLevelUp(xpAmount = 50, goldAmount = 50)
        }
        return true
    }

    // Trigger Dungeon Completion
    suspend fun completeDungeon(dungeon: Dungeon) {
        val updatedDungeon = dungeon.copy(isCleared = true)
        playerDao.updateDungeon(updatedDungeon)

        val player = getPlayerStateDirect() ?: return
        // Give XP and Gold
        addRewardsAndCheckLevelUp(xpAmount = dungeon.xpReward, goldAmount = dungeon.goldReward)

        // Give customized loot based on Dungeons!
        val lootItem = when (dungeon.id) {
            "dungeon_strength" -> InventoryItem(
                name = "دستکش کمیاب قدرت (Rare Gloves)",
                type = "Equipable",
                iconName = "ic_gloves",
                rarity = "Rare",
                attributeBonus = "قدرت +۵",
                bonusValue = 5,
                description = "دستکشی جادویی که توان ضربات دست شما را دو چندان می‌کند."
            )
            "dungeon_shadow" -> InventoryItem(
                name = "کفش حماسی استتار (Epic Shoes)",
                type = "Equipable",
                iconName = "ic_shoes",
                rarity = "Epic",
                attributeBonus = "چابکی +۸",
                bonusValue = 8,
                description = "کفشی فوق‌العاده سبک آغشته به ذات تاریک قلعه سایه‌ها."
            )
            "dungeon_discipline" -> InventoryItem(
                name = "کمربند افسانه‌ای نظم (Legendary Belt)",
                type = "Equipable",
                iconName = "ic_belt",
                rarity = "Legendary",
                attributeBonus = "استقامت +۱۲",
                bonusValue = 12,
                description = "کمربندی تافته از فولاد اراده؛ نیروی حیات بازیکن را تا حد اعلا نگه می‌دارد."
            )
            else -> null
        }

        if (lootItem != null) {
            playerDao.insertInventoryItem(lootItem)
        }

        // Adjust Achievement progress
        adjustAchievementProgress("ach_dungeons", 1)
    }

    suspend fun adjustAchievementProgress(achId: String, amount: Int) {
        val list = playerDao.getAchievementsDirect()
        val ach = list.find { it.id == achId } ?: return
        if (ach.isUnlocked) return

        val nextProgress = (ach.progress + amount).coerceAtMost(ach.maxProgress)
        val isNowUnlocked = nextProgress >= ach.maxProgress

        val updatedAch = ach.copy(
            progress = nextProgress,
            isUnlocked = isNowUnlocked
        )
        playerDao.updateAchievement(updatedAch)

        if (isNowUnlocked) {
            // Reward Gold and XP
            addRewardsAndCheckLevelUp(xpAmount = 100, goldAmount = ach.rewardGold)
        }
    }

    private suspend fun updateAchievementDirectProgress(achId: String, currentProgress: Int) {
        val list = playerDao.getAchievementsDirect()
        val ach = list.find { it.id == achId } ?: return
        if (ach.isUnlocked) return

        val nextProgress = currentProgress.coerceAtMost(ach.maxProgress)
        val isNowUnlocked = nextProgress >= ach.maxProgress

        val updatedAch = ach.copy(
            progress = nextProgress,
            isUnlocked = isNowUnlocked
        )
        playerDao.updateAchievement(updatedAch)

        if (isNowUnlocked) {
            addRewardsAndCheckLevelUp(xpAmount = 100, goldAmount = ach.rewardGold)
        }
    }

    // Check Streak / Penalty logic
    suspend fun checkDailyPenalty() {
        val player = playerDao.getPlayerStateDirect() ?: return
        val todayStr = getTodayDateString()

        if (player.lastActiveDate.isNotEmpty() && player.lastActiveDate != todayStr) {
            // Check if day was missed! If yes, show Penalty screen
            val calendar = java.util.Calendar.getInstance()
            calendar.add(java.util.Calendar.DAY_OF_YEAR, -1)
            val yesterdayStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(calendar.time)

            // If player didn't complete quests yesterday, impose penalty!
            if (player.lastActiveDate != yesterdayStr) {
                // Inflict Penalty!
                val penaltyState = player.copy(
                    xp = (player.xp - 100).coerceAtLeast(0),
                    streak = 0,
                    showPenaltyScreen = true,
                    lastActiveDate = todayStr
                )
                playerDao.insertPlayerState(penaltyState)
            } else {
                // Transition active date without penalty
                playerDao.insertPlayerState(player.copy(lastActiveDate = todayStr))
            }
        } else if (player.lastActiveDate.isEmpty()) {
            playerDao.insertPlayerState(player.copy(lastActiveDate = todayStr))
        }
    }

    suspend fun dismissPenalty() {
        val player = playerDao.getPlayerStateDirect() ?: return
        playerDao.insertPlayerState(player.copy(showPenaltyScreen = false))
    }

    fun getTodayDateString(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return sdf.format(Date())
    }

    fun getXpForNextLevel(lvl: Int): Int = lvl * 100

    fun calculateRankFromLevel(lvl: Int): String {
        return when {
            lvl < 5 -> "E Rank"
            lvl < 10 -> "D Rank"
            lvl < 15 -> "C Rank"
            lvl < 20 -> "B Rank"
            lvl < 25 -> "A Rank"
            lvl < 30 -> "S Rank"
            lvl < 35 -> "SS Rank"
            lvl < 40 -> "SSS Rank"
            lvl < 45 -> "National Level Hunter"
            lvl < 50 -> "Monarch Candidate"
            else -> "Shadow Monarch (پادشاه سایه‌ها)"
        }
    }
}
