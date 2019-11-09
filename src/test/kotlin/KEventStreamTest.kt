import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Test
import ru.dmansurov.kstream.suspend.KEventStream
import ru.dmansurov.kstream.suspend.KEventStreamDispatcher

class KLogger {
    val stream = KEventStream<LogEvent>()

    private val dispatcher =
        KEventStreamDispatcher(stream)

    init {
        stream.listen {
            println(it)
        }
    }

    suspend fun info(message: String) {
        GlobalScope.launch {
            dispatcher.dispatch(LogEvent("info", message))
        }
    }
}

class KEventStreamTest {

    @Test
    fun EventStreamTest() {
        runBlocking {
            val stream = KEventStream<String>()
            val dispatcher = KEventStreamDispatcher(stream)
            val received = mutableListOf<String>()
            val subscription = stream
                .where { it == "event2" }
                .map { it + it }
                .listen { received.add(it) }
            dispatcher.dispatch("event1")
            dispatcher.dispatch("event2")
            assert(received.size == 1)
            assert(received.first() == "event2event2")
            var subCleaned = false
            stream.onLastListenerRemoved = {
                subCleaned = true
            }
            subscription.cancel()
            assert(subCleaned)
            dispatcher.dispatch("event2")
            assert(received.size == 1)
        }
    }

    @Test
    fun loggerTest() {
        runBlocking {
            val logger = KLogger()
            val logMessages = mutableListOf<String>()
            val stream = logger.stream
                .where { it.level == "info" }
                .map { it.message }
            stream.listen {
                logMessages.add(it)
            }
            logger.info("test")
            assert(logMessages.size == 0)
            Thread.sleep(10)
            assert(logMessages.size == 1)
        }
    }

    @Test
    fun pauseStream() {
        runBlocking {
            val stream = KEventStream<Int>()
            val dispatcher = KEventStreamDispatcher(stream)
            val events = mutableListOf<Int>()
            val listener = stream.listen { events.add(it) }
            dispatcher.dispatch(1)
            assert(events.size == 1)
            listener.pause()
            dispatcher.dispatch(2)
            assert(events.size == 1)
            listener.resume()
            dispatcher.dispatch(3)
            assert(events.size == 2)
        }
    }

    @Test
    fun whereIsInstance(){
        runBlocking {
            val stream = KEventStream<Any>()
            val dispatcher = KEventStreamDispatcher(stream)
            val events = mutableListOf<Int>()
            val listener = stream.mapNotNull { it as? Int }
                .listen { events.add(it) }
            dispatcher.dispatch("string")
            dispatcher.dispatch(1)
            assert(events.size == 1)
            listener.cancel()
        }
    }
}
