package com.example.smritisetu.ui.games

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smritisetu.data.AuthManager
import com.example.smritisetu.data.CognitiveGameLog
import com.example.smritisetu.data.LocalAppStrings
import com.example.smritisetu.data.PerkType
import com.example.smritisetu.theme.GlassCard
import com.example.smritisetu.theme.getGlassGradientBrush
import com.example.smritisetu.theme.isAppInDarkTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class PatternSymbol(
    val id: Int,
    val name: String,
    val icon: ImageVector,
    val colorHex: Long
)

private val symbolPool = listOf(
    PatternSymbol(1, "Red Triangle", Icons.Default.ChangeHistory, 0xFFFF5252),
    PatternSymbol(2, "Cyan Diamond", Icons.Default.Diamond, 0xFF00E5FF),
    PatternSymbol(3, "Amber Circle", Icons.Default.Circle, 0xFFFFD600),
    PatternSymbol(4, "Green Square", Icons.Default.Square, 0xFF00E676),
    PatternSymbol(5, "Golden Star", Icons.Default.Star, 0xFFFF6D00),
    PatternSymbol(6, "Lotus Blossom", Icons.Default.LocalFlorist, 0xFFFF4081),
    PatternSymbol(7, "Green Leaf", Icons.Default.Spa, 0xFF69F0AE),
    PatternSymbol(8, "Temple Bell", Icons.Default.Notifications, 0xFF7C4DFF)
)

data class PatternLevelData(
    val sequence: List<PatternSymbol>,
    val correctNextSymbol: PatternSymbol,
    val choices: List<PatternSymbol>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatternGameScreen(
    initialLevel: Int = 1,
    onNavigateToShop: () -> Unit = {},
    onNavigateBack: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val themeMode by AuthManager.themeMode.collectAsState()
    val currentUser by AuthManager.currentUser.collectAsState()
    val hintsCount by AuthManager.hintsCount.collectAsState()
    val showAgainCount by AuthManager.showAgainCount.collectAsState()
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
    var eliminatedChoiceIds by remember { mutableStateOf<Set<Int>>(emptySet()) }
    var selectedWrongChoiceId by remember { mutableStateOf<Int?>(null) }
    var isSuccessAnimation by remember { mutableStateOf(false) }

    fun getPreviewDurationForLevel(level: Int): Int {
        return when {
            level <= 5 -> 6 // Easy: 6 seconds to memorize
            level <= 10 -> 4 // Normal: 4 seconds
            else -> 3 // Hard: 3 seconds
        }
    }

    var isPatternPreviewActive by remember { mutableStateOf(true) }
    var previewTimeRemainingSeconds by remember { mutableIntStateOf(getPreviewDurationForLevel(initialLevel)) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    fun getTimeLimitForLevel(level: Int): Int {
        return when {
            level <= 5 -> 60 // Easy 60s
            level <= 10 -> 45 // Normal 45s
            else -> 30 // Hard 30s
        }
    }

    fun getDifficultyName(level: Int): String {
        return when {
            level <= 5 -> "EASY"
            level <= 10 -> "NORMAL"
            else -> "HARD"
        }
    }

    // Procedural Pattern Generator
    fun generatePatternForLevel(level: Int): PatternLevelData {
        val shuffledPool = symbolPool.shuffled()

        when {
            // Easy: AB AB A? (Repeating 2 symbols, length 4 or 5)
            level <= 5 -> {
                val symA = shuffledPool[0]
                val symB = shuffledPool[1]
                val patternTemplate = listOf(symA, symB)
                val sequenceLength = if (level <= 2) 4 else 5
                val fullSequence = (0 until sequenceLength).map { patternTemplate[it % 2] }
                val correctNext = patternTemplate[sequenceLength % 2]

                val distractors = shuffledPool.filter { it.id != correctNext.id }.take(2)
                val choices = (distractors + correctNext).shuffled()
                return PatternLevelData(fullSequence, correctNext, choices)
            }

            // Normal: ABC ABC AB? or AABB AAB? (3 symbols or double step, length 6)
            level <= 10 -> {
                val isDoubleStep = level % 2 == 0
                if (isDoubleStep) {
                    val symA = shuffledPool[0]
                    val symB = shuffledPool[1]
                    val patternTemplate = listOf(symA, symA, symB, symB)
                    val sequenceLength = 6
                    val fullSequence = (0 until sequenceLength).map { patternTemplate[it % 4] }
                    val correctNext = patternTemplate[sequenceLength % 4]

                    val distractors = shuffledPool.filter { it.id != correctNext.id }.take(3)
                    val choices = (distractors + correctNext).shuffled()
                    return PatternLevelData(fullSequence, correctNext, choices)
                } else {
                    val symA = shuffledPool[0]
                    val symB = shuffledPool[1]
                    val symC = shuffledPool[2]
                    val patternTemplate = listOf(symA, symB, symC)
                    val sequenceLength = 6
                    val fullSequence = (0 until sequenceLength).map { patternTemplate[it % 3] }
                    val correctNext = patternTemplate[sequenceLength % 3]

                    val distractors = shuffledPool.filter { it.id != correctNext.id }.take(3)
                    val choices = (distractors + correctNext).shuffled()
                    return PatternLevelData(fullSequence, correctNext, choices)
                }
            }

            // Hard: ABAC ABAC A? (Complex alternating patterns, length 7)
            else -> {
                val symA = shuffledPool[0]
                val symB = shuffledPool[1]
                val symC = shuffledPool[2]
                val symD = shuffledPool[3]
                val patternTemplate = listOf(symA, symB, symA, symC, symA, symD)
                val sequenceLength = 7
                val fullSequence = (0 until sequenceLength).map { patternTemplate[it % 6] }
                val correctNext = patternTemplate[sequenceLength % 6]

                val distractors = shuffledPool.filter { it.id != correctNext.id }.take(3)
                val choices = (distractors + correctNext).shuffled()
                return PatternLevelData(fullSequence, correctNext, choices)
            }
        }
    }

    var levelData by remember(currentLevel) { mutableStateOf(generatePatternForLevel(currentLevel)) }
    var timeRemainingSeconds by remember(currentLevel) { mutableIntStateOf(getTimeLimitForLevel(currentLevel)) }

    fun resetCurrentLevel() {
        triesCount = 0
        hintsTriggeredCount = 0
        levelStartTime = System.currentTimeMillis()
        timeRemainingSeconds = getTimeLimitForLevel(currentLevel)
        isPatternPreviewActive = true
        previewTimeRemainingSeconds = getPreviewDurationForLevel(currentLevel)
        showVictoryDialog = false
        showTimesUpDialog = false
        showBuyPromptDialog = null
        eliminatedChoiceIds = emptySet()
        selectedWrongChoiceId = null
        isSuccessAnimation = false
        levelData = generatePatternForLevel(currentLevel)
    }

    LaunchedEffect(currentLevel) {
        resetCurrentLevel()
    }

    // Pattern Preview Countdown (Memorization phase)
    LaunchedEffect(currentLevel, isPatternPreviewActive, showVictoryDialog, showTimesUpDialog) {
        if (isPatternPreviewActive && !showVictoryDialog && !showTimesUpDialog) {
            while (previewTimeRemainingSeconds > 0) {
                delay(1000L)
                if (isPatternPreviewActive && !showVictoryDialog && !showTimesUpDialog) {
                    previewTimeRemainingSeconds--
                    if (previewTimeRemainingSeconds <= 0) {
                        isPatternPreviewActive = false
                    }
                }
            }
        }
    }

    // Main Game Countdown Timer (Runs only after pattern is hidden)
    LaunchedEffect(currentLevel, isPatternPreviewActive, showVictoryDialog, showTimesUpDialog) {
        if (!isPatternPreviewActive && !showVictoryDialog && !showTimesUpDialog) {
            while (timeRemainingSeconds > 0) {
                delay(1000L)
                if (!isPatternPreviewActive && !showVictoryDialog && !showTimesUpDialog) {
                    timeRemainingSeconds--
                    if (timeRemainingSeconds <= 0) {
                        showTimesUpDialog = true
                    }
                }
            }
        }
    }

    // Handle Choice Selection
    fun onChoiceSelected(symbol: PatternSymbol) {
        if (isPatternPreviewActive || isSuccessAnimation || showVictoryDialog || showTimesUpDialog || eliminatedChoiceIds.contains(symbol.id)) return

        if (symbol.id == levelData.correctNextSymbol.id) {
            // Correct Choice!
            isSuccessAnimation = true
            val timeElapsedMs = System.currentTimeMillis() - levelStartTime
            levelTimeElapsedSeconds = timeElapsedMs / 1000

            // Record cognitive telemetry
            val telemetryLog = CognitiveGameLog(
                gameName = "PatternMatching",
                level = currentLevel,
                tries = triesCount + 1,
                totalCards = levelData.sequence.size + 1,
                timeElapsedMs = timeElapsedMs,
                hintsUsed = hintsTriggeredCount,
                difficulty = getDifficultyName(currentLevel)
            )
            AuthManager.recordGameTelemetry(telemetryLog)

            scope.launch {
                delay(600L)
                // Award 15 XP + 200 Coins & Unlock next level
                AuthManager.addRewards(xp = 15, coins = 200)
                AuthManager.unlockNextPatternLevel(currentLevel)
                showVictoryDialog = true
            }
        } else {
            // Wrong Choice: gentle feedback
            triesCount++
            selectedWrongChoiceId = symbol.id
            scope.launch {
                delay(800L)
                selectedWrongChoiceId = null
            }
        }
    }

    // Hint Perk Handler (Eliminates 1 incorrect choice)
    fun triggerHintPerk() {
        if (hintsCount > 0) {
            val used = AuthManager.useHint()
            if (used) {
                hintsTriggeredCount++
                val wrongChoices = levelData.choices.filter { it.id != levelData.correctNextSymbol.id && !eliminatedChoiceIds.contains(it.id) }
                if (wrongChoices.isNotEmpty()) {
                    val toEliminate = wrongChoices.first().id
                    eliminatedChoiceIds = eliminatedChoiceIds + toEliminate
                    scope.launch {
                        snackbarHostState.showSnackbar("💡 Hint used: 1 incorrect option eliminated!")
                    }
                }
            }
        } else {
            showBuyPromptDialog = "hint"
        }
    }

    // Show Again (Peek Pattern) Perk Handler
    fun triggerShowAgainPerk() {
        if (showAgainCount > 0) {
            val used = AuthManager.useShowAgain()
            if (used) {
                isPatternPreviewActive = true
                previewTimeRemainingSeconds = 4 // Peek for 4 seconds
                scope.launch {
                    snackbarHostState.showSnackbar("👁️ Peek activated! Pattern re-revealed for 4 seconds.")
                }
            }
        } else {
            showBuyPromptDialog = "peek"
        }
    }

    // Skip Level Perk Handler
    fun triggerSkipLevelPerk() {
        if (skipLevelCount > 0) {
            val used = AuthManager.useSkipLevel()
            if (used) {
                val timeElapsedMs = System.currentTimeMillis() - levelStartTime
                levelTimeElapsedSeconds = timeElapsedMs / 1000

                AuthManager.addRewards(xp = 15, coins = 200)
                AuthManager.unlockNextPatternLevel(currentLevel)
                showVictoryDialog = true
            }
        } else {
            showBuyPromptDialog = "skip"
        }
    }

    // Quick Buy Prompt Dialog when 0 perks remaining
    if (showBuyPromptDialog != null) {
        val perkType = when (showBuyPromptDialog) {
            "hint" -> PerkType.HINT
            "peek" -> PerkType.SHOW_AGAIN
            else -> PerkType.SKIP_LEVEL
        }

        AlertDialog(
            onDismissRequest = { showBuyPromptDialog = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = when (showBuyPromptDialog) {
                            "hint" -> Icons.Default.Lightbulb
                            "peek" -> Icons.Default.Visibility
                            else -> Icons.Default.FastForward
                        },
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Get ${perkType.displayName}",
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = when (showBuyPromptDialog) {
                            "hint" -> "You have 0 Extra Hints left. Purchase 1 Extra Hint for 1,000 Coins?"
                            "peek" -> "You have 0 Show Again perks left. Purchase 1 Peek Perk for 800 Coins to reveal the pattern again?"
                            else -> "You have 0 Skip Level perks left. Purchase 1 Skip Level for 2,000 Coins?"
                        }
                    )
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
                    ) {
                        Text(
                            text = "Your Balance: ${currentUser?.coins ?: 0} Coins",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val result = AuthManager.buyPerk(perkType)
                        showBuyPromptDialog = null
                        if (result.isSuccess) {
                            scope.launch {
                                snackbarHostState.showSnackbar("Purchased 1 ${perkType.displayName}!")
                            }
                            if (perkType == PerkType.SHOW_AGAIN) {
                                triggerShowAgainPerk()
                            }
                        } else {
                            scope.launch {
                                snackbarHostState.showSnackbar(result.exceptionOrNull()?.message ?: "Insufficient coins")
                            }
                        }
                    }
                ) {
                    Text("Buy Now (${perkType.costCoins} Coins)")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBuyPromptDialog = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Victory Dialog
    if (showVictoryDialog) {
        AlertDialog(
            onDismissRequest = { /* Force explicit button action */ },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = "Victory",
                        tint = Color(0xFFFFD700),
                        modifier = Modifier.size(32.dp)
                    )
                    Text(
                        text = strings.levelComplete,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Great cognitive focus! You identified the pattern correctly.",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )

                    // Rewards Banner
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "+15 XP",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "League XP",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                )
                            }
                            VerticalDivider(modifier = Modifier.height(32.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "+200 Coins",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.tertiary
                                )
                                Text(
                                    text = "Brain Coins",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }

                    // Stat summary
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Tries",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "$triesCount",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Time",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${levelTimeElapsedSeconds}s",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showVictoryDialog = false
                        currentLevel++
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(strings.nextLevel)
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        showVictoryDialog = false
                        resetCurrentLevel()
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(strings.replayGame)
                }
            }
        )
    }

    // Time's Up Dialog
    if (showTimesUpDialog) {
        AlertDialog(
            onDismissRequest = { /* Force action */ },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.HourglassEmpty,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = strings.timesUp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            text = {
                Text("Take a deep breath and try again! Cognitive patterns take practice and steady observation.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showTimesUpDialog = false
                        resetCurrentLevel()
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Try Again")
                }
            },
            dismissButton = {
                TextButton(onClick = onNavigateBack) {
                    Text("Levels")
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
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 8.dp)
                    ) {
                        Column {
                            Text(
                                text = "Pattern Match • ${strings.level} $currentLevel",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "${getDifficultyName(currentLevel)} • Memorize & Guess",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = when (getDifficultyName(currentLevel)) {
                                    "EASY" -> Color(0xFF4CAF50)
                                    "NORMAL" -> Color(0xFFFF9800)
                                    else -> Color(0xFFF44336)
                                }
                            )
                        }

                        // Coins pill
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.8f)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MonetizationOn,
                                    contentDescription = "Coins",
                                    tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${currentUser?.coins ?: 1000}",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
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
                }
            )
        },
        bottomBar = {
            // Floating Glassmorphic Bottom Perk Bar
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                shape = RoundedCornerShape(22.dp),
                color = if (darkTheme) Color(0xEB162421) else Color(0xEBFFFFFF),
                border = BorderStroke(1.2.dp, if (darkTheme) Color(0x33FFFFFF) else Color(0x66FFFFFF)),
                shadowElevation = 10.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 1. Hint Perk
                    PerkBarButton(
                        icon = Icons.Default.Lightbulb,
                        title = "Hint",
                        count = hintsCount,
                        cost = 1000,
                        color = MaterialTheme.colorScheme.secondary,
                        onClick = { triggerHintPerk() },
                        modifier = Modifier.weight(1f)
                    )

                    // 2. Show Again (Peek) Perk
                    PerkBarButton(
                        icon = Icons.Default.Visibility,
                        title = "Peek",
                        count = showAgainCount,
                        cost = 800,
                        color = MaterialTheme.colorScheme.primary,
                        onClick = { triggerShowAgainPerk() },
                        modifier = Modifier.weight(1f)
                    )

                    // 3. Skip Level Perk
                    PerkBarButton(
                        icon = Icons.Default.FastForward,
                        title = "Skip",
                        count = skipLevelCount,
                        cost = 2000,
                        color = MaterialTheme.colorScheme.tertiary,
                        onClick = { triggerSkipLevelPerk() },
                        modifier = Modifier.weight(1f)
                    )
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
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Timer & Preview Indicator Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Timer pill
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (timeRemainingSeconds <= 10 && !isPatternPreviewActive) MaterialTheme.colorScheme.errorContainer
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = null,
                                tint = if (timeRemainingSeconds <= 10 && !isPatternPreviewActive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isPatternPreviewActive) "Ready: ${timeRemainingSeconds}s" else "${timeRemainingSeconds}s",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (timeRemainingSeconds <= 10 && !isPatternPreviewActive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    // Memorization / Recall State Badge
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isPatternPreviewActive) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f)
                        else MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.9f)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = if (isPatternPreviewActive) Icons.Default.Visibility else Icons.Default.Psychology,
                                contentDescription = null,
                                tint = if (isPatternPreviewActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isPatternPreviewActive) "Memorize: ${previewTimeRemainingSeconds}s" else "Recall Active",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (isPatternPreviewActive) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }

                // Dynamic Prompt Banner
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    darkTheme = darkTheme
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (isPatternPreviewActive)
                                "👁️ Memorize the pattern before it disappears!"
                            else
                                "🧠 What comes next in the sequence?",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (isPatternPreviewActive)
                                "Carefully observe the order of symbols ($previewTimeRemainingSeconds seconds remaining)"
                            else
                                "Select the missing symbol from memory before the time runs out.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // Pattern Sequence Area (Shows real symbols during preview, mystery hidden tiles during guessing)
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    darkTheme = darkTheme
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Render sequence items
                            levelData.sequence.forEachIndexed { index, symbol ->
                                if (isPatternPreviewActive || isSuccessAnimation) {
                                    PatternItemTile(
                                        symbol = symbol,
                                        stepNumber = index + 1
                                    )
                                } else {
                                    PatternHiddenTile(
                                        stepNumber = index + 1
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            }

                            // The Missing Slot Card (?)
                            Box(
                                modifier = Modifier
                                    .size(68.dp)
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(
                                        if (isSuccessAnimation) Color(levelData.correctNextSymbol.colorHex).copy(alpha = 0.3f)
                                        else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                                    )
                                    .padding(4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSuccessAnimation) {
                                    Icon(
                                        imageVector = levelData.correctNextSymbol.icon,
                                        contentDescription = "Correct",
                                        tint = Color(levelData.correctNextSymbol.colorHex),
                                        modifier = Modifier.size(38.dp)
                                    )
                                } else {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = "?",
                                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Section Title: Pick the next pattern
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isPatternPreviewActive) "Memorizing sequence..." else "Select what comes next:",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    if (isPatternPreviewActive) {
                        Text(
                            text = "Unlocks in ${previewTimeRemainingSeconds}s",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Choices Grid: 2 columns with large, high-contrast touch targets
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(230.dp)
                ) {
                    items(levelData.choices, key = { it.id }) { symbol ->
                        val isEliminated = eliminatedChoiceIds.contains(symbol.id)
                        val isSelectedWrong = selectedWrongChoiceId == symbol.id

                        ChoiceTile(
                            symbol = symbol,
                            isEliminated = isEliminated,
                            isSelectedWrong = isSelectedWrong,
                            enabled = !isPatternPreviewActive && !isEliminated,
                            onClick = { onChoiceSelected(symbol) },
                            darkTheme = darkTheme
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PerkBarButton(
    icon: ImageVector,
    title: String,
    count: Int,
    cost: Int,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = color.copy(alpha = 0.14f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.35f)),
        modifier = modifier.height(52.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 6.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = color,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Column(
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = color
                )
                Text(
                    text = if (count > 0) "x$count" else "$cost C",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun PatternItemTile(
    symbol: PatternSymbol,
    stepNumber: Int
) {
    val symbolColor = Color(symbol.colorHex)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(symbolColor.copy(alpha = 0.2f))
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = symbol.icon,
                contentDescription = symbol.name,
                tint = symbolColor,
                modifier = Modifier.size(34.dp)
            )
        }
        Text(
            text = "#$stepNumber",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun PatternHiddenTile(
    stepNumber: Int
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.HelpOutline,
                contentDescription = "Hidden #$stepNumber",
                tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
                modifier = Modifier.size(28.dp)
            )
        }
        Text(
            text = "#$stepNumber",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ChoiceTile(
    symbol: PatternSymbol,
    isEliminated: Boolean,
    isSelectedWrong: Boolean,
    enabled: Boolean = true,
    onClick: () -> Unit,
    darkTheme: Boolean
) {
    val symbolColor = Color(symbol.colorHex)

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .clip(RoundedCornerShape(20.dp))
            .clickable(enabled = enabled && !isEliminated) { onClick() },
        shape = RoundedCornerShape(20.dp),
        darkTheme = darkTheme
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    when {
                        isSelectedWrong -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.6f)
                        isEliminated -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        !enabled -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                        else -> Color.Transparent
                    }
                )
                .padding(10.dp),
            contentAlignment = Alignment.Center
        ) {
            if (isEliminated) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Eliminated",
                    tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                    modifier = Modifier.size(30.dp)
                )
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = symbol.icon,
                        contentDescription = symbol.name,
                        tint = if (isSelectedWrong) MaterialTheme.colorScheme.error
                        else if (!enabled) symbolColor.copy(alpha = 0.4f)
                        else symbolColor,
                        modifier = Modifier.size(38.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = symbol.name,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (isSelectedWrong) MaterialTheme.colorScheme.error
                        else if (!enabled) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}
