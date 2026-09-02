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
    Pair(Icons.Default.EmojiNature, "Kaziranga Nature"),
    Pair(Icons.Default.Star, "Golden Star"),
    Pair(Icons.Default.CrueltyFree, "Peacock Spirit")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchCardGameScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val themeMode by AuthManager.themeMode.collectAsState()
    val currentUser by AuthManager.currentUser.collectAsState()
    val darkTheme = isAppInDarkTheme(themeMode)
    val strings = LocalAppStrings.current

    var currentLevel by remember { mutableIntStateOf(1) }
    var triesCount by remember { mutableIntStateOf(0) }
    var hintsTriggeredCount by remember { mutableIntStateOf(0) }
    var levelStartTime by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var showVictoryDialog by remember { mutableStateOf(false) }
    var levelTimeElapsedSeconds by remember { mutableLongStateOf(0L) }

    val scope = rememberCoroutineScope()

    // Determine number of pairs for current level
    // Level 1: 2 pairs (4 cards)
    // Level 2: 3 pairs (6 cards)
    // Level 3: 4 pairs (8 cards)
    // Level 4: 5 pairs (10 cards)
    // Level 5: 6 pairs (12 cards)
    // Level 6+: (Level - 1) % 5 + 2 pairs
    fun getPairCountForLevel(level: Int): Int {
        val basePairs = ((level - 1) % 5) + 2
        return basePairs.coerceIn(2, culturalSymbols.size)
    }

    // Generate cards for current level
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

    // Selected cards for matching
    var firstSelectedCard by remember { mutableStateOf<CardItem?>(null) }
    var secondSelectedCard by remember { mutableStateOf<CardItem?>(null) }
    var isProcessingMatch by remember { mutableStateOf(false) }

    // Inactivity timer state: auto-highlight matching cards after 6 seconds of idle
    var lastInteractionTime by remember { mutableLongStateOf(System.currentTimeMillis()) }

    LaunchedEffect(currentLevel) {
        triesCount = 0
        hintsTriggeredCount = 0
        levelStartTime = System.currentTimeMillis()
        lastInteractionTime = System.currentTimeMillis()
        firstSelectedCard = null
        secondSelectedCard = null
        isProcessingMatch = false
        showVictoryDialog = false
    }

    // Inactivity Auto-Highlight Hint Coroutine (6 seconds of no clicks)
    LaunchedEffect(lastInteractionTime, cards, isProcessingMatch) {
        if (!isProcessingMatch && cards.any { !it.isMatched }) {
            delay(6000L) // 6 seconds idle threshold
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

    // Handle Card Click
    fun onCardClick(card: CardItem) {
        if (isProcessingMatch || card.isFlipped || card.isMatched) return

        lastInteractionTime = System.currentTimeMillis()

        // Remove highlights on user action
        cards = cards.map { it.copy(isHighlighted = false) }

        // Flip card
        cards = cards.map { if (it.id == card.id) it.copy(isFlipped = true) else it }

        if (firstSelectedCard == null) {
            firstSelectedCard = card
        } else if (secondSelectedCard == null && card.id != firstSelectedCard?.id) {
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
                        difficulty = "NORMAL"
                    )
                    AuthManager.recordGameTelemetry(telemetryLog)

                    // Award 15 XP + 200 Coins
                    AuthManager.addRewards(xp = 15, coins = 200)

                    showVictoryDialog = true
                }
            }
        }
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
                        currentLevel++ // Advance to next level (1..5, 6..10, 11..15, etc.)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(strings.nextLevel, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    Scaffold(
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

                        // Top-Right Coins Balance Counter
                        Surface(
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
                }
            )
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
                    .padding(horizontal = 18.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Game Subtitle / Hint Banner
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = strings.findPairs,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    ) {
                        Text(
                            text = "${strings.tries}: $triesCount",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                if (cards.any { it.isHighlighted }) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.8f)
                    ) {
                        Text(
                            text = strings.hintActive,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Card Grid (2 to 3 columns depending on card count)
                val gridColumns = if (cards.size <= 6) 2 else 3

                LazyVerticalGrid(
                    columns = GridCells.Fixed(gridColumns),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(cards, key = { it.id }) { card ->
                        val isRevealed = card.isFlipped || card.isMatched
                        val cardScale = if (card.isHighlighted) hintPulseScale else 1.0f

                        val borderStroke = when {
                            card.isHighlighted -> BorderStroke(2.5.dp, Color(0xFFF59E0B))
                            card.isMatched -> BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
                            else -> BorderStroke(1.dp, if (darkTheme) Color(0x33FFFFFF) else Color(0x66FFFFFF))
                        }

                        val cardBgColor = when {
                            card.isMatched -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.85f)
                            isRevealed -> if (darkTheme) Color(0xEB1E332E) else Color(0xF5FFFFFF)
                            card.isHighlighted -> Color(0xFFFEF3C7)
                            else -> if (darkTheme) Color(0xCC1A2B27) else Color(0xE6FFFFFF)
                        }

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1.0f)
                                .scale(cardScale)
                                .clickable(enabled = !isRevealed && !isProcessingMatch) {
                                    onCardClick(card)
                                },
                            shape = RoundedCornerShape(20.dp),
                            color = cardBgColor,
                            border = borderStroke,
                            shadowElevation = if (card.isHighlighted) 8.dp else 4.dp
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize().padding(8.dp)
                            ) {
                                if (isRevealed) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = card.icon,
                                            contentDescription = card.label,
                                            tint = if (card.isMatched) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.size(36.dp)
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = card.label,
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                            color = MaterialTheme.colorScheme.onSurface,
                                            textAlign = TextAlign.Center,
                                            fontSize = 10.sp
                                        )
                                    }
                                } else {
                                    // Hidden card back (Serene Lotus / Smriti symbol)
                                    Icon(
                                        imageVector = Icons.Default.Psychology,
                                        contentDescription = "Hidden Card",
                                        tint = if (card.isHighlighted) Color(0xFFD97706) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
