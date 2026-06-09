package com.noztek.xend.core.time

actual fun currentEpochSeconds(): Long = System.currentTimeMillis() / 1000
