package com.example

import android.content.Context
import android.inputmethodservice.InputMethodService
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.example.engine.AmharicTransliterationEngine
import com.example.engine.BackspaceStep
import com.example.engine.CandidateItem
import com.example.ime.KeyAction
import com.example.ime.KeyboardLayout
import com.example.ime.KeyboardPage
import com.example.ime.ShiftState
import com.example.ime.TransliterationMode
import com.example.ui.theme.MyApplicationTheme

/**
 * System-wide InputMethodService providing native QWERTY keyboard with
 * real-time phonetic Amharic (Ge'ez) transliteration.
 */
class AmharicInputMethodService : InputMethodService(),
    LifecycleOwner,
    ViewModelStoreOwner,
    SavedStateRegistryOwner {

    private val lifecycleRegistry = LifecycleRegistry(this)
    private val store = ViewModelStore()
    private val savedStateRegistryController = SavedStateRegistryController.create(this)

    override val lifecycle: Lifecycle
        get() = lifecycleRegistry

    override val viewModelStore: ViewModelStore
        get() = store

    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateRegistryController.savedStateRegistry

    private val engine = AmharicTransliterationEngine()

    // Keyboard observable state
    private var currentPage by mutableStateOf(KeyboardPage.QWERTY)
    private var currentShift by mutableStateOf(ShiftState.OFF)
    private var currentMode by mutableStateOf(TransliterationMode.AMHARIC)
    private var isComposing by mutableStateOf(false)
    private var composingChar by mutableStateOf("")
    private var rawBuffer by mutableStateOf("")
    private var candidates by mutableStateOf<List<CandidateItem>>(emptyList())

    private var audioManager: AudioManager? = null
    private var vibrator: Vibrator? = null

    override fun onCreate() {
        super.onCreate()
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)

        audioManager = getSystemService(Context.AUDIO_SERVICE) as? AudioManager
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    override fun onCreateInputView(): View {
        window?.window?.decorView?.let { decorView ->
            decorView.setViewTreeLifecycleOwner(this)
            decorView.setViewTreeViewModelStoreOwner(this)
            decorView.setViewTreeSavedStateRegistryOwner(this)
        }
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)

        return ComposeView(this).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                MyApplicationTheme {
                    KeyboardLayout(
                        page = currentPage,
                        shiftState = currentShift,
                        mode = currentMode,
                        isComposing = isComposing,
                        composingChar = composingChar,
                        rawBuffer = rawBuffer,
                        candidates = candidates,
                        onKeyAction = { action -> handleKeyAction(action) },
                        onSelectCandidate = { candidate -> handleCandidateSelection(candidate) }
                    )
                }
            }
        }
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        resetTransliterationState()
        currentPage = KeyboardPage.QWERTY
        currentShift = ShiftState.OFF
    }

    override fun onFinishInputView(finishingInput: Boolean) {
        commitAnyPending()
        super.onFinishInputView(finishingInput)
    }

    override fun onDestroy() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        store.clear()
        super.onDestroy()
    }

    private fun handleKeyAction(action: KeyAction) {
        playFeedback()

        when (action) {
            is KeyAction.Text -> {
                handleTextInput(action.char)
            }
            is KeyAction.Backspace -> {
                handleBackspace()
            }
            is KeyAction.Space -> {
                handleSpace()
            }
            is KeyAction.Enter -> {
                handleEnter()
            }
            is KeyAction.Shift -> {
                currentShift = currentShift.next()
            }
            is KeyAction.SwitchPage -> {
                currentPage = action.target
            }
            is KeyAction.ToggleMode -> {
                commitAnyPending()
                currentMode = if (currentMode == TransliterationMode.AMHARIC) {
                    TransliterationMode.ENGLISH
                } else {
                    TransliterationMode.AMHARIC
                }
            }
            is KeyAction.SwitchIme -> {
                commitAnyPending()
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                imm?.showInputMethodPicker()
            }
            is KeyAction.HideKeyboard -> {
                commitAnyPending()
                requestHideSelf(0)
            }
        }
    }

    private fun handleTextInput(text: String) {
        val ic = currentInputConnection ?: return

        if (currentMode == TransliterationMode.AMHARIC && text.length == 1) {
            val ch = text[0]
            val step = engine.onCharacterInput(ch)

            if (step.committedText.isNotEmpty()) {
                ic.commitText(step.committedText, 1)
            }

            if (step.composingText.isNotEmpty()) {
                ic.setComposingText(step.composingText, 1)
                isComposing = true
                composingChar = step.composingText
                rawBuffer = step.rawBuffer
                candidates = step.candidates
            } else {
                ic.finishComposingText()
                isComposing = false
                composingChar = ""
                rawBuffer = ""
                candidates = emptyList()
            }
        } else {
            // Direct input (English mode or multi-char strings)
            ic.commitText(text, 1)
        }

        // If shift was ONCE, revert to OFF
        if (currentShift == ShiftState.ONCE) {
            currentShift = ShiftState.OFF
        }
    }

    private fun handleBackspace() {
        val ic = currentInputConnection ?: return

        if (currentMode == TransliterationMode.AMHARIC) {
            when (val result = engine.onBackspace()) {
                is BackspaceStep.UpdatedComposing -> {
                    ic.setComposingText(result.composingText, 1)
                    isComposing = true
                    composingChar = result.composingText
                    rawBuffer = result.rawBuffer
                    candidates = result.candidates
                }
                is BackspaceStep.ClearedComposing -> {
                    ic.setComposingText("", 0)
                    ic.finishComposingText()
                    isComposing = false
                    composingChar = ""
                    rawBuffer = ""
                    candidates = emptyList()
                }
                is BackspaceStep.PassToEditor -> {
                    ic.deleteSurroundingText(1, 0)
                }
            }
        } else {
            ic.deleteSurroundingText(1, 0)
        }
    }

    private fun handleSpace() {
        val ic = currentInputConnection ?: return
        commitAnyPending()
        ic.commitText(" ", 1)
    }

    private fun handleEnter() {
        val ic = currentInputConnection ?: return
        commitAnyPending()

        val editorInfo = currentInputEditorInfo
        val imeAction = editorInfo?.imeOptions?.and(EditorInfo.IME_MASK_ACTION)
        if (imeAction != null && imeAction != EditorInfo.IME_ACTION_NONE && imeAction != EditorInfo.IME_ACTION_UNSPECIFIED) {
            ic.performEditorAction(imeAction)
        } else {
            ic.commitText("\n", 1)
        }
    }

    private fun handleCandidateSelection(candidate: String) {
        val ic = currentInputConnection ?: return
        ic.commitText(candidate, 1)
        resetTransliterationState()
    }

    private fun commitAnyPending() {
        val ic = currentInputConnection ?: return
        val pending = engine.commitAndReset()
        if (pending.isNotEmpty()) {
            ic.commitText(pending, 1)
        }
        ic.finishComposingText()
        resetTransliterationState()
    }

    private fun resetTransliterationState() {
        engine.reset()
        isComposing = false
        composingChar = ""
        rawBuffer = ""
        candidates = emptyList()
    }

    private fun playFeedback() {
        try {
            audioManager?.playSoundEffect(AudioManager.FX_KEYPRESS_STANDARD, 0.5f)
        } catch (_: Exception) {}

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(10, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(10)
            }
        } catch (_: Exception) {}
    }
}
