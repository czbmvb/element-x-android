import config.BuildTimeConfig
import extension.buildConfigFieldStr

/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2022-2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */
plugins {
    id("io.element.android-library")
}

android {
    namespace = "io.element.android.appconfig"

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        // GSPCOMS: sin URLs de Element en build FOSS.
        buildConfigFieldStr(
            name = "URL_POLICY",
            value = BuildTimeConfig.URL_POLICY ?: "https://gspcoms.net",
        )
        // BUG_REPORT_URL vacío => se desactiva el envío de reportes (no se postea a rageshakes.element.io).
        buildConfigFieldStr(
            name = "BUG_REPORT_URL",
            value = BuildTimeConfig.BUG_REPORT_URL ?: "",
        )
        buildConfigFieldStr(
            name = "BUG_REPORT_APP_NAME",
            value = BuildTimeConfig.BUG_REPORT_APP_NAME ?: "gspcoms-chat",
        )
    }
}

dependencies {
    implementation(libs.coroutines.core)
    implementation(libs.androidx.annotationjvm)
    implementation(libs.androidx.corektx)
    implementation(projects.libraries.matrix.api)
}
