package ro.infrasoft.infralib.logger.infralogger.printer;

import ro.infrasoft.infralib.logger.infralogger.message.LogMessage;
import ro.infrasoft.infralib.logger.infralogger.message.formatter.LogMessageFormatter;

/**
 * Defineste un log printer, care se ocupa cu scrierea efectiva de log intr-o anumita zona (in bd, pe disk, in output stream etc).
 */
public abstract class LogPrinter {
    private LogMessageFormatter logMessageFormatter;

    /**
     * Printeaza efectiv un mesaj.
     *
     * @param logMessage mesaj de logat
     */
    public abstract void print(LogMessage logMessage);

    public LogMessageFormatter getLogMessageFormatter() {
        return logMessageFormatter;
    }

    public void setLogMessageFormatter(LogMessageFormatter logMessageFormatter) {
        this.logMessageFormatter = logMessageFormatter;
    }
}
