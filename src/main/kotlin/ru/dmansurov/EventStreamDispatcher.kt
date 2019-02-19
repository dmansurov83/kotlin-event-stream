package ru.dmansurov

import java.util.concurrent.Executor

interface EventStreamDispatcher {
    fun dispatch(call: () -> Unit)
}

class SyncEventStreamDispatcher : EventStreamDispatcher {
    override fun dispatch(call: () -> Unit) {
        call()
    }
}

class AsyncEventStreamDispatcher(private val executor: Executor) : EventStreamDispatcher {
    override fun dispatch(call: () -> Unit) {
        executor.execute {
            call()
        }
    }
}
