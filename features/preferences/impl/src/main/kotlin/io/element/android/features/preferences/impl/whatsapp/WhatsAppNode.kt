/*
 * GSPCOMS: nodo de navegación de la pantalla "WhatsApp".
 */

package io.element.android.features.preferences.impl.whatsapp

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import io.element.android.annotations.ContributesNode
import io.element.android.libraries.androidutils.system.openUrlInExternalApp
import io.element.android.libraries.di.SessionScope

@ContributesNode(SessionScope::class)
@AssistedInject
class WhatsAppNode(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    private val presenter: WhatsAppPresenter,
) : Node(buildContext, plugins = plugins) {
    @Composable
    override fun View(modifier: Modifier) {
        val context = LocalContext.current
        val state = presenter.present()
        WhatsAppView(
            state = state,
            onBackClick = ::navigateUp,
            onOpenWhatsApp = { url -> context.openUrlInExternalApp(url) },
            modifier = modifier,
        )
    }
}
