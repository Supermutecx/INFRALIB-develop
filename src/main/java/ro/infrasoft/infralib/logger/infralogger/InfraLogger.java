package ro.infrasoft.infralib.logger.infralogger;

import ro.infrasoft.infralib.exceptions.ResultException;
import ro.infrasoft.infralib.exceptions.ScSecurityException;
import ro.infrasoft.infralib.exceptions.UtilException;
import ro.infrasoft.infralib.logger.infralogger.message.LogMessage;
import ro.infrasoft.infralib.logger.infralogger.printer.LogPrinter;
import ro.infrasoft.infralib.logger.infralogger.type.LogType;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

/**
 * Clasa care se ocupa cu logging pe sistemul nou.
 */
public class InfraLogger {
    private Map<String, LogPrinter> logPrinters;
    private Map<String, Map<LogType, Boolean>> logTypesEnabled;
    private static List<Class<? extends Exception>> infoExceptions = Arrays.asList(ScSecurityException.class, UtilException.class, ResultException.class);

    /**
     * Verifica daca aceasta exceptie nu e ceva care trebuie logat ca eroare, fiind logata ca info.
     *
     * @param th throwable
     * @return true/false
     */
    public static boolean shouldLogInfo(Throwable th){
        for (Class clazz: infoExceptions){
            if (th.getClass().isAssignableFrom(clazz)){
                return true;
            }
        }
        return false;
    }

    public InfraLogger(Map<String, LogPrinter> logPrinters, Map<String, Map<LogType, Boolean>> logTypesEnabled) {
        this.logPrinters = logPrinters;
        this.logTypesEnabled = logTypesEnabled;
    }

    /**
     * Determina daca ar trebui sa logheze in functie de logger name si tip.
     *
     * @param loggerName nume logger
     * @param logMessage mesaj de log
     * @return tip
     */
    private Boolean shouldLog(String loggerName, LogMessage logMessage) {

        // false pt null-uri, daca nu e in log printers sau in log types enabled
        if (loggerName == null
                || logMessage == null
                || loggerName.trim().isEmpty()
                || logMessage.getMessage() == null
                || logMessage.getMessage().trim().isEmpty()
                || !logPrinters.containsKey(loggerName)
                || !logTypesEnabled.containsKey(loggerName))
            return false;

        return logMessage.getLogType() == null
                || (logTypesEnabled.get(loggerName).containsKey(logMessage.getLogType())
                    && logTypesEnabled.get(loggerName).get(logMessage.getLogType()) != null
                    && logTypesEnabled.get(loggerName).get(logMessage.getLogType()));
    }

    /**
     * Logheaza un mesaj cu obiectul standard.
     *
     * @param logMessage mesaj
     */
    public void log(LogMessage logMessage) {
        for (Map.Entry<String, LogPrinter> printerEntry : logPrinters.entrySet()) {
            if (shouldLog(printerEntry.getKey(), logMessage))
                printerEntry.getValue().print(logMessage);
        }
    }

    /**
     * Logheaza cu toti parametrii separati.
     *
     * @param message  mesaj log
     * @param username username
     * @param ip       ip
     * @param logType  tip log
     * @param logLevel nivel log
     */
    public void log(String message, String username, String ip, LogType logType, Level logLevel) {
        log(new LogMessage(message, username, ip, logType, logLevel));
    }

    /**
     * Logheaza un mesaj trace doar cu mesaj, username, ip.
     * Nivelele default vor fi TRACE cu log level FINEST.
     *
     * @param message  mesaj
     * @param username username
     * @param ip       ip
     */
    public void logTrace(String message, String username, String ip) {
        log(message, username, ip, LogType.TRACE, Level.FINEST);
    }

    /**
     * Logheaza un mesaj trace doar cu mesaj, username.
     * Nivelele default vor fi TRACE cu log level FINEST.
     *
     * @param message  mesaj
     * @param username username
     */
    public void logTrace(String message, String username) {
        logTrace(message, username, null);
    }

    /**
     * Logheaza un mesaj trace.
     * Nivelele default vor fi TRACE cu log level FINEST.
     *
     * @param message mesaj
     */
    public void logTrace(String message) {
        logTrace(message, null);
    }

    /**
     * Logheaza un mesaj sql doar cu mesaj, username, ip.
     * Nivelele default vor fi SQL cu log level INFO.
     *
     * @param message  mesaj
     * @param username username
     * @param ip       ip
     */
    public void logSql(String message, String username, String ip) {
        log(message, username, ip, LogType.SQL, Level.INFO);
    }

    /**
     * Logheaza un mesaj sql doar cu mesaj, username.
     * Nivelele default vor fi SQL cu log level INFO.
     *
     * @param message  mesaj
     * @param username username
     */
    public void logSql(String message, String username) {
        logSql(message, username, null);
    }

    /**
     * Logheaza un mesaj sql.
     * Nivelele default vor fi SQL cu log level INFO.
     *
     * @param message mesaj
     */
    public void logSql(String message) {
        logSql(message, null);
    }

    /**
     * Logheaza un mesaj eroare doar cu mesaj, username, ip.
     * Nivelele default vor fi ERROR cu log level SEVERE.
     *
     * @param message  mesaj
     * @param username username
     * @param ip       ip
     */
    public void logError(String message, String username, String ip) {
        log(message, username, ip, LogType.ERROR, Level.SEVERE);
    }

    /**
     * Logheaza un mesaj eroare doar cu mesaj, username.
     * Nivelele default vor fi ERROR cu log level SEVERE.
     *
     * @param message  mesaj
     * @param username username
     */
    public void logError(String message, String username) {
        logError(message, username, null);
    }

    /**
     * Logheaza un mesaj eroare.
     * Nivelele default vor fi ERROR cu log level SEVERE.
     *
     * @param message mesaj
     */
    public void logError(String message) {
        logError(message, null);
    }

    /**
     * Logheaza o exceptie (throwable) doar cu throwable, username, ip.
     * Nivelele default vor fi ERROR cu log level SEVERE.
     *
     * @param th       throwable
     * @param username username
     * @param ip       ip
     */
    public void logError(Throwable th, String username, String ip) {
        StringBuilder stringBuilder = new StringBuilder();
        if (th.getMessage() != null)
            stringBuilder.append(th.getMessage()).append("\n");

        for (StackTraceElement ste : th.getStackTrace()) {
            stringBuilder.append(ste.toString()).append("\n");
        }

        if (shouldLogInfo(th))
            logTrace(stringBuilder.toString(), username, ip);
        else
            logError(stringBuilder.toString(), username, ip);
    }

    /**
     * Logheaza o exceptie (throwable) doar cu throwable, username.
     * Nivelele default vor fi ERROR cu log level SEVERE.
     *
     * @param th       throwable
     * @param username username
     */
    public void logError(Throwable th, String username) {
        logError(th, username, null);
    }

    /**
     * Logheaza o exceptie (throwable) doar cu throwable.
     * Nivelele default vor fi ERROR cu log level SEVERE.
     *
     * @param th throwable
     */
    public void logError(Throwable th) {
        logError(th, null, null);
    }

    public Map<String, LogPrinter> getLogPrinters() {
        return logPrinters;
    }

    public void setLogPrinters(Map<String, LogPrinter> logPrinters) {
        this.logPrinters = logPrinters;
    }
}
