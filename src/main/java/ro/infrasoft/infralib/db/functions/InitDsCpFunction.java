package ro.infrasoft.infralib.db.functions;

import ro.infrasoft.infralib.db.datasource.JDBCDataSourceCp;

/**
 * Adapter special pt init.
 */
public abstract class InitDsCpFunction implements DsCpFunction {
    @Override
    public void apply(JDBCDataSourceCp dataSourceCp, String sql) throws Throwable {
        throw new Exception("Not implemented.");
    }
}
