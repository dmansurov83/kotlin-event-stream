package ru.dmansurov.kstream.suspend

class KStreamSubscription<T> internal constructor(
    private val onEvent: suspend (T) -> Unit,
    private val onCancel: (KStreamSubscription<T>) -> Unit
) {
    private var isActive = true

    fun pause() {
        isActive = false
    }

    fun resume() {
        isActive = true
    }

    internal suspend fun notify(e: T) {
        if (isActive)
            onEvent(e)
    }

    fun cancel() {
        onCancel(this)
    }
}
