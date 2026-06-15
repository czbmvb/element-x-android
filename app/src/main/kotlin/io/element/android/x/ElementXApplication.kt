/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2022-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.x

import android.app.Application
import android.app.LocaleManager
import android.os.Build
import android.os.LocaleList
import androidx.compose.material3.ComposeMaterial3Flags.isAnchoredDraggableComponentsStrictOffsetCheckEnabled
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.startup.AppInitializer
import androidx.work.Configuration
import dev.zacsweers.metro.createGraphFactory
import io.element.android.libraries.di.DependencyInjectionGraphOwner
import io.element.android.libraries.workmanager.api.di.MetroWorkerFactory
import io.element.android.x.di.AppGraph
import io.element.android.x.info.logApplicationInfo
import io.element.android.x.initializer.CacheCleanerInitializer
import io.element.android.x.initializer.CrashInitializer
import io.element.android.x.initializer.PlatformInitializer

class ElementXApplication : Application(), DependencyInjectionGraphOwner, Configuration.Provider {
    override val graph: AppGraph = createGraphFactory<AppGraph.Factory>().create(this)

    override val workManagerConfiguration: Configuration = Configuration.Builder()
        .setWorkerFactory(MetroWorkerFactory(graph.workerProviders))
        .build()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate() {
        super.onCreate()

        // GSPCOMS: español por defecto. Solo en el primer arranque (si el usuario no ha
        // elegido idioma), para no pisar una selección manual posterior. Android 13+.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val localeManager = getSystemService(LocaleManager::class.java)
            if (localeManager != null && localeManager.applicationLocales.isEmpty) {
                localeManager.applicationLocales = LocaleList.forLanguageTags("es")
            }
        }

        AppInitializer.getInstance(this).apply {
            initializeComponent(CrashInitializer::class.java)
            initializeComponent(PlatformInitializer::class.java)
            initializeComponent(CacheCleanerInitializer::class.java)
        }

        logApplicationInfo(this)

        // Disable the strict offset check for anchored draggable components, as it can cause issues with bottom sheets.
        // Remove once https://issuetracker.google.com/issues/477038695 is fixed.
        isAnchoredDraggableComponentsStrictOffsetCheckEnabled = false
    }
}
