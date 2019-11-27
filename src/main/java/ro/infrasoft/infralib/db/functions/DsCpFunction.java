package ro.infrasoft.infralib.db.functions;

import ro.infrasoft.infralib.db.datasource.JDBCDataSourceCp;

/**
 * Functie care ajuta sa faci ceva pe datasource.
  */
public interface DsCpFunction {

    /**
     * Apply care merge doar pe datasource.
     *
     * @param dataSourceCp data source
     */
    public void apply(JDBCDataSourceCp dataSourceCp) throws Throwable;

    /**
     * Apply care merge pe data source si pe sql.
     *
     * @param dataSourceCp data source
     * @param sql sql
     */
    public void apply(JDBCDataSourceCp dataSourceCp, String sql) throws Throwable;
}
