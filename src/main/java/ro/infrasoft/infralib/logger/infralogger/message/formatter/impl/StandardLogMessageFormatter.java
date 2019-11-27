package ro.infrasoft.infralib.logger.infralogger.message.formatter.impl;

import ro.infrasoft.infralib.logger.infralogger.message.LogMessage;
import ro.infrasoft.infralib.logger.infralogger.message.formatter.LogMessageFormatter;

/**
 * Formatter standard.
 */
public class StandardLogMessageFormatter implements LogMessageFormatter {
    @Override
    public String formatMessage(LogMessage logMessage) {
        if (logMessage == null)
            return null;

        StringBuilder ret = new StringBuilder();

        ret.append("[TYPE=");
        if (logMessage.getLogType() != null)
            ret.append(logMessage.getLogType().getName());
        else
            ret.append("unknown");

        ret.append(",");

        ret.append("IP=");
        if (logMessage.getIp() != null && !logMessage.getIp().trim().isEmpty())
            ret.append(logMessage.getIp().trim());
        else
            ret.append("unknown");

        ret.append(",");

        ret.append("USER=");
        if (logMessage.getUsername() != null && !logMessage.getUsername().trim().isEmpty())
            ret.append(logMessage.getUsername().trim());
        else
            ret.append("unknown");

        ret.append("] ");

        if (logMessage.getMessage() != null && !logMessage.getMessage().trim().isEmpty())
            ret.append(logMessage.getMessage().trim());
        else
            ret.append("unknown log message");

        return ret.toString();
    }
}
