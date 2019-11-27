package ro.infrasoft.infralib.db.pool;

import org.apache.log4j.Logger;
import ro.infrasoft.infralib.db.datasource.JDBCDataSourceCp;
import ro.infrasoft.infralib.db.functions.DsCpFunction;
import ro.infrasoft.infralib.logger.LoggerUtil;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Pool custom sincronizat bazat pe un id.
 */
public class CustomPool {
    private ReentrantLock lock = new ReentrantLock();
    private Logger logger = LoggerUtil.getLogger("customPool");
    private CustomPoolProperties customPoolProperties;
    private Map<Integer, JDBCDataSourceCp> pool;
    private Queue<Integer> queue;
    private List<Integer> msList;

    /**
     * Constructor care creaza pool-ul si coada si primeste proprietatile.
     *
     * @param customPoolProperties proprietati pool
     */
    public CustomPool(CustomPoolProperties customPoolProperties) {
        this.customPoolProperties = customPoolProperties;
        pool = new HashMap<Integer, JDBCDataSourceCp>(customPoolProperties.getMaxActive());
        queue = new ArrayDeque<Integer>(customPoolProperties.getMaxActive());
        msList = new ArrayList<>();

        info("Pool initialized. " + "URL = " + customPoolProperties.getUrl() + " DBUSER = " + customPoolProperties.getUsername());
    }

    /**
     * Intoarce o conexiune dupa id. Aceasta metoda e sincronizata.
     *
     * @param id             id
     * @param debug          daca debug e enabled
     * @param logSql         daca logare sql e enabled - trebuie specificata si functie
     * @param logFunction    optional - functie de log sql - null = nu vrem sa avem log sql
     * @param initDbFunction optional - functie de init context - null = nu vrem sa avem init context
     * @return conexiune
     * @throws SQLException           exceptie sql
     * @throws ClassNotFoundException nu s-a gasit driverul
     * @deprecated utilizati getConnection(Integer id, DsCpFunction logFunction, DsCpFunction initDbFunction)
     */
    public JDBCDataSourceCp getConnection(Integer id, boolean debug, boolean logSql, DsCpFunction logFunction, DsCpFunction initDbFunction) throws SQLException, ClassNotFoundException {
        info("getConnection. id = " + id);

        // mai intai, cautam sa vedem daca exista deja o conexiune
        JDBCDataSourceCp ds = null;

        lock.lock();
        try {
            ds = pool.get(id);
        } finally {
            lock.unlock();
        }

        // daca e null, se creaza una noua cu considerente de max connection
        if (ds == null) {
            lock.lock();
            try {
                ds = createNewConnection(id, debug, logSql, logFunction, initDbFunction);
            } finally {
                lock.unlock();
            }
        }

        // daca e inchisa sau ne-functionala, se re-initializeaza
        if (ds.isClosed() || !ds.isValid()) {
            lock.lock();
            try {
                ds = reCreateConnection(id, ds, debug, logSql, logFunction, initDbFunction);
            } finally {
                lock.unlock();
            }
        }

        return ds;
    }

    /**
     * Intoarce o conexiune dupa id. Aceasta metoda e sincronizata.
     *
     * @param id             id
     * @param logFunction    optional - functie de log sql - null = nu vrem sa avem log sql
     * @param initDbFunction optional - functie de init context - null = nu vrem sa avem init context
     * @return conexiune
     * @throws SQLException           exceptie sql
     * @throws ClassNotFoundException nu s-a gasit driverul
     */
    public JDBCDataSourceCp getConnection(Integer id, DsCpFunction logFunction, DsCpFunction initDbFunction) throws SQLException, ClassNotFoundException {
        return getConnection(id, false, false, logFunction, initDbFunction);
    }

    /**
     * Se re-initializeaza doar conexiunea unui ds vechi.
     *
     * @param id id
     * @param ds ds
     * @return conexiunea
     * @throws SQLException           exceptie sql
     * @throws ClassNotFoundException nu s-a gasit driverul
     */
    public Connection reinitRealConnection(Integer id, JDBCDataSourceCp ds) throws SQLException, ClassNotFoundException {
        lock.lock();

        try {
            info("reinitRealConnection. id = " + id);

            // mai intai se inchide ds-ul vechi
            ds.directClose();

            // apoi se initializeaza o conexiune noua
            Connection conn = getDatabaseConnection();
            ds.setConn(conn);

            // se ruleaza initializarea de context bd, daca e configurata
            ds.initDbEnv();

            // se re-stocheaza in harta
            pool.put(id, ds);

            return conn;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Reseteaza context-ul pt o conexiune.
     *
     * @param id             id
     * @param ds             ds
     * @param initDbFunction functie de init db
     * @return conexiunea
     */
    public JDBCDataSourceCp resetContext(Integer id, JDBCDataSourceCp ds, DsCpFunction initDbFunction) {
        lock.lock();

        try {
            info("resetContext. id = " + id);

            // se pune init function
            ds.setInitDbEnvFunction(initDbFunction);

            // se ruleaza init env-ul nou
            ds.initDbEnv();

            // apoi se re-stocheaza in harta
            pool.put(id, ds);

            return ds;
        } finally {
            lock.unlock();
        }
    }


    /**
     * Intoarce conexiunea in pool. Momentan nu trebuie sa faca nimic.
     * Atentie la implementare, trebuie sincronizata la fel ca si {@link ro.infrasoft.infralib.db.pool.CustomPool#getConnection}.
     *
     * @param ds datasource
     */
    public void returnConnection(JDBCDataSourceCp ds) {
        lock.lock();

        try {
            info("return connection id = " + ds.getId());

            if (customPoolProperties.getMarkAndSweep()) {
                info("return in mark and sweep mode.");

                if (msList.contains(ds.getId())) {
                    info("sweeping. id = " + ds.getId());
                    queue.remove(ds.getId());
                    JDBCDataSourceCp dsToKill = pool.remove(ds.getId());
                    if (dsToKill != null) {
                        dsToKill.directClose();
                        dsToKill = null;
                    }
                    msList.remove(ds.getId());
                }
            }
        } finally {
            lock.unlock();
        }

    }

    /**
     * Sterge conexiunea si o inchide.
     *
     * @param id id
     */
    public void removeConnection(Integer id) {
        lock.lock();

        try {
            info("remove connection. id = " + id);

            if (pool.containsKey(id)) {
                if (customPoolProperties.getMarkAndSweep()) {
                    info("marked for sweep. id = " + id);
                    if (!msList.contains(id))
                        msList.add(id);
                } else {
                    info("direct remove. id = " + id);
                    JDBCDataSourceCp conn = pool.remove(id);
                    conn.directClose();
                    queue.remove(id);
                }
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * Ia numerele.
     */
    public Map<String, String> getNumbers() {
        lock.lock();

        try {
            info("get numbers. id = ");

            Map<String, String> numbers = new HashMap<String, String>();
            numbers.put("pool size", String.valueOf(pool.size()));
            numbers.put("queue size", String.valueOf(queue.size()));
            numbers.put("mark and sweep size", String.valueOf(msList.size()));

            StringBuilder sb = new StringBuilder();
            for (Integer poolS : pool.keySet()) {
                sb.append(poolS).append(" ");
            }

            numbers.put("pool vals", sb.toString());

            sb = new StringBuilder();
            for (Integer queueS : queue) {
                sb.append(queueS).append(" ");
            }

            numbers.put("queue vals", sb.toString());


            sb = new StringBuilder();
            for (Integer msS : msList) {
                sb.append(msS).append(" ");
            }

            numbers.put("ms vals", sb.toString());

            return numbers;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Creaza o noua conexiune. O stocheaza in harta.
     * Daca s-a ajuns la maximul de conexiuni, scoate cea mai veche conexiune de pe harta si o inchide si de-aloca.
     *
     * @param id             id
     * @param debug          daca debug e enabled
     * @param logSql         daca logare sql e enabled - trebuie specificata si functie
     * @param logSqlFunction optional - functie de log sql - null = nu vrem sa avem log sql
     * @param initDbFunction optional - functie de init context - null = nu vrem sa avem init context
     * @return conexine
     * @throws SQLException           exceptie sql
     * @throws ClassNotFoundException nu s-a gasit driverul
     */
    private JDBCDataSourceCp createNewConnection(Integer id, boolean debug, boolean logSql, DsCpFunction logSqlFunction, DsCpFunction initDbFunction) throws SQLException, ClassNotFoundException {
        info("createNewConnection. id = " + id);

        // se creaza noua conexiune
        JDBCDataSourceCp ds = initNewConnection(id, debug, logSql, logSqlFunction, initDbFunction);

        // s-a ajuns la limita, scoate ceva din pool
        if (queue.size() > customPoolProperties.getMaxActive()) {
            if (customPoolProperties.getMarkAndSweep()) {
                Integer foundMsId = findMsId(id);
                if (!foundMsId.equals(0)) {
                    info("marked for sweep. id = " + foundMsId);
                    if (!msList.contains(foundMsId))
                        msList.add(foundMsId);
                } else {
                    info("Found id 0 to mark and sweep. No need to mark.");
                }
            } else {
                Integer idToKill = queue.remove();
                info("direct remove. id = " + idToKill);

                JDBCDataSourceCp dsToKill = pool.remove(idToKill);
                if (dsToKill != null) {
                    dsToKill.directClose();
                    dsToKill = null;
                }
            }
        }

        // se aduaga in pool si in coada daca nu exista
        if (!pool.containsKey(id)) {
            queue.add(id);
            pool.put(id, ds);
        }

        return ds;
    }

    /**
     * Re-creaza o conexiune veche.
     *
     * @param id             id
     * @param ds             datasource
     * @param debug          daca debug e enabled
     * @param logSql         daca logare sql e enabled - trebuie specificata si functie
     * @param initDbFunction optional - functie de init context - null = nu vrem sa avem init context
     * @return conexine
     * @throws SQLException           exceptie sql
     * @throws ClassNotFoundException nu s-a gasit driverul
     */
    private JDBCDataSourceCp reCreateConnection(Integer id, JDBCDataSourceCp ds, boolean debug, boolean logSql, DsCpFunction logSqlFunction, DsCpFunction initDbFunction) throws SQLException, ClassNotFoundException {
        info("reCreateConnection. id = " + id);

        // mai intai, se inchide ds-ul vechi
        ds.directClose();

        // apoi se initializeaza din nou
        ds = initNewConnection(id, debug, logSql, logSqlFunction, initDbFunction);

        // se re-stocheaza in harta
        pool.put(id, ds);

        return ds;
    }

    /**
     * Initializeaza o conexiune noua.
     *
     * @param id             id
     * @param debug          daca debug e enabled
     * @param logSql         daca logare sql e enabled - trebuie specificata si functie
     * @param logSqlFunction optional - functie de log sql - null = nu vrem sa avem log sql
     * @param initDbFunction optional - functie de init context - null = nu vrem sa avem init context
     * @return noua conexiune
     * @throws SQLException           exceptie sql
     * @throws ClassNotFoundException nu s-a gasit driveruln
     */
    private JDBCDataSourceCp initNewConnection(Integer id, boolean debug, boolean logSql, DsCpFunction logSqlFunction, DsCpFunction initDbFunction) throws SQLException, ClassNotFoundException {
        info("initNewConnection. id = " + id);

        // se creaza noua conexiune
        JDBCDataSourceCp ds = new JDBCDataSourceCp(this, getDatabaseConnection(), id);
        ds.setDebug(debug);
        ds.setLogSql(logSql);
        ds.setInitDbEnvFunction(initDbFunction);
        ds.setLogSqlFunction(logSqlFunction);
        ds.setDbType(customPoolProperties.getDbType());
        ds.setSchemaName(customPoolProperties.getSchemaName());

        // se ruleaza initializarea de context bd, daca e configurata
        ds.initDbEnv();

        return ds;
    }

    /**
     * Forteaza incarcarea driverului in memorie.
     *
     * @return driverul
     * @throws ClassNotFoundException daca nu exista
     */
    private Class loadDriver() throws ClassNotFoundException {
        return Class.forName(customPoolProperties.getClassName());
    }

    /**
     * Intoarce o conexiune jdbc.
     *
     * @throws SQLException           exceptie sql
     * @throws ClassNotFoundException nu s-a gasit driverul
     */
    private Connection getDatabaseConnection() throws SQLException, ClassNotFoundException {
        info("getDatabaseConnection");

        loadDriver();
        Connection connection = DriverManager.getConnection(customPoolProperties.getUrl(), customPoolProperties.getUsername(), customPoolProperties.getPassword());
        connection.setAutoCommit(true);
        return connection;
    }

    /**
     * Finds the next id to mark and sweep.
     *
     * @return id
     */
    private Integer findMsId(Integer initiator) {
        Integer foundId = 0;

        for (Integer id : queue) {
            if (!id.equals(initiator) && (msList.isEmpty() || !msList.contains(id))) {
                foundId = id;
                break;
            }
        }
        return foundId;
    }

    /**
     * Scrie in log un mesaj cu info daca log e enabled.
     *
     * @param message mesaj
     */
    private void info(String message) {
        if (customPoolProperties.getLog()) {
            logger.info(message);
        }
    }

    /**
     * Scrie in log o eroare daca log e enabled
     *
     * @param error eroare
     */
    private void error(String error) {
        if (customPoolProperties.getLog()) {
            logger.error(error);
        }
    }

    /**
     * Scrie in log un throwable daca log e enabled.
     *
     * @param th throwable
     */
    private void error(Throwable th) {
        if (customPoolProperties.getLog()) {
            logger.error(LoggerUtil.printException(th));
        }
    }

    public CustomPoolProperties getCustomPoolProperties() {
        return customPoolProperties;
    }

    public void setCustomPoolProperties(CustomPoolProperties customPoolProperties) {
        this.customPoolProperties = customPoolProperties;
    }
}
