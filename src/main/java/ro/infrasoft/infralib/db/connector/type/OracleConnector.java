package ro.infrasoft.infralib.db.connector.type;

import ro.infrasoft.infralib.db.connector.AbstractConnector;
import ro.infrasoft.infralib.db.type.DbType;

/**
 * Clasa abstracta care specifica un conector Oracle la baza
 * de date, lasand detaliile de conectare pentru imnplementarile concrete.
 */
public abstract class OracleConnector implements AbstractConnector {

    @Override
    public String driver() {
        return "oracle.jdbc.OracleDriver";
    }

    @Override
    public DbType dbType() {
        return DbType.ORACLE;
    }
}
