/*
 * GSPCOMS: pantalla "WhatsApp" — conectar el WhatsApp del usuario al chat (puente mautrix).
 */

package io.element.android.features.preferences.impl.whatsapp

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.features.preferences.impl.R
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.designsystem.components.list.ListItemContent
import io.element.android.libraries.designsystem.components.preferences.PreferencePage
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Button
import io.element.android.libraries.designsystem.theme.components.CircularProgressIndicator
import io.element.android.libraries.designsystem.theme.components.IconSource
import io.element.android.libraries.designsystem.theme.components.ListItem
import io.element.android.libraries.designsystem.theme.components.OutlinedButton
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.theme.components.TextButton
import io.element.android.libraries.qrcode.QrCodeImage

@Composable
fun WhatsAppView(
    state: WhatsAppState,
    onBackClick: () -> Unit,
    onOpenWhatsApp: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    PreferencePage(
        modifier = modifier,
        onBackClick = onBackClick,
        title = stringResource(id = R.string.screen_whatsapp_title),
    ) {
        when (val link = state.link) {
            WhatsAppLinkState.Idle -> ConnectedList(state)
            WhatsAppLinkState.Starting -> Centered {
                CircularProgressIndicator()
                Spacer(Modifier.height(12.dp))
                Text(text = stringResource(id = R.string.screen_whatsapp_starting), textAlign = TextAlign.Center)
            }
            is WhatsAppLinkState.ShowingQr -> Centered {
                Text(
                    text = stringResource(id = R.string.screen_whatsapp_same_phone_hint),
                    style = ElementTheme.typography.fontBodyLgRegular,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(16.dp))
                Button(
                    text = stringResource(id = R.string.screen_whatsapp_open_whatsapp),
                    onClick = { onOpenWhatsApp(link.qrData) },
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(24.dp))
                Text(
                    text = stringResource(id = R.string.screen_whatsapp_other_phone_hint),
                    style = ElementTheme.typography.fontBodyMdRegular,
                    color = ElementTheme.colors.textSecondary,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(12.dp))
                QrCodeImage(data = link.qrData, modifier = Modifier.fillMaxWidth(0.65f))
                Spacer(Modifier.height(16.dp))
                Text(
                    text = stringResource(id = R.string.screen_whatsapp_waiting),
                    style = ElementTheme.typography.fontBodySmRegular,
                    color = ElementTheme.colors.textSecondary,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(8.dp))
                TextButton(
                    text = stringResource(id = R.string.screen_whatsapp_cancel),
                    onClick = { state.eventSink(WhatsAppEvents.CancelLink) },
                )
            }
            is WhatsAppLinkState.ShowingCode -> Centered {
                Text(text = stringResource(id = R.string.screen_whatsapp_code_hint), textAlign = TextAlign.Center)
                Spacer(Modifier.height(12.dp))
                Text(text = link.code, style = ElementTheme.typography.fontHeadingLgBold)
                Spacer(Modifier.height(12.dp))
                TextButton(
                    text = stringResource(id = R.string.screen_whatsapp_cancel),
                    onClick = { state.eventSink(WhatsAppEvents.CancelLink) },
                )
            }
            WhatsAppLinkState.Success -> Centered {
                Text(
                    text = stringResource(id = R.string.screen_whatsapp_success),
                    style = ElementTheme.typography.fontBodyLgRegular,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(16.dp))
                Button(
                    text = stringResource(id = R.string.screen_whatsapp_done),
                    onClick = { state.eventSink(WhatsAppEvents.DismissLink) },
                )
            }
            is WhatsAppLinkState.Error -> Centered {
                Text(
                    text = stringResource(id = R.string.screen_whatsapp_error, link.message),
                    color = ElementTheme.colors.textCriticalPrimary,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(16.dp))
                OutlinedButton(
                    text = stringResource(id = R.string.screen_whatsapp_retry),
                    onClick = { state.eventSink(WhatsAppEvents.Connect) },
                )
                TextButton(
                    text = stringResource(id = R.string.screen_whatsapp_cancel),
                    onClick = { state.eventSink(WhatsAppEvents.DismissLink) },
                )
            }
        }
    }
}

@Composable
private fun ConnectedList(state: WhatsAppState) {
    when (val info = state.info) {
        AsyncData.Uninitialized, is AsyncData.Loading -> Centered { CircularProgressIndicator() }
        is AsyncData.Failure -> Centered {
            Text(
                text = stringResource(id = R.string.screen_whatsapp_error, info.error.message ?: ""),
                color = ElementTheme.colors.textCriticalPrimary,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(12.dp))
            OutlinedButton(
                text = stringResource(id = R.string.screen_whatsapp_retry),
                onClick = { state.eventSink(WhatsAppEvents.Refresh) },
            )
        }
        is AsyncData.Success -> {
            Text(
                text = stringResource(id = R.string.screen_whatsapp_intro),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                style = ElementTheme.typography.fontBodyMdRegular,
                color = ElementTheme.colors.textSecondary,
            )
            if (info.data.isEmpty()) {
                Text(
                    text = stringResource(id = R.string.screen_whatsapp_none),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )
            } else {
                info.data.forEach { login ->
                    val stateRes = if (login.isConnected) {
                        R.string.screen_whatsapp_state_connected
                    } else {
                        R.string.screen_whatsapp_state_disconnected
                    }
                    ListItem(
                        headlineContent = { Text(text = login.name) },
                        supportingContent = { Text(text = stringResource(id = stateRes)) },
                        leadingContent = ListItemContent.Icon(IconSource.Vector(CompoundIcons.Mobile())),
                        trailingContent = ListItemContent.Text(stringResource(id = R.string.screen_whatsapp_disconnect)),
                        enabled = !state.disconnecting,
                        onClick = { state.eventSink(WhatsAppEvents.Disconnect(login.id)) },
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
            Button(
                text = stringResource(id = R.string.screen_whatsapp_connect),
                onClick = { state.eventSink(WhatsAppEvents.Connect) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                leadingIcon = IconSource.Vector(CompoundIcons.Plus()),
            )
        }
    }
}

@Composable
private fun Centered(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        content()
    }
}

@PreviewsDayNight
@Composable
internal fun WhatsAppViewPreview(@PreviewParameter(WhatsAppStateProvider::class) state: WhatsAppState) = ElementPreview {
    WhatsAppView(
        state = state,
        onBackClick = {},
        onOpenWhatsApp = {},
    )
}
