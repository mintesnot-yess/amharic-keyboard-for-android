package com.example.ime

/**
 * State for shift / uppercase.
 */
enum class ShiftState {
    OFF,
    ONCE,
    LOCKED;

    fun next(): ShiftState = when (this) {
        OFF -> ONCE
        ONCE -> LOCKED
        LOCKED -> OFF
    }
}

/**
 * Page layout mode for keyboard.
 */
enum class KeyboardPage {
    QWERTY,
    SYMBOLS,
    MORE_SYMBOLS,
    GEEZ_PUNCTUATION
}

/**
 * Typing language mode.
 */
enum class TransliterationMode {
    AMHARIC,
    ENGLISH
}

/**
 * Sealed class representing keyboard key types.
 */
sealed class KeyAction {
    data class Text(val char: String) : KeyAction()
    data object Backspace : KeyAction()
    data object Shift : KeyAction()
    data object Enter : KeyAction()
    data object Space : KeyAction()
    data class SwitchPage(val target: KeyboardPage) : KeyAction()
    data object ToggleMode : KeyAction() // Toggle between Amharic and English
    data object SwitchIme : KeyAction()  // Switch to next system keyboard
    data object HideKeyboard : KeyAction()
}
