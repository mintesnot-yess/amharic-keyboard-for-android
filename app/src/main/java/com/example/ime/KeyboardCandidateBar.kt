package com.example.ime

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.CandidateItem

val DEFAULT_QUICK_PHRASES = listOf("ሰላም", "ጤና ይስጥልኝ", "አመሰግናለሁ", "አዎ", "አይ", "እንዴት ነህ", "መልካም ቀን", "፡", "።", "፣")

@Composable
fun KeyboardCandidateBar(
    isComposing: Boolean,
    composingChar: String,
    rawBuffer: String,
    candidates: List<CandidateItem>,
    mode: TransliterationMode,
    onSelectCandidate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHighest,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 4.dp)
                .horizontalScroll(scrollState),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isComposing && candidates.isNotEmpty()) {
                // Active composing indicator badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "[$rawBuffer] $composingChar",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }

                // Family candidates
                candidates.forEach { candidate ->
                    val isSelected = candidate.geezChar == composingChar
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surface
                            )
                            .clickable { onSelectCandidate(candidate.geezChar) }
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                            .testTag("candidate_${candidate.geezChar}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = candidate.geezChar,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                                    else MaterialTheme.colorScheme.onSurface
                                )
                            )
                            Text(
                                text = candidate.keystroke,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.sp,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }
                }
            } else {
                // Quick phrases / punctuation when not actively composing
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            if (mode == TransliterationMode.AMHARIC) MaterialTheme.colorScheme.tertiaryContainer
                            else MaterialTheme.colorScheme.secondaryContainer
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (mode == TransliterationMode.AMHARIC) "አማ (Phonetic)" else "EN",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = if (mode == TransliterationMode.AMHARIC) MaterialTheme.colorScheme.onTertiaryContainer
                            else MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    )
                }

                DEFAULT_QUICK_PHRASES.forEach { phrase ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .clickable { onSelectCandidate(phrase) }
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                            .testTag("quick_phrase_$phrase"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = phrase,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                    }
                }
            }
        }
    }
}
