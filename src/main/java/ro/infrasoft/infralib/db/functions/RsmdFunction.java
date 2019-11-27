package ro.infrasoft.infralib.db.functions;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;

/**
 * Functie care te ajuta sa rulezi ceva peste un obiect
 * de tip {@link java.sql.ResultSetMetaData} fara sa inchizi
 * prepared statement-ul.
 */
public interface RsmdFunction {
    public void apply(ResultSetMetaData rsmd) throws SQLException;
}
