package ro.infrasoft.infralib.logger.infralogger.message.formatter.impl;

import org.joda.time.DateTime;
import ro.infrasoft.infralib.logger.infralogger.message.LogMessage;

import java.text.SimpleDateFormat;

/**
 * Formatter pt SysOut.
 */
public class SysOutLogMessageFormatter extends StandardLogMessageFormatter {
    final SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss.SSS");

    @Override
    public String formatMessage(LogMessage logMessage) {
        String ret = super.formatMessage(logMessage);
        if (ret == null)
            return null;

        StringBuilder retBuilder = new StringBuilder();
        DateTime now = new DateTime();
        retBuilder.append("[").append(sdf.format(now.toDate()));

        retBuilder.append(" ");

        if (logMessage.getLogLevel() != null)
            retBuilder.append(logMessage.getLogLevel().getName());
        else
            retBuilder.append("UNKNOWN");

        retBuilder.append("]").append(ret);

        return retBuilder.toString();
    }
}
