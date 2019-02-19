import org.junit.Test
import ru.dmansurov.AsyncEventStreamDispatcher
import ru.dmansurov.EventStream
import java.util.concurrent.Executors

data class LogEvent(val level: String, val message: String)

class Logger {
    val stream = EventStream<LogEvent>(AsyncEventStreamDispatcher(Executors.newSingleThreadExecutor()))

    init {
        stream.listen {
            println(it)
        }
    }

    fun info(message: String) {
        stream.dispatch(LogEvent("info", message))
    }
}

class EventStreamTest {

    @Test
    fun EventStreamTest() {
        val stream = EventStream<String>()
        val received = mutableListOf<String>()
        val subscription = stream
            .where { it == "event2" }
            .map { it + it }
            .listen { received.add(it) }
        stream.dispatch("event1")
        stream.dispatch("event2")
        assert(received.size == 1)
        assert(received.first() == "event2event2")
        var subCleaned = false
        stream.onLastListenerRemoved = {
            subCleaned = true
        }
        subscription.cancel()
        assert(subCleaned)
        stream.dispatch("event2")
        assert(received.size == 1)
    }

    @Test
    fun loggerTest() {
        val logger = Logger()
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
        assert(stream.last() == "test")
        assert(logMessages.size == 1)
    }

    @Test
    fun pauseStream(){
        val stream = EventStream<Int>()
        val events = mutableListOf<Int>()
        val listener = stream.listen { events.add(it) }
        stream.dispatch(1)
        assert(events.size == 1)
        listener.pause()
        stream.dispatch(2)
        assert(events.size == 1)
        listener.resume()
        stream.dispatch(3)
        assert(events.size == 2)
    }
}
