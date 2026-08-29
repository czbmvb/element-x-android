/*
 * GSPCOMS: estado de la pantalla "WhatsApp" (conectar/desconectar cuentas vía el puente).
 */

package io.element.android.features.preferences.impl.whatsapp

import io.element.android.libraries.architecture.AsyncData
import kotlinx.collections.immutable.ImmutableList

sealed interface WhatsAppLinkState {
    data object Idle : WhatsAppLinkState
    data object Starting : WhatsAppLinkState
    data class ShowingQr(val loginId: String, val qrData: String) : WhatsAppLinkState
    data class ShowingCode(val loginId: String, val code: String) : WhatsAppLinkState
    data object Success : WhatsAppLinkState
    data class Error(val message: String) : WhatsAppLinkState
}

data class WhatsAppState(
    val info: AsyncData<ImmutableList<WhatsAppLogin>>,
    val link: WhatsAppLinkState,
    val disconnecting: Boolean,
    val eventSink: (WhatsAppEvents) -> Unit,
)
