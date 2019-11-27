package ro.infrasoft.infralib.logger.infralogger.message.formatter.impl;

import org.joda.time.DateTime;
import ro.infrasoft.infralib.logger.infralogger.message.LogMessage;
import ro.infrasoft.infralib.logger.infralogger.message.formatter.LogMessageFormatter;

import java.text.SimpleDateFormat;

/**
 * Formatter pt SysOut.
 */
public class NoMessageFormatter implements LogMessageFormatter {
    final SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss.SSS");

    @Override
    public String formatMessage(LogMessage logMessage) {
        return logMessage.getMessage();
    }
}
