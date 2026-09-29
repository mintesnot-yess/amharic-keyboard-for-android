package com.example

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.AmharicTransliterationEngine
import com.example.engine.BackspaceStep
import com.example.ime.KeyAction
import com.example.ime.KeyboardLayout
import com.example.ime.KeyboardPage
import com.example.ime.ShiftState
import com.example.ime.TransliterationMode
import com.example.ui.PhoneticReferenceScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                AmharicKeyboardMainApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AmharicKeyboardMainApp() {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Sandbox & Setup, 1: Guide, 2: Settings

    // Transliteration Engine instance for in-app sandbox
    val sandboxEngine = remember { AmharicTransliterationEngine() }

    // Sandbox state
    var textOutput by remember { mutableStateOf("") }
    var composingChar by remember { mutableStateOf("") }
    var rawBuffer by remember { mutableStateOf("") }
    var isComposing by remember { mutableStateOf(false) }
    var candidates by remember { mutableStateOf(emptyList<com.example.engine.CandidateItem>()) }

    // Virtual in-app keyboard settings
    var showEmbeddedKeyboard by remember { mutableStateOf(true) }
    var sandboxPage by remember { mutableStateOf(KeyboardPage.QWERTY) }
    var sandboxShift by remember { mutableStateOf(ShiftState.OFF) }
    var sandboxMode by remember { mutableStateOf(TransliterationMode.AMHARIC) }

    // App Preferences
    var useAmharicWordDivider by remember { mutableStateOf(false) }

    // System IME status
    var isEnabledInSystem by remember { mutableStateOf(false) }
    var isSelectedInSystem by remember { mutableStateOf(false) }

    fun refreshImeStatus() {
        isEnabledInSystem = isImeEnabled(context)
        isSelectedInSystem = isImeSelected(context)
    }

    LaunchedEffect(Unit) {
        refreshImeStatus()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "አ",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = stringResource(R.string.app_name),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "QWERTY → Ge'ez Transliteration",
                                style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = {
                        selectedTab = 0
                        refreshImeStatus()
                    },
                    icon = { Icon(Icons.Default.Keyboard, contentDescription = "Keyboard") },
                    label = { Text("Keyboard") },
                    modifier = Modifier.testTag("nav_keyboard")
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.MenuBook, contentDescription = "Guide") },
                    label = { Text("Phonetic Guide") },
                    modifier = Modifier.testTag("nav_guide")
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = {
                        selectedTab = 2
                        refreshImeStatus()
                    },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                    label = { Text("Settings") },
                    modifier = Modifier.testTag("nav_settings")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> {
                    KeyboardSandboxScreen(
                        isEnabledInSystem = isEnabledInSystem,
                        isSelectedInSystem = isSelectedInSystem,
                        onEnableClicked = {
                            val intent = Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)
                            context.startActivity(intent)
                        },
                        onSelectClicked = {
                            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                            imm?.showInputMethodPicker()
                        },
                        textOutput = textOutput,
                        onTextOutputChange = { textOutput = it },
                        composingChar = composingChar,
                        rawBuffer = rawBuffer,
                        isComposing = isComposing,
                        candidates = candidates,
                        showEmbeddedKeyboard = showEmbeddedKeyboard,
                        onToggleEmbeddedKeyboard = { showEmbeddedKeyboard = !showEmbeddedKeyboard },
                        keyboardPage = sandboxPage,
                        shiftState = sandboxShift,
                        mode = sandboxMode,
                        onKeyAction = { action ->
                            when (action) {
                                is KeyAction.Text -> {
                                    if (sandboxMode == TransliterationMode.AMHARIC && action.char.length == 1) {
                                        val step = sandboxEngine.onCharacterInput(action.char[0])
                                        if (step.committedText.isNotEmpty()) {
                                            textOutput += step.committedText
                                        }
                                        composingChar = step.composingText
                                        rawBuffer = step.rawBuffer
                                        isComposing = step.composingText.isNotEmpty()
                                        candidates = step.candidates
                                    } else {
                                        textOutput += action.char
                                    }
                                    if (sandboxShift == ShiftState.ONCE) {
                                        sandboxShift = ShiftState.OFF
                                    }
                                }
                                is KeyAction.Backspace -> {
                                    if (sandboxMode == TransliterationMode.AMHARIC) {
                                        when (val bs = sandboxEngine.onBackspace()) {
                                            is BackspaceStep.UpdatedComposing -> {
                                                composingChar = bs.composingText
                                                rawBuffer = bs.rawBuffer
                                                isComposing = true
                                                candidates = bs.candidates
                                            }
                                            is BackspaceStep.ClearedComposing -> {
                                                composingChar = ""
                                                rawBuffer = ""
                                                isComposing = false
                                                candidates = emptyList()
                                            }
                                            is BackspaceStep.PassToEditor -> {
                                                if (textOutput.isNotEmpty()) {
                                                    textOutput = textOutput.dropLast(1)
                                                }
                                            }
                                        }
                                    } else {
                                        if (textOutput.isNotEmpty()) {
                                            textOutput = textOutput.dropLast(1)
                                        }
                                    }
                                }
                                is KeyAction.Space -> {
                                    val pending = sandboxEngine.commitAndReset()
                                    if (pending.isNotEmpty()) {
                                        textOutput += pending
                                    }
                                    textOutput += if (useAmharicWordDivider) "፡" else " "
                                    isComposing = false
                                    composingChar = ""
                                    rawBuffer = ""
                                    candidates = emptyList()
                                }
                                is KeyAction.Enter -> {
                                    val pending = sandboxEngine.commitAndReset()
                                    if (pending.isNotEmpty()) {
                                        textOutput += pending
                                    }
                                    textOutput += "\n"
                                    isComposing = false
                                    composingChar = ""
                                    rawBuffer = ""
                                    candidates = emptyList()
                                }
                                is KeyAction.Shift -> {
                                    sandboxShift = sandboxShift.next()
                                }
                                is KeyAction.SwitchPage -> {
                                    sandboxPage = action.target
                                }
                                is KeyAction.ToggleMode -> {
                                    val pending = sandboxEngine.commitAndReset()
                                    if (pending.isNotEmpty()) {
                                        textOutput += pending
                                    }
                                    isComposing = false
                                    composingChar = ""
                                    rawBuffer = ""
                                    candidates = emptyList()
                                    sandboxMode = if (sandboxMode == TransliterationMode.AMHARIC) {
                                        TransliterationMode.ENGLISH
                                    } else {
                                        TransliterationMode.AMHARIC
                                    }
                                }
                                is KeyAction.SwitchIme -> {
                                    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                                    imm?.showInputMethodPicker()
                                }
                                is KeyAction.HideKeyboard -> {
                                    showEmbeddedKeyboard = false
                                }
                            }
                        },
                        onSelectCandidate = { candidate ->
                            textOutput += candidate
                            sandboxEngine.reset()
                            isComposing = false
                            composingChar = ""
                            rawBuffer = ""
                            candidates = emptyList()
                        },
                        onClearText = {
                            textOutput = ""
                            sandboxEngine.reset()
                            isComposing = false
                            composingChar = ""
                            rawBuffer = ""
                            candidates = emptyList()
                        }
                    )
                }
                1 -> {
                    PhoneticReferenceScreen(
                        onExampleClicked = { exampleKey ->
                            selectedTab = 0
                            val transliterated = sandboxEngine.transliterateString(exampleKey)
                            textOutput = if (textOutput.isEmpty()) transliterated else "$textOutput $transliterated"
                        }
                    )
                }
                2 -> {
                    SettingsAndActivationScreen(
                        isEnabledInSystem = isEnabledInSystem,
                        isSelectedInSystem = isSelectedInSystem,
                        useAmharicWordDivider = useAmharicWordDivider,
                        onToggleWordDivider = { useAmharicWordDivider = it },
                        onEnableClicked = {
                            val intent = Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)
                            context.startActivity(intent)
                        },
                        onSelectClicked = {
                            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                            imm?.showInputMethodPicker()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun KeyboardSandboxScreen(
    isEnabledInSystem: Boolean,
    isSelectedInSystem: Boolean,
    onEnableClicked: () -> Unit,
    onSelectClicked: () -> Unit,
    textOutput: String,
    onTextOutputChange: (String) -> Unit,
    composingChar: String,
    rawBuffer: String,
    isComposing: Boolean,
    candidates: List<com.example.engine.CandidateItem>,
    showEmbeddedKeyboard: Boolean,
    onToggleEmbeddedKeyboard: () -> Unit,
    keyboardPage: KeyboardPage,
    shiftState: ShiftState,
    mode: TransliterationMode,
    onKeyAction: (KeyAction) -> Unit,
    onSelectCandidate: (String) -> Unit,
    onClearText: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    Column(modifier = Modifier.fillMaxSize()) {
        // Upper test and activation content
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Activation Banner if not yet enabled or active
            if (!isEnabledInSystem || !isSelectedInSystem) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (!isEnabledInSystem) MaterialTheme.colorScheme.errorContainer
                        else MaterialTheme.colorScheme.secondaryContainer
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (!isEnabledInSystem) Icons.Default.Warning else Icons.Default.Keyboard,
                                contentDescription = null,
                                tint = if (!isEnabledInSystem) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (!isEnabledInSystem) "Keyboard is not enabled yet" else "Not currently active keyboard",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (!isEnabledInSystem)
                                "Enable Amharic Keyboard in Android System Settings to type anywhere across all apps."
                            else
                                "Switch your current input method to Amharic Keyboard to use it in other apps.",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (!isEnabledInSystem) {
                                Button(
                                    onClick = onEnableClicked,
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                    modifier = Modifier.testTag("btn_enable_ime")
                                ) {
                                    Text("1. Enable in Settings")
                                }
                            }
                            Button(
                                onClick = onSelectClicked,
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                                modifier = Modifier.testTag("btn_select_ime")
                            ) {
                                Text(if (!isEnabledInSystem) "2. Select Keyboard" else "Switch to Amharic")
                            }
                        }
                    }
                }
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Active System Keyboard",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "Ready to type Amharic in WhatsApp, Telegram, Browser, etc.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }

            // Interactive Text Output Box
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Live Transliteration Sandbox",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Row {
                            if (textOutput.isNotEmpty() || isComposing) {
                                IconButton(onClick = onClearText, modifier = Modifier.testTag("btn_clear_text")) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }
                            IconButton(
                                onClick = {
                                    val full = textOutput + if (isComposing) composingChar else ""
                                    if (full.isNotEmpty()) {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        clipboard.setPrimaryClip(ClipData.newPlainText("Amharic Text", full))
                                        Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.testTag("btn_copy_text")
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Document text display with active composing underline
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(90.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(12.dp)
                    ) {
                        if (textOutput.isEmpty() && !isComposing) {
                            Text(
                                text = "Type using the keyboard below (e.g., try 'selam', 'bet', 'habesha')...",
                                style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.outline)
                            )
                        } else {
                            Row {
                                Text(
                                    text = textOutput,
                                    style = MaterialTheme.typography.headlineSmall.copy(fontSize = 22.sp),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                if (isComposing) {
                                    Box(
                                        modifier = Modifier
                                            .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(4.dp))
                                            .padding(horizontal = 4.dp)
                                    ) {
                                        Text(
                                            text = composingChar,
                                            style = MaterialTheme.typography.headlineSmall.copy(
                                                fontSize = 22.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Live state inspection chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "Buffer: \"$rawBuffer\"",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "Composing: \"$composingChar\"",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                )
                            }
                        }

                        // Toggle keyboard visibility
                        Text(
                            text = if (showEmbeddedKeyboard) "Hide Sandbox Keyboard" else "Show Sandbox Keyboard",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            ),
                            modifier = Modifier
                                .clickable { onToggleEmbeddedKeyboard() }
                                .padding(4.dp)
                        )
                    }
                }
            }

            // Real-time System Input Field (To test with system IME)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "System Input Field (Tap to pop up OS Keyboard)",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    var systemInputText by remember { mutableStateOf("") }
                    OutlinedTextField(
                        value = systemInputText,
                        onValueChange = { systemInputText = it },
                        placeholder = { Text("Tap here to test with system keyboard...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("system_test_input"),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }
        }

        // Embedded Interactive Jetpack Compose Keyboard
        AnimatedVisibility(visible = showEmbeddedKeyboard) {
            KeyboardLayout(
                page = keyboardPage,
                shiftState = shiftState,
                mode = mode,
                isComposing = isComposing,
                composingChar = composingChar,
                rawBuffer = rawBuffer,
                candidates = candidates,
                onKeyAction = onKeyAction,
                onSelectCandidate = onSelectCandidate
            )
        }
    }
}

@Composable
fun SettingsAndActivationScreen(
    isEnabledInSystem: Boolean,
    isSelectedInSystem: Boolean,
    useAmharicWordDivider: Boolean,
    onToggleWordDivider: (Boolean) -> Unit,
    onEnableClicked: () -> Unit,
    onSelectClicked: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Keyboard Settings & Setup",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary
        )

        // System Integration
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "System Input Method Integration",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("1. System Setting", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                        Text(
                            text = if (isEnabledInSystem) "Enabled in Android" else "Disabled in Android",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = if (isEnabledInSystem) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                            )
                        )
                    }
                    Button(onClick = onEnableClicked) {
                        Text(if (isEnabledInSystem) "Open Settings" else "Enable")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("2. Active Keyboard", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                        Text(
                            text = if (isSelectedInSystem) "Currently selected" else "Other keyboard active",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = if (isSelectedInSystem) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                            )
                        )
                    }
                    OutlinedButton(onClick = onSelectClicked) {
                        Text("Switch")
                    }
                }
            }
        }

        // Typing Preferences
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "Typing Options",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Amharic Word Divider for Spacebar", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium))
                        Text(
                            "Output '፡' (Ge'ez neteb) instead of standard space ' '",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }
                    Switch(
                        checked = useAmharicWordDivider,
                        onCheckedChange = onToggleWordDivider
                    )
                }
            }
        }

        // About & Version
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text("About Amharic Keyboard", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    "Standard English QWERTY keys layout dynamically converting phonetic input into Ge'ez script in real time.",
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    "Designed with Jetpack Compose & Material 3.",
                    style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.primary)
                )
            }
        }
    }
}

private fun isImeEnabled(context: Context): Boolean {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager ?: return false
    val enabledList = imm.enabledInputMethodList
    return enabledList.any { it.packageName == context.packageName }
}

private fun isImeSelected(context: Context): Boolean {
    val currentIme = Settings.Secure.getString(
        context.contentResolver,
        Settings.Secure.DEFAULT_INPUT_METHOD
    ) ?: return false
    return currentIme.contains(context.packageName)
}
