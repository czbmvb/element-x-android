/*
 * GSPCOMS: eventos de la pantalla "WhatsApp".
 */

package io.element.android.features.preferences.impl.whatsapp

sealed interface WhatsAppEvents {
    data object Refresh : WhatsAppEvents
    data object Connect : WhatsAppEvents
    data object CancelLink : WhatsAppEvents
    data object DismissLink : WhatsAppEvents
    data class Disconnect(val loginId: String) : WhatsAppEvents
}
