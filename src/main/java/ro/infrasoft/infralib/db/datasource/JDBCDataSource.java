package ro.infrasoft.infralib.db.datasource;

import org.joda.time.DateTime;
import ro.infrasoft.infralib.db.connector.AbstractConnector;
import ro.infrasoft.infralib.db.result.Result;
import ro.infrasoft.infralib.db.result.Row;
import ro.infrasoft.infralib.db.transaction.Transaction;
import ro.infrasoft.infralib.db.type.DbType;

import javax.sql.DataSource;
import java.io.InputStream;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Se ocupa cu toate cerintele unei interfete catre baza de date.
 */
@SuppressWarnings("JDBCResourceOpenedButNotSafelyClosed")
public class JDBCDataSource implements BaseDataSource, AutoCloseable {
    private boolean debug;
    private boolean logSql;
    private AbstractConnector connector;
    private Connection conn;
    private DataSource datasource;
    private boolean injectionProtection = true;
    private DbType dbType;
    private String schemaName;

    /**
     * Constructor principal care primeste un {@link AbstractConnector}. Prin intermediul acestuia,
     * va sti ce driver sa foloseasca si la ce baza de date sa se conecteze.
     * <p>
     * Nu exista alt constructor deoarece aceasta clasa nu are sens fara un {@link AbstractConnector}.
     * </p>
     *
     * @param connector Un Connector care contine informatii despre bd
     */
    public JDBCDataSource(AbstractConnector connector) {
        this.connector = connector;

        // set db type
        setDbType(connector.dbType());
        setSchemaName(connector.schemaName());
    }

    /**
     * Constructor care primeste datasource si conector.
     *
     * @param connector  conector
     * @param datasource datasource
     */
    public JDBCDataSource(AbstractConnector connector, DataSource datasource) {
        this.connector = connector;
        this.datasource = datasource;

        // set db type
        setDbType(connector.dbType());
        setSchemaName(connector.schemaName());
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            if (conn != null)
                conn.close();
        } catch (Throwable t) {
            //IGNORE
        }
    }

    @Override
    public void close() throws SQLException {
        if (conn != null)
            try {
                conn.close();
            } catch (Exception ignore) {
            }
    }

    @Override
    public void directClose() throws SQLException {
        close();
    }

    @Override
    public boolean isClosed() throws SQLException {
        boolean closed = true;

        if (conn != null) {
            try {
                closed = conn.isClosed();
            } catch (Exception ignore) {
            }
        }

        return closed;
    }

    @Override
    public void startTransaction() throws SQLException {
        conn.setAutoCommit(false);
    }

    @Override
    public void endTransaction() throws SQLException {
        conn.setAutoCommit(true);
    }

    @Override
    public void commit() throws SQLException {
        conn.commit();
    }

    @Override
    public void rollback() throws SQLException {
        conn.rollback();
    }

    @Override
    public void transact(Transaction transaction) throws Exception {
        try {
            startTransaction();
            transaction.exec(this);
            commit();
        } catch (Exception e) {
            rollback();
            throw e;
        } finally {
            endTransaction();
        }
    }

    @Override
    public void batchDML(List<String> batchSql) throws SQLException {
        Statement statement = null;
        try {
            statement = conn.createStatement();
            for (String sql : batchSql) {
                statement.addBatch(sql);
            }
            statement.executeBatch();
        } finally {
            try {
                statement.close();
            } catch (Exception ignore) {
            }
        }
    }

    @Override
    public void batchDMLDirect(List<String> batchSql) throws SQLException, ClassNotFoundException {
        for (String sqlStmt: batchSql){
            sql(sqlStmt).put();
        }
    }

    @Override
    public Result sql(String sql, Object... params) throws SQLException, ClassNotFoundException {
        if (injectionProtection) {
            if (sql != null && sql.contains("--")) {
                throw new SQLException("Injection denied.");
            }
        }

        if (schemaName != null){
            sql = sql.replace("$[SCHEMA_NAME]",schemaName);
        }

        //pregateste statement-ul cu sql
        PreparedStatement pstmt = conn.prepareStatement(sql);

        //pstmt primeste index-uri de la 1
        int index = 1;

        /*
        * Trece prin toti parametrii si seteaza-i in functie de ce tip de date au.
        * Codul este ceva mai urat din cauza if-urilor multiple insa e eficient si utilizatorii
        * librariei nu trebuie sa stie de el.
        */
        for (Object param : params) {
            if (param instanceof Boolean)
                pstmt.setInt(index, ((Boolean) param) ? 1 : 0);
            else if (param instanceof Double)
                pstmt.setDouble(index, (Double) param);
            else if (param instanceof Float)
                pstmt.setFloat(index, (Float) param);
            else if (param instanceof Long)
                pstmt.setLong(index, (Long) param);
            else if (param instanceof Integer)
                pstmt.setInt(index, (Integer) param);
            else if (param instanceof Short)
                pstmt.setShort(index, (Short) param);
            else if (param instanceof String)
                pstmt.setString(index, (String) param);
            else if (param instanceof InputStream)
                pstmt.setBinaryStream(index, (InputStream) param);
            else if (param instanceof java.sql.Date)
                pstmt.setDate(index, (java.sql.Date) param);
            else if (param instanceof java.util.Date)
                pstmt.setDate(index, new java.sql.Date(((java.util.Date) param).getTime()));
            else if (param instanceof DateTime)
                pstmt.setDate(index, new java.sql.Date(((DateTime) param).toDate().getTime()));
            else if (param instanceof Clob)
                pstmt.setClob(index, (Clob) param);
            else {
                if (param == null)
                    pstmt.setNull(index, Types.VARCHAR);
                else
                    pstmt.setString(index, param.toString());
            }
            index++;
        }

        /**
         * Daca e setat parametrul de debug, construieste string-ul si il arata.
         */
        if (debug || logSql) {
            String printSql = sql;
            for (Object param : params) {
                if (param instanceof String) {
                    printSql = printSql.replaceFirst("\\?", "'" + param.toString().replace("$", "[dolar]") + "'");
                } else {
                    printSql = printSql.replaceFirst("\\?", String.valueOf(param));
                }
            }

            if (debug)
                System.out.println(printSql);

            if (logSql) {
                PreparedStatement pstmtL = null;
                try {
                    String sqlTrim = printSql.replace("'", "`");
                    sqlTrim = sqlTrim.substring(0, Math.min(3998, sqlTrim.length()));
                    pstmtL = conn.prepareStatement("begin sys_add_log2('SQL', 'SQL_OR_PROC', '" + sqlTrim + "', sys_get_id_unitate(), 0); end;");
                    pstmtL.executeUpdate();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (pstmtL != null) {
                        try {
                            pstmtL.close();
                        } catch (Exception ignore) {
                        }
                    }
                }

            }
        }


        /**
         * In final, creeaza un obiect de tip Result cu PreparedStatement-ul primit si trimite-l mai departe.
         * Nota: nu aici se face executia, deoarece aceste lucruri se realizeaza in Result.
         */
        return new Result(pstmt);
    }


    @Override
    public Result sql(String sql, String sql1, DbType dbType1, Object... params) throws SQLException, ClassNotFoundException {
        if (dbType.equals(dbType1)){
            return sql(sql1, params);
        }else{
            return sql(sql, params);
        }
    }

    @Override
    public Result sql(String sql, String sql1, DbType dbType1, String sql2, DbType dbType2, Object... params) throws SQLException, ClassNotFoundException {
        if (dbType.equals(dbType1)){
            return sql(sql1, params);
        }else if (dbType.equals(dbType2)){
            return sql(sql2, params);
        }else{
            return sql(sql, params);
        }
    }

    @Override
    public Result sql(String sql, String sql1, DbType dbType1, String sql2, DbType dbType2, String sql3, DbType dbType3, Object... params) throws SQLException, ClassNotFoundException {
        if (dbType.equals(dbType1)){
            return sql(sql1, params);
        }else if (dbType.equals(dbType2)){
            return sql(sql2, params);
        }else if (dbType.equals(dbType3)){
            return sql(sql3, params);
        }else{
            return sql(sql, params);
        }
    }

    @Override
    public List<Map<String, String>> list(String sql, Object... params) throws Exception {
        final List<Map<String, String>> valueList = new ArrayList<Map<String, String>>();
        this.sql(sql, params).each(new Row() {
            @Override
            public void exec(ResultSet rs) throws Exception {
                Map<String, String> value = new HashMap<String, String>();
                for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                    String cName = rs.getMetaData().getColumnName(i);
                    value.put(cName.toLowerCase(), rs.getString(cName));
                }
                valueList.add(value);
            }
        });
        return valueList;
    }

    @Override
    public List<Map<String, String>> list(String sql, String sql1, DbType dbType1, Object... params) throws Exception {
        if (dbType.equals(dbType1)){
            return list(sql1, params);
        }else{
            return list(sql, params);
        }
    }

    @Override
    public List<Map<String, String>> list(String sql, String sql1, DbType dbType1, String sql2, DbType dbType2, Object... params) throws Exception {
        if (dbType.equals(dbType1)){
            return list(sql1, params);
        }else if (dbType.equals(dbType2)){
            return list(sql2, params);
        }else{
            return list(sql, params);
        }
    }

    @Override
    public List<Map<String, String>> list(String sql, String sql1, DbType dbType1, String sql2, DbType dbType2, String sql3, DbType dbType3, Object... params) throws Exception {
        if (dbType.equals(dbType1)){
            return list(sql1, params);
        }else if (dbType.equals(dbType2)){
            return list(sql2, params);
        }else if (dbType.equals(dbType3)){
            return list(sql3, params);
        }else{
            return list(sql, params);
        }
    }

    @Override
    public Result proc(String procName, Object... params) throws SQLException, ClassNotFoundException {

        //construieste sql-ul special pentru procedura
        StringBuilder query = new StringBuilder(5);

        switch (dbType) {
            case ORACLE:
                query.append("begin ").append(procName).append(" (");

                for (int i = 0; i <= params.length - 1; i++) {
                    query.append('?')
                            .append(',');
                }
                if (params.length > 0)
                    query.delete(query.lastIndexOf(","), query.length());

                query.append("); end;");
                break;
            case MYSQL:
                query.append("call ").append(procName).append(" (");

                for (int i = 0; i <= params.length - 1; i++) {
                    query.append('?')
                            .append(',');
                }
                if (params.length > 0)
                    query.delete(query.lastIndexOf(","), query.length());

                query.append(");");
                break;
            case SQL_SERVER:
                query.append("exec ").append(procName).append(" (");

                for (int i = 0; i <= params.length - 1; i++) {
                    query.append('?')
                            .append(',');
                }
                if (params.length > 0)
                    query.delete(query.lastIndexOf(","), query.length());

                query.append(");");
                break;
        }

        //acum doar executa metoda sql si intoarce rezultatul
        return sql(query.toString(), params);
    }

    @Override
    public Result proc(String procName, String procName1, DbType dbType1, Object... params) throws SQLException, ClassNotFoundException {
        if (dbType.equals(dbType1)){
            return proc(procName1, params);
        }else{
            return proc(procName, params);
        }
    }

    @Override
    public Result proc(String procName, String procName1, DbType dbType1, String procName2, DbType dbType2, Object... params) throws SQLException, ClassNotFoundException {
        if (dbType.equals(dbType1)){
            return proc(procName1, params);
        }else if (dbType.equals(dbType2)){
            return proc(procName2, params);
        }else{
            return proc(procName, params);
        }
    }

    @Override
    public Result proc(String procName, String procName1, DbType dbType1, String procName2, DbType dbType2, String procName3, DbType dbType3, Object... params) throws SQLException, ClassNotFoundException {
        if (dbType.equals(dbType1)){
            return proc(procName1, params);
        }else if (dbType.equals(dbType2)){
            return proc(procName2, params);
        }else if (dbType.equals(dbType3)){
            return proc(procName3, params);
        }else{
            return proc(procName, params);
        }
    }

    @Override
    public Connection getConn() {
        return conn;
    }


    /**
     * Metoda care forteaza driverul de jdbc in memorie prin reflexie.
     * De asemenea, metoda returneaza aceasta instanta de {@link Class}.
     * <p/>
     * Nota: Aceasta metoda este default (package private) si deci e accesibila doar local
     * sau in acelasi pachet. Cum testele sunt in acelasi pachet, poate fi testata.
     * <p/>
     *
     * @return Clasa driverului incarcat
     * @throws ClassNotFoundException In interiorul lui <b>loadDriver</b> poate fi o exceptie de reflexie
     */
    Class loadDriver() throws ClassNotFoundException {
        return Class.forName(connector.driver());
    }

    public void openDirect() throws SQLException, ClassNotFoundException {
        loadDriver();
        if (conn == null || conn.isClosed()) {
            conn = null;
            conn = DriverManager.getConnection(connector.url(), connector.user(), connector.password());
        }
    }

    /**
     * Deschide o conexiune la baza de date specificata de conector.
     *
     * @throws SQLException           In interiorul lui <b>open</b> poate fi o exceptie de sql
     * @throws ClassNotFoundException In interiorul lui <b>open</b> poate fi o exceptie de reflexie
     */
    public void open() throws SQLException, ClassNotFoundException {
        if (!(conn != null && !conn.isClosed() && conn.isValid(1)))
            conn = datasource.getConnection();

        conn.setAutoCommit(true);
    }

    @Override
    public boolean isValid() {
        return true;
    }

    public AbstractConnector getConnector() {
        return connector;
    }

    public void setConnector(AbstractConnector connector) {
        this.connector = connector;
    }

    public void setConn(Connection conn) {
        this.conn = conn;
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public boolean isLogSql() {
        return logSql;
    }

    public void setLogSql(boolean logSql) {
        this.logSql = logSql;
    }

    public boolean isInjectionProtection() {
        return injectionProtection;
    }

    public void setInjectionProtection(boolean injectionProtection) {
        this.injectionProtection = injectionProtection;
    }

    @Override
    public DbType getDbType() {
        return dbType;
    }

    @Override
    public void setDbType(DbType dbType) {
        this.dbType = dbType;
    }

    @Override
    public String getSchemaName() {
        return schemaName;
    }

    @Override
    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }
}

