package co.com.pragma.model.utils.gateways;

public interface Logger {
    void info(String message);
    void debug(String message);
    void error(String message, Throwable throwable);
}
