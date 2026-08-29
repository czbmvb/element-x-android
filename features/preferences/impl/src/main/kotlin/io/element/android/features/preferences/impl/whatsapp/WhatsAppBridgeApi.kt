/*
 * GSPCOMS: cliente mínimo de la API de aprovisionamiento del puente mautrix-whatsapp.
 * Habla con <homeserver>/_matrix/provision/v3 usando el access token de la sesión.
 */

package io.element.android.features.preferences.impl.whatsapp

import dev.zacsweers.metro.Inject
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.sessionstorage.api.SessionStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

data class WhatsAppLogin(
    val id: String,
    val name: String,
    val isConnected: Boolean,
)

data class WhatsAppInfo(
    val bridgeBot: String,
    val logins: List<WhatsAppLogin>,
)

sealed interface WhatsAppLoginStep {
    data class ShowQr(val loginId: String, val stepId: String, val qrData: String) : WhatsAppLoginStep
    data class ShowCode(val loginId: String, val stepId: String, val code: String) : WhatsAppLoginStep
    data class Complete(val loginId: String) : WhatsAppLoginStep
    data class Other(val loginId: String, val type: String) : WhatsAppLoginStep
}

class WhatsAppBridgeException(message: String) : IOException(message)

@Inject
class WhatsAppBridgeApi(
    private val okHttpClient: OkHttpClient,
    private val sessionStore: SessionStore,
    private val matrixClient: MatrixClient,
) {
    private val json = "application/json; charset=utf-8".toMediaType()

    // display_and_wait bloquea hasta que WhatsApp confirma (o el QR caduca): timeout largo.
    private val longPollClient: OkHttpClient by lazy {
        okHttpClient.newBuilder()
            .readTimeout(150, TimeUnit.SECONDS)
            .build()
    }

    private suspend fun session() = sessionStore.getSession(matrixClient.sessionId.value)
        ?: throw WhatsAppBridgeException("Sin sesión")

    private suspend fun url(path: String): String {
        val s = session()
        val base = s.homeserverUrl.trimEnd('/')
        return "$base/_matrix/provision/v3/$path".toHttpUrl()
            .newBuilder()
            .addQueryParameter("user_id", s.userId)
            .build()
            .toString()
    }

    private suspend fun call(
        path: String,
        post: Boolean,
        body: String? = null,
        longPoll: Boolean = false,
    ): JSONObject = withContext(Dispatchers.IO) {
        val token = session().accessToken
        val builder = Request.Builder()
            .url(url(path))
            .header("Authorization", "Bearer $token")
        if (post) builder.post((body ?: "{}").toRequestBody(json))
        val client = if (longPoll) longPollClient else okHttpClient
        client.newCall(builder.build()).execute().use { response ->
            val text = response.body.string()
            val obj = runCatching { JSONObject(text) }.getOrElse { JSONObject() }
            if (!response.isSuccessful) {
                val err = obj.optString("error").ifBlank { "HTTP ${response.code}" }
                throw WhatsAppBridgeException(err)
            }
            obj
        }
    }

    suspend fun whoami(): WhatsAppInfo {
        val obj = call("whoami", post = false)
        val logins = mutableListOf<WhatsAppLogin>()
        val arr = obj.optJSONArray("logins")
        if (arr != null) {
            for (i in 0 until arr.length()) {
                val l = arr.getJSONObject(i)
                val id = l.optString("id")
                val name = l.optString("name")
                    .ifBlank { l.optJSONObject("profile")?.optString("phone").orEmpty() }
                    .ifBlank { id }
                val state = l.optJSONObject("state")?.optString("state_event").orEmpty()
                logins += WhatsAppLogin(id = id, name = name, isConnected = state.isBlank() || state == "CONNECTED")
            }
        }
        return WhatsAppInfo(bridgeBot = obj.optString("bridge_bot"), logins = logins)
    }

    private fun parseStep(obj: JSONObject): WhatsAppLoginStep {
        val loginId = obj.optString("login_id")
        val stepId = obj.optString("step_id")
        return when (obj.optString("type")) {
            "complete" -> WhatsAppLoginStep.Complete(loginId)
            "display_and_wait" -> {
                val dw = obj.optJSONObject("display_and_wait") ?: JSONObject()
                val data = dw.optString("data")
                when (dw.optString("type")) {
                    "qr" -> WhatsAppLoginStep.ShowQr(loginId, stepId, data)
                    "code" -> WhatsAppLoginStep.ShowCode(loginId, stepId, data)
                    else -> WhatsAppLoginStep.Other(loginId, dw.optString("type"))
                }
            }
            else -> WhatsAppLoginStep.Other(loginId, obj.optString("type"))
        }
    }

    suspend fun startQrLogin(): WhatsAppLoginStep = parseStep(call("login/start/qr", post = true))

    /** Espera a que WhatsApp confirme; devuelve el siguiente paso (QR renovado, completado, etc.). */
    suspend fun waitForStep(loginId: String, stepId: String): WhatsAppLoginStep =
        parseStep(call("login/step/$loginId/$stepId/display_and_wait", post = true, longPoll = true))

    suspend fun cancelLogin(loginId: String) {
        runCatching { call("login/cancel/$loginId", post = true) }
    }

    suspend fun logout(loginId: String) {
        call("logout/$loginId", post = true)
    }
}
