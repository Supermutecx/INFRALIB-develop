package ro.infrasoft.infralib.logger.infralogger.type;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * Defineste un tip de logging (SQL (logare query sql), TRACE (logare acces pe meniuri, click butoane), ERROR (logare erori)).
 */
public enum LogType {
    TRACE("TRACE"), SQL("SQL"), ERROR("ERROR");

    String name;

    Map<String, LogType> lookupByName;

    public LogType findByName(String name) {
        if (lookupByName == null) {
            lookupByName = new HashMap<String, LogType>();

            for (LogType logType : EnumSet.allOf(LogType.class)) {
                lookupByName.put(logType.getName(), logType);
            }
        }

        if (name != null && lookupByName != null && lookupByName.containsKey(name))
            return lookupByName.get(name);
        else
            return null;
    }

    LogType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
