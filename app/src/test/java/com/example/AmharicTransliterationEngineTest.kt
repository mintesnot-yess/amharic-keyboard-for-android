package com.example

import com.example.engine.AmharicTransliterationEngine
import com.example.engine.BackspaceStep
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AmharicTransliterationEngineTest {

    private lateinit var engine: AmharicTransliterationEngine

    @Before
    fun setUp() {
        engine = AmharicTransliterationEngine()
    }

    @Test
    fun testVowels() {
        assertEquals("አ", engine.transliterateString("a"))
        assertEquals("ኣ", engine.transliterateString("aa"))
        assertEquals("ኡ", engine.transliterateString("u"))
        assertEquals("ኢ", engine.transliterateString("i"))
        assertEquals("እ", engine.transliterateString("e"))
        assertEquals("ኤ", engine.transliterateString("ee"))
        assertEquals("ኦ", engine.transliterateString("o"))
    }

    @Test
    fun testWFamily() {
        assertEquals("ው", engine.transliterateString("w"))
        assertEquals("ወ", engine.transliterateString("we"))
        assertEquals("ዉ", engine.transliterateString("wu"))
        assertEquals("ዊ", engine.transliterateString("wi"))
        assertEquals("ዋ", engine.transliterateString("wa"))
        assertEquals("ዎ", engine.transliterateString("wo"))
    }

    @Test
    fun testHFamily() {
        assertEquals("ህ", engine.transliterateString("h"))
        assertEquals("ሀ", engine.transliterateString("he"))
        assertEquals("ሁ", engine.transliterateString("hu"))
        assertEquals("ሂ", engine.transliterateString("hi"))
        assertEquals("ሃ", engine.transliterateString("ha"))
        assertEquals("ሆ", engine.transliterateString("ho"))
    }

    @Test
    fun testLFamily() {
        assertEquals("ል", engine.transliterateString("l"))
        assertEquals("ለ", engine.transliterateString("le"))
        assertEquals("ሉ", engine.transliterateString("lu"))
        assertEquals("ሊ", engine.transliterateString("li"))
        assertEquals("ላ", engine.transliterateString("la"))
        assertEquals("ሎ", engine.transliterateString("lo"))
    }

    @Test
    fun testMFamily() {
        assertEquals("ም", engine.transliterateString("m"))
        assertEquals("መ", engine.transliterateString("me"))
        assertEquals("ሙ", engine.transliterateString("mu"))
        assertEquals("ሚ", engine.transliterateString("mi"))
        assertEquals("ማ", engine.transliterateString("ma"))
        assertEquals("ሞ", engine.transliterateString("mo"))
    }

    @Test
    fun testRFamily() {
        assertEquals("ር", engine.transliterateString("r"))
        assertEquals("ረ", engine.transliterateString("re"))
        assertEquals("ሩ", engine.transliterateString("ru"))
        assertEquals("ሪ", engine.transliterateString("ri"))
        assertEquals("ራ", engine.transliterateString("ra"))
        assertEquals("ሮ", engine.transliterateString("ro"))
    }

    @Test
    fun testSFamily() {
        assertEquals("ስ", engine.transliterateString("s"))
        assertEquals("ሰ", engine.transliterateString("se"))
        assertEquals("ሱ", engine.transliterateString("su"))
        assertEquals("ሲ", engine.transliterateString("si"))
        assertEquals("ሳ", engine.transliterateString("sa"))
        assertEquals("ሶ", engine.transliterateString("so"))
    }

    @Test
    fun testZFamily() {
        assertEquals("ዝ", engine.transliterateString("z"))
        assertEquals("ዘ", engine.transliterateString("ze"))
        assertEquals("ዙ", engine.transliterateString("zu"))
        assertEquals("ዚ", engine.transliterateString("zi"))
        assertEquals("ዛ", engine.transliterateString("za"))
        assertEquals("ዞ", engine.transliterateString("zo"))
    }

    @Test
    fun testQFamily() {
        assertEquals("ቅ", engine.transliterateString("q"))
        assertEquals("ቀ", engine.transliterateString("qe"))
        assertEquals("ቁ", engine.transliterateString("qu"))
        assertEquals("ቂ", engine.transliterateString("qi"))
        assertEquals("ቃ", engine.transliterateString("qa"))
        assertEquals("ቆ", engine.transliterateString("qo"))
    }

    @Test
    fun testBFamily() {
        assertEquals("ብ", engine.transliterateString("b"))
        assertEquals("በ", engine.transliterateString("be"))
        assertEquals("ቡ", engine.transliterateString("bu"))
        assertEquals("ቢ", engine.transliterateString("bi"))
        assertEquals("ባ", engine.transliterateString("ba"))
        assertEquals("ቦ", engine.transliterateString("bo"))
    }

    @Test
    fun testTFamily() {
        assertEquals("ት", engine.transliterateString("t"))
        assertEquals("ተ", engine.transliterateString("te"))
        assertEquals("ቱ", engine.transliterateString("tu"))
        assertEquals("ቲ", engine.transliterateString("ti"))
        assertEquals("ታ", engine.transliterateString("ta"))
        assertEquals("ቶ", engine.transliterateString("to"))
    }

    @Test
    fun testNFamily() {
        assertEquals("ን", engine.transliterateString("n"))
        assertEquals("ነ", engine.transliterateString("ne"))
        assertEquals("ኑ", engine.transliterateString("nu"))
        assertEquals("ኒ", engine.transliterateString("ni"))
        assertEquals("ና", engine.transliterateString("na"))
        assertEquals("ኖ", engine.transliterateString("no"))
    }

    @Test
    fun testKFamily() {
        assertEquals("ክ", engine.transliterateString("k"))
        assertEquals("ከ", engine.transliterateString("ke"))
        assertEquals("ኩ", engine.transliterateString("ku"))
        assertEquals("ኪ", engine.transliterateString("ki"))
        assertEquals("ካ", engine.transliterateString("ka"))
        assertEquals("ኮ", engine.transliterateString("ko"))
    }

    @Test
    fun testBackspaceStateTransition() {
        // Typing 'w' -> 'a' -> backspace -> 'e'
        val step1 = engine.onCharacterInput('w')
        assertEquals("ው", step1.composingText)
        assertEquals("w", step1.rawBuffer)

        val step2 = engine.onCharacterInput('a')
        assertEquals("ዋ", step2.composingText)
        assertEquals("wa", step2.rawBuffer)

        // First backspace should revert to 'w' ("ው")
        val bs1 = engine.onBackspace()
        assertTrue(bs1 is BackspaceStep.UpdatedComposing)
        assertEquals("ው", (bs1 as BackspaceStep.UpdatedComposing).composingText)
        assertEquals("w", bs1.rawBuffer)

        // Second backspace should clear composing
        val bs2 = engine.onBackspace()
        assertEquals(BackspaceStep.ClearedComposing, bs2)

        // Third backspace should pass to editor
        val bs3 = engine.onBackspace()
        assertEquals(BackspaceStep.PassToEditor, bs3)
    }

    @Test
    fun testMultiCharacterWords() {
        assertEquals("ሰላም", engine.transliterateString("selam"))
        assertEquals("ቁርስ", engine.transliterateString("qurs"))
        assertEquals("ኪዳን", engine.transliterateString("kidan"))
        assertEquals("ሸገር", engine.transliterateString("sheger"))
    }
}
