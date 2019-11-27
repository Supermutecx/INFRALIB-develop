package ro.infrasoft.infralib.db.datasource;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import ro.infrasoft.infralib.db.type.DbType;
import ro.infrasoft.infralib.db.functions.DsCpFunction;
import ro.infrasoft.infralib.db.pool.CustomPool;
import ro.infrasoft.infralib.db.result.Result;
import ro.infrasoft.infralib.db.result.Row;
import ro.infrasoft.infralib.db.transaction.Transaction;
import ro.infrasoft.infralib.logger.LoggerUtil;

import java.io.InputStream;
import java.sql.Clob;
import java.sql.Connection;
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
 * Datasource cu pool custom.
 */
@SuppressWarnings("JDBCResourceOpenedButNotSafelyClosed")
public class JDBCDataSourceCp implements BaseDataSource, AutoCloseable {
    private Integer id;
    private CustomPool customPool;
    private DsCpFunction logSqlFunction;
    private DsCpFunction initDbEnvFunction;
    private Connection conn;
    private Logger logger = LoggerUtil.getLogger("customPool");
    private boolean injectionProtection = true;
    private DbType dbType;
    private String schemaName;

    /**
     * Constructor principal.
     *
     * @param conn       conexiune
     * @param customPool pool custom
     */
    public JDBCDataSourceCp(CustomPool customPool, Connection conn, Integer id) {
        this.conn = conn;
        this.customPool = customPool;
        this.id = id;
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            if (conn != null)
                conn.close();
        } catch (Throwable t) {
            logger.error("Error on finalization of ds.");
            logger.error(LoggerUtil.printException(t));
        }
    }


    /**
     * Pune conexiunea la loc in pool.
     * Metoda se numeste 'close' pt try cu resurse.
     */
    @Override
    public void close() throws SQLException {
        customPool.returnConnection(this);
    }

    @Override
    public void directClose() {
        try {
            if (this.conn != null) {
                this.conn.close();
                this.conn = null;
            }
        } catch (Throwable t) {
            logger.error("Error on direct close of ds.");
            logger.error(LoggerUtil.printException(t));
        }
    }

    @Override
    public boolean isClosed() throws SQLException {
        boolean closed = true;

        if (conn != null) {
            try {
                closed = conn.isClosed();
            } catch (Throwable t) {
                logger.error("Error on is closed attempt on ds.");
                logger.error(LoggerUtil.printException(t));
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
            } catch (Throwable t) {
                logger.error("Error on batchDML for ds.");
                logger.error(LoggerUtil.printException(t));
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
        sql = sql.replace("--","- -");
        if (schemaName != null){
            sql = sql.replace("$[SCHEMA_NAME]",schemaName);
        }
        if (injectionProtection) {
            if (sql != null && (sql.contains("--") || sql.toLowerCase().contains("truncate"))) {
                throw new SQLException("Injection denied.");
            }
        }


        if (isClosed()) {
            setConn(customPool.reinitRealConnection(id, this));
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
            else if (param instanceof java.sql.Timestamp)
                pstmt.setTimestamp(index, (java.sql.Timestamp) param);
            else if (param instanceof java.sql.Date)
                pstmt.setDate(index, (java.sql.Date) param);
            else if (param instanceof java.util.Date)
                pstmt.setDate(index, new java.sql.Date(((java.util.Date) param).getTime()));
            else if (param instanceof DateTime)
                pstmt.setDate(index, new java.sql.Date(((DateTime) param).getMillis()));
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
        String printSql = null;
        if (logSqlFunction != null) {
            printSql = sql;
            for (Object param : params) {
                if (param instanceof String) {
                    printSql = printSql.replaceFirst("\\?", "'" + param.toString().replace("$", "[dolar]") + "'");
                } else {
                    printSql = printSql.replaceFirst("\\?", String.valueOf(param));
                }
            }

            if (logSqlFunction != null) {
                try {
                    logSqlFunction.apply(this, printSql);
                } catch (Throwable th) {
                    logger.error("Error on log function.");
                    logger.error(LoggerUtil.printException(th));
                }
            }
        }


        /**
         * In final, creeaza un obiect de tip Result cu PreparedStatement-ul primit si trimite-l mai departe.
         * Nota: nu aici se face executia, deoarece aceste lucruri se realizeaza in Result.
         */
        return new Result(pstmt, printSql);
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
                    String cName = rs.getMetaData().getColumnLabel(i);
                    if (cName == null || cName.trim().isEmpty()){
                        cName = rs.getMetaData().getColumnName(i);
                    }
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

        switch (dbType){
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
                query.append("exec ").append(procName).append(" ");

                for (int i = 0; i <= params.length - 1; i++) {
                    query.append('?')
                            .append(',');
                }
                if (params.length > 0)
                    query.delete(query.lastIndexOf(","), query.length());

                query.append(";");
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

    @Override
    public boolean isValid() {
        Statement stmt = null;
        ResultSet rs = null;

        try {
            stmt = conn.createStatement();
            if (stmt != null) {
                rs = stmt.executeQuery("select 1 a from dual");
                if (rs != null) {
                    rs.next();
                    boolean ret = rs.getInt("a") == 1;
                    return ret;
                }
            }
        } catch (Throwable e) {
            logger.error(LoggerUtil.printException(e));
        } finally {
            try {
                if (rs != null)
                    rs.close();
            } catch (Exception e) {
                logger.error(LoggerUtil.printException(e));
            }
            try {
                if (stmt != null)
                    stmt.close();
            } catch (Exception e) {
                logger.error(LoggerUtil.printException(e));
            }
        }
        return false;
    }

    /**
     * Initializeaza environment-ul de bd.
     */
    public void initDbEnv() {
        if (initDbEnvFunction != null) {
            try {
                initDbEnvFunction.apply(this);
            } catch (Throwable t) {
                logger.error("Error on init env.");
                logger.error(LoggerUtil.printException(t));
            }
        }
    }

    public void setConn(Connection conn) {
        this.conn = conn;
    }

    /**
     * @deprecated nu se mai utilizeaza
     * @return false intotdeauna
     */
    public boolean isDebug() {
        return false;
    }

    /**
     * @deprecated nu se mai utilizeaza
     * @param debug debug (nu se face nimic cu el)
     */
    public void setDebug(boolean debug) {

    }

    /**
     * @deprecated nu se mai utilizeaza
     * @return false intotdeauna
     */
    public boolean isLogSql() {
        return false;
    }

    /**
     * @deprecated nu se mai utilizeaza
     * @param logSql logSql (nu se face nimic cu el)
     */
    public void setLogSql(boolean logSql) {

    }

    public CustomPool getCustomPool() {
        return customPool;
    }

    public void setCustomPool(CustomPool customPool) {
        this.customPool = customPool;
    }

    public DsCpFunction getLogSqlFunction() {
        return logSqlFunction;
    }

    public void setLogSqlFunction(DsCpFunction logSqlFunction) {
        this.logSqlFunction = logSqlFunction;
    }

    public DsCpFunction getInitDbEnvFunction() {
        return initDbEnvFunction;
    }

    public void setInitDbEnvFunction(DsCpFunction initDbEnvFunction) {
        this.initDbEnvFunction = initDbEnvFunction;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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
