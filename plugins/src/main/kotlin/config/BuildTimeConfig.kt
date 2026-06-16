/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package config

object BuildTimeConfig {
    const val APPLICATION_ID = "net.gspcoms.chat"
    const val APPLICATION_NAME = "GSPCOMS Chat"
    const val GOOGLE_APP_ID_RELEASE = "1:195085396171:android:f2c4983e3c512329d6534e"
    const val GOOGLE_APP_ID_DEBUG = "1:912726360885:android:def0a4e454042e9b00427c"
    const val GOOGLE_APP_ID_NIGHTLY = "1:912726360885:android:e17435e0beb0303000427c"

    // DNS-inverso del host de URL_WEBSITE: define el scheme del redirect OAuth (net.gspcoms:/).
    // MAS exige que el scheme = DNS-inverso del client_uri (gspcoms.net), si no rechaza el registro.
    val METADATA_HOST_REVERSED: String? = "net.gspcoms"
    // Mismo host (apex gspcoms.net) que las demás URLs y que el scheme net.gspcoms.
    val URL_WEBSITE: String? = "https://gspcoms.net"
    val URL_LOGO: String? = null
    val URL_COPYRIGHT: String? = "https://gspcoms.net"
    val URL_ACCEPTABLE_USE: String? = "https://gspcoms.net"
    val URL_PRIVACY: String? = "https://gspcoms.net"
    val URL_POLICY: String? = "https://gspcoms.net"
    val SERVICES_MAPTILER_BASE_URL: String? = null
    val SERVICES_MAPTILER_APIKEY: String? = null
    val SERVICES_MAPTILER_LIGHT_MAPID: String? = null
    val SERVICES_MAPTILER_DARK_MAPID: String? = null
    val SERVICES_POSTHOG_HOST: String? = null
    val SERVICES_POSTHOG_APIKEY: String? = null
    val SERVICES_SENTRY_DSN: String? = null
    val SERVICES_SENTRY_DSN_RUST: String? = null
    val BUG_REPORT_URL: String? = null
    val BUG_REPORT_APP_NAME: String? = null

    const val PUSH_CONFIG_INCLUDE_FIREBASE = true
    const val PUSH_CONFIG_INCLUDE_UNIFIED_PUSH = true
}
