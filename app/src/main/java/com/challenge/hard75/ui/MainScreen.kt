package com.challenge.hard75.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.challenge.hard75.viewmodel.ChallengeViewModel

// ─── Navigation ───────────────────────────────────────────────────────────────

sealed class Screen { object Home : Screen(); object Setup : Screen(); object Settings : Screen() }

@Composable
fun AppNav(viewModel: ChallengeViewModel) {
    val state by viewModel.uiState.collectAsState()
    var screen by remember { mutableStateOf<Screen>(Screen.Home) }

    Hard75Theme {
        Surface(modifier = Modifier.fillMaxSize(), color = DarkBg) {
            if (!state.challengeStarted && screen == Screen.Home) {
                SetupScreen(viewModel) { screen = Screen.Home }
            } else {
                when (screen) {
                    is Screen.Home -> HomeScreen(viewModel, onSettings = { screen = Screen.Settings })
                    is Screen.Settings -> SettingsScreen(viewModel, onBack = { screen = Screen.Home })
                    else -> HomeScreen(viewModel, onSettings = { screen = Screen.Settings })
                }
            }
        }
    }
}

// ─── Setup Screen ─────────────────────────────────────────────────────────────

@Composable
fun SetupScreen(viewModel: ChallengeViewModel, onDone: () -> Unit) {
    val state by viewModel.uiState.collectAsState()
    var newRule by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(60.dp))
        Text("75 HARD", fontSize = 48.sp, fontWeight = FontWeight.Black, color = TextPrimary)
        Text("Set your rules before you begin", color = TextSecondary, fontSize = 14.sp)
        Spacer(Modifier.height(40.dp))

        // Default rules hint
        if (state.rules.isEmpty()) {
            listOf(
                "Drink 1 gallon of water",
                "Follow a diet — no alcohol",
                "2 workouts (one outdoors)",
                "Read 10 pages non-fiction",
                "Take a progress photo"
            ).forEach { hint ->
                TextButton(onClick = { viewModel.addRule(hint) }) {
                    Icon(Icons.Default.Add, null, tint = Green, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(hint, color = TextSecondary, fontSize = 13.sp)
                }
            }
            Spacer(Modifier.height(8.dp))
        }

        // Current rules list
        LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth()) {
            items(state.rules) { rule ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(CardColor)
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.DragHandle, null, tint = TextSecondary, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(10.dp))
                    Text(rule.text, color = TextPrimary, modifier = Modifier.weight(1f))
                    IconButton(onClick = { viewModel.deleteRule(rule) }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }

        // Add rule input
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = newRule,
                onValueChange = { newRule = it },
                placeholder = { Text("Add a custom rule...", color = TextSecondary) },
                modifier = Modifier.weight(1f),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedBorderColor = Green,
                    unfocusedBorderColor = BorderColor,
                    cursorColor = Green
                ),
                singleLine = true
            )
            Spacer(Modifier.width(8.dp))
            IconButton(
                onClick = { viewModel.addRule(newRule); newRule = "" },
                modifier = Modifier
                    .size(48.dp)
                    .background(Green, CircleShape)
            ) {
                Icon(Icons.Default.Add, null, tint = Color.Black)
            }
        }

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = { if (state.rules.isNotEmpty()) { viewModel.startChallenge(); onDone() } },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            enabled = state.rules.isNotEmpty(),
            colors = ButtonDefaults.buttonColors(containerColor = Green),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("START THE CHALLENGE", fontWeight = FontWeight.Bold, color = Color.Black, fontSize = 16.sp)
        }
        Spacer(Modifier.height(24.dp))
    }
}

// ─── Home Screen ──────────────────────────────────────────────────────────────

@Composable
fun HomeScreen(viewModel: ChallengeViewModel, onSettings: () -> Unit) {
    val state by viewModel.uiState.collectAsState()
    var showResetDialog by remember { mutableStateOf(false) }

    if (showResetDialog) {
        ResetDialog(
            onConfirm = { viewModel.resetChallenge(); showResetDialog = false },
            onDismiss = { showResetDialog = false }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top bar
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 52.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "75 HARD",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    letterSpacing = 4.sp
                )
                Row {
                    IconButton(onClick = { showResetDialog = true }) {
                        Icon(Icons.Default.Refresh, "Reset", tint = TextSecondary)
                    }
                    IconButton(onClick = onSettings) {
                        Icon(Icons.Default.Settings, "Settings", tint = TextSecondary)
                    }
                }
            }
        }

        // Hero Day Counter
        item {
            Spacer(Modifier.height(16.dp))
            HeroDayCounter(day = state.currentDay, allDone = state.allDoneToday)
            Spacer(Modifier.height(32.dp))
        }

        // Progress bar
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("TASKS TODAY", color = TextSecondary, fontSize = 11.sp, letterSpacing = 2.sp)
                    Text(
                        "${state.completedRuleIds.size}/${state.rules.size}",
                        color = if (state.allDoneToday) Green else TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { if (state.rules.isEmpty()) 0f else state.completedRuleIds.size.toFloat() / state.rules.size },
                    modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                    color = Green,
                    trackColor = BorderColor
                )
            }
            Spacer(Modifier.height(20.dp))
        }

        // Checklist
        items(state.rules) { rule ->
            val isChecked = rule.id in state.completedRuleIds
            RuleItem(
                text = rule.text,
                checked = isChecked,
                onToggle = { viewModel.toggleRule(rule.id, it) }
            )
        }

        // 75-Day Grid
        item {
            Spacer(Modifier.height(32.dp))
            Text("YOUR HORIZON", color = TextSecondary, fontSize = 11.sp, letterSpacing = 2.sp,
                modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(12.dp))
            DayGrid(
                currentDay = state.currentDay,
                completedDates = state.fullyCompletedDates,
                startDateMillis = null // simplified: use currentDay for coloring
            )
            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
fun HeroDayCounter(day: Int, allDone: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = if (allDone) 1.03f else 1f,
        animationSpec = infiniteRepeatable(tween(1000), RepeatMode.Reverse),
        label = "scale"
    )

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(200.dp)
                .scale(scale)
                .clip(CircleShape)
                .background(
                    if (allDone)
                        Brush.radialGradient(listOf(Color(0xFF2E7D32), Color(0xFF1B5E20)))
                    else
                        Brush.radialGradient(listOf(CardColor, SurfaceColor))
                )
                .border(2.dp, if (allDone) Green else BorderColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "$day",
                    fontSize = 80.sp,
                    fontWeight = FontWeight.Black,
                    color = if (allDone) Green else TextPrimary,
                    lineHeight = 80.sp
                )
                Text(
                    text = "of 75",
                    fontSize = 16.sp,
                    color = TextSecondary,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        if (allDone) {
            Spacer(Modifier.height(12.dp))
            Text(
                "✓ DAY COMPLETE",
                color = Green,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 3.sp
            )
        }
    }
}

@Composable
fun RuleItem(text: String, checked: Boolean, onToggle: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(CardColor)
            .clickable { onToggle(!checked) }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(if (checked) Green else Color.Transparent)
                .border(2.dp, if (checked) Green else BorderColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (checked) Icon(Icons.Default.Check, null, tint = Color.Black, modifier = Modifier.size(14.dp))
        }
        Spacer(Modifier.width(14.dp))
        Text(
            text = text,
            color = if (checked) TextSecondary else TextPrimary,
            fontSize = 16.sp,
            textDecoration = if (checked) TextDecoration.LineThrough else TextDecoration.None,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun DayGrid(currentDay: Int, completedDates: List<String>, startDateMillis: Long?) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(15),
        modifier = Modifier.fillMaxWidth().height(130.dp),
        contentPadding = PaddingValues(0.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(75) { index ->
            val dayNum = index + 1
            val isPast = dayNum < currentDay
            val isToday = dayNum == currentDay

            Box(
                modifier = Modifier
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(3.dp))
                    .background(
                        when {
                            isPast -> Green.copy(alpha = 0.8f)
                            isToday -> Amber
                            else -> BorderColor
                        }
                    )
            )
        }
    }
    Spacer(Modifier.height(8.dp))
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        LegendDot(color = Green, label = "Done")
        LegendDot(color = Amber, label = "Today")
        LegendDot(color = BorderColor, label = "Ahead")
    }
}

@Composable
fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(color))
        Spacer(Modifier.width(4.dp))
        Text(label, color = TextSecondary, fontSize = 11.sp)
    }
}

// ─── Settings Screen ──────────────────────────────────────────────────────────

@Composable
fun SettingsScreen(viewModel: ChallengeViewModel, onBack: () -> Unit) {
    val state by viewModel.uiState.collectAsState()
    var newRule by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .padding(20.dp)
    ) {
        Spacer(Modifier.height(52.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, null, tint = TextPrimary)
            }
            Text("Settings", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        }
        Spacer(Modifier.height(24.dp))

        Text("END OF DAY CUTOFF", color = TextSecondary, fontSize = 11.sp, letterSpacing = 2.sp)
        Spacer(Modifier.height(8.dp))
        Text(
            "Tasks reset at ${state.endOfDayHour}:00 AM. Adjust if you stay up past midnight.",
            color = TextSecondary, fontSize = 13.sp
        )
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(1, 2, 3, 4).forEach { h ->
                FilterChip(
                    selected = state.endOfDayHour == h,
                    onClick = { viewModel.setEndOfDayHour(h) },
                    label = { Text("${h}AM") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Green,
                        selectedLabelColor = Color.Black
                    )
                )
            }
        }

        Spacer(Modifier.height(32.dp))
        Text("MANAGE RULES", color = TextSecondary, fontSize = 11.sp, letterSpacing = 2.sp)
        Spacer(Modifier.height(8.dp))

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(state.rules) { rule ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(CardColor)
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(rule.text, color = TextPrimary, modifier = Modifier.weight(1f), fontSize = 14.sp)
                    IconButton(onClick = { viewModel.deleteRule(rule) }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Delete, null, tint = Color(0xFFEF5350), modifier = Modifier.size(18.dp))
                    }
                }
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = newRule,
                onValueChange = { newRule = it },
                placeholder = { Text("New rule...", color = TextSecondary) },
                modifier = Modifier.weight(1f),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary,
                    focusedBorderColor = Green, unfocusedBorderColor = BorderColor,
                    cursorColor = Green
                ),
                singleLine = true
            )
            Spacer(Modifier.width(8.dp))
            IconButton(
                onClick = { viewModel.addRule(newRule); newRule = "" },
                modifier = Modifier.size(48.dp).background(Green, CircleShape)
            ) {
                Icon(Icons.Default.Add, null, tint = Color.Black)
            }
        }
    }
}

// ─── Reset Dialog ─────────────────────────────────────────────────────────────

@Composable
fun ResetDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(CardColor)
                .padding(24.dp)
        ) {
            Text("Reset Challenge?", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 18.sp)
            Spacer(Modifier.height(8.dp))
            Text(
                "This will restart from Day 1. All progress will be lost.",
                color = TextSecondary, fontSize = 14.sp
            )
            Spacer(Modifier.height(20.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = onDismiss, modifier = Modifier.weight(1f),
                    border = BorderStroke(1.dp, BorderColor)
                ) { Text("Cancel", color = TextSecondary) }
                Button(
                    onClick = onConfirm, modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF5350))
                ) { Text("Reset", color = Color.White) }
            }
        }
    }
}
