package org.stianloader.sml6.starplane.sourcegen;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.java.decompiler.main.extern.IFernflowerLogger;
import org.slf4j.Logger;

public class FernflowerLoggerAdapter extends IFernflowerLogger {
    private final Logger logger;

    public FernflowerLoggerAdapter(Logger logger, @NotNull Severity severity) {
        this.setSeverity(severity);
        this.logger = logger;
    }

    @Override
    public void writeMessage(String message, Severity severity) {
        if (!this.accepts(severity)) {
            return;
        }
        message = severity + ": " + message;
        switch (severity) {
        case ERROR:
            this.logger.error(message);
            break;
        case INFO:
            this.logger.info(message);
            break;
        case TRACE:
            this.logger.trace(message);
            break;
        case WARN:
            this.logger.warn(message);
            break;
        default:
            this.logger.error(message);
            break;
        }
    }

    @Override
    public void writeMessage(String message, Severity severity, Throwable t) {
        if (!this.accepts(severity)) {
            return;
        }
        message = severity + ": " + message;
        switch (severity) {
        case ERROR:
            this.logger.error(message, t);
            break;
        case INFO:
            this.logger.info(message, t);
            break;
        case TRACE:
            this.logger.trace(message, t);
            break;
        case WARN:
            this.logger.warn(message, t);
            break;
        default:
            this.logger.error(message, t);
            break;
        }
    }
}
