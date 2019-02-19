package ru.dmansurov

class StreamSubscription<T> internal constructor(
    private val dispatcher: EventStreamDispatcher,
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
            dispatcher.dispatch { onEvent(e) }
    }

    fun cancel() {
        onCancel(this)
    }
}
