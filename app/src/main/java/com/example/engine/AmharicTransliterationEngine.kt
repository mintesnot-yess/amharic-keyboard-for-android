package com.example.engine

/**
 * Result returned after processing a character input.
 */
data class TransliterationStep(
    val committedText: String = "",
    val composingText: String = "",
    val rawBuffer: String = "",
    val candidates: List<CandidateItem> = emptyList()
)

/**
 * Result returned after processing a backspace.
 */
sealed interface BackspaceStep {
    data class UpdatedComposing(
        val composingText: String,
        val rawBuffer: String,
        val candidates: List<CandidateItem> = emptyList()
    ) : BackspaceStep

    data object ClearedComposing : BackspaceStep

    data object PassToEditor : BackspaceStep
}

/**
 * Candidate item for the candidate / suggestion bar.
 */
data class CandidateItem(
    val geezChar: String,
    val label: String,
    val keystroke: String
)

/**
 * State engine that manages the active phonetic buffer and maps Latin keystrokes
 * into Ge'ez (Amharic) characters according to phonetic transliteration rules.
 */
class AmharicTransliterationEngine {

    private var currentBuffer: String = ""

    /**
     * Inspect current raw buffer (e.g. "s", "sh", "we").
     */
    fun getBuffer(): String = currentBuffer

    /**
     * Reset the active buffer.
     */
    fun reset() {
        currentBuffer = ""
    }

    /**
     * Commit any currently active composing text and clear buffer.
     * Returns the Amharic text that was finalized, or empty string.
     */
    fun commitAndReset(): String {
        val committed = if (currentBuffer.isNotEmpty()) {
            AmharicPhoneticRules.PATTERNS[currentBuffer] ?: currentBuffer
        } else {
            ""
        }
        currentBuffer = ""
        return committed
    }

    /**
     * Feed a single typed character into the engine.
     * Evaluates whether it can extend the current phonetic syllable,
     * or if the previous syllable must be committed and a new one started.
     */
    fun onCharacterInput(char: Char): TransliterationStep {
        val charStr = char.toString()
        val candidateBuffer = currentBuffer + charStr

        // Check if candidate buffer is a valid prefix or exact pattern
        if (AmharicPhoneticRules.PREFIXES.contains(candidateBuffer)) {
            currentBuffer = candidateBuffer
            val composing = AmharicPhoneticRules.PATTERNS[currentBuffer] ?: ""
            return TransliterationStep(
                committedText = "",
                composingText = composing,
                rawBuffer = currentBuffer,
                candidates = getCandidatesForBuffer(currentBuffer)
            )
        }

        // Candidate buffer cannot form a valid syllable prefix.
        // We finalize the previous buffer (if any), and attempt to start a new one with charStr.
        val previousCommitted = if (currentBuffer.isNotEmpty()) {
            AmharicPhoneticRules.PATTERNS[currentBuffer] ?: currentBuffer
        } else {
            ""
        }

        return if (AmharicPhoneticRules.PREFIXES.contains(charStr)) {
            // New character starts a valid syllable
            currentBuffer = charStr
            val composing = AmharicPhoneticRules.PATTERNS[currentBuffer] ?: ""
            TransliterationStep(
                committedText = previousCommitted,
                composingText = composing,
                rawBuffer = currentBuffer,
                candidates = getCandidatesForBuffer(currentBuffer)
            )
        } else {
            // Non-transliterable character (e.g. punctuation, digits, whitespace)
            currentBuffer = ""
            TransliterationStep(
                committedText = previousCommitted + charStr,
                composingText = "",
                rawBuffer = "",
                candidates = emptyList()
            )
        }
    }

    /**
     * Handle backspace key.
     * Updates or deletes the active transliterating sequence before deleting committed characters.
     */
    fun onBackspace(): BackspaceStep {
        if (currentBuffer.isEmpty()) {
            return BackspaceStep.PassToEditor
        }

        currentBuffer = currentBuffer.dropLast(1)
        if (currentBuffer.isEmpty()) {
            return BackspaceStep.ClearedComposing
        }

        val composing = AmharicPhoneticRules.PATTERNS[currentBuffer] ?: ""
        return BackspaceStep.UpdatedComposing(
            composingText = composing,
            rawBuffer = currentBuffer,
            candidates = getCandidatesForBuffer(currentBuffer)
        )
    }

    /**
     * When user taps a candidate from the candidate strip.
     * Commits the selected Ge'ez character and clears the buffer.
     */
    fun onSelectCandidate(candidateChar: String): TransliterationStep {
        currentBuffer = ""
        return TransliterationStep(
            committedText = candidateChar,
            composingText = "",
            rawBuffer = "",
            candidates = emptyList()
        )
    }

    /**
     * Convenience method to convert an entire English string into Amharic.
     * Useful for batch conversion and automated testing.
     */
    fun transliterateString(input: String): String {
        reset()
        val result = StringBuilder()
        for (ch in input) {
            val step = onCharacterInput(ch)
            if (step.committedText.isNotEmpty()) {
                result.append(step.committedText)
            }
        }
        val remaining = commitAndReset()
        if (remaining.isNotEmpty()) {
            result.append(remaining)
        }
        return result.toString()
    }

    /**
     * Generate candidates for the current buffer's root consonant family.
     */
    private fun getCandidatesForBuffer(buffer: String): List<CandidateItem> {
        if (buffer.isEmpty()) return emptyList()

        // Find matching family: check for 2-letter base first, then 1-letter
        val family = (if (buffer.length >= 2) AmharicPhoneticRules.FAMILY_BY_KEY[buffer.substring(0, 2)] else null)
            ?: AmharicPhoneticRules.FAMILY_BY_KEY[buffer.substring(0, 1)]

        return if (family != null) {
            val base = family.baseKey
            listOfNotNull(
                CandidateItem(family.sixth, "6th (default)", base),
                CandidateItem(family.first, "1st (+e)", "${base}e"),
                CandidateItem(family.second, "2nd (+u)", "${base}u"),
                CandidateItem(family.third, "3rd (+i)", "${base}i"),
                CandidateItem(family.fourth, "4th (+a)", "${base}a"),
                CandidateItem(family.fifth, "5th (+ee)", "${base}ee"),
                CandidateItem(family.seventh, "7th (+o)", "${base}o"),
                family.eighth?.let { CandidateItem(it, "8th (+wa)", "${base}wa") }
            )
        } else {
            // Independent vowels or unknown
            val mapped = AmharicPhoneticRules.PATTERNS[buffer]
            if (mapped != null) {
                listOf(CandidateItem(mapped, "Vowel", buffer))
            } else {
                emptyList()
            }
        }
    }
}
