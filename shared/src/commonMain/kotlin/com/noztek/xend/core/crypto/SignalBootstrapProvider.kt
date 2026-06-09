package com.noztek.xend.core.crypto

interface SignalBootstrapProvider {
    fun create(): SignalBootstrap
}
