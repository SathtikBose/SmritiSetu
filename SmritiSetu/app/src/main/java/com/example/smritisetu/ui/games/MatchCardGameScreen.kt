package com.example.smritisetu.ui.games

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smritisetu.data.AuthManager
import com.example.smritisetu.data.CognitiveGameLog
import com.example.smritisetu.data.LocalAppStrings
import com.example.smritisetu.theme.GlassCard
import com.example.smritisetu.theme.getGlassGradientBrush
import com.example.smritisetu.theme.isAppInDarkTheme
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class CardItem(
    val id: Int,
    val pairId: Int,
    val icon: ImageVector,
    val label: String,
    var isFlipped: Boolean = false,
    var isMatched: Boolean = false,
    var isHighlighted: Boolean = false
)

private val culturalSymbols = listOf(
    Pair(Icons.Default.Spa, "Tea Leaf"),
    Pair(Icons.Default.Audiotrack, "Bihu Dhol"),
    Pair(Icons.Default.WbSunny, "River Dawn"),
    Pair(Icons.Default.LocalFlorist, "Kopou Orchid"),
    Pair(Icons.Default.Palette, "Gamusa Motif"),
    Pair(Icons.Default.EmojiNature, "Kaziranga Rhino"),
    Pair(Icons.Default.Star, "Golden Star"),
    Pair(Icons.Default.CrueltyFree, "Peacock"),
    Pair(Icons.Default.Notifications, "Temple Bell"),
    Pair(Icons.Default.Park, "Rainforest"),
    Pair(Icons.Default.Sailing, "River Boat"),
    Pair(Icons.Default.Yard, "Muga Silk"),
    Pair(Icons.Default.FilterVintage, "Sacred Lotus"),
    Pair(Icons.Default.MusicNote, "Bihu Horn"),
    Pair(Icons.Default.Favorite, "Warm Hearth"),
    Pair(Icons.Default.Diamond, "Heritage Gem")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchCardGameScreen(
    initialLevel: Int = 1,
    onNavigateToShop: () -> Unit = {},
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val themeMode by AuthManager.themeMode.collectAsState()
    val currentUser by AuthManager.currentUser.collectAsState()
    val hintsCount by AuthManager.hintsCount.collectAsState()
    val skipLevelCount by AuthManager.skipLevelCount.collectAsState()
    val darkTheme = isAppInDarkTheme(themeMode)
    val strings = LocalAppStrings.current

    var currentLevel by remember { mutableIntStateOf(initialLevel) }
    var triesCount by remember { mutableIntStateOf(0) }
    var hintsTriggeredCount by remember { mutableIntStateOf(0) }
    var levelStartTime by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var showVictoryDialog by remember { mutableStateOf(false) }
    var showTimesUpDialog by remember { mutableStateOf(false) }
    var showBuyPromptDialog by remember { mutableStateOf<String?>(null) }
    var levelTimeElapsedSeconds by remember { mutableLongStateOf(0L) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Determine monotonic pair count per level (e.g. Level 6 = 7 pairs / 14 cards, Level 7+ = 8 pairs / 16 cards)
    fun getPairCountForLevel(level: Int): Int {
        return when (level) {
            1 -> 2 // 4 cards
            2 -> 3 // 6 cards
            3 -> 4 // 8 cards
            4 -> 5 // 10 cards
            5 -> 6 // 12 cards
            6 -> 7 // 14 cards
            else -> 8 // 16 cards (elder vision cap with maximum symbol pool randomness)
        }
    }

    // Timers: Easy = 150s, Normal = 100s, Hard = 50s
    fun getTimeLimitForLevel(level: Int): Int {
        return when {
            level <= 5 -> 150 // Easy 150s
            level <= 10 -> 100 // Normal 100s
            else -> 50 // Hard 50s
        }
    }

    fun getIdleHintThresholdForLevel(level: Int): Long {
        return when {
            level <= 5 -> 5000L // 5 seconds on early levels
            level <= 10 -> 6000L // 6 seconds
            else -> 7000L // 7 seconds on harder levels
        }
    }

    fun getDifficultyName(level: Int): String {
        return when {
            level <= 5 -> "EASY"
            level <= 10 -> "NORMAL"
            else -> "HARD"
        }
    }

    // Cognitive Memory Exposure Time for the First Clicked Card (Adjusted by Difficulty)
    fun getFirstCardExposureMs(level: Int): Long {
        return when {
            level <= 5 -> 4500L // Easy (150s total): 4.5 seconds to observe and memorize
            level <= 10 -> 3000L // Normal (100s total): 3.0 seconds
            else -> 1800L // Hard (50s total): 1.8 seconds (requires prompt memory recall)
        }
    }

    // Time remaining state
    var timeRemainingSeconds by remember(currentLevel) { mutableIntStateOf(getTimeLimitForLevel(currentLevel)) }

    // Generate cards for current level with high randomness from 16-symbol pool
    var cards by remember(currentLevel) {
        val numPairs = getPairCountForLevel(currentLevel)
        val selectedSymbols = culturalSymbols.shuffled().take(numPairs)
        val cardList = mutableListOf<CardItem>()
        var cardId = 0
        selectedSymbols.forEachIndexed { pairIndex, (icon, label) ->
            cardList.add(CardItem(id = cardId++, pairId = pairIndex, icon = icon, label = label))
            cardList.add(CardItem(id = cardId++, pairId = pairIndex, icon = icon, label = label))
        }
        cardList.shuffle()
        mutableStateOf(cardList.toList())
    }

    // Selected cards for matching & auto-flip coroutine job
    var firstSelectedCard by remember { mutableStateOf<CardItem?>(null) }
    var secondSelectedCard by remember { mutableStateOf<CardItem?>(null) }
    var isProcessingMatch by remember { mutableStateOf(false) }
    var autoFlipBackJob by remember { mutableStateOf<Job?>(null) }

    // Inactivity timer state
    var lastInteractionTime by remember { mutableLongStateOf(System.currentTimeMillis()) }

    // Reset state on level change
    fun resetCurrentLevel() {
        autoFlipBackJob?.cancel()
        triesCount = 0
        hintsTriggeredCount = 0
        levelStartTime = System.currentTimeMillis()
        lastInteractionTime = System.currentTimeMillis()
        timeRemainingSeconds = getTimeLimitForLevel(currentLevel)
        firstSelectedCard = null
        secondSelectedCard = null
        isProcessingMatch = false
        showVictoryDialog = false
        showTimesUpDialog = false
        showBuyPromptDialog = null

        val numPairs = getPairCountForLevel(currentLevel)
        val selectedSymbols = culturalSymbols.shuffled().take(numPairs)
        val cardList = mutableListOf<CardItem>()
        var cardId = 0
        selectedSymbols.forEachIndexed { pairIndex, (icon, label) ->
            cardList.add(CardItem(id = cardId++, pairId = pairIndex, icon = icon, label = label))
            cardList.add(CardItem(id = cardId++, pairId = pairIndex, icon = icon, label = label))
        }
        cardList.shuffle()
        cards = cardList.toList()
    }

    LaunchedEffect(currentLevel) {
        resetCurrentLevel()
    }

    // Live Countdown Timer Coroutine
    LaunchedEffect(currentLevel, showVictoryDialog, showTimesUpDialog) {
        if (!showVictoryDialog && !showTimesUpDialog) {
            while (timeRemainingSeconds > 0) {
                delay(1000L)
                if (!showVictoryDialog && !showTimesUpDialog) {
                    timeRemainingSeconds--
                    if (timeRemainingSeconds <= 0) {
                        showTimesUpDialog = true
                    }
                }
            }
        }
    }

    // Inactivity Auto-Highlight Hint Coroutine
    val idleThreshold = remember(currentLevel) { getIdleHintThresholdForLevel(currentLevel) }
    LaunchedEffect(lastInteractionTime, cards, isProcessingMatch, showVictoryDialog, showTimesUpDialog) {
        if (!isProcessingMatch && !showVictoryDialog && !showTimesUpDialog && cards.any { !it.isMatched }) {
            delay(idleThreshold)
            val unmatchedUnflipped = cards.filter { !it.isMatched && !it.isFlipped }
            if (unmatchedUnflipped.isNotEmpty()) {
                val pairIdToHighlight = unmatchedUnflipped.first().pairId
                cards = cards.map { card ->
                    if (card.pairId == pairIdToHighlight && !card.isMatched) {
                        card.copy(isHighlighted = true)
                    } else {
                        card.copy(isHighlighted = false)
                    }
                }
                hintsTriggeredCount++
            }
        }
    }

    // Pulse animation for hints
    val infiniteTransition = rememberInfiniteTransition(label = "hintPulse")
    val hintPulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "hintPulseScale"
    )

    // Handle Hint Perk Trigger
    fun triggerHintPerk() {
        if (hintsCount > 0) {
            val used = AuthManager.useHint()
            if (used) {
                val unmatched = cards.filter { !it.isMatched }
                if (unmatched.isNotEmpty()) {
                    val targetPairId = unmatched.first().pairId
                    cards = cards.map {
                        if (it.pairId == targetPairId) it.copy(isHighlighted = true) else it.copy(isHighlighted = false)
                    }
                    scope.launch { snackbarHostState.showSnackbar(strings.hintActive) }
                }
            }
        } else {
            showBuyPromptDialog = "hint"
        }
    }

    // Handle Skip Level Perk Trigger
    fun triggerSkipLevelPerk() {
        if (skipLevelCount > 0) {
            val used = AuthManager.useSkipLevel()
            if (used) {
                val timeElapsedMs = System.currentTimeMillis() - levelStartTime
                levelTimeElapsedSeconds = timeElapsedMs / 1000

                // Mark all cards as matched
                cards = cards.map { it.copy(isMatched = true, isFlipped = true) }

                AuthManager.addRewards(xp = 15, coins = 200)
                AuthManager.unlockNextLevel(currentLevel)

                showVictoryDialog = true
            }
        } else {
            showBuyPromptDialog = "skip"
        }
    }

    // Handle Card Click
    fun onCardClick(card: CardItem) {
        if (isProcessingMatch || card.isFlipped || card.isMatched || showTimesUpDialog || showVictoryDialog) return

        lastInteractionTime = System.currentTimeMillis()

        // Remove highlights on user action
        cards = cards.map { it.copy(isHighlighted = false) }

        // Flip card
        cards = cards.map { if (it.id == card.id) it.copy(isFlipped = true) else it }

        if (firstSelectedCard == null) {
            firstSelectedCard = card
            autoFlipBackJob?.cancel()
            val exposureMs = getFirstCardExposureMs(currentLevel)
            autoFlipBackJob = scope.launch {
                delay(exposureMs)
                // If user hasn't clicked a 2nd card during exposure window, flip 1st card back to challenge memory!
                if (firstSelectedCard?.id == card.id && secondSelectedCard == null && !isProcessingMatch) {
                    cards = cards.map {
                        if (it.id == card.id && !it.isMatched) it.copy(isFlipped = false) else it
                    }
                    firstSelectedCard = null
                }
            }
        } else if (secondSelectedCard == null && card.id != firstSelectedCard?.id) {
            // Cancel auto-flip job since user tapped the 2nd card
            autoFlipBackJob?.cancel()
            secondSelectedCard = card
            triesCount++
            isProcessingMatch = true

            scope.launch {
                delay(800L)
                if (firstSelectedCard?.pairId == card.pairId) {
                    // Match found!
                    cards = cards.map {
                        if (it.pairId == card.pairId) it.copy(isMatched = true, isFlipped = true) else it
                    }
                } else {
                    // Missed: flip back
                    cards = cards.map {
                        if (it.id == firstSelectedCard?.id || it.id == card.id) it.copy(isFlipped = false) else it
                    }
                }
                firstSelectedCard = null
                secondSelectedCard = null
                isProcessingMatch = false

                // Check if all cards matched
                if (cards.all { it.isMatched }) {
                    val timeElapsedMs = System.currentTimeMillis() - levelStartTime
                    levelTimeElapsedSeconds = timeElapsedMs / 1000

                    // Record cognitive telemetry to console / logcat
                    val telemetryLog = CognitiveGameLog(
                        gameName = "MatchTheCard",
                        level = currentLevel,
                        tries = triesCount,
                        totalCards = cards.size,
                        timeElapsedMs = timeElapsedMs,
                        hintsUsed = hintsTriggeredCount,
                        difficulty = getDifficultyName(currentLevel)
                    )
                    AuthManager.recordGameTelemetry(telemetryLog)

                    // Award 15 XP + 200 Coins & Unlock next level!
                    AuthManager.addRewards(xp = 15, coins = 200)
                    AuthManager.unlockNextLevel(currentLevel)

                    showVictoryDialog = true
                }
            }
        }
    }

    // Quick Buy Prompt Dialog when 0 perks remaining
    if (showBuyPromptDialog != null) {
        AlertDialog(
            onDismissRequest = { showBuyPromptDialog = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Storefront,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (showBuyPromptDialog == "hint") strings.buyHint else strings.buySkipLevel,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            },
            text = {
                Text(
                    text = if (showBuyPromptDialog == "hint") strings.noHintsLeft else strings.noSkipsLeft,
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showBuyPromptDialog = null
                        onNavigateToShop()
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Visit Shop", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showBuyPromptDialog = null }) {
                    Text(strings.cancel)
                }
            }
        )
    }

    // Time's Up Dialog (User must replay level)
    if (showTimesUpDialog) {
        AlertDialog(
            onDismissRequest = { },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        Icons.Default.HourglassEmpty,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = strings.timesUp,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            text = {
                Text(
                    text = strings.timesUpSubtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        resetCurrentLevel()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(strings.replayGame, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(strings.selectLevel)
                }
            }
        )
    }

    // Victory Dialog
    if (showVictoryDialog) {
        AlertDialog(
            onDismissRequest = { },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = strings.levelComplete,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "${strings.level} $currentLevel Completed!",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // Rewards Summary
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = strings.xpReward,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.tertiaryContainer
                        ) {
                            Text(
                                text = strings.coinsReward,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onTertiaryContainer,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                            )
                        }
                    }

                    // Cognitive Stats
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = strings.tries,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "$triesCount",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = strings.timeTaken,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${levelTimeElapsedSeconds}s",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showVictoryDialog = false
                        currentLevel++ // Advance to next level directly!
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(strings.nextLevel, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(strings.selectLevel)
                }
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth().padding(end = 12.dp)
                    ) {
                        // Level Badge
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = "${strings.level} $currentLevel",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }

                        // Top-Right Coins Balance Counter (Clickable to open Shop)
                        Surface(
                            onClick = onNavigateToShop,
                            shape = RoundedCornerShape(18.dp),
                            color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.85f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.4f))
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MonetizationOn,
                                    contentDescription = "Coins",
                                    tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${currentUser?.coins ?: 1000}",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = strings.back
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToShop) {
                        Icon(
                            imageVector = Icons.Default.Storefront,
                            contentDescription = "Shop",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        },
        bottomBar = {
            // In-Game Bottom Perks Bar
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                shape = RoundedCornerShape(22.dp),
                color = if (darkTheme) Color(0xEB162421) else Color(0xEBFFFFFF),
                border = BorderStroke(1.dp, if (darkTheme) Color(0x33FFFFFF) else Color(0x66FFFFFF)),
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Hint Perk Button
                    OutlinedButton(
                        onClick = { triggerHintPerk() },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(1f),
                        border = BorderStroke(1.2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.6f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lightbulb,
                            contentDescription = strings.useHint,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${strings.useHint} ($hintsCount)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Skip Level Perk Button
                    OutlinedButton(
                        onClick = { triggerSkipLevelPerk() },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(1f),
                        border = BorderStroke(1.2.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.FastForward,
                            contentDescription = strings.useSkip,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${strings.useSkip} ($skipLevelCount)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        },
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(getGlassGradientBrush(darkTheme))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 18.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Info Bar: Live Timer + Tries Counter + Difficulty
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Live Countdown Timer
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (timeRemainingSeconds <= 15) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.85f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = "Timer",
                                tint = if (timeRemainingSeconds <= 15) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${timeRemainingSeconds}s",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (timeRemainingSeconds <= 15) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    // Tries Counter
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    ) {
                        Text(
                            text = "${strings.tries}: $triesCount",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }

                if (cards.any { it.isHighlighted }) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.85f)
                    ) {
                        Text(
                            text = strings.hintActive,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Card Grid: 2 columns for <= 6 cards, 3 columns for 8 to 12 cards, 4 columns for 14 to 16 cards
                val gridColumns = when {
                    cards.size <= 6 -> 2
                    cards.size <= 12 -> 3
                    else -> 4
                }

                LazyVerticalGrid(
                    columns = GridCells.Fixed(gridColumns),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(bottom = 12.dp)
                ) {
                    items(cards, key = { it.id }) { card ->
                        FlippableCardTile(
                            card = card,
                            isProcessingMatch = isProcessingMatch,
                            hintPulseScale = hintPulseScale,
                            gridColumns = gridColumns,
                            darkTheme = darkTheme,
                            onClick = { onCardClick(card) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FlippableCardTile(
    card: CardItem,
    isProcessingMatch: Boolean,
    hintPulseScale: Float,
    gridColumns: Int,
    darkTheme: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isRevealed = card.isFlipped || card.isMatched

    // Animate rotation from 0f (back) to 180f (front)
    val rotation by animateFloatAsState(
        targetValue = if (isRevealed) 180f else 0f,
        animationSpec = tween(durationMillis = 380, easing = FastOutSlowInEasing),
        label = "cardFlipAnimation"
    )

    val cardScale = if (card.isHighlighted) hintPulseScale else 1.0f

    val borderStroke = when {
        card.isHighlighted -> BorderStroke(2.5.dp, Color(0xFFF59E0B))
        card.isMatched -> BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
        else -> BorderStroke(1.dp, if (darkTheme) Color(0x33FFFFFF) else Color(0x66FFFFFF))
    }

    val cardBgColor = when {
        card.isMatched -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.85f)
        rotation > 90f -> if (darkTheme) Color(0xEB1E332E) else Color(0xF5FFFFFF)
        card.isHighlighted -> Color(0xFFFEF3C7)
        else -> if (darkTheme) Color(0xCC1A2B27) else Color(0xE6FFFFFF)
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1.0f)
            .scale(cardScale)
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 14f * density
            }
            .clickable(enabled = !isRevealed && !isProcessingMatch) {
                onClick()
            },
        shape = RoundedCornerShape(16.dp),
        color = cardBgColor,
        border = borderStroke,
        shadowElevation = if (card.isHighlighted) 8.dp else 3.dp
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize().padding(6.dp)
        ) {
            if (rotation > 90f) {
                // Front Face (Counter-rotated by 180deg so content isn't mirrored horizontally)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.graphicsLayer { rotationY = 180f }
                ) {
                    Icon(
                        imageVector = card.icon,
                        contentDescription = card.label,
                        tint = if (card.isMatched) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(if (gridColumns == 4) 26.dp else 34.dp)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = card.label,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                        fontSize = if (gridColumns == 4) 8.sp else 10.sp,
                        maxLines = 1
                    )
                }
            } else {
                // Back Face (Pattern / Icon)
                Icon(
                    imageVector = Icons.Default.Psychology,
                    contentDescription = "Hidden Card",
                    tint = if (card.isHighlighted) Color(0xFFD97706) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f),
                    modifier = Modifier.size(if (gridColumns == 4) 24.dp else 30.dp)
                )
            }
        }
    }
}
