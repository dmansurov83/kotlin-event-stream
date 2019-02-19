package ru.dmansurov

open class EventStream<T>(
    private val dispatcher: EventStreamDispatcher = SyncEventStreamDispatcher(),
    private val events: MutableList<T> = mutableListOf()
) {
    private val listeners = mutableListOf<StreamSubscription<T>>()

    internal var onLastListenerRemoved: () -> Unit = {}

    fun getAll(): List<T> {
        return events.toList()
    }

    fun last(): T? = events.lastOrNull()

    fun dispatch(event: T) {
        events.add(event)
        listeners.forEach {
            it.notify(event)
        }
    }

    fun listen(onEvent: (T) -> Unit): StreamSubscription<T> {
        val sub = StreamSubscription(dispatcher, onEvent = onEvent, onCancel = {
            listeners.remove(it)
            if (listeners.isEmpty()) {
                onLastListenerRemoved()
            }
        })
        listeners.add(sub)
        return sub
    }

    fun where(predicate: (T) -> Boolean): EventStream<T> {
        val stream = EventStream(dispatcher, events.filter { predicate(it) }.toMutableList())
        val subscription = listen {
            if (predicate(it)) stream.dispatch(it)
        }
        stream.onLastListenerRemoved = {
            subscription.cancel()
        }
        return stream
    }

    fun <T2> map(mapper: (T) -> T2): EventStream<T2> {
        val stream = EventStream(dispatcher, events.map { mapper(it) }.toMutableList())
        val subscription = listen {
            stream.dispatch(mapper(it))
        }
        stream.onLastListenerRemoved = {
            subscription.cancel()
        }
        return stream
    }
}


