package ro.infrasoft.infralib.db.functions;

import java.sql.SQLException;
import java.util.HashMap;

/**
 * Functie care te ajuta sa rulezi ceva peste un obiect map rezultat din rulatul
 * unui {@link java.sql.ResultSetMetaData}. Te ajuta sa lucrezi cu acel map fara
 * sa inchizi prepared statement-ul.
 */
public interface RsmdMapFunction {
	public void apply(HashMap<String, Integer> rsmdMap) throws SQLException;
}
