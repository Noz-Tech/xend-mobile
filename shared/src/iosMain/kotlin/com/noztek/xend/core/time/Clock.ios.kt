package com.noztek.xend.core.time

import kotlinx.cinterop.ExperimentalForeignApi
import platform.posix.time

@OptIn(ExperimentalForeignApi::class)
actual fun currentEpochSeconds(): Long = time(null)
