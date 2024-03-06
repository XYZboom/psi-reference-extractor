/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.api.standalone

import org.jetbrains.kotlin.analysis.api.KtAnalysisApiInternals
import org.jetbrains.kotlin.analysis.api.lifetime.KtAlwaysAccessibleLifetimeTokenFactory
import org.jetbrains.kotlin.analysis.api.lifetime.KtLifetimeTokenFactory
import org.jetbrains.kotlin.analysis.api.lifetime.KtLifetimeTokenProvider

@OptIn(KtAnalysisApiInternals::class)
public class KtAlwaysAccessibleLifetimeTokenProvider : KtLifetimeTokenProvider() {
    override fun getLifetimeTokenFactory(): KtLifetimeTokenFactory {
        return KtAlwaysAccessibleLifetimeTokenFactory
    }
}