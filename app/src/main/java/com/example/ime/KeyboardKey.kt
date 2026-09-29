package com.example.ime

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.KeyboardHide
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun KeyboardKey(
    action: KeyAction,
    label: String,
    subLabel: String? = null,
    modifier: Modifier = Modifier,
    isSpecial: Boolean = false,
    isActive: Boolean = false,
    onAction: (KeyAction) -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val keyBackground = when {
        isPressed -> MaterialTheme.colorScheme.primaryContainer
        isActive -> MaterialTheme.colorScheme.primary
        isSpecial -> MaterialTheme.colorScheme.surfaceVariant
        else -> MaterialTheme.colorScheme.surface
    }

    val contentColor = when {
        isPressed -> MaterialTheme.colorScheme.onPrimaryContainer
        isActive -> MaterialTheme.colorScheme.onPrimary
        isSpecial -> MaterialTheme.colorScheme.onSurfaceVariant
        else -> MaterialTheme.colorScheme.onSurface
    }

    Surface(
        modifier = modifier
            .padding(horizontal = 2.5.dp, vertical = 3.5.dp)
            .height(48.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = { onAction(action) }
            )
            .testTag("key_${label.lowercase().replace(" ", "_")}"),
        color = keyBackground,
        shadowElevation = if (isPressed) 0.dp else 1.5.dp,
        shape = RoundedCornerShape(8.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            when (action) {
                is KeyAction.Backspace -> {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Backspace",
                        tint = contentColor
                    )
                }
                is KeyAction.Shift -> {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowUp,
                        contentDescription = "Shift",
                        tint = contentColor
                    )
                }
                is KeyAction.Enter -> {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Enter",
                        tint = contentColor
                    )
                }
                is KeyAction.SwitchIme -> {
                    Icon(
                        imageVector = Icons.Default.Language,
                        contentDescription = "Switch Input Method",
                        tint = contentColor
                    )
                }
                is KeyAction.HideKeyboard -> {
                    Icon(
                        imageVector = Icons.Default.KeyboardHide,
                        contentDescription = "Hide Keyboard",
                        tint = contentColor
                    )
                }
                else -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(1.dp)
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = if (isSpecial) FontWeight.Medium else FontWeight.SemiBold,
                                fontSize = if (label.length > 3) 12.sp else 18.sp,
                                color = contentColor
                            )
                        )
                        if (subLabel != null) {
                            Text(
                                text = subLabel,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (isActive) contentColor.copy(alpha = 0.8f) else MaterialTheme.colorScheme.primary
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
