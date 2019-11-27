package ro.infrasoft.infralib.logger.infralogger.message;

import ro.infrasoft.infralib.logger.infralogger.type.LogType;

import java.util.logging.Level;

/**
 * Defineste un mesaj de log.
 */
public class LogMessage {
    private String message;
    private String username;
    private String ip;
    private LogType logType;
    private Level logLevel;

    public LogMessage() {
    }

    public LogMessage(String message, String username, String ip, LogType logType, Level logLevel) {
        this.message = message;
        this.username = username;
        this.ip = ip;
        this.logType = logType;
        this.logLevel = logLevel;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public LogType getLogType() {
        return logType;
    }

    public void setLogType(LogType logType) {
        this.logType = logType;
    }

    public Level getLogLevel() {
        return logLevel;
    }

    public void setLogLevel(Level logLevel) {
        this.logLevel = logLevel;
    }
}
