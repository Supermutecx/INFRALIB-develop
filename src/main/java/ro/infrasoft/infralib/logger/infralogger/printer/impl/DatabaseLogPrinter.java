package ro.infrasoft.infralib.logger.infralogger.printer.impl;

import ro.infrasoft.infralib.db.datasource.BaseDataSource;
import ro.infrasoft.infralib.logger.infralogger.message.LogMessage;
import ro.infrasoft.infralib.logger.infralogger.message.formatter.impl.NoMessageFormatter;
import ro.infrasoft.infralib.logger.infralogger.message.formatter.impl.StandardLogMessageFormatter;
import ro.infrasoft.infralib.logger.infralogger.message.formatter.impl.SysOutLogMessageFormatter;
import ro.infrasoft.infralib.logger.infralogger.printer.LogPrinter;

import java.sql.SQLException;

/**
 * Log printer care scrie cate database, tabela log.
 */
public class DatabaseLogPrinter extends LogPrinter {
    private BaseDataSource bd;

    public DatabaseLogPrinter() {
        // default formatter = no formatter
        setLogMessageFormatter(new NoMessageFormatter());
    }

    @Override
    public void print(LogMessage logMessage) {
        String message = getLogMessageFormatter().formatMessage(logMessage);
        if (message != null) {
            try {
                bd.sql("INSERT INTO LOG (id, modul, log_code, log_val, created_by, ip) values (log_id.nextval, ?, ?, ?, ?, ?)", logMessage.getLogType().getName(), logMessage.getLogType().getName(), message, logMessage.getUsername(), logMessage.getIp()).put();
            } catch (Exception e) {
                // nu a reusit sa logheze
            }
        }
    }

    public BaseDataSource getBd() {
        return bd;
    }

    public void setBd(BaseDataSource bd) {
        this.bd = bd;
    }
}
