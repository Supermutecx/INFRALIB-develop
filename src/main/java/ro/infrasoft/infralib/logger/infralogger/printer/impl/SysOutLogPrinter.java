package ro.infrasoft.infralib.logger.infralogger.printer.impl;

import ro.infrasoft.infralib.logger.infralogger.message.LogMessage;
import ro.infrasoft.infralib.logger.infralogger.message.formatter.impl.SysOutLogMessageFormatter;
import ro.infrasoft.infralib.logger.infralogger.printer.LogPrinter;

import java.util.concurrent.locks.ReentrantLock;

/**
 * Log printer care scrie cate system out.
 */
public class SysOutLogPrinter extends LogPrinter {
    public static ReentrantLock lock;

    static {
        lock = new ReentrantLock();
    }

    public SysOutLogPrinter() {
        // default formatter = sys out formatter
        setLogMessageFormatter(new SysOutLogMessageFormatter());
    }

    @Override
    public void print(LogMessage logMessage) {
        String message = getLogMessageFormatter().formatMessage(logMessage);
        if (message != null) {
            SysOutLogPrinter.lock.lock();
            try {
                System.out.println(message);
            } finally {
                SysOutLogPrinter.lock.unlock();
            }
        }
    }
}
