package com.example.ime

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.engine.CandidateItem

// Sub-labels for English QWERTY keys showing their primary Amharic mapping
val AMHARIC_KEY_SUB_LABELS = mapOf(
    "q" to "ቅ", "w" to "ው", "e" to "እ", "r" to "ር", "t" to "ት",
    "y" to "ይ", "u" to "ኡ", "i" to "ኢ", "o" to "ኦ", "p" to "ፕ",
    "a" to "አ", "s" to "ስ", "d" to "ድ", "f" to "ፍ", "g" to "ግ",
    "h" to "ህ", "j" to "ጅ", "k" to "ክ", "l" to "ል",
    "z" to "ዝ", "x" to "ሽ", "c" to "ች", "v" to "ቭ", "b" to "ብ",
    "n" to "ን", "m" to "ም"
)

@Composable
fun KeyboardLayout(
    page: KeyboardPage,
    shiftState: ShiftState,
    mode: TransliterationMode,
    isComposing: Boolean,
    composingChar: String,
    rawBuffer: String,
    candidates: List<CandidateItem>,
    onKeyAction: (KeyAction) -> Unit,
    onSelectCandidate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("keyboard_layout"),
        color = MaterialTheme.colorScheme.surfaceContainer
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .navigationBarsPadding()
        ) {
            // 1. Candidate Strip
            KeyboardCandidateBar(
                isComposing = isComposing,
                composingChar = composingChar,
                rawBuffer = rawBuffer,
                candidates = candidates,
                mode = mode,
                onSelectCandidate = onSelectCandidate
            )

            Spacer(modifier = Modifier.height(2.dp))

            // 2. Key Pages
            when (page) {
                KeyboardPage.QWERTY -> QwertyPage(
                    shiftState = shiftState,
                    mode = mode,
                    onKeyAction = onKeyAction
                )
                KeyboardPage.SYMBOLS -> SymbolsPage(
                    onKeyAction = onKeyAction
                )
                KeyboardPage.MORE_SYMBOLS -> MoreSymbolsPage(
                    onKeyAction = onKeyAction
                )
                KeyboardPage.GEEZ_PUNCTUATION -> GeezPage(
                    onKeyAction = onKeyAction
                )
            }
        }
    }
}

@Composable
private fun QwertyPage(
    shiftState: ShiftState,
    mode: TransliterationMode,
    onKeyAction: (KeyAction) -> Unit
) {
    val isShifted = shiftState != ShiftState.OFF
    val row1 = listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p")
    val row2 = listOf("a", "s", "d", "f", "g", "h", "j", "k", "l")
    val row3 = listOf("z", "x", "c", "v", "b", "n", "m")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 2.dp)
    ) {
        // Row 1
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            row1.forEach { k ->
                val displayChar = if (isShifted) k.uppercase() else k
                val sub = if (mode == TransliterationMode.AMHARIC) AMHARIC_KEY_SUB_LABELS[k] else null
                KeyboardKey(
                    action = KeyAction.Text(displayChar),
                    label = displayChar,
                    subLabel = sub,
                    modifier = Modifier.weight(1f),
                    onAction = onKeyAction
                )
            }
        }

        // Row 2
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            row2.forEach { k ->
                val displayChar = if (isShifted) k.uppercase() else k
                val sub = if (mode == TransliterationMode.AMHARIC) AMHARIC_KEY_SUB_LABELS[k] else null
                KeyboardKey(
                    action = KeyAction.Text(displayChar),
                    label = displayChar,
                    subLabel = sub,
                    modifier = Modifier.weight(1f),
                    onAction = onKeyAction
                )
            }
        }

        // Row 3 (Shift, Z-M, Backspace)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            KeyboardKey(
                action = KeyAction.Shift,
                label = if (shiftState == ShiftState.LOCKED) "⇪" else "⇧",
                isSpecial = true,
                isActive = shiftState != ShiftState.OFF,
                modifier = Modifier.weight(1.5f),
                onAction = onKeyAction
            )

            row3.forEach { k ->
                val displayChar = if (isShifted) k.uppercase() else k
                val sub = if (mode == TransliterationMode.AMHARIC) AMHARIC_KEY_SUB_LABELS[k] else null
                KeyboardKey(
                    action = KeyAction.Text(displayChar),
                    label = displayChar,
                    subLabel = sub,
                    modifier = Modifier.weight(1f),
                    onAction = onKeyAction
                )
            }

            KeyboardKey(
                action = KeyAction.Backspace,
                label = "⌫",
                isSpecial = true,
                modifier = Modifier.weight(1.5f),
                onAction = onKeyAction
            )
        }

        // Row 4 (Bottom controls)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            // ?123 key
            KeyboardKey(
                action = KeyAction.SwitchPage(KeyboardPage.SYMBOLS),
                label = "?123",
                isSpecial = true,
                modifier = Modifier.weight(1.3f),
                onAction = onKeyAction
            )

            // Ge'ez Numerals & Punctuation page
            KeyboardKey(
                action = KeyAction.SwitchPage(KeyboardPage.GEEZ_PUNCTUATION),
                label = "፩፪፫",
                isSpecial = true,
                modifier = Modifier.weight(1.1f),
                onAction = onKeyAction
            )

            // Mode Toggle (Amharic <-> English)
            KeyboardKey(
                action = KeyAction.ToggleMode,
                label = if (mode == TransliterationMode.AMHARIC) "አማ" else "EN",
                isSpecial = true,
                isActive = mode == TransliterationMode.AMHARIC,
                modifier = Modifier.weight(1.1f),
                onAction = onKeyAction
            )

            // Spacebar
            KeyboardKey(
                action = KeyAction.Space,
                label = if (mode == TransliterationMode.AMHARIC) "ክፍተት (Space)" else "Space",
                modifier = Modifier.weight(3.5f),
                onAction = onKeyAction
            )

            // Quick Amharic punctuation key (፡ / ።)
            KeyboardKey(
                action = KeyAction.Text("፡"),
                label = "፡",
                subLabel = "።",
                isSpecial = true,
                modifier = Modifier.weight(1.0f),
                onAction = onKeyAction
            )

            // Enter key
            KeyboardKey(
                action = KeyAction.Enter,
                label = "↵",
                isSpecial = true,
                isActive = true,
                modifier = Modifier.weight(1.4f),
                onAction = onKeyAction
            )
        }
    }
}

@Composable
private fun SymbolsPage(
    onKeyAction: (KeyAction) -> Unit
) {
    val row1 = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0")
    val row2 = listOf("@", "#", "$", "%", "&", "-", "+", "(", ")", "/")
    val row3 = listOf("*", "\"", "'", ":", ";", "!", "?")

    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp)) {
        // Row 1
        Row(modifier = Modifier.fillMaxWidth()) {
            row1.forEach { k ->
                KeyboardKey(action = KeyAction.Text(k), label = k, modifier = Modifier.weight(1f), onAction = onKeyAction)
            }
        }
        // Row 2
        Row(modifier = Modifier.fillMaxWidth()) {
            row2.forEach { k ->
                KeyboardKey(action = KeyAction.Text(k), label = k, modifier = Modifier.weight(1f), onAction = onKeyAction)
            }
        }
        // Row 3
        Row(modifier = Modifier.fillMaxWidth()) {
            KeyboardKey(
                action = KeyAction.SwitchPage(KeyboardPage.MORE_SYMBOLS),
                label = "=\\<",
                isSpecial = true,
                modifier = Modifier.weight(1.5f),
                onAction = onKeyAction
            )
            row3.forEach { k ->
                KeyboardKey(action = KeyAction.Text(k), label = k, modifier = Modifier.weight(1f), onAction = onKeyAction)
            }
            KeyboardKey(
                action = KeyAction.Backspace,
                label = "⌫",
                isSpecial = true,
                modifier = Modifier.weight(1.5f),
                onAction = onKeyAction
            )
        }
        // Row 4
        Row(modifier = Modifier.fillMaxWidth()) {
            KeyboardKey(
                action = KeyAction.SwitchPage(KeyboardPage.QWERTY),
                label = "ABC",
                isSpecial = true,
                modifier = Modifier.weight(1.5f),
                onAction = onKeyAction
            )
            KeyboardKey(action = KeyAction.Text("፡"), label = "፡", isSpecial = true, modifier = Modifier.weight(1f), onAction = onKeyAction)
            KeyboardKey(action = KeyAction.Text(","), label = ",", modifier = Modifier.weight(1f), onAction = onKeyAction)
            KeyboardKey(action = KeyAction.Space, label = "Space", modifier = Modifier.weight(3.5f), onAction = onKeyAction)
            KeyboardKey(action = KeyAction.Text("."), label = ".", modifier = Modifier.weight(1f), onAction = onKeyAction)
            KeyboardKey(action = KeyAction.Text("።"), label = "።", isSpecial = true, modifier = Modifier.weight(1f), onAction = onKeyAction)
            KeyboardKey(action = KeyAction.Enter, label = "↵", isSpecial = true, isActive = true, modifier = Modifier.weight(1.5f), onAction = onKeyAction)
        }
    }
}

@Composable
private fun MoreSymbolsPage(
    onKeyAction: (KeyAction) -> Unit
) {
    val row1 = listOf("~", "`", "|", "•", "√", "π", "÷", "×", "¶", "∆")
    val row2 = listOf("£", "€", "¥", "¢", "^", "°", "=", "{", "}", "\\")
    val row3 = listOf("%", "©", "®", "™", "[", "]", "<", ">")

    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp)) {
        Row(modifier = Modifier.fillMaxWidth()) {
            row1.forEach { k -> KeyboardKey(action = KeyAction.Text(k), label = k, modifier = Modifier.weight(1f), onAction = onKeyAction) }
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            row2.forEach { k -> KeyboardKey(action = KeyAction.Text(k), label = k, modifier = Modifier.weight(1f), onAction = onKeyAction) }
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            KeyboardKey(action = KeyAction.SwitchPage(KeyboardPage.SYMBOLS), label = "?123", isSpecial = true, modifier = Modifier.weight(1.5f), onAction = onKeyAction)
            row3.forEach { k -> KeyboardKey(action = KeyAction.Text(k), label = k, modifier = Modifier.weight(1f), onAction = onKeyAction) }
            KeyboardKey(action = KeyAction.Backspace, label = "⌫", isSpecial = true, modifier = Modifier.weight(1.5f), onAction = onKeyAction)
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            KeyboardKey(action = KeyAction.SwitchPage(KeyboardPage.QWERTY), label = "ABC", isSpecial = true, modifier = Modifier.weight(1.5f), onAction = onKeyAction)
            KeyboardKey(action = KeyAction.Space, label = "Space", modifier = Modifier.weight(4.5f), onAction = onKeyAction)
            KeyboardKey(action = KeyAction.Enter, label = "↵", isSpecial = true, isActive = true, modifier = Modifier.weight(1.5f), onAction = onKeyAction)
        }
    }
}

@Composable
private fun GeezPage(
    onKeyAction: (KeyAction) -> Unit
) {
    val numbers1To10 = listOf("፩", "፪", "፫", "፬", "፭", "፮", "፯", "፰", "፱", "፲")
    val numbersTens = listOf("፳", "፴", "፵", "፶", "፷", "፸", "፹", "፺", "፻", "፼")
    val punctuation = listOf("፡", "።", "፣", "፤", "፥", "፦", "፧", "፠")

    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp)) {
        Row(modifier = Modifier.fillMaxWidth()) {
            numbers1To10.forEach { k -> KeyboardKey(action = KeyAction.Text(k), label = k, modifier = Modifier.weight(1f), onAction = onKeyAction) }
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            numbersTens.forEach { k -> KeyboardKey(action = KeyAction.Text(k), label = k, modifier = Modifier.weight(1f), onAction = onKeyAction) }
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            punctuation.forEach { k -> KeyboardKey(action = KeyAction.Text(k), label = k, isSpecial = true, modifier = Modifier.weight(1f), onAction = onKeyAction) }
            KeyboardKey(action = KeyAction.Backspace, label = "⌫", isSpecial = true, modifier = Modifier.weight(1.5f), onAction = onKeyAction)
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            KeyboardKey(action = KeyAction.SwitchPage(KeyboardPage.QWERTY), label = "ABC", isSpecial = true, modifier = Modifier.weight(1.5f), onAction = onKeyAction)
            KeyboardKey(action = KeyAction.SwitchPage(KeyboardPage.SYMBOLS), label = "123", isSpecial = true, modifier = Modifier.weight(1.2f), onAction = onKeyAction)
            KeyboardKey(action = KeyAction.Space, label = "Space", modifier = Modifier.weight(4f), onAction = onKeyAction)
            KeyboardKey(action = KeyAction.Enter, label = "↵", isSpecial = true, isActive = true, modifier = Modifier.weight(1.5f), onAction = onKeyAction)
        }
    }
}
