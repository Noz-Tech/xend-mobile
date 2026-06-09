package com.noztek.xend.core.presentation

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

fun defaultViewModelScope(): CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
