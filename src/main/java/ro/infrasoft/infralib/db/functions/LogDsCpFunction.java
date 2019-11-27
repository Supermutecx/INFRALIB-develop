package ro.infrasoft.infralib.db.functions;

import ro.infrasoft.infralib.db.datasource.JDBCDataSourceCp;

/**
 * Adapter special pt log.
 */
public abstract class LogDsCpFunction implements DsCpFunction {
    @Override
    public void apply(JDBCDataSourceCp dataSourceCp) throws Throwable {
        throw new Exception("Not implemented.");
    }
}
