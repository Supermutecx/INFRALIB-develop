package ro.infrasoft.infralib.db.datasource;

import ro.infrasoft.infralib.db.result.Result;
import ro.infrasoft.infralib.db.transaction.Transaction;
import ro.infrasoft.infralib.db.type.DbType;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Interfata comuna pt JDBCDataSource si JDBCDataSourceCp.
 */
public interface BaseDataSource extends AutoCloseable{
    /**
     * Metoda cheie in aceasta clasa. Ea primeste un sql si o lista variabila
     * de parametrii care reprezinta necunoscutele in sql-ul specificat.
     * Cu acestea, el determina tipul de date al parametriilor si ii adauga la query.
     * <p>
     * Query-ul trebuie sa contina ? pentru necunoscute ex: <br/>
     * <b>select * from unitate where nume like ? and id > ?</b>
     * </p>
     * <p>
     * Parametrii se recomanda sa se trimita cu tipul lor real de date. Daca sunt int,
     * se trimit ca int. Functioneaza si cu fisiere (care se vor salva ca blob-uri) si totodata
     * merge cu orice obiect (ca fallback, in caz ca nu se stie tipul de date). Acest obiect va fi
     * serializat si salvat in baza de date ca blob.
     * </p>
     * <p>
     * Metoda returneaza un obiect {@link Result} care e wrapper de {@link java.sql.PreparedStatement}.
     * Acest obiect are multe metode utile pentru simplificarea lucrului cu bd.
     * </p>
     *
     * @param sql    String sql cu necunoscute marcate cu ?
     * @param params lista variabila de parametrii, indiferent de tipul de date al lor
     * @return Un obiect Result care e wrapper de PreparedStatement
     * @throws SQLException In interiorul lui <b>sql</b> poate fi o exceptie de sql
     */
    public Result sql(String sql, Object... params) throws SQLException, ClassNotFoundException;
    public Result sql(String sql, String sql1, DbType dbType1, Object... params) throws SQLException, ClassNotFoundException;
    public Result sql(String sql, String sql1, DbType dbType1, String sql2, DbType dbType2, Object... params) throws SQLException, ClassNotFoundException;
    public Result sql(String sql, String sql1, DbType dbType1, String sql2, DbType dbType2, String sql3, DbType dbType3, Object... params) throws SQLException, ClassNotFoundException;

    /**
     * Metoda care executa o procedura in bd. Este definita ca un wrapper peste metoda {@link #sql}
     * si returneaza un obiect {@link Result}. Trebuie folosita cu metoda {@link Result#put} deoarece o procedura
     * nu are return type.
     *
     * @param procName Numele procedurii sql
     * @param params   lista variabila de parametrii, indiferent de tipul de date al lor
     * @return Un obiect Result care e wrapper de PreparedStatement
     * @throws SQLException In interiorul lui <b>proc</b> poate fi o exceptie de sql
     */
    public Result proc(String procName, Object... params) throws SQLException, ClassNotFoundException;
    public Result proc(String procName, String procName1, DbType dbType1, Object... params) throws SQLException, ClassNotFoundException;
    public Result proc(String procName, String procName1, DbType dbType1, String procName2, DbType dbType2, Object... params) throws SQLException, ClassNotFoundException;
    public Result proc(String procName, String procName1, DbType dbType1, String procName2, DbType dbType2, String procName3, DbType dbType3, Object... params) throws SQLException, ClassNotFoundException;

    /**
     * Metoda importanta care executa DML batch.
     *
     * @param batchSql sql de batch
     */
    public void batchDML(List<String> batchSql) throws SQLException;

    public void batchDMLDirect(List<String> batchSql) throws SQLException, ClassNotFoundException;

    /**
     * Aceasta metoda cheama metoda <b>exec</b> a unui obiect anonim {@link ro.infrasoft.infralib.db.transaction.Transaction} si face rollback daca e nevoie sau commit.
     * Este una dintre putinele metode care prinde exceptii (dar le arunca mai departe). Motivul este ca daca s-a intamplat ceva,
     * trebuie sa se faca rollback la tranzactie.
     *
     * @param transaction Un obiect de tip Transaction care are implementata metoda {@link ro.infrasoft.infralib.db.transaction.Transaction#exec(BaseDataSource)}
     * @throws Exception In interiorul lui <b>transact</b> pot fi mai multe exceptii
     */
    public void transact(Transaction transaction) throws Exception;

    /**
     * Aceasta metoda populeaza o lista de valori apeland metoda each
     *
     * @param sql    String sql
     * @param params Parametri pentru ResultSet
     * @throws Exception
     */
    public List<Map<String, String>> list(String sql, Object... params) throws Exception;
    public List<Map<String, String>> list(String sql, String sql1, DbType dbType1, Object... params) throws Exception;
    public List<Map<String, String>> list(String sql, String sql1, DbType dbType1, String sql2, DbType dbType2, Object... params) throws Exception;
    public List<Map<String, String>> list(String sql, String sql1, DbType dbType1, String sql2, DbType dbType2, String sql3, DbType dbType3, Object... params) throws Exception;

    /**
     * Face rollback la tranzactie.
     *
     * @throws SQLException In interiorul lui <b>rollback</b> poate fi o exceptie de sql
     */
    public void rollback() throws SQLException;

    /**
     * Face commit la tranzactie.
     *
     * @throws SQLException In interiorul lui <b>commit</b> poate fi o exceptie de sql
     */
    public void commit() throws SQLException;

    /**
     * Porneste o tranzactie in bd.
     *
     * @throws SQLException In interiorul lui <b>startTransaction</b> poate fi o exceptie de sql
     */
    public void startTransaction() throws SQLException;

    /**
     * Opreste o tranzactie in bd.
     *
     * @throws SQLException In interiorul lui <b>endTransaction</b> poate fi o exceptie de sql
     */
    public void endTransaction() throws SQLException;

    /**
     * Verifica daca e inchisa conexiunea.
     *
     * @return Un boolean care specifica daca e inchisa conexiunea
     * @throws SQLException In interiorul lui <b>isClosed</b> poate fi o exceptie de sql
     */
    public boolean isClosed() throws SQLException;

    /**
     * Intoarce conexiunea reala de db.
     *
     * @return conexiunea reala
     */
    public Connection getConn();


    /**
     * Inchide conexiunea sau face ceva mai special.
     *
     * @throws SQLException In interiorul lui <b>close</b> poate fi o exceptie de sql
     */
    @Override
    public void close() throws SQLException;

    /**
     * Chiar inchide conexiunea.
     *
     * @throws SQLException In interiorul lui <b>close</b> poate fi o exceptie de sql
     */
    public void directClose() throws SQLException;

    public boolean isValid();

    public DbType getDbType();

    public void setDbType(DbType dbType);

    public String getSchemaName();

    public void setSchemaName(String schemaName);
}
