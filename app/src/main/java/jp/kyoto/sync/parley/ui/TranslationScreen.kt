package jp.kyoto.sync.parley.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import jp.kyoto.sync.parley.R
import jp.kyoto.sync.parley.core.Lang
import jp.kyoto.sync.parley.core.Mode
import jp.kyoto.sync.parley.core.Status
import jp.kyoto.sync.parley.ui.theme.ParleyBrand
import jp.kyoto.sync.parley.ui.theme.ParleyEmerald
import jp.kyoto.sync.parley.ui.theme.ParleySky

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TranslationScreen(
    vm: TranslationViewModel,
    onOpenSettings: () -> Unit,
    onOpenHistory: () -> Unit,
) {
    val status by vm.status.collectAsState()
    val mode by vm.mode.collectAsState()
    val conv by vm.conversation.collectAsState()
    val partnerText by vm.partnerTranscript.collectAsState()
    val myText by vm.myTranscript.collectAsState()
    val error by vm.errorMessage.collectAsState()

    val running = status == Status.RUNNING
    val connecting = status == Status.CONNECTING
    val idle = status == Status.IDLE || status == Status.ERROR

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.app_name),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                },
                actions = {
                    if (running || connecting) {
                        IconButton(onClick = vm::stop) {
                            Icon(Icons.Filled.Stop, contentDescription = stringResource(R.string.cd_stop))
                        }
                    }
                    IconButton(onClick = onOpenHistory) {
                        Icon(Icons.Filled.History, contentDescription = stringResource(R.string.cd_history))
                    }
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Filled.Settings, contentDescription = stringResource(R.string.cd_settings))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 20.dp),
        ) {
            LanguageSelector(
                myLang = conv.myLang,
                partnerLang = conv.partnerLang,
                enabled = idle,
                onSwap = vm::swapDirection,
            )
            Spacer(Modifier.height(16.dp))

            CaptionCard(
                role = stringResource(R.string.label_partner),
                target = stringResource(R.string.target_arrow, conv.myLang.displayName),
                icon = Icons.Filled.Headphones,
                accent = MaterialTheme.colorScheme.secondary,
                active = running && mode == Mode.LISTENING,
                text = partnerText,
                placeholder = stringResource(R.string.caption_partner_placeholder),
                modifier = Modifier.weight(1f),
            )
            Spacer(Modifier.height(12.dp))
            CaptionCard(
                role = stringResource(R.string.label_you),
                target = stringResource(R.string.target_arrow, conv.partnerLang.displayName),
                icon = Icons.Filled.Mic,
                accent = MaterialTheme.colorScheme.tertiary,
                active = running && mode == Mode.SPEAKING,
                text = myText,
                placeholder = stringResource(R.string.caption_you_placeholder),
                modifier = Modifier.weight(1f),
            )

            Spacer(Modifier.height(14.dp))
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                StatusPill(status, mode)
            }

            error?.let {
                Spacer(Modifier.height(8.dp))
                Text(
                    stringResource(R.string.error_prefix, it),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                )
            }

            Spacer(Modifier.height(6.dp))
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                HeroButton(
                    status = status,
                    speaking = mode == Mode.SPEAKING,
                    onStart = vm::start,
                    onSpeakStart = vm::pttDown,
                    onSpeakEnd = vm::pttUp,
                )
            }
            Text(
                text = when {
                    connecting -> stringResource(R.string.status_connecting)
                    running && mode == Mode.SPEAKING -> stringResource(R.string.hint_speaking)
                    running -> stringResource(R.string.hint_hold_to_talk)
                    else -> stringResource(R.string.hint_tap_to_start)
                },
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
            )
            if (idle && (partnerText.isNotEmpty() || myText.isNotEmpty())) {
                TextButton(
                    onClick = vm::newConversation,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(stringResource(R.string.new_conversation))
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun LanguageSelector(
    myLang: Lang,
    partnerLang: Lang,
    enabled: Boolean,
    onSwap: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        LangPill(stringResource(R.string.label_you), myLang, Modifier.weight(1f))
        FilledTonalIconButton(onClick = onSwap, enabled = enabled) {
            Icon(Icons.Filled.SwapHoriz, contentDescription = stringResource(R.string.cd_swap_lang))
        }
        LangPill(stringResource(R.string.label_partner), partnerLang, Modifier.weight(1f))
    }
}

@Composable
private fun LangPill(label: String, lang: Lang, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
        modifier = modifier,
    ) {
        Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Text(
                label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                lang.displayName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun CaptionCard(
    role: String,
    target: String,
    icon: ImageVector,
    accent: Color,
    active: Boolean,
    text: String,
    placeholder: String,
    modifier: Modifier = Modifier,
) {
    val border = if (active) {
        BorderStroke(1.5.dp, accent)
    } else {
        BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
    }
    val container = if (active) accent.copy(alpha = 0.10f) else MaterialTheme.colorScheme.surface

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = container,
        border = border,
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(16.dp).fillMaxSize()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(28.dp).clip(CircleShape).background(accent.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(16.dp))
                }
                Spacer(Modifier.width(8.dp))
                Text(role, style = MaterialTheme.typography.labelLarge, color = accent)
                Spacer(Modifier.weight(1f))
                Text(
                    target,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (active) {
                    Spacer(Modifier.width(8.dp))
                    LiveDot(accent)
                }
            }
            Spacer(Modifier.height(10.dp))
            val scroll = rememberScrollState()
            LaunchedEffect(text) { scroll.animateScrollTo(scroll.maxValue) }
            Text(
                text = text.ifEmpty { placeholder },
                style = MaterialTheme.typography.bodyLarge,
                color = if (text.isEmpty()) {
                    MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                modifier = Modifier.fillMaxWidth().weight(1f).verticalScroll(scroll),
            )
        }
    }
}

@Composable
private fun LiveDot(color: Color) {
    val infinite = rememberInfiniteTransition(label = "live")
    val alpha by infinite.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(700), RepeatMode.Reverse),
        label = "liveAlpha",
    )
    Box(Modifier.size(8.dp).clip(CircleShape).background(color.copy(alpha = alpha)))
}

@Composable
private fun StatusPill(status: Status, mode: Mode) {
    val dotColor: Color
    val label: String
    when (status) {
        Status.IDLE -> {
            dotColor = MaterialTheme.colorScheme.onSurfaceVariant
            label = stringResource(R.string.status_idle)
        }
        Status.CONNECTING -> {
            dotColor = ParleySky
            label = stringResource(R.string.status_connecting)
        }
        Status.RUNNING -> if (mode == Mode.SPEAKING) {
            dotColor = MaterialTheme.colorScheme.tertiary
            label = stringResource(R.string.status_speaking)
        } else {
            dotColor = MaterialTheme.colorScheme.secondary
            label = stringResource(R.string.status_listening)
        }
        Status.ERROR -> {
            dotColor = MaterialTheme.colorScheme.error
            label = stringResource(R.string.status_error)
        }
    }
    Surface(shape = RoundedCornerShape(50), color = MaterialTheme.colorScheme.surface) {
        Row(
            Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(Modifier.size(8.dp).clip(CircleShape).background(dotColor))
            Text(
                label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun HeroButton(
    status: Status,
    speaking: Boolean,
    onStart: () -> Unit,
    onSpeakStart: () -> Unit,
    onSpeakEnd: () -> Unit,
) {
    val running = status == Status.RUNNING
    val connecting = status == Status.CONNECTING

    val infinite = rememberInfiniteTransition(label = "pulse")
    val pulse by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1300, easing = LinearEasing), RepeatMode.Restart),
        label = "pulseValue",
    )

    Box(modifier = Modifier.size(176.dp), contentAlignment = Alignment.Center) {
        if (running && speaking) {
            Box(
                Modifier
                    .size(120.dp + 52.dp * pulse)
                    .clip(CircleShape)
                    .background(ParleyEmerald.copy(alpha = 0.28f * (1f - pulse))),
            )
        }

        val brush = if (running && speaking) ParleyBrand.activeGradient else ParleyBrand.gradient
        val interactionMod = if (running) {
            Modifier.pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        onSpeakStart()
                        tryAwaitRelease()
                        onSpeakEnd()
                    },
                )
            }
        } else {
            Modifier.clickable(enabled = !connecting) { onStart() }
        }

        Box(
            modifier = Modifier
                .size(116.dp)
                .clip(CircleShape)
                .background(brush)
                .then(interactionMod),
            contentAlignment = Alignment.Center,
        ) {
            when {
                connecting -> CircularProgressIndicator(
                    color = Color.White,
                    strokeWidth = 3.dp,
                    modifier = Modifier.size(36.dp),
                )
                running -> Icon(
                    Icons.Filled.Mic,
                    contentDescription = stringResource(R.string.hint_hold_to_talk),
                    tint = Color.White,
                    modifier = Modifier.size(44.dp),
                )
                else -> Icon(
                    Icons.Filled.PlayArrow,
                    contentDescription = stringResource(R.string.cd_start),
                    tint = Color.White,
                    modifier = Modifier.size(48.dp),
                )
            }
        }
    }
}
