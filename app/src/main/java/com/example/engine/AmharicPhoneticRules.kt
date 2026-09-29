package com.example.engine

/**
 * Phonetic transliteration definitions for Ge'ez / Amharic Fidel.
 * Covers all standard consonants, vowels, diphthongs (wa), and punctuation.
 */
object AmharicPhoneticRules {

    /**
     * Syllable family definition for a consonant.
     * Orders:
     * 1st: ä / e (e.g., ሀ, ለ)
     * 2nd: u (e.g., ሁ, ሉ)
     * 3rd: i (e.g., ሂ, ሊ)
     * 4th: a (e.g., ሃ, ላ)
     * 5th: ee / ie (e.g., ሄ, ሌ)
     * 6th: standalone consonant / ï (e.g., ህ, ል)
     * 7th: o (e.g., ሆ, ሎ)
     * 8th: wa diphthong (e.g., ኋ, ሏ)
     */
    data class FidelFamily(
        val baseKey: String,
        val first: String,   // 1st order (+e)
        val second: String,  // 2nd order (+u)
        val third: String,   // 3rd order (+i)
        val fourth: String,  // 4th order (+a)
        val fifth: String,   // 5th order (+ee/+ie)
        val sixth: String,   // 6th order (consonant alone)
        val seventh: String, // 7th order (+o)
        val eighth: String? = null // 8th order (+wa)
    ) {
        val allForms: List<Pair<String, String>>
            get() = buildList {
                add("6th (base)" to sixth)
                add("1st (+e)" to first)
                add("2nd (+u)" to second)
                add("3rd (+i)" to third)
                add("4th (+a)" to fourth)
                add("5th (+ee)" to fifth)
                add("7th (+o)" to seventh)
                if (eighth != null) add("8th (+wa)" to eighth)
            }
    }

    // Core families including all explicitly requested ones:
    // w, h, l, m, r, s, z, q, b, t, n, k
    val FAMILIES: List<FidelFamily> = listOf(
        // Requested: "w" -> "ው", "we" -> "ወ", "wu" -> "ዉ", "wi" -> "ዊ", "wa" -> "ዋ", "wo" -> "ዎ"
        FidelFamily("w", "ወ", "ዉ", "ዊ", "ዋ", "ዌ", "ው", "ዎ"),

        // Requested: "h" -> "ህ", "he" -> "ሀ", "hu" -> "ሁ", "hi" -> "ሂ", "ha" -> "ሃ", "ho" -> "ሆ"
        FidelFamily("h", "ሀ", "ሁ", "ሂ", "ሃ", "ሄ", "ህ", "ሆ", "ኋ"),

        // Requested: "l" -> "ል", "le" -> "ለ", "lu" -> "ሉ", "li" -> "ሊ", "la" -> "ላ", "lo" -> "ሎ"
        FidelFamily("l", "ለ", "ሉ", "ሊ", "ላ", "ሌ", "ል", "ሎ", "ሏ"),

        // Requested: "m" -> "ም", "me" -> "መ", "mu" -> "ሙ", "mi" -> "ሚ", "ma" -> "ማ", "mo" -> "ሞ"
        FidelFamily("m", "መ", "ሙ", "ሚ", "ማ", "ሜ", "ም", "ሞ", "ሟ"),

        // Requested: "r" -> "ር", "re" -> "ረ", "ru" -> "ሩ", "ri" -> "ሪ", "ra" -> "ራ", "ro" -> "ሮ"
        FidelFamily("r", "ረ", "ሩ", "ሪ", "ራ", "ሬ", "ር", "ሮ", "ሯ"),

        // Requested: "s" -> "ስ", "se" -> "ሰ", "su" -> "ሱ", "si" -> "ሲ", "sa" -> "ሳ", "so" -> "ሶ"
        FidelFamily("s", "ሰ", "ሱ", "ሲ", "ሳ", "ሴ", "ስ", "ሶ", "ሷ"),

        // Requested: "z" -> "ዝ", "ze" -> "ዘ", "zu" -> "ዙ", "zi" -> "ዚ", "za" -> "ዛ", "zo" -> "ዞ"
        FidelFamily("z", "ዘ", "ዙ", "ዚ", "ዛ", "ዜ", "ዝ", "ዞ", "ዟ"),

        // Requested: "q" -> "ቅ", "qe" -> "ቀ", "qu" -> "ቁ", "qi" -> "ቂ", "qa" -> "ቃ", "qo" -> "ቆ"
        FidelFamily("q", "ቀ", "ቁ", "ቂ", "ቃ", "ቄ", "ቅ", "ቆ", "ቋ"),

        // Requested: "b" -> "ብ", "be" -> "በ", "bu" -> "ቡ", "bi" -> "ቢ", "ba" -> "ባ", "bo" -> "ቦ"
        FidelFamily("b", "በ", "ቡ", "ቢ", "ባ", "ቤ", "ብ", "ቦ", "ቧ"),

        // Requested: "t" -> "ት", "te" -> "ተ", "tu" -> "ቱ", "ti" -> "ቲ", "ta" -> "ታ", "to" -> "ቶ"
        FidelFamily("t", "ተ", "ቱ", "ቲ", "ታ", "ቴ", "ት", "ቶ", "ቷ"),

        // Requested: "n" -> "ን", "ne" -> "ነ", "nu" -> "ኑ", "ni" -> "ኒ", "na" -> "ና", "no" -> "ኖ"
        FidelFamily("n", "ነ", "ኑ", "ኒ", "ና", "ኔ", "ን", "ኖ", "ኗ"),

        // Requested: "k" -> "ክ", "ke" -> "ከ", "ku" -> "ኩ", "ki" -> "ኪ", "ka" -> "ካ", "ko" -> "ኮ"
        FidelFamily("k", "ከ", "ኩ", "ኪ", "ካ", "ኬ", "ክ", "ኮ", "ኳ"),

        // Additional standard Amharic consonants to enable typing any word:
        FidelFamily("d", "ደ", "ዱ", "ዲ", "ዳ", "ዴ", "ድ", "ዶ", "ዷ"),
        FidelFamily("j", "ጀ", "ጁ", "ጂ", "ጃ", "ጄ", "ጅ", "ጆ", "ጇ"),
        FidelFamily("g", "ገ", "ጉ", "ጊ", "ጋ", "ጌ", "ግ", "ጎ", "ጓ"),
        FidelFamily("f", "ፈ", "ፉ", "ፊ", "ፋ", "ፌ", "ፍ", "ፎ", "ፏ"),
        FidelFamily("p", "ፐ", "ፑ", "ፒ", "ፓ", "ፔ", "ፕ", "ፖ", "ፗ"),
        FidelFamily("v", "ቨ", "ቩ", "ቪ", "ቫ", "ቬ", "ቭ", "ቮ", "ቯ"),
        FidelFamily("y", "የ", "ዩ", "ዪ", "ያ", "ዬ", "ይ", "ዮ"),
        FidelFamily("c", "ቸ", "ቹ", "ቺ", "ቻ", "ቼ", "ች", "ቾ", "ቿ"),
        FidelFamily("ch", "ቸ", "ቹ", "ቺ", "ቻ", "ቼ", "ች", "ቾ", "ቿ"),
        FidelFamily("sh", "ሸ", "ሹ", "ሺ", "ሻ", "ሼ", "ሽ", "ሾ", "ሿ"),
        FidelFamily("x", "ሸ", "ሹ", "ሺ", "ሻ", "ሼ", "ሽ", "ሾ", "ሿ"),
        FidelFamily("gn", "ኘ", "ኙ", "ኚ", "ኛ", "ኜ", "ኝ", "ኞ", "ኟ"),
        FidelFamily("ny", "ኘ", "ኙ", "ኚ", "ኛ", "ኜ", "ኝ", "ኞ", "ኟ"),
        FidelFamily("ts", "ጸ", "ጹ", "ጺ", "ጻ", "ጼ", "ጽ", "ጾ", "ጿ"),
        FidelFamily("zh", "ዠ", "ዡ", "ዢ", "ዣ", "ዤ", "ዥ", "ዦ", "ዧ"),

        // Glottal / Emphatic consonants (accessible via uppercase or digraphs):
        FidelFamily("T", "ጠ", "ጡ", "ጢ", "ጣ", "ጤ", "ጥ", "ጦ", "ጧ"),
        FidelFamily("C", "ጨ", "ጩ", "ጪ", "ጫ", "ጬ", "ጭ", "ጮ", "ቿ"),
        FidelFamily("P", "ጰ", "ጱ", "ጲ", "ጳ", "ጴ", "ጵ", "ጶ", "ጷ"),
        FidelFamily("S", "ጸ", "ጹ", "ጺ", "ጻ", "ጼ", "ጽ", "ጾ", "ጿ"),
        FidelFamily("H", "ሐ", "ሑ", "ሒ", "ሓ", "ሔ", "ሕ", "ሖ", "ሗ"),
        FidelFamily("N", "ኘ", "ኙ", "ኚ", "ኛ", "ኜ", "ኝ", "ኞ", "ኟ"),
        FidelFamily("K", "ኸ", "ኹ", "ኺ", "ኻ", "ኼ", "ኽ", "ኾ")
    )

    /**
     * Complete lookup map from phonetic Latin sequence to Amharic character.
     */
    val PATTERNS: Map<String, String> by lazy {
        val map = mutableMapOf<String, String>()

        // 1. Independent Vowels (requested: "a" -> "አ")
        map["a"] = "አ"
        map["aa"] = "ኣ"
        map["u"] = "ኡ"
        map["uu"] = "ኡ"
        map["i"] = "ኢ"
        map["ii"] = "ኢ"
        map["e"] = "እ"
        map["ee"] = "ኤ"
        map["ie"] = "ኤ"
        map["ea"] = "ኤ"
        map["o"] = "ኦ"
        map["oo"] = "ኦ"

        // 2. Consonant mappings from families
        for (f in FAMILIES) {
            val k = f.baseKey
            // 6th order (consonant alone)
            map[k] = f.sixth
            // 1st order (+e)
            map["${k}e"] = f.first
            // 2nd order (+u)
            map["${k}u"] = f.second
            // 3rd order (+i)
            map["${k}i"] = f.third
            // 4th order (+a)
            map["${k}a"] = f.fourth
            // 5th order (+ee or +ie)
            map["${k}ee"] = f.fifth
            map["${k}ie"] = f.fifth
            // 7th order (+o)
            map["${k}o"] = f.seventh
            // 8th order (+wa)
            if (f.eighth != null) {
                map["${k}wa"] = f.eighth
            }
        }

        // 3. Ge'ez punctuation shortcuts
        map[":"] = "፡"
        map["::"] = "።"
        map[".."] = "።"
        map[",,"] = "፣"
        map[";;"] = "፤"

        map
    }

    /**
     * All prefixes that can potentially lead to a valid pattern.
     */
    val PREFIXES: Set<String> by lazy {
        val set = mutableSetOf<String>()
        for (key in PATTERNS.keys) {
            for (i in 1..key.length) {
                set.add(key.substring(0, i))
            }
        }
        set
    }

    /**
     * Quick family lookup by base key or prefix.
     */
    val FAMILY_BY_KEY: Map<String, FidelFamily> by lazy {
        FAMILIES.associateBy { it.baseKey }
    }
}
