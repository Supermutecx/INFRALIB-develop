package ro.infrasoft.infralib.logger.infralogger.message.formatter;

import ro.infrasoft.infralib.logger.infralogger.message.LogMessage;

/**
 * Formatter pt log message, determina cum va arata un item de log.
 */
public interface LogMessageFormatter {

    /**
     * Formateaza mesajul logMessage.
     *
     * @param logMessage mesaj
     * @return string formatat
     */
    public String formatMessage(LogMessage logMessage);
}
