package com.icure.cardinal.compose.multiplatform.ui.screens.auth

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import cardinalcomposemultiplatform.shared.generated.resources.Res
import cardinalcomposemultiplatform.shared.generated.resources.solving_body
import cardinalcomposemultiplatform.shared.generated.resources.solving_cancel
import cardinalcomposemultiplatform.shared.generated.resources.solving_percent
import cardinalcomposemultiplatform.shared.generated.resources.solving_step_done
import cardinalcomposemultiplatform.shared.generated.resources.solving_step_proof
import cardinalcomposemultiplatform.shared.generated.resources.solving_step_request
import cardinalcomposemultiplatform.shared.generated.resources.solving_step_running
import cardinalcomposemultiplatform.shared.generated.resources.solving_step_waiting
import cardinalcomposemultiplatform.shared.generated.resources.solving_title
import com.icure.cardinal.compose.multiplatform.ui.components.CardinalHaloIcon
import com.icure.cardinal.compose.multiplatform.ui.components.CardinalProgressTrack
import com.icure.cardinal.compose.multiplatform.ui.components.CardinalSecondaryButton
import com.icure.cardinal.compose.multiplatform.ui.viewmodels.AppViewModel
import com.icure.cardinal.compose.multiplatform.ui.viewmodels.AuthState
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

/** Digits that do not shift width as the percentage counts up. */
private val TabularFigures = TextStyle(fontFeatureSettings = "tnum")

/**
 * The Kerberus proof-of-work check, shown full screen between the email step and the
 * code step.
 *
 * The SDK is initialised the moment an address is submitted, so this is where the
 * user waits. Nothing here is interactive: the point of the screen is that the couple
 * of seconds are accounted for rather than unexplained.
 */
@Composable
fun SolvingChallengeScreen(
    appViewModel: AppViewModel,
    modifier: Modifier = Modifier
) {
    val state by appViewModel.authState.collectAsState()
    val solving = state as? AuthState.SolvingChallenge ?: return

    SolvingChallengeScreen(
        progress = solving.progress.toFloat(),
        // Nothing cancels an in-flight `initializeWithProcess` yet, so the button
        // stays out. Add a cancel intent and pass it here to bring it back.
        onCancel = null,
        modifier = modifier
    )
}

/**
 * The check as pure UI.
 *
 * @param progress 0f..1f, the fraction of the proof of work completed.
 * @param onCancel pass `null` when the work cannot be abandoned — the button is then
 *   omitted rather than rendered dead.
 */
@Composable
fun SolvingChallengeScreen(
    progress: Float,
    modifier: Modifier = Modifier,
    onCancel: (() -> Unit)? = null
) {
    val colors = MaterialTheme.colorScheme
    val clamped = progress.coerceIn(0f, 1f)

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = colors.background
    ) { insets ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(insets)
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterVertically)
            ) {
                CardinalHaloIcon(icon = Icons.Default.Lock)

                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = stringResource(Res.string.solving_title),
                            style = MaterialTheme.typography.titleLarge,
                            color = colors.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = stringResource(
                                Res.string.solving_percent,
                                (clamped * 100).toInt().toString()
                            ),
                            style = MaterialTheme.typography.bodySmall.merge(TabularFigures),
                            color = colors.primary
                        )
                    }
                    Text(
                        text = stringResource(Res.string.solving_body),
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }

                CardinalProgressTrack(progress = clamped)

                // Named steps, so a failure can say which one broke. The proof of work
                // reports its own progress; the request that follows it does not, so
                // reaching 100 % is what marks the handover.
                Column(
                    modifier = Modifier.padding(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StepRow(
                        text = stringResource(Res.string.solving_step_proof),
                        state = if (clamped < 1f) StepState.Running else StepState.Done
                    )
                    StepRow(
                        text = stringResource(Res.string.solving_step_request),
                        state = if (clamped < 1f) StepState.Waiting else StepState.Running
                    )
                }
            }

            if (onCancel != null) {
                CardinalSecondaryButton(
                    text = stringResource(Res.string.solving_cancel),
                    onClick = onCancel
                )
            }
        }
    }
}

/** Where a named step has got to. */
private enum class StepState { Done, Running, Waiting }

/**
 * One named step.
 *
 * The glyph carries the state visually and the row carries it in its content
 * description, so the two never have to be read together.
 */
@Composable
private fun StepRow(
    text: String,
    state: StepState,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme
    val stateLabel = when (state) {
        StepState.Done -> stringResource(Res.string.solving_step_done)
        StepState.Running -> stringResource(Res.string.solving_step_running)
        StepState.Waiting -> stringResource(Res.string.solving_step_waiting)
    }
    val alpha = when (state) {
        StepState.Done, StepState.Running -> 1f
        StepState.Waiting -> 0.4f
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) { contentDescription = "$text — $stateLabel" },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier.size(18.dp),
            contentAlignment = Alignment.Center
        ) {
            when (state) {
                StepState.Done -> Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = colors.primary,
                    modifier = Modifier.size(18.dp)
                )

                StepState.Running -> CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = colors.primary
                )

                // An empty ring, drawn rather than iconified: the core icon set has no
                // unfilled circle.
                StepState.Waiting -> Box(
                    modifier = Modifier
                        .size(16.dp)
                        .border(1.dp, colors.onSurface.copy(alpha = alpha), CircleShape)
                )
            }
        }
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.onSurface.copy(alpha = alpha),
            modifier = Modifier.clearAndSetSemantics { }
        )
    }
}

@Preview
@Composable
private fun SolvingChallengeStartPreview() {
    MaterialTheme {
        SolvingChallengeScreen(progress = 0.12f)
    }
}

@Preview
@Composable
private fun SolvingChallengeMidPreview() {
    MaterialTheme {
        SolvingChallengeScreen(progress = 0.68f, onCancel = {})
    }
}

@Preview
@Composable
private fun SolvingChallengeHandoverPreview() {
    MaterialTheme {
        SolvingChallengeScreen(progress = 1f)
    }
}
