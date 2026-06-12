package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.*
import com.example.ui.viewmodel.LootBoxReward
import com.example.ui.viewmodel.PlayerViewModel
import kotlinx.coroutines.delay
import java.io.Serializable

@Composable
fun GameRoot(viewModel: PlayerViewModel) {
    // Force RTL for Persian Interface
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        val playerState by viewModel.playerState.collectAsState()

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF030308))
        ) {
            val player = playerState
            if (player == null) {
                // Initial loading state
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF00E5FF))
                }
            } else if (!player.isAwakened) {
                // Starter Cinematic
                IntroScreen(
                    onAwaken = { name -> viewModel.awakenPlayer(name) },
                    onRenounce = { viewModel.renouncePlayer() }
                )
            } else {
                // Main active RPG Game loop
                MainGameHub(viewModel = viewModel, player = player)
            }
        }
    }
}

// -----------------------------------------------------
// 1. STARTER CINEMATIC (INTRO SCREEN)
// -----------------------------------------------------
@Composable
fun IntroScreen(
    onAwaken: (String) -> Unit,
    onRenounce: () -> Unit
) {
    var introStage by remember { mutableStateOf(1) } // 1: Heartbeat warning, 2: Do you wish..., 3: GameOver City, 4: Awakening Glow
    var nameInput by remember { mutableStateOf("") }
    
    val infiniteTransition = rememberInfiniteTransition(label = "heartbeat")
    val heartScale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "heartbeat_scale"
    )

    val neonColorState by infiniteTransition.animateColor(
        initialValue = Color(0xFF00E5FF),
        targetValue = Color(0xFFD500F9),
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "neon_color"
    )

    when (introStage) {
        1 -> {
            // Dark Screen + Heartbeat & WARNING PLAYER DETECTED
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF020205))
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.weight(1f))
                
                // Heartbeat Icon with Neon glow
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .background(Color(0x0AFFFFFF), shape = CircleShape)
                        .border(2.dp, neonColorState, shape = CircleShape)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "ضربان قلب",
                        tint = Color(0xFFFF1744),
                        modifier = Modifier
                            .size(80.dp)
                            .rotate(heartScale * 5f)
                    )
                }

                Spacer(modifier = Modifier.height(48.dp))

                Text(
                    text = "هشدار سیستم",
                    color = Color(0xFFFF1744),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "WARNING: PLAYER DETECTED",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "یک موجودیت فانی مستعد ارتقا در محدوده شناسایی شد.",
                    color = Color.Gray,
                    fontSize = 15.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = { introStage = 2 },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Text(
                        text = "ادامه و بررسی وضعیت",
                        color = Color.Black,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        2 -> {
            // Your Heart Has Stopped. Do You Wish To Become A Player? YES / NO
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF020205))
                    .padding(28.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "سیستم هدایت ارتقای سطح (Solo)",
                    color = Color(0xFF00E5FF),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(30.dp))

                Text(
                    text = "قلب شما از تپش ایستاده است.",
                    color = Color(0xFFFF1744),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "آیا تمایل دارید به عنوان یک «بازیکن» در سیستم بیدار شوید و زندگی واقعی خود را شروع کنید؟",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    lineHeight = 28.sp
                )

                Spacer(modifier = Modifier.height(40.dp))

                // Name Input Field
                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it },
                    label = { Text("نام مستعار شکارچی (مثلا: جین‌وو)", color = Color.Gray) },
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.White),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(30.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // NO button
                    OutlinedButton(
                        onClick = { introStage = 3 },
                        border = BorderStroke(1.dp, Color(0xFFFF1744)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                    ) {
                        Text("خیر (رد کردن)", color = Color(0xFFFF1744), fontWeight = FontWeight.Bold)
                    }

                    // YES button
                    Button(
                        onClick = {
                            if (nameInput.isBlank()) {
                                nameInput = "شکارچی ناشناس"
                            }
                            introStage = 4 // Awakening process
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                    ) {
                        Text("بله (Awaken)", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        3 -> {
            // GAME OVER - City destroyed
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF100305))
                    .padding(28.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "نابود شد",
                    tint = Color(0xFFFF1744),
                    modifier = Modifier.size(90.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "بازی تمام شد / GAME OVER",
                    color = Color(0xFFFF1744),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "با پاسخ منفی شما به درخواست کاندیداتوری سیستم، سیاهچاله‌ها در تمام شهر گشوده شدند و بشریت به کام هیولاها نابود شد.",
                    color = Color.LightGray,
                    fontSize = 17.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 26.sp
                )

                Spacer(modifier = Modifier.height(48.dp))

                Button(
                    onClick = { introStage = 2 },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF1744)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Text("خیر من پادشاه سایه‌ها می‌شوم! (تلاش مجدد)", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
        4 -> {
            // Blue glow System Activated screen
            var animateWelcome by remember { mutableStateOf(false) }
            LaunchedEffect(Unit) {
                animateWelcome = true
                delay(2500)
                onAwaken(nameInput)
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF030D1B))
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AnimatedVisibility(
                    visible = animateWelcome,
                    enter = fadeIn(animationSpec = tween(1000)) + expandVertically()
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .background(Color(0x1F00E5FF), shape = CircleShape)
                                .border(3.dp, Color(0xFF00E5FF), shape = CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "تایید",
                                tint = Color(0xFF00E5FF),
                                modifier = Modifier.size(60.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(36.dp))

                        Text(
                            text = "سیستم فعال شد",
                            color = Color(0xFF00E5FF),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 4.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "SYSTEM ACTIVATED",
                            color = Color.White,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = "به جمع بازیکنان سیستم ارتقای سولو خوش آمدید\nجناب شکارچی: $nameInput",
                            color = Color.LightGray,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center,
                            lineHeight = 28.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "برخیزید... (ARISE)",
                            color = Color(0xFFD500F9),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}


// -----------------------------------------------------
// 2. MAIN ACTIVE GAME HUB
// -----------------------------------------------------
@Composable
fun MainGameHub(viewModel: PlayerViewModel, player: PlayerState) {
    var selectedTab by remember { mutableStateOf("dashboard") }

    // Collect variables reacticely
    val quests by viewModel.quests.collectAsState()
    val dungeons by viewModel.dungeons.collectAsState()
    val achievements by viewModel.achievements.collectAsState()
    val inventory by viewModel.inventoryItems.collectAsState()
    val guildState by viewModel.guildState.collectAsState()

    val activeDungeon by viewModel.activeDungeon.collectAsState()
    val rawDungeonTimeLeft by viewModel.activeDungeonTimeLeft.collectAsState()
    val dungeonStepsProgress by viewModel.activeDungeonStepsProgress.collectAsState()

    val isBoxOpening by viewModel.isLootBoxOpening.collectAsState()
    val boxReward by viewModel.lootBoxResult.collectAsState()

    val hiddenQuestMsg by viewModel.hiddenQuestMessage.collectAsState()

    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it }
            )
        },
        containerColor = Color(0xFF050510)
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Background ambient pattern
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0x0A00E5FF), Color.Transparent),
                        radius = 800f
                    ),
                    center = center
                )
            }

            Column(modifier = Modifier.fillMaxSize()) {
                // Persistent Top Status Strip
                GameTopHeader(player = player, viewModel = viewModel)

                // Loaded Screen Route Display
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                ) {
                    when (selectedTab) {
                        "dashboard" -> StatusPanel(player = player, inventory = inventory, viewModel = viewModel)
                        "daily_quests" -> DailyQuestsView(quests = quests, player = player, viewModel = viewModel)
                        "dungeons" -> DungeonListView(dungeons = dungeons, player = player, viewModel = viewModel)
                        "boss_fights" -> BossFightsView(player = player, viewModel = viewModel)
                        "skill_tree" -> SkillTreeView(player = player, viewModel = viewModel)
                        "store_and_loot" -> StoreAndLootView(inventory = inventory, player = player, viewModel = viewModel)
                        "guild_and_rank" -> GuildAndRankingView(guild = guildState, player = player, achievements = achievements, viewModel = viewModel)
                    }
                }
            }

            // 1. ACTIVE DUNGEON OVERLAY HUD
            if (activeDungeon != null) {
                ActiveDungeonOverlay(
                    dungeon = activeDungeon!!,
                    timeLeftSeconds = rawDungeonTimeLeft,
                    stepsProgress = dungeonStepsProgress,
                    onStepCheck = { index -> viewModel.toggleDungeonStep(index) },
                    onExit = { viewModel.failDungeon() }
                )
            }

            // 2. PENALTY RED WARNING OVERLAY SCREEN
            if (player.showPenaltyScreen) {
                PenaltyOverlay(onAcknowledge = { viewModel.dismissPenalty() })
            }

            // 3. LOOT BOX ANIMATION DISPLAY DRAWER
            if (isBoxOpening || boxReward != null) {
                LootBoxAnimationView(
                    isOpening = isBoxOpening,
                    reward = boxReward,
                    onClaim = { viewModel.claimLootBoxReward(it) }
                )
            }

            // 4. HIDDEN QUEST DISCOVERY POPUP
            if (hiddenQuestMsg != null) {
                HiddenQuestDiscoveryDialog(
                    message = hiddenQuestMsg!!,
                    onDismiss = { viewModel.dismissHiddenQuestWarning() }
                )
            }
        }
    }
}

// -----------------------------------------------------
// TOP COMPOSABLE APP HEAD (STATUS STRIP)
// -----------------------------------------------------
@Composable
fun GameTopHeader(player: PlayerState, viewModel: PlayerViewModel) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0x1F121226)),
        border = BorderStroke(1.dp, Color(0x2200E5FF)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = player.name,
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = player.selectedTitle ?: "بدون عنوان",
                        color = Color(0xFFD500F9),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Level Badge with Neon blue circle
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .background(Color(0x1F00E5FF), shape = CircleShape)
                        .border(1.5.dp, Color(0xFF00E5FF), shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Lvl ${player.level}",
                        color = Color(0xFF00E5FF),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // XP Progression Line
            val nextLevelXp = player.level * 100
            val progressPercent = (player.xp.toFloat() / nextLevelXp.toFloat()).coerceIn(0f, 1f)

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "XP",
                    color = Color.Gray,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(end = 6.dp)
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(6.dp)
                        .background(Color(0x33FFFFFF), shape = RoundedCornerShape(3.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(progressPercent)
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(Color(0xFF00E5FF), Color(0xFF0083B0))
                                ),
                                shape = RoundedCornerShape(3.dp)
                            )
                    )
                }
                Text(
                    text = "${player.xp}/$nextLevelXp",
                    color = Color.White,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(start = 6.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Gold and Rank Details Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "سکه",
                        tint = Color(0xFFFFD600),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${player.gold} سکه",
                        color = Color(0xFFFFD600),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "رتبه: ",
                        color = Color.Gray,
                        fontSize = 11.sp
                    )
                    Text(
                        text = player.rank,
                        color = Color(0xFF00E5FF),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (player.statPoints > 0) {
                    Box(
                        modifier = Modifier
                            .background(Color(0xFF00E5FF), shape = RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "${player.statPoints} امتیاز ارتقا!",
                            color = Color.Black,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

// -----------------------------------------------------
// BOTTOM NAVIGATION BAR
// -----------------------------------------------------
@Composable
fun BottomNavigationBar(selectedTab: String, onTabSelected: (String) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xEA050512)),
        border = BorderStroke(1.dp, Color(0x3300E5FF)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val tabs = listOf(
                Triple("dashboard", "وضعیت", Icons.Default.Person),
                Triple("daily_quests", "روزانه", Icons.Default.Check),
                Triple("dungeons", "سیاه‌چال", Icons.Default.Lock),
                Triple("boss_fights", "باس‌ها", Icons.Default.Warning),
                Triple("skill_tree", "مهارت", Icons.Default.PlayArrow),
                Triple("store_and_loot", "فروشگاه", Icons.Default.ShoppingCart),
                Triple("guild_and_rank", "صنف", Icons.Default.Info)
            )

            tabs.forEach { (tabId, label, icon) ->
                val isActive = selectedTab == tabId
                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onTabSelected(tabId) }
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = if (isActive) Color(0xFF00E5FF) else Color(0x66FFFFFF),
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = label,
                        color = if (isActive) Color(0xFF00E5FF) else Color(0x66FFFFFF),
                        fontSize = 9.sp,
                        fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}


// -----------------------------------------------------
// SCREEN 1: STATUS PANEL & STATUS PANEL (آواتار و مشخصات)
// -----------------------------------------------------
@Composable
fun StatusPanel(player: PlayerState, inventory: List<InventoryItem>, viewModel: PlayerViewModel) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp)
    ) {
        item {
            Text(
                text = "مشخصات و وضعیت بازیکن",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 12.dp)
            )
        }

        // Avatar Preview Frame with Glassmorphic equipped display
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0x1F121226)),
                border = BorderStroke(1.dp, Color(0x22D500F9))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left: Interactive Blueprint Avatar
                    Box(
                        modifier = Modifier
                            .size(130.dp)
                            .background(Color(0x05FFFFFF), shape = RoundedCornerShape(12.dp))
                            .border(1.dp, Color(0x11FFFFFF), shape = RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            // Draw dynamic vector avatar nodes
                            drawCircle(Color(0x1100E5FF), radius = 100f, center = center)
                            drawCircle(Color(0xFF00E5FF), radius = 8f, center = center)
                            drawLine(
                                color = Color(0x4400E5FF),
                                start = androidx.compose.ui.geometry.Offset(center.x, center.y - 80f),
                                end = androidx.compose.ui.geometry.Offset(center.x, center.y + 80f),
                                strokeWidth = 3f
                            )
                        }

                        // Superimposed Glow Labels
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "آواتار",
                                tint = Color(0xFF00E5FF),
                                modifier = Modifier.size(60.dp)
                            )
                            Text(
                                text = "سطح ${player.level}",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // Right: Gear Slots Display
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "تجهیزات فعال زره", color = Color.Gray, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        EquipmentSlotRow(label = "دستکش:", value = player.equippedGloves ?: "خالی", rarity = "Rare")
                        EquipmentSlotRow(label = "کفش ورزشی:", value = player.equippedShoes ?: "خالی", rarity = "Epic")
                        EquipmentSlotRow(label = "کمربند قدرت:", value = player.equippedBelt ?: "خالی", rarity = "Legendary")
                        EquipmentSlotRow(label = "تاج شاهانه:", value = player.equippedCrown ?: "خالی", rarity = "Mythic")
                    }
                }
            }
        }

        // Action Status List
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0x1F121226)),
                border = BorderStroke(1.dp, Color(0x2200E5FF))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "آمارهای بقا و ویژگی شکارچی",
                        color = Color(0xFF00E5FF),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    StatModifyRow(label = "قدرت بدنی (Strength)", value = player.strength, statId = "strength", points = player.statPoints, viewModel = viewModel)
                    Divider(color = Color(0x11FFFFFF), modifier = Modifier.padding(vertical = 8.dp))
                    StatModifyRow(label = "چابکی حسی (Agility)", value = player.agility, statId = "agility", points = player.statPoints, viewModel = viewModel)
                    Divider(color = Color(0x11FFFFFF), modifier = Modifier.padding(vertical = 8.dp))
                    StatModifyRow(label = "استقامت ریه (Endurance)", value = player.endurance, statId = "endurance", points = player.statPoints, viewModel = viewModel)
                    Divider(color = Color(0x11FFFFFF), modifier = Modifier.padding(vertical = 8.dp))
                    StatModifyRow(label = "نظم و تداوم (Discipline)", value = player.discipline, statId = "discipline", points = player.statPoints, viewModel = viewModel)
                    Divider(color = Color(0x11FFFFFF), modifier = Modifier.padding(vertical = 8.dp))
                    StatModifyRow(label = "دانش روزمره (Knowledge)", value = player.knowledge, statId = "knowledge", points = player.statPoints, viewModel = viewModel)
                    Divider(color = Color(0x11FFFFFF), modifier = Modifier.padding(vertical = 8.dp))
                    StatModifyRow(label = "جذبه کاریزما (Charisma)", value = player.charisma, statId = "charisma", points = player.statPoints, viewModel = viewModel)
                    Divider(color = Color(0x11FFFFFF), modifier = Modifier.padding(vertical = 8.dp))
                    StatModifyRow(label = "قدرت متمرکز ذهن (Mental)", value = player.mentalPower, statId = "mentalPower", points = player.statPoints, viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun EquipmentSlotRow(label: String, value: String, rarity: String) {
    val color = when (rarity) {
        "Mythic" -> Color(0xFFFF0055)
        "Legendary" -> Color(0xFFFF9D00)
        "Epic" -> Color(0xFF9D00FF)
        else -> Color(0xFF0070F3)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = Color.Gray, fontSize = 11.sp)
        Text(
            text = value,
            color = if (value == "خالی") Color.DarkGray else color,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun StatModifyRow(label: String, value: Int, statId: String, points: Int, viewModel: PlayerViewModel) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(text = label, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            Text(text = "مقدار پایه: $value", color = Color.Gray, fontSize = 11.sp)
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = value.toString(),
                color = Color(0xFF00E5FF),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 12.dp)
            )

            if (points > 0) {
                IconButton(
                    onClick = { viewModel.distributeStatPoint(statId) },
                    modifier = Modifier
                        .size(28.dp)
                        .background(Color(0xFF00E5FF), shape = CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "ارتقا",
                        tint = Color.Black,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}


// -----------------------------------------------------
// SCREEN 2: DAILY QUESTS VIEW
// -----------------------------------------------------
@Composable
fun DailyQuestsView(quests: List<Quest>, player: PlayerState, viewModel: PlayerViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "مأموریت‌های روزانه امروز",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            // Test trigger for penalty system setup
            Button(
                onClick = { viewModel.resetDailyQuestsManually() },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0x33FF1744)),
                border = BorderStroke(1.dp, Color(0xFFFF1744)),
                shape = RoundedCornerShape(6.dp),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text("شبیه‌ساز جریمه / ریست", color = Color.White, fontSize = 11.sp)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Streak Count Banner
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0x1F00E5FF)),
            border = BorderStroke(1.dp, Color(0x4400E5FF))
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Streak",
                    tint = Color(0xFFFFD600),
                    modifier = Modifier.size(34.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "تمرینات پیوسته متوالی: ${player.streak} روز",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "هر روز تمرینات را تیک بزنید تا دچار جریمه خطرناک سیستم نشوید!",
                        color = Color.LightGray,
                        fontSize = 11.sp
                    )
                }
            }
        }

        if (quests.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("هیچ مأموریتی بارگذاری نشده است. دکمه بالا را برای تولید مأموریت بزنید.", color = Color.Gray, textAlign = TextAlign.Center)
            }
        } else {
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(quests) { quest ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 5.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (quest.isCompleted) Color(0x1A00E5FF) else Color(0x13FFFFFF)
                        ),
                        border = BorderStroke(
                            1.dp, 
                            if (quest.isCompleted) Color(0xFF00E5FF) else Color(0x22FFFFFF)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = quest.isCompleted,
                                onCheckedChange = { if (!quest.isCompleted) viewModel.completeQuestItem(quest) },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = Color(0xFF00E5FF),
                                    checkmarkColor = Color.Black
                                )
                            )

                            Spacer(modifier = Modifier.width(10.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = quest.title,
                                    color = if (quest.isCompleted) Color.Gray else Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = quest.description,
                                    color = Color.Gray,
                                    fontSize = 11.sp
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Box(
                                    modifier = Modifier
                                        .background(
                                            if (quest.difficulty == "Easy") Color(0x3300FF66) else Color(0x33FF9800),
                                            shape = RoundedCornerShape(4.dp)
                                        )
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = if (quest.difficulty == "Easy") "آسان" else "متوسط",
                                        color = Color.White,
                                        fontSize = 10.sp
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "+ ${quest.xpReward} XP",
                                    color = Color(0xFF00E5FF),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}


// -----------------------------------------------------
// SCREEN 3: DUNGEON LIST VIEW
// -----------------------------------------------------
@Composable
fun DungeonListView(dungeons: List<Dungeon>, player: PlayerState, viewModel: PlayerViewModel) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        item {
            Text(
                text = "کاشف دروازه سیاه‌چال‌ها (Dungeons)",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 10.dp)
            )
        }

        items(dungeons) { d ->
            val isUnlocked = player.level >= d.levelRequired

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isUnlocked) Color(0x1F121226) else Color(0x1a120508)
                ),
                border = BorderStroke(
                    1.dp,
                    if (d.isCleared) Color(0xFF00E5FF) else if (isUnlocked) Color(0x3300E5FF) else Color(0x22FF1744)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = d.name,
                            color = if (isUnlocked) Color.White else Color.Gray,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )

                        if (d.isCleared) {
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFF00E5FF), shape = RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("پاکسازی شده", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        } else if (!isUnlocked) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Lock, contentDescription = "قفل", tint = Color(0xFFFF1744), modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("نیاز به سطح ${d.levelRequired}", color = Color(0xFFFF1744), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Text("در حال بهره‌برداری", color = Color(0xFFFF9D00), fontSize = 11.sp, fontWeight = FontWeight.Medium)
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = d.description, color = Color.Gray, fontSize = 12.sp)

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "فهرست تمرین فیزیکی سیاه‌چال:",
                        color = Color(0xFFD500F9),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    val stepsList = d.questSteps.split(";")
                    stepsList.forEach { step ->
                        Text(
                            text = "• $step",
                            color = Color.LightGray,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(top = 2.dp, start = 8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "پاداش شکست: +${d.xpReward} XP", color = Color(0xFF00E5FF), fontSize = 11.sp)
                            Text(text = "طلا: +${d.goldReward} سکه", color = Color(0xFFFFD600), fontSize = 11.sp)
                        }

                        if (isUnlocked && !d.isCleared) {
                            Button(
                                onClick = { viewModel.startDungeon(d) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text("ورود به دروازه", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}


// -----------------------------------------------------
// SCREEN 4: BOSS FIGHTS (نبرد با غول‌ها)
// -----------------------------------------------------
@Composable
fun BossFightsView(player: PlayerState, viewModel: PlayerViewModel) {
    var bossVictoryAnimMsg by remember { mutableStateOf<String?>(null) }

    val bosses = listOf(
        BossData(
            id = "boss_laziness",
            name = "پادشاه کاهلی و تنبلی (King of Laziness)",
            requiredCondition = "۷ روز متوالی تمرین مداوم بی وقفه در سیستم",
            isMet = player.streak >= 7,
            currentProgress = player.streak,
            targetProgress = 7,
            xp = 1000,
            gold = 2000,
            rewardItem = "لقب افسانه‌ای: بدن آهنین "
        ),
        BossData(
            id = "boss_weakness",
            name = "ارباب ضعف بدنی (Lord of Weakness)",
            requiredCondition = "سی روز متوالی استمرار فعالیت حقیقی ورزشی",
            isMet = player.streak >= 30,
            currentProgress = player.streak,
            targetProgress = 30,
            xp = 3000,
            gold = 5000,
            rewardItem = "لقب حماسی: جنگجوی سایه (Shadow Warrior)"
        ),
        BossData(
            id = "boss_giant",
            name = "غول سایه غول‌پیکر (Shadow Giant)",
            requiredCondition = "دستیابی به حداقل سطح رتبه ۵۰ شکار شکارچیان",
            isMet = player.level >= 50,
            currentProgress = player.level,
            targetProgress = 50,
            xp = 6000,
            gold = 10000,
            rewardItem = "لقب اساطیری: فرمانروای کل سایه‌ها"
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "نبرد با روسای کبیر (Boss Fight System)",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 10.dp)
        )

        bosses.forEach { b ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF140713)),
                border = BorderStroke(1.dp, Color(0x33FF1744))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = b.name,
                        color = Color(0xFFFF1744),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "پیش‌نیاز شکست باس: ${b.requiredCondition}", color = Color.Gray, fontSize = 12.sp)

                    Spacer(modifier = Modifier.height(12.dp))

                    // Progress Tracker Bar
                    val percent = (b.currentProgress.toFloat() / b.targetProgress.toFloat()).coerceIn(0f, 1f)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("میزان آمادگی:", color = Color.LightGray, fontSize = 11.sp, modifier = Modifier.padding(end = 6.dp))
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(6.dp)
                                .background(Color(0xFFFF1744).copy(alpha = 0.2f), shape = RoundedCornerShape(3.dp))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(percent)
                                    .background(Color(0xFFFF1744), shape = RoundedCornerShape(3.dp))
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("${b.currentProgress}/${b.targetProgress}", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "پاداش نبرد: +${b.xp} XP", color = Color(0xFF00E5FF), fontSize = 11.sp)
                            Text(text = "غنائم: ${b.rewardItem}", color = Color(0xFFD500F9), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                if (b.isMet) {
                                    viewModel.defeatBoss(b.name, b.xp, b.gold, b.id)
                                    bossVictoryAnimMsg = "شما با اراده بی‌رحمانه خود باوفا ماندید و راس کریه ${b.name} را سرنگون ساختید!"
                                }
                            },
                            enabled = b.isMet,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFFF1744),
                                disabledContainerColor = Color(0x33FF1744)
                            ),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "مبارزه با غول", 
                                color = if (b.isMet) Color.White else Color.Gray,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }

    if (bossVictoryAnimMsg != null) {
        Dialog(onDismissRequest = { bossVictoryAnimMsg = null }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF02091A)),
                border = BorderStroke(2.dp, Color(0xFF00E5FF)),
                modifier = Modifier.padding(20.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(70.dp)
                            .background(Color(0x1a00E5FF), shape = CircleShape)
                            .border(2.dp, Color(0xFF00E5FF), shape = CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = "پیروزی", tint = Color(0xFF00E5FF), modifier = Modifier.size(40.dp))
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("سیاست باطل شد! پیروزی بزرگ", color = Color(0xFF00E5FF), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = bossVictoryAnimMsg!!,
                        color = Color.White,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = { bossVictoryAnimMsg = null },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF))
                    ) {
                        Text("غارت جوایز و اتمام سیاهچال", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

data class BossData(
    val id: String,
    val name: String,
    val requiredCondition: String,
    val isMet: Boolean,
    val currentProgress: Int,
    val targetProgress: Int,
    val xp: Int,
    val gold: Int,
    val rewardItem: String
)


// -----------------------------------------------------
// SCREEN 5: SKILL TREE VIEW (درخت مهارت‌ها)
// -----------------------------------------------------
@Composable
fun SkillTreeView(player: PlayerState, viewModel: PlayerViewModel) {
    val skills = listOf(
        SkillNode("Warrior", "مسیر جنگجو (Warrior Path)", "افزایش قدرت مطلق ضربات و توان جسمی عمیق.", player.activeSkillWarriorLevel, Icons.Default.Star),
        SkillNode("Athlete", "مسیر ورزشکار (Athlete Path)", "استقامت ریوی، چابکی فرار و سرعت جابجایی.", player.activeSkillAthleteLevel, Icons.Default.PlayArrow),
        SkillNode("Leader", "مسیر رهبری (Leader Path)", "جذبه کاریزما برای مدیریت کارهای جمعی صنف.", player.activeSkillLeaderLevel, Icons.Default.Person),
        SkillNode("Scholar", "مسیر دانشمند (Scholar Path)", "سرعت عمل در یادگیری کتب درسی و چند برابر کننده فکری.", player.activeSkillScholarLevel, Icons.Default.Info),
        SkillNode("Shadow", "مسیر شکارچی سایه (Shadow Hunter)", "افزایش احتمال کشف مأموریت‌های مخفی غنی.", player.activeSkillShadowLevel, Icons.Default.Lock)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "درخت مهارت‌های فعال شکارچی",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 10.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0x13FFFFFF)),
            border = BorderStroke(1.dp, Color(0x22FFFFFF))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "نحوه ارتقای درخت مهارت:",
                    color = Color(0xFF00E5FF),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "با افزایش سطح شکارچی، ۵ امتیاز ارتقا دریافت می‌کنید. خرج کردن هر نود مهارتی به ۲ امتیاز ارتقا نیاز دارد.",
                    color = Color.LightGray,
                    fontSize = 11.sp
                )
            }
        }

        skills.forEach { s ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0x1F121226)),
                border = BorderStroke(1.dp, Color(0x2200E5FF))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color(0x1F00E5FF), shape = CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = s.icon, contentDescription = null, tint = Color(0xFF00E5FF))
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(text = s.title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text(text = s.desc, color = Color.Gray, fontSize = 11.sp)
                            Text(text = "سطح فعلی ارتقا: ${s.currentLevel}", color = Color(0xFFD500F9), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Button(
                        onClick = { viewModel.upgradeSkillNode(s.id) },
                        enabled = player.statPoints >= 2,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text("ارتقا (۲ امتیاز)", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

data class SkillNode(
    val id: String,
    val title: String,
    val desc: String,
    val currentLevel: Int,
    val icon: ImageVector
)


// -----------------------------------------------------
// SCREEN 6: STORE AND INVENTORY (فروشگاه و کوله‌پشتی)
// -----------------------------------------------------
@Composable
fun StoreAndLootView(inventory: List<InventoryItem>, player: PlayerState, viewModel: PlayerViewModel) {
    var isStoreMode by remember { mutableStateOf(false) }

    val storeItems = listOf(
        StoreProduct("معجون سرخ التیام", 200, "Potion", "معجون بازسازی توان عضلانی برای رفع خستگی ورزشی."),
        StoreProduct("پوشن طلایی کمال طلایی", 500, "Potion", "بهترین دارو برای احیای روان و ارتقای تمامی توانایی‌ها."),
        StoreProduct("بلیت سیاهچال تمرینی", 100, "Ticket", "بلیت آزمایشی ویژه برای شرکت در هر تعداد سیاهچال."),
        StoreProduct("تقویت‌کننده دو برابری XP", 400, "Booster", "تاثیر EXP تمرینات روزانه را به مدت ۲ ساعت دوبرابر کمال می‌کند.")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        // Toggle view
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isStoreMode) "فروشگاه مرکزی شکارچی" else "کوله‌پشتی و وسایل شکارچی (Inventory)",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Button(
                onClick = { isStoreMode = !isStoreMode },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD500F9)),
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    text = if (isStoreMode) "نمایش کیف وسایل" else "ورود به فروشگاه",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (isStoreMode) {
            // Purchase view
            LazyColumn(modifier = Modifier.weight(1f)) {
                item {
                    Text(text = "سکه طلای شما: ${player.gold} سکه", color = Color(0xFFFFD600), fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.padding(bottom = 12.dp))
                }

                items(storeItems) { p ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 5.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0x1F121226)),
                        border = BorderStroke(1.dp, Color(0x3300E5FF))
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = p.name, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Text(text = p.desc, color = Color.Gray, fontSize = 11.sp, modifier = Modifier.padding(top = 2.dp))
                            }

                            Button(
                                onClick = { viewModel.purchaseStoreProduct(p.name, p.price, p.type) },
                                enabled = player.gold >= p.price,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFFFD600),
                                    disabledContainerColor = Color(0x33FFD600)
                                ),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = Color.Black, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text("${p.price} سکه", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                item {
                    Divider(color = Color(0x11FFFFFF), modifier = Modifier.padding(vertical = 12.dp))
                    Text(text = "لاتاری بخت آزمایی سیستم (Loot Box Drawing)", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 6.dp))
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0x13FFFFFF)),
                        border = BorderStroke(1.dp, Color(0x22FFFFFF))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(text = "صندوق غنائم تصادفی شانس سیستم سولو", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text(text = "درصد شانس: ۵۰٪ معمولی، ۲۵٪ کمیاب، ۱۵٪ حماسی، ۸٪ افسانه‌ای، ۲٪ اساطیری!", color = Color.Gray, fontSize = 10.sp)

                            Spacer(modifier = Modifier.height(12.dp))

                            Button(
                                onClick = { viewModel.purchaseLootBoxTicket("بلیط لوت باکس شانس طلایی", 400) },
                                enabled = player.gold >= 400,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text("خرید بلیت لوت باکس شانس (۴۰۰ سکه)", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        } else {
            // Inventory display view
            if (inventory.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("کوله پشتی شما در حال حاضر خالی است.", color = Color.Gray, textAlign = TextAlign.Center)
                }
            } else {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(inventory) { item ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 5.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0x13FFFFFF)),
                            border = BorderStroke(
                                1.dp,
                                if (item.isEquipped) Color(0xFF00E5FF) else Color(0x22FFFFFF)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .background(Color(0x1F00E5FF), shape = CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = when (item.type) {
                                                "Equipable" -> Icons.Default.Lock
                                                "Potion" -> Icons.Default.Favorite
                                                "Ticket" -> Icons.Default.Star
                                                else -> Icons.Default.Info
                                            },
                                            contentDescription = null,
                                            tint = Color(0xFF00E5FF)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(10.dp))

                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(text = item.name, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(text = "تعداد: ${item.quantity}", color = Color.Gray, fontSize = 11.sp)
                                        }
                                        Text(text = item.description, color = Color.Gray, fontSize = 11.sp)
                                        if (item.attributeBonus != null) {
                                            Text(text = "قدرت اثر: ${item.attributeBonus}", color = Color(0xFFD500F9), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }

                                Row {
                                    if (item.type == "Equipable" || item.type == "Title") {
                                        Button(
                                            onClick = { viewModel.toggleEquipItem(item) },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (item.isEquipped) Color.DarkGray else Color(0xFF00E5FF)
                                            ),
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = if (item.isEquipped) "غیرفعال‌سازی" else "تجهیز کردن",
                                                color = if (item.isEquipped) Color.White else Color.Black,
                                                fontSize = 10.sp
                                            )
                                        }
                                    } else if (item.type == "Potion") {
                                        Button(
                                            onClick = { viewModel.usePotion(item) },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text("نوشیدن / مصرف", color = Color.Black, fontSize = 10.sp)
                                        }
                                    } else if (item.type == "Ticket") {
                                        Button(
                                            onClick = { viewModel.openLootBoxWithChance(item) },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD500F9)),
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text("استفاده و قرعه", color = Color.White, fontSize = 10.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

data class StoreProduct(
    val name: String,
    val price: Int,
    val type: String,
    val desc: String
)


// -----------------------------------------------------
// SCREEN 7: GUILD, DEEP RANKING & ACHIEVEMENTS (صنف)
// -----------------------------------------------------
@Composable
fun GuildAndRankingView(
    guild: GuildState?,
    player: PlayerState,
    achievements: List<Achievement>,
    viewModel: PlayerViewModel
) {
    var isGuildMode by remember { mutableStateOf(true) }
    var newGuildName by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isGuildMode) "سامانه اصناف شکارچیان (Guild)" else "افتخارات و مدال‌های جهانی رنک",
                color = Color.White,
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold
            )

            Button(
                onClick = { isGuildMode = !isGuildMode },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    text = if (isGuildMode) "رنک جهانی" else "سیستم صنف",
                    color = Color.Black,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (isGuildMode) {
            // GUILD SYSTEM WORKFLOW
            if (player.guildName == null) {
                // Not in guild, create one
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0x1F121226)),
                    border = BorderStroke(1.dp, Color(0x3300E5FF))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = "شما عضو هیچ صنفی نیستید!", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        Text(text = "برای تسریع پیشرفت و فعال کردن چالش‌های تیمی روزانه، اولین صنف اختصاصی خود را با پرداخت سکه تاسیس کنید.", color = Color.Gray, fontSize = 11.sp)

                        Spacer(modifier = Modifier.height(14.dp))

                        OutlinedTextField(
                            value = newGuildName,
                            onValueChange = { newGuildName = it },
                            label = { Text("نام پیشنهادی صنف", color = Color.Gray) },
                            textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.White),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = {
                                if (newGuildName.isNotBlank() && player.gold >= 1000) {
                                    viewModel.createOrUpgradeGuild(newGuildName)
                                }
                            },
                            enabled = player.gold >= 1000 && newGuildName.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFFFD600),
                                disabledContainerColor = Color(0x33FFD600)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("تاسیس صنف شخصی (۱۰۰۰ سکه طلا)", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                // Already has a guild
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0x1F121226)),
                    border = BorderStroke(1.dp, Color(0xFF00E5FF))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = "صنف: ${player.guildName}", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                Text(text = "سمت شما: رهبر صنف (Leader)", color = Color(0xFFD500F9), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFF00E5FF), shape = RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("سطح ۲ صنف", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Text(text = "تعداد اعضای شکارچی: ۷ نفر همکار", color = Color.Gray, fontSize = 11.sp)
                        Text(text = "قدرت همبستگی صنف: +۱۵٪ دریافت طلا از مأموریت‌ها", color = Color(0xFF00E5FF), fontSize = 11.sp)

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(text = "مأموریت‌های فعال صنف:", color = Color.LightGray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text(text = "• تیک زدن مجموعاً ۲۰ مأموریت روزانه تیمی (۷۵٪ با موفقیت انجام شد)", color = Color.Gray, fontSize = 11.sp)
                    }
                }
            }
        } else {
            // WORLD RANKINGS (رنکینگ فرضی و مدال‌ها)
            LazyColumn(modifier = Modifier.weight(1f)) {
                item {
                    Text(text = "جدول رده‌بندی شکارچیان جهانی (Solo)", color = Color(0xFFD500F9), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                }

                val leaderMock = listOf(
                    Triple("رتبه ۱: سونگ جین‌وو (پادشاه سایه‌ها)", "سطح ۹۹ (S-Rank SSS)", "فعال"),
                    Triple("رتبه ۲: توماس آندره (گلیات ملی)", "سطح ۹۲ (National S)", "فعال"),
                    Triple("رتبه ۳: لیو ژیگانگ (قهرمان چین)", "سطح ۸۹ (National S)", "فعال"),
                    Triple("رتبه ۴: کریستوفر رید (آمریکا)", "سطح ۸۸ (National S)", "غیرفعال"),
                    Triple("رتبه ۵: ${player.name} (شما)", "سطح ${player.level} (${player.rank})", "خودکار")
                )

                items(leaderMock) { l ->
                    val isUser = l.first.contains("شما")
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isUser) Color(0x1F00E5FF) else Color(0x0EFFFFFF)
                        ),
                        border = BorderStroke(1.dp, if (isUser) Color(0xFF00E5FF) else Color(0x11FFFFFF))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = l.first, color = if (isUser) Color(0xFF00E5FF) else Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text(text = l.second, color = Color.Gray, fontSize = 11.sp)
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = "افتخارات ثبت شده شما (Achievements):", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                }

                items(achievements) { a ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 5.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0x13FFFFFF)),
                        border = BorderStroke(1.dp, if (a.isUnlocked) Color(0xFF00E5FF) else Color(0x11FFFFFF))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = a.title, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                if (a.isUnlocked) {
                                    Text(text = "آزاد شده", color = Color(0xFF00E5FF), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                } else {
                                    Text(text = "${a.progress}/${a.maxProgress}", color = Color.Gray, fontSize = 11.sp)
                                }
                            }
                            Text(text = a.description, color = Color.Gray, fontSize = 11.sp, modifier = Modifier.padding(top = 2.dp))
                        }
                    }
                }
            }
        }
    }
}


// -----------------------------------------------------
// SCREEN OVERLAYS: DUNGEON RUN COUNTDOWN TIMER HUD
// -----------------------------------------------------
@Composable
fun ActiveDungeonOverlay(
    dungeon: Dungeon,
    timeLeftSeconds: Int,
    stepsProgress: List<Boolean>,
    onStepCheck: (Int) -> Unit,
    onExit: () -> Unit
) {
    val mins = timeLeftSeconds / 60
    val secs = timeLeftSeconds % 60
    val timeFormatted = String.format("%02d:%02d", mins, secs)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xEE020208))
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF050B18)),
            border = BorderStroke(2.dp, Color(0xFF00E5FF))
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "هشدار: سیاه‌چال در حال تصرف",
                    color = ColorxFFFF1744, // Using standard Color helper
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = dungeon.name,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // HIGH-INTENSITY COUNTDOWN RUNNER
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0x33FF1744)),
                    border = BorderStroke(1.5.dp, Color(0xFFFF1744)),
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = timeFormatted,
                        color = Color.White,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "خواهشمند است تمرینات فیزیکی زیر را در جهان واقعی با اراده بالا انجام داده و پس از پایان هر یک تیک تایید را بزنید:",
                    color = Color.LightGray,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                val steps = dungeon.questSteps.split(";")
                Column {
                    steps.forEachIndexed { idx, s ->
                        val completed = stepsProgress.getOrElse(idx) { false }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onStepCheck(idx) }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = completed,
                                onCheckedChange = { onStepCheck(idx) },
                                colors = CheckboxDefaults.colors(checkedColor = Color(0xFF00E5FF))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = s,
                                color = if (completed) Color.Gray else Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onExit,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF1744)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("انصراف و پذیرش شکست در سیاهچال", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

val ColorxFFFF1744 = Color(0xFFFF1744)


// -----------------------------------------------------
// SCREEN OVERLAYS: SYSTEM FAILURE PENALTY OVERLAY SYSTEM
// -----------------------------------------------------
@Composable
fun PenaltyOverlay(onAcknowledge: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFB1F0307))
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = "جریمه سیستم",
            tint = Color(0xFFFF1744),
            modifier = Modifier.size(90.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "تنبیه انضباطی صادر شد",
            color = Color(0xFFFF1744),
            fontSize = 24.sp,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "The System Is Disappointed In You.\nسیستم از عدم تداوم شما ناراضی است.",
            color = Color.White,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "به دلیل عدم تکمیل مأموریت‌های روزانه دیروز، ۱۰۰ امتیاز XP کسر شد و زنجیره تداوم تمرین (Streak) شما کاملاً منحل گردید.",
            color = Color.LightGray,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )

        Spacer(modifier = Modifier.height(40.dp))

        Button(
            onClick = onAcknowledge,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF1744)),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Text("قوانین سیستم را می‌پذیرم و برمی‌خیزم", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}


// -----------------------------------------------------
// SCREEN OVERLAYS: LOOT BOX ROLL ANIMATOR View
// -----------------------------------------------------
@Composable
fun LootBoxAnimationView(
    isOpening: Boolean,
    reward: LootBoxReward?,
    onClaim: (LootBoxReward) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xF2030308))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        if (isOpening) {
            // Unlocked roll loading spinner
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(
                    color = Color(0xFFD500F9),
                    modifier = Modifier.size(64.dp),
                    strokeWidth = 6.dp
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "سیستم در حال چرخش و کشف لوت باکس...",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "دریچه‌ای از غنائم شانس در حال گشوده شدن است.",
                    color = Color.Gray,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        } else if (reward != null) {
            val rarityColor = Color(reward.colorHex)

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF020712)),
                border = BorderStroke(2.dp, rarityColor),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .background(rarityColor.copy(alpha = 0.15f), shape = CircleShape)
                            .border(2.dp, rarityColor, shape = CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = reward.rarity,
                            tint = rarityColor,
                            modifier = Modifier.size(42.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "رتبه‌ی غارت: ${reward.rarity}",
                        color = rarityColor,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = reward.title,
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = reward.subtitle,
                        color = Color.Gray,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 2.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Divider(color = Color(0x11FFFFFF))

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "تاثیر اثرگذار: ${reward.bonusDescription}",
                        color = Color(0xFF00E5FF),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = { onClaim(reward) },
                        colors = ButtonDefaults.buttonColors(containerColor = rarityColor),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("غارت آیتم و افزودن به کوله‌پشتی", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}


// -----------------------------------------------------
// SCREEN OVERLAYS: HIDDEN QUEST POPUP ALERT GATEWAY
// -----------------------------------------------------
@Composable
fun HiddenQuestDiscoveryDialog(message: String, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0A0214)),
            border = BorderStroke(2.dp, Color(0xFFD500F9)),
            modifier = Modifier.padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Hidden Quest",
                    tint = Color(0xFFD500F9),
                    modifier = Modifier.size(54.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "مأموریت مخفی کشف شد!",
                    color = Color(0xFFD500F9),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Black
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = message,
                    color = Color.White,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD500F9))
                ) {
                    Text("پذیرفتن مأموریت پنهانی", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
