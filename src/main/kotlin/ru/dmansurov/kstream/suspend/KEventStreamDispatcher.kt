package ru.dmansurov.kstream.suspend

open class KEventStreamDispatcher<T>(protected val stream: KEventStream<T>) {
    open suspend fun dispatch(event: T){
        stream.listeners.forEach {
            it.notify(event)
        }
    }
}
