package ru.dmansurov.kstream

class StreamSubscription<T> internal constructor(
    private val onEvent: (T) -> Unit,
    private val onCancel: (StreamSubscription<T>) -> Unit
) {
    private var isActive = true

    fun pause() {
        isActive = false
    }

    fun resume() {
        isActive = true
    }

    internal fun notify(e: T) {
        if (isActive)
            onEvent(e)
    }

    fun cancel() {
        onCancel(this)
    }
}