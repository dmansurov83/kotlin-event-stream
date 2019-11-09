package ru.dmansurov.kstream.suspend

open class KEventStream<T>() {
    internal val listeners = mutableListOf<KStreamSubscription<T>>()

    internal var onLastListenerRemoved: () -> Unit = {}

    internal suspend fun dispatch(event: T) {
        listeners.forEach {
            it.notify(event)
        }
    }

    fun listen(onEvent: suspend (T) -> Unit): KStreamSubscription<T> {
        val sub = KStreamSubscription(onEvent = onEvent, onCancel = {
            listeners.remove(it)
            if (listeners.isEmpty()) {
                onLastListenerRemoved()
            }
        })
        listeners.add(sub)
        return sub
    }

    fun where(predicate: (T) -> Boolean): KEventStream<T> {
        val stream = KEventStream<T>()
        val subscription = listen {
            if (predicate(it)) stream.dispatch(it)
        }
        stream.onLastListenerRemoved = {
            subscription.cancel()
        }
        return stream
    }

    fun <T2> map(mapper: (T) -> T2): KEventStream<T2> {
        val stream = KEventStream<T2>()
        val subscription = listen {
            stream.dispatch(mapper(it))
        }
        stream.onLastListenerRemoved = {
            subscription.cancel()
        }
        return stream
    }

    fun <T2> mapNotNull(mapper: (T) -> T2?): KEventStream<T2> {
        val stream = KEventStream<T2>()
        val subscription = listen {
            mapper(it)?.let { nn -> stream.dispatch(nn) }
        }
        stream.onLastListenerRemoved = {
            subscription.cancel()
        }
        return stream
    }
}



