package com.cometcourier.run.presentation.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import com.cometcourier.run.R
import com.cometcourier.run.core.theme.*
import com.cometcourier.run.core.ui.*
import com.cometcourier.run.domain.model.CourierSettings
import com.cometcourier.run.domain.model.SettingKey
import com.cometcourier.run.presentation.dialog.HowToPlayDialog

@Composable
fun SettingsScreen(state: SettingsUiState, onChange: (CourierSettings) -> Unit, onRules: (Boolean) -> Unit, onMenu: () -> Unit, onRetry: () -> Unit) {
    val enabled = !state.loading && !state.saveError && !state.readError
    val s = state.settings
    AppPage(actions = {
        Text(stringResource(R.string.local_preferences), color = Ink, style = MaterialTheme.typography.bodyMedium)
        SecondaryButton(R.string.menu, onMenu, Modifier.fillMaxWidth(), icon = R.drawable.icon_home)
    }) {
        ScreenHeader(R.string.settings_title, trailing = { CourierArtwork("sprout", 64.dp, decorative = true) })
        StatusMessage(state.readError, state.saveError, onRetry)
        if (state.loading || state.savingKeys.isNotEmpty()) LinearProgressIndicator(Modifier.fillMaxWidth(), color = CourierGreen)
        OrbitSurface(Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.controls), style = MaterialTheme.typography.titleLarge)
            listOf(false, true).forEach { buttonsOnly ->
                val label = stringResource(if (buttonsOnly) R.string.buttons_only else R.string.swipe_buttons)
                Row(Modifier.fillMaxWidth().heightIn(min = 56.dp).selectable(s.buttonsOnly == buttonsOnly, enabled = enabled && SettingKey.ButtonsOnly !in state.savingKeys, role = Role.RadioButton,
                    onClick = { onChange(s.copy(buttonsOnly = buttonsOnly)) }).semantics { stateDescription = label }, verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(s.buttonsOnly == buttonsOnly, null, enabled = enabled && SettingKey.ButtonsOnly !in state.savingKeys, colors = RadioButtonDefaults.colors(selectedColor = Ink, unselectedColor = Ink))
                    Text(label, Modifier.padding(start = 12.dp).weight(1f), style = MaterialTheme.typography.bodyLarge)
                }
            }
            SecondaryButton(R.string.how_to_play, { onRules(true) }, Modifier.fillMaxWidth())
        }
        OrbitSurface(Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.feedback), style = MaterialTheme.typography.titleLarge)
            PreferenceRow(R.string.sound, R.string.sound_hint, s.sound, enabled && SettingKey.Sound !in state.savingKeys) { onChange(s.copy(sound = it)) }
            PreferenceRow(R.string.haptics, R.string.haptics_hint, s.haptics, enabled && SettingKey.Haptics !in state.savingKeys) { onChange(s.copy(haptics = it)) }
        }
        OrbitSurface(Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.motion), style = MaterialTheme.typography.titleLarge)
            PreferenceRow(R.string.reduced_motion, R.string.motion_hint, s.reducedMotion, enabled && SettingKey.ReducedMotion !in state.savingKeys) { onChange(s.copy(reducedMotion = it)) }
        }
    }
    if (state.showRules) HowToPlayDialog { onRules(false) }
}

@Composable
private fun PreferenceRow(label: Int, hint: Int, checked: Boolean, enabled: Boolean, onChange: (Boolean) -> Unit) {
    val description = stringResource(if (checked) R.string.enabled else R.string.disabled)
    Row(Modifier.fillMaxWidth().heightIn(min = 64.dp).semantics(mergeDescendants = true) { stateDescription = description }, verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f).padding(end = 8.dp)) {
            Text(stringResource(label), style = MaterialTheme.typography.bodyLarge)
            Text(stringResource(hint), style = MaterialTheme.typography.bodyMedium)
        }
        Switch(checked, onChange, enabled = enabled, modifier = Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp),
            colors = SwitchDefaults.colors(checkedThumbColor = MintBackground, checkedTrackColor = Ink, uncheckedThumbColor = Ink, uncheckedTrackColor = SoftMint))
    }
}
