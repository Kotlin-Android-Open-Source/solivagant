package com.hoc081098.solivagant.sample

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

@Suppress("FunctionName")
fun ImmediateMainScope() = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
