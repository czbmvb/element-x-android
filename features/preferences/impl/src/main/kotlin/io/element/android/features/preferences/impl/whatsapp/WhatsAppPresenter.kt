/*
 * GSPCOMS: lógica de la pantalla "WhatsApp".
 */

package io.element.android.features.preferences.impl.whatsapp

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import dev.zacsweers.metro.Inject
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.architecture.Presenter
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@Inject
class WhatsAppPresenter(
    private val api: WhatsAppBridgeApi,
) : Presenter<WhatsAppState> {
    @Composable
    override fun present(): WhatsAppState {
        val coroutineScope = rememberCoroutineScope()
        var info by remember { mutableStateOf<AsyncData<ImmutableList<WhatsAppLogin>>>(AsyncData.Uninitialized) }
        var link by remember { mutableStateOf<WhatsAppLinkState>(WhatsAppLinkState.Idle) }
        var disconnecting by remember { mutableStateOf(false) }
        var linkJob by remember { mutableStateOf<Job?>(null) }

        suspend fun load() {
            info = AsyncData.Loading()
            info = runCatching { api.whoami().logins.toImmutableList() }
                .fold(
                    onSuccess = { AsyncData.Success(it) },
                    onFailure = { AsyncData.Failure(it) },
                )
        }

        LaunchedEffect(Unit) { load() }

        fun CoroutineScope.startLink() = launch {
            link = WhatsAppLinkState.Starting
            try {
                var step = api.startQrLogin()
                // Bucle: mostrar QR/código y esperar; el puente renueva el QR cuando caduca.
                while (true) {
                    when (val current = step) {
                        is WhatsAppLoginStep.ShowQr -> {
                            link = WhatsAppLinkState.ShowingQr(current.loginId, current.qrData)
                            step = api.waitForStep(current.loginId, current.stepId)
                        }
                        is WhatsAppLoginStep.ShowCode -> {
                            link = WhatsAppLinkState.ShowingCode(current.loginId, current.code)
                            step = api.waitForStep(current.loginId, current.stepId)
                        }
                        is WhatsAppLoginStep.Complete -> {
                            link = WhatsAppLinkState.Success
                            load()
                            return@launch
                        }
                        is WhatsAppLoginStep.Other -> {
                            link = WhatsAppLinkState.Error("Paso no soportado: ${current.type}")
                            api.cancelLogin(current.loginId)
                            return@launch
                        }
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                link = WhatsAppLinkState.Error(e.message ?: e.javaClass.simpleName)
            }
        }

        fun currentLoginId(): String? = when (val l = link) {
            is WhatsAppLinkState.ShowingQr -> l.loginId
            is WhatsAppLinkState.ShowingCode -> l.loginId
            else -> null
        }

        fun handleEvent(event: WhatsAppEvents) {
            when (event) {
                WhatsAppEvents.Refresh -> coroutineScope.launch { load() }
                WhatsAppEvents.Connect -> {
                    linkJob?.cancel()
                    linkJob = coroutineScope.startLink()
                }
                WhatsAppEvents.CancelLink -> {
                    val id = currentLoginId()
                    linkJob?.cancel()
                    linkJob = null
                    link = WhatsAppLinkState.Idle
                    if (id != null) coroutineScope.launch { api.cancelLogin(id) }
                }
                WhatsAppEvents.DismissLink -> {
                    link = WhatsAppLinkState.Idle
                }
                is WhatsAppEvents.Disconnect -> coroutineScope.launch {
                    disconnecting = true
                    runCatching { api.logout(event.loginId) }
                        .onFailure { link = WhatsAppLinkState.Error(it.message ?: it.javaClass.simpleName) }
                    disconnecting = false
                    load()
                }
            }
        }

        return WhatsAppState(
            info = info,
            link = link,
            disconnecting = disconnecting,
            eventSink = ::handleEvent,
        )
    }
}
