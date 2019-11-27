package ro.infrasoft.infralib.db.result;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;

import ro.infrasoft.infralib.db.functions.RsmdFunction;
import ro.infrasoft.infralib.db.functions.RsmdMapFunction;
import ro.infrasoft.infralib.exceptions.ResultException;

/**
 * Clasa wrapper a clasei {@link PreparedStatement} care permite mai multe
 * operatiuni utile peste un sql executat.
 */
public class Result {
	private PreparedStatement pstmt;
	private ResultSetMetaData rsmd;
	private HashMap<String, Integer> info;
	private HashMap<String, Integer> rsmdColMap;
	private String printSql;

	/**
	 * Constructor care primeste un {@link PreparedStatement}. Nu exista
	 * constructor default deoarece Result nu are sens fara acest obiect.
	 *
	 * @param pstmt
	 *            Obiectul PreparedStatement obtinut dintr-un query
	 */
	public Result(final PreparedStatement pstmt) {
		this.pstmt = pstmt;
	}

	/**
	 * Constructor care primeste un {@link PreparedStatement} si sql-ul
	 * de printat in caz de eroare.
	 *
	 * @param pstmt
	 *            Obiectul PreparedStatement obtinut dintr-un query
	 */
	public Result(final PreparedStatement pstmt, String printSql) {
		this(pstmt);
		this.printSql = printSql;
	}

	/**
	 * Executa query-ul in regim de update (insert, update, delete) si
	 * returneaza cate randuri s-au modificat.
	 * <p>
	 * Statement-ul trebuie inchis aici pentru ca e o metoda de finalizare.
	 * </p>
	 *
	 * @return Numarul de randuri afectate
	 * @throws SQLException
	 *             In interiorul lui <b>put</b> poate fi o exceptie sql
	 */
	public int put() throws SQLException {
		int affected = 0;
		try {
			affected = pstmt.executeUpdate();
		} finally {
			closePstmt();
		}

		return affected;
	}

	/**
	 * Aceasta metoda cheama metoda <b>exec</b> a unui obiect anonim {@link Row}
	 * si mai important, incrementeaza (next) si inchide rs-ul la sfarsit. Vezi
	 * documentatia pentru exemple de utilizare.
	 *
	 * @param row
	 *            Un obiect de tip Row care are implelemntata metoda
	 *            {@link Row#exec(java.sql.ResultSet)}
	 * @throws Exception
	 *             In interiorul lui <b>each</b> pot fi mai mute exceptii
	 */
	public void each(final Row row) throws Exception {
		ResultSet rs = null;

		try {
			rs = pstmt.executeQuery();

			while (rs.next()) {
				row.exec(rs);
			}
		} finally {
			if (rs != null && !rs.isClosed()) {
				try {
					rs.close();
				} catch (final Exception ignore) {
				}
			}
			closePstmt();
		}
	}

	/**
	 * Metoda care returneaza un singur rand sub forma unui {@link HashMap}<
	 * {@link String},{@link String}>. In cazul in care query-ul nu intoarce
	 * nici un rand, metoda arunca o exceptie.
	 * <p/>
	 * <p>
	 * <b>Nota, datorita naturii sale, aceasta metoda nu va merge pentru
	 * BLOB.</b>
	 * </p>
	 *
	 * @return Un rand din db sub forma unei liste
	 * @throws Exception
	 *             In interiorul lui <b>get</b> pot fi mai mute exceptii
	 */
	public HashMap<String, String> get() throws Exception {
		final ResultSet[] rs = new ResultSet[1];
		final HashMap<String, String> ret = new HashMap<String, String>(20);

		try {
			rs[0] = pstmt.executeQuery();

			if (rs[0].next()) {
				info(new RsmdMapFunction() {
					@Override
					public void apply(final HashMap<String, Integer> rsmdMap) throws SQLException {
						for (final String column : rsmdMap.keySet()) {
							if (rsmdMap.get(column) != Types.BLOB)
								ret.put(column, rs[0].getString(column));
						}
					}
				});
			}
		} finally {
			if (rs[0] != null && !rs[0].isClosed()) {
				try {
					rs[0].close();
				} catch (final Exception ignore) {
				}
			}
			closePstmt();
		}
		return ret;
	}

	/**
	 * Metoda care returneaza un singur rand sub forma unui {@link String}. In
	 * cazul in care query-ul nu intoarce nici un rand, metoda arunca o
	 * exceptie.
	 * <p/>
	 * <p>
	 * <b>Nota, datorita naturii sale, aceasta metoda nu va merge pentru
	 * BLOB.</b>
	 * </p>
	 *
	 * @param key
	 *            Cheia din bd cautata
	 * @return Un rand din db sub forma unui String
	 * @throws Exception
	 *             In interiorul lui <b>get</b> pot fi mai mute exceptii
	 */
	public String get(final String key) throws Exception {
		String ret;
		ResultSet rs = null;

		try {
			rs = pstmt.executeQuery();

			if (rs.next()) {
				ret = rs.getString(key);
			} else
				throw new ResultException("ResultSet-ul e gol.");
		} finally {
			if (rs != null && !rs.isClosed()) {
				try {
					rs.close();
				} catch (final Exception ignore) {
				}
			}
			closePstmt();
		}
		return ret;
	}

	/**
	 * Aceasta metoda executa codul sql si returneaza {@link ResultSet} set-ul.
	 * <p>
	 * <b>Nota: Acesta este modul manual de operare, va trebui sa parcurgi apoi
	 * sa inchizi result set-ul manual. Se recomanda metoda {@link #each} in
	 * schimb. Vezi documentatie.</b>
	 * </p>
	 *
	 * @return Un obiect ResultSet
	 * @throws SQLException
	 *             In interiorul lui <b>rs</b> poate fi o exceptie sql
	 */
	public ResultSet rs() throws SQLException {
		return pstmt.executeQuery();
	}

	/**
	 * Inchide statement-ul.
	 *
	 * @throws SQLException
	 *             In interiorul lui <b>closePstmt</b> poate fi o exceptie sql
	 */
	public void closePstmt() {
		if (pstmt != null) {
			try {
				pstmt.close();
			} catch (final Exception ignore) {
			}
		}
	}

	/**
	 * Ia numele coloanelor si tipurile de date ale acestora din query. Tipurile
	 * de date sunt constante int de tipul {@link java.sql.Types}. Numele
	 * coloanelor sunt insasi cheile map-ului.
	 *
	 * @param function
	 *            functie pe care sa o rulezi peste map
	 * @throws SQLException
	 *             In interiorul lui <b>info</b> poate fi o exceptie sql
	 */
	public void info(final RsmdMapFunction function) throws SQLException {
		if (info == null) {
			try {
				rsmd = pstmt.getMetaData();

				info = new HashMap<String, Integer>(20);
				final int cc = rsmd.getColumnCount();

				if (cc > 0) {
					// ia din metadate
					for (int i = 1; i <= cc; i++){
						String cName = rsmd.getColumnLabel(i);
						if (cName == null || cName.trim().isEmpty()){
							cName = rsmd.getColumnName(i);
						}
						info.put(cName.toLowerCase(), rsmd.getColumnType(i));
					}

				}

				// aplica functia peste noul info
				function.apply(info);
			} finally {
				closePstmt();
			}
		} else {
			// aplica functia peste vechiul info
			function.apply(info);
		}
	}

	/**
	 * Genereaza o mapare nume_coloana => pozitie coloana pentru rsmd.
	 *
	 * @param function
	 *            functie pe care sa o rulezi peste map
	 * @throws SQLException
	 *             In interiorul lui <b>info</b> poate fi o exceptie sql
	 */
	public void rsmdColMap(final RsmdMapFunction function) throws SQLException {
		if (rsmdColMap == null) {
			try {
				rsmd = pstmt.getMetaData();

				rsmdColMap = new HashMap<String, Integer>(20);
				final int cc = rsmd.getColumnCount();

				if (cc > 0) {
					// ia din metadate
					for (int i = 1; i <= cc; i++) {
						String cName = rsmd.getColumnLabel(i);
						if (cName == null || cName.trim().isEmpty()){
							cName = rsmd.getColumnName(i);
						}
						rsmdColMap.put(cName.toLowerCase(), i);
					}
				}

				// aplica functia peste noul info
				function.apply(rsmdColMap);
			} finally {
				closePstmt();
			}
		} else {
			// aplica functia peste vechiul info
			function.apply(rsmdColMap);
		}
	}

	public ResultSetMetaData getRsmd(final RsmdFunction function) throws SQLException {
		try {
			if (rsmd == null) {
				rsmd = pstmt.getMetaData();

				// aplica functia peste noul rsmd
				function.apply(rsmd);
			} else {
				// aplica functia peste vechiul rsmd
				function.apply(rsmd);
			}
		} finally {
			closePstmt();
		}
		return rsmd;
	}

	public PreparedStatement getPstmt() {
		return pstmt;
	}

	public void setPstmt(final PreparedStatement pstmt) {
		this.pstmt = pstmt;
	}

	public void eachInfo(final RsmdMapFunction function, final Row row) throws Exception {
		ResultSet rs = null;
		try {
			if (info == null) {
				rsmd = pstmt.getMetaData();

				info = new HashMap<String, Integer>(20);
				final int cc = rsmd.getColumnCount();

				if (cc > 0) {
					// ia din metadate
					for (int i = 1; i <= cc; i++) {
						String cName = rsmd.getColumnLabel(i);
						if (cName == null || cName.trim().isEmpty()){
							cName = rsmd.getColumnName(i);
						}
						info.put(cName.toLowerCase(), rsmd.getColumnType(i));
					}
				}
			}

			// aplica functia peste vechiul info
			function.apply(info);

			rs = pstmt.executeQuery();
			while (rs.next()) {
				row.exec(rs);
			}
		} finally {
			if (rs != null && !rs.isClosed()) {
				rs.close();
			}
			closePstmt();
		}
	}

	public String getPrintSql() {
		return printSql;
	}

	public void setPrintSql(String printSql) {
		this.printSql = printSql;
	}
}
