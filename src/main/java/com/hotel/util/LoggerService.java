package com.hotel.util;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public final class LoggerService {

    private static final String LOG_DIRECTORY = "logs";
    private static final int MAX_LOG_SIZE_BYTES = 1024 * 1024; // 1 MB
    private static final int MAX_LOG_FILES = 10;

    private static LoggerService instance;

    private final Logger logger;

    private LoggerService() {
        logger = Logger.getLogger("com.hotel");
        try {
            java.nio.file.Files.createDirectories(java.nio.file.Paths.get(LOG_DIRECTORY));
            FileHandler fileHandler = new FileHandler(
                    LOG_DIRECTORY + "/system_logs.%g.log", MAX_LOG_SIZE_BYTES, MAX_LOG_FILES, true);
            fileHandler.setFormatter(new SimpleFormatter());
            logger.addHandler(fileHandler);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to initialize rotating log handler", e);
        }
    }

    public static synchronized LoggerService getInstance() {
        if (instance == null) {
            instance = new LoggerService();
        }
        return instance;
    }

    public void info(String message) {
        logger.log(Level.INFO, message);
    }

    public void warning(String message) {
        logger.log(Level.WARNING, message);
    }

    public void severe(String message, Throwable throwable) {
        logger.log(Level.SEVERE, message, throwable);
    }
}
