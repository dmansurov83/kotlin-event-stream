package ru.dmansurov.kstream

open class EventStream<T>() {
    private val listeners = mutableListOf<StreamSubscription<T>>()

    internal var onLastListenerRemoved: () -> Unit = {}

    internal fun dispatch(event: T) {
        listeners.forEach {
            it.notify(event)
        }
    }

    fun listen(onEvent: (T) -> Unit): StreamSubscription<T> {
        val sub = StreamSubscription(onEvent = onEvent, onCancel = {
            listeners.remove(it)
            if (listeners.isEmpty()) {
                onLastListenerRemoved()
            }
        })
        listeners.add(sub)
        return sub
    }

    fun where(predicate: (T) -> Boolean): EventStream<T> {
        val stream = EventStream<T>()
        val subscription = listen {
            if (predicate(it)) stream.dispatch(it)
        }
        stream.onLastListenerRemoved = {
            subscription.cancel()
        }
        return stream
    }

    fun <T2> map(mapper: (T) -> T2): EventStream<T2> {
        val stream = EventStream<T2>()
        val subscription = listen {
            stream.dispatch(mapper(it))
        }
        stream.onLastListenerRemoved = {
            subscription.cancel()
        }
        return stream
    }
}


