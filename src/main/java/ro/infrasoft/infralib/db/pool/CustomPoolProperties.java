package ro.infrasoft.infralib.db.pool;

import ro.infrasoft.infralib.db.type.DbType;

/**
 * Holds custom pool properties.
 */
public class CustomPoolProperties {
    private String url;
    private String className;
    private String username;
    private String password;
    private Integer maxActive;
    private Boolean log;
    private Boolean markAndSweep;
    private DbType dbType;
    private String schemaName;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Integer getMaxActive() {
        return maxActive;
    }

    public void setMaxActive(Integer maxActive) {
        this.maxActive = maxActive;
    }

    public Boolean getLog() {
        return log;
    }

    public void setLog(Boolean log) {
        this.log = log;
    }

    public Boolean getMarkAndSweep() {
        return markAndSweep;
    }

    public void setMarkAndSweep(Boolean markAndSweep) {
        this.markAndSweep = markAndSweep;
    }

    public DbType getDbType() {
        return dbType;
    }

    public void setDbType(DbType dbType) {
        this.dbType = dbType;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }
}
