/*
 * GSPCOMS: estados de ejemplo para previews de la pantalla "WhatsApp".
 */

package io.element.android.features.preferences.impl.whatsapp

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.libraries.architecture.AsyncData
import kotlinx.collections.immutable.persistentListOf

open class WhatsAppStateProvider : PreviewParameterProvider<WhatsAppState> {
    override val values: Sequence<WhatsAppState>
        get() = sequenceOf(
            aWhatsAppState(),
            aWhatsAppState(link = WhatsAppLinkState.ShowingQr("l1", "https://wa.me/settings/linked_devices#demo")),
            aWhatsAppState(link = WhatsAppLinkState.Success),
            aWhatsAppState(link = WhatsAppLinkState.Error("Sin permiso")),
        )
}

fun aWhatsAppState(
    link: WhatsAppLinkState = WhatsAppLinkState.Idle,
) = WhatsAppState(
    info = AsyncData.Success(persistentListOf(WhatsAppLogin(id = "1", name = "+52 55 1234 5678", isConnected = true))),
    link = link,
    disconnecting = false,
    eventSink = {},
)
