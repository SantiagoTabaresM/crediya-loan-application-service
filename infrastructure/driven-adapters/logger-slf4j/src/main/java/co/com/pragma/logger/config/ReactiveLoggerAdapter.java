package co.com.pragma.logger.config;

import co.com.pragma.model.utils.gateways.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;


@Component
public class ReactiveLoggerAdapter implements Logger {
    // usamos el logger de SLF4J explícitamente
    private final org.slf4j.Logger logger = LoggerFactory.getLogger(ReactiveLoggerAdapter.class);

    private static final String PREFIX = "[Reactive] {}";

    @Override
    public void debug(String message) {
        logger.debug(PREFIX, message);
    }

    @Override
    public void info(String message) {
        logger.info(PREFIX, message);
    }

    @Override
    public void error(String message, Throwable throwable) {
        logger.error(PREFIX, message, throwable);
    }
}
