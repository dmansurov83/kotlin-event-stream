package ru.dmansurov

import java.util.concurrent.Executor

open class EventStreamDispatcher<T>(protected val stream: EventStream<T>) {
    open fun dispatch(event: T){
        stream.dispatch(event)
    }
}

class AsyncEventStreamDispatcher<T>(private val executor: Executor, stream: EventStream<T>) : EventStreamDispatcher<T>(stream) {
    override fun dispatch(event: T){
        executor.execute { stream.dispatch(event) }
    }
}