package com.hoc081098.solivagant.navigation.internal

internal actual class WeakReference<T : Any> actual constructor(referred: T) {
  private var ref: T? = referred

  actual fun get(): T? = ref

  actual fun clear() {
    ref = null
  }
}
