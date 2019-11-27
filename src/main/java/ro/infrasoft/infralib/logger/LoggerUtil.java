package ro.infrasoft.infralib.logger;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

public class LoggerUtil {

    public static Logger getLogger(String name) {
        System.setProperty("documenta.home", System.getenv("DOCUMENTA_HOME") != null ? System.getenv("DOCUMENTA_HOME") : "C:/");
        final Logger logger = Logger.getLogger(name);
        return logger;
    }

    public static Logger getLocalFolderLogger(String name, Class clazz) {
        Logger logger = null;
        try {
            String path = System.getProperty("user.dir");
            System.setProperty("documenta.home", path);
            logger = Logger.getLogger(name);
        } catch (Exception ee) {
            logger = getLogger(name);
        }
        return logger;
    }

    public static String printException(Throwable e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        String ret = sw.toString();
        pw.close();
        try {
            sw.close();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        return ret;
    }

    public static String getMessage(Throwable t) {
        if (t.getMessage() == null)
            return "unknown";
        else
            return t.getMessage();
    }

    public static String getMessageExtended(Throwable t) {
        String message = t.getMessage();
        if (message == null) {
            if (t.getCause() == null) {
                return "Unknown error.";
            } else {
                message = t.getCause().getMessage();
                if (message == null) {
                    return "Unknown error.";
                } else {
                    return message;
                }
            }
        } else {
            return message;
        }
    }
}
