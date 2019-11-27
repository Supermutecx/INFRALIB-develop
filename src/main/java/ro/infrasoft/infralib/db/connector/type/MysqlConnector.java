package ro.infrasoft.infralib.db.connector.type;

import ro.infrasoft.infralib.db.connector.AbstractConnector;
import ro.infrasoft.infralib.db.type.DbType;

/**
 * Clasa abstracta care specifica un conector Mysql la baza
 * de date, lasand detaliile de conectare pentru imnplementarile concrete.
 */
public abstract class MysqlConnector implements AbstractConnector {

    @Override
    public String driver() {
        return "com.mysql.jdbc.Driver";
    }

    @Override
    public DbType dbType() {
        return DbType.MYSQL;
    }
}
