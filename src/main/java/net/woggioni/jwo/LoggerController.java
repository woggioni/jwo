package net.woggioni.jwo;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.LoggingEvent;
import org.slf4j.event.SubstituteLoggingEvent;
import org.slf4j.helpers.SubstituteLogger;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class LoggerController {

    private static final Object lock = new Object();

    private static boolean initialized = false;
    private static final Queue<SubstituteLoggingEvent> eventQueue = new LinkedBlockingQueue<>();
    private static final List<SubstituteLogger> substituteLoggers = new ArrayList<>();

    public static Logger lazyLogger(final String className) {
        synchronized (lock) {
            Logger result;
            if (initialized) {
                result = LoggerFactory.getLogger(className);
            } else {
                final SubstituteLogger substituteLogger = new SubstituteLogger(className, eventQueue, false);
                substituteLoggers.add(substituteLogger);
                result = substituteLogger;
            }
            return result;
        }
    }

    public static Logger lazyLogger(final Class<?> cls) {
        return lazyLogger(cls.getName());
    }

    public static void initializeLoggers() {
        synchronized (lock) {
            SubstituteLogger firstLogger = null;
            for (final SubstituteLogger log : substituteLoggers) {
                if (firstLogger == null) firstLogger = log;
                final Logger realLogger = LoggerFactory.getLogger(log.getName());
                log.setDelegate(realLogger);
            }
            if (firstLogger != null) {
                for (final LoggingEvent evt : eventQueue) {
                    firstLogger.log(evt);
                }
            }
            initialized = true;
        }
    }
}

