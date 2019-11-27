package ro.infrasoft.infralib.bean;

import ro.infrasoft.infralib.bean.help.Order;
import ro.infrasoft.infralib.db.datasource.BaseDataSource;
import ro.infrasoft.infralib.db.functions.RsmdMapFunction;
import ro.infrasoft.infralib.db.result.Result;
import ro.infrasoft.infralib.db.result.Row;
import ro.infrasoft.infralib.db.type.DbType;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Bean care are toate lucrurile necesare pentru lucrul cu o singura
 * tablea: crud, paginatie, filtrare, ordonare.
 */
public class InfraLibNomenc {
    protected String tableName;
    protected boolean paginated;
    protected boolean caseSensitiveFilter;
    protected boolean trimFilter;
    protected int page;
    protected int numPerPage;
    protected boolean forceFromTo;
    protected int forceFrom;
    protected int forceTo;
    protected String pk;
    protected HashMap<String, String> searchValues = new HashMap<String, String>();
    protected Set<String> dateSearchColumns = new HashSet<String>();
    protected HashMap<String, String> conditions = new HashMap<String, String>();
    protected HashSet<Order> orders = new HashSet<Order>();
    protected String sql;
    protected String sqlOne;
    protected String sqlCount;
    protected String sqlNoPage;
    protected String sqlBase;
    protected String defaultOrderColumn;
    protected String defaultOrderDir;
    protected String selectColumnList;
    protected String ids;
    protected BaseDataSource db;
    protected Boolean useManualSql = false;
    protected String manuaSql;
    protected Map<String, Integer> info;
    protected boolean noCount = false;
    protected String cleanSql = null;
    protected boolean distinct = false;
    protected String distinctKey = null;

    /**
     * Constructor default.
     */
    public InfraLibNomenc() {
        this(null, 20, 1);
    }

    /**
     * Constructor principal care primeste toti parametrii.
     *
     * @param tableName    Numele tabelei
     * @param searchValues Valori pentru cautare
     * @param numPerPage   Numar de inregistrari per pagina
     * @param page         Pagina curenta
     * @param conditions   Conditii de filtrare
     * @param orders       conditii de ordonare
     */
    public InfraLibNomenc(String tableName, int numPerPage, int page, HashMap<String, String> searchValues, HashMap<String, String> conditions, HashSet<Order> orders) {
        this.tableName = tableName;
        this.numPerPage = numPerPage;
        this.page = page;
        this.searchValues = searchValues;
        this.conditions = conditions;
        this.orders = orders;
    }

    /**
     * Constructor principal care primeste informatiile de paginare.
     *
     * @param tableName  Numele tabelei
     * @param numPerPage Numar de inregistrari per pagina
     * @param page       Pagina curenta
     */
    public InfraLibNomenc(String tableName, int numPerPage, int page) {
        this(tableName, numPerPage, page, new HashMap<String, String>(), new HashMap<String, String>(), new HashSet<Order>());
    }

    /**
     * Constructor care primeste doar numele tabelei.
     *
     * @param tableName numele tabelei
     */
    public InfraLibNomenc(String tableName) {
        this(tableName, 20, 1);
    }

    /**
     * Metoda care adauga o conditie pentru acest nomenclator.
     * <p/>
     * <p>
     * Exemplu <b>id_unitate > 0</b>
     * </p>
     *
     * @param condition Un string care reprezinta o conditie pentru nomenclator
     * @param key       Cheia sub care sa puna aceasta conditie
     * @return aceeasi instanta pentru chaining
     */
    public InfraLibNomenc addCondition(String key, String condition) {
        if (conditions.containsKey(key))
            conditions.remove(key);

        conditions.put(key, condition.trim());
        return this;
    }

    /**
     * Metoda care sterge o conditie pentru acest nomenclator.
     * Daca sunt scrise la fel (se recomanda lower case) atunci
     * sistemul garanteaza unicitatea conditiilor.
     * <p/>
     * <p>
     * Exemplu <b>id_unitate > 0</b>
     * </p>
     *
     * @param condition Un string care reprezinta o conditie pentru nomenclator
     * @return aceeasi instanta pentru chaining
     */
    public InfraLibNomenc remCondition(String condition) {
        conditions.remove(condition.trim());
        return this;
    }

    /**
     * Metoda care adauga un order by pentru acest nomenclator.
     * Daca sunt scrise la fel (se recomanda lower case) atunci
     * sistemul garanteaza unicitatea acestora.
     * <p/>
     * <p>
     * Exemplu <b>id_unitate desc</b> sau <b> nume </b> sau <b> prenume asc </b>
     * </p>
     *
     * @param order Un obiect care reprezinta o conditie de ordonare pentru nomenclator
     * @return aceeasi instanta pentru chaining
     */
    public InfraLibNomenc addOrder(Order order) {
        orders.add(order);
        return this;
    }

    /**
     * Metoda care adauga o o filtrare pentru acest nomenclator.
     * Daca sunt scrise la fel (se recomanda lower case) atunci
     * sistemul garanteaza unicitatea conditiilor.
     * <p/>
     * <p>
     * Exemplu <b>cod,'xh4'</b>
     * </p>
     *
     * @param coloana Coloana pentru care se va filtra
     * @param valoare Valoarea care se filtreaza
     * @return aceeasi instanta pentru chaining
     */
    public InfraLibNomenc addSearchValue(String coloana, String valoare) {
        if (searchValues.containsKey(coloana))
            searchValues.remove(coloana);

        searchValues.put(coloana, valoare);
        return this;
    }

    /**
     * Metoda care sterge o o filtrare pentru acest nomenclator.
     * Daca sunt scrise la fel (se recomanda lower case) atunci
     * sistemul garanteaza unicitatea conditiilor.
     * <p/>
     * <p>
     * Exemplu <b>cod</b>
     * </p>
     *
     * @param coloana Coloana pentru care se va filtra
     * @return aceeasi instanta pentru chaining
     */
    public InfraLibNomenc remSearchValue(String coloana) {
        searchValues.remove(coloana);
        return this;
    }

    /**
     * Metoda care sterge un order by pentru acest nomenclator.
     * Daca sunt scrise la fel (se recomanda lower case) atunci
     * sistemul garanteaza unicitatea acestora.
     * <p/>
     * <p>
     * Exemplu <b>id_unitate desc</b> sau <b> nume </b> sau <b> prenume asc </b>
     * </p>
     *
     * @param order Un obiect care reprezinta o conditie de ordonare pentru nomenclator
     * @return aceeasi instanta pentru chaining
     */
    public InfraLibNomenc remOrderBy(Order order) {
        orders.add(order);
        return this;
    }

    /**
     * Metoda care seteaza numarul de records per pagina.
     *
     * @param numPerPage Numarul de records per pagina
     * @return aceeasi instanta pentru chaining
     */
    public InfraLibNomenc setNumPerPage(int numPerPage) {
        this.numPerPage = numPerPage;
        return this;
    }

    /**
     * Metoda care seteaza numele tabelei.
     *
     * @param tableName Numele tabelei
     * @return aceeasi instanta pentru chaining
     */
    public InfraLibNomenc setTableName(String tableName) {
        this.tableName = tableName;
        return this;
    }

    public HashMap<String, String> getSearchValues() {
        return searchValues;
    }

    public void setSearchValues(HashMap<String, String> searchValues) {
        this.searchValues = searchValues;
    }

    /**
     * Metoda care seteaza pagina curenta.
     *
     * @param page Pagina curenta
     * @return aceeasi instanta pentru chaining
     */
    public InfraLibNomenc setPage(int page) {
        this.page = page;
        return this;
    }

    /**
     * Metoda care seteaza harta de conditii.
     *
     * @param conditions Harta de conditii
     * @return aceeasi instanta pentru chaining
     */
    public InfraLibNomenc setConditions(HashMap<String, String> conditions) {
        this.conditions = conditions;
        return this;
    }

    /**
     * Metoda care seteaza lista de order by - uri.
     *
     * @param orders Lista de order by - uri
     * @return aceeasi instanta pentru chaining
     */
    public InfraLibNomenc setOrders(HashSet<Order> orders) {
        this.orders = orders;
        return this;
    }

    /**
     * Metoda care genereaza sql-ul din toti parametrii primiti.
     *
     * @return aceeasi instanta pentru chaining
     */
    public InfraLibNomenc generate() {
        String selectTarget = "";
        if (useManualSql)
            selectTarget = "(" + manuaSql + ") a";
        else
            selectTarget = tableName;


        // Luam metadatele pentru tabela respectiva

        try {
            db.sql("select * from " + selectTarget + " where rownum<1",
                    "select top 1 * from " + selectTarget, DbType.SQL_SERVER,
                    "select * from " + selectTarget + " LIMIT 1", DbType.MYSQL).info(new RsmdMapFunction() {
                @Override
                public void apply(HashMap<String, Integer> rsmdMap) throws SQLException {
                    info = rsmdMap;
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }


        StringBuilder sb = new StringBuilder(20);
        sb.append("select * from ")
                .append(selectTarget)
                .append(" where 1=1 ");

        //conditii de filtrare
        for (String condKey : conditions.keySet())
            sb.append(" and ")
                    .append(conditions.get(condKey))
                    .append(' ');


        //cheile de cautare
        if (!searchValues.isEmpty()) {
            sb.append(" and (1=1 ");
            for (String key : searchValues.keySet()) {
                sb.append(" and ");
                String searchKey = key.toUpperCase();
                if (dateSearchColumns.contains(searchKey)) {
                    if (db.getDbType().equals(DbType.SQL_SERVER)){
                        searchKey = " format(" + searchKey + ",'dd.MM.yyyy HH:mi') ";
                    } else if (db.getDbType().equals(DbType.MYSQL)){
                        searchKey = " date_format(" + searchKey + ",'%d.%m.%Y %H:%i') ";
                    } else if (db.getDbType().equals(DbType.ORACLE)){
                        searchKey = " to_char(" + searchKey + ",'dd.mm.yyyy hh24:mi') ";
                    }
                }

                String searchValue = searchValues.get(key);
                if (!caseSensitiveFilter) {
                    searchKey = "upper(" + searchKey + ")";
                    searchValue = searchValue.toUpperCase();
                }

                if (trimFilter) {
                    if(db.getDbType().equals(DbType.ORACLE)) {
                        searchKey = "trim(" + searchKey + ")";
                    } else if(db.getDbType().equals(DbType.SQL_SERVER)) {
                        searchKey = "ltrim(rtrim(" + searchKey + "))";
                    } else if(db.getDbType().equals(DbType.MYSQL)) {
                        searchKey = "trim(" + searchKey + ")";
                    }
                    searchValue = searchValue.toUpperCase().trim();
                }

                sb.append(searchKey)
                        .append(" like '%")
                        .append(searchValue)
                        .append("%' ");
            }
            sb.append(" ) ");
        }

        //salveaza clean sql inainte de paginare
        cleanSql = sb.toString().trim();

        //calculare paginare
        int to = 0;
        int from = 0;
        if (forceFromTo){
            to = forceTo;
            from = forceFrom;
        } else {
            to = page * numPerPage;
            from = to - (numPerPage - 1);
        }

        //builder pt string
        sb = new StringBuilder(20);
        if (db.getDbType().equals(DbType.MYSQL)) {
            sb.append("select ")
                    .append(selectColumnList)
                    .append(" from (select x.*   from ( select *,(@row_number:=@row_number + 1) as nrow from (")
                    .append(cleanSql)
                    .append(") x, (SELECT @ROW_NUMBER := 0) AS T ");
        } else if (db.getDbType().equals(DbType.SQL_SERVER)) {
            sb.append("select top 100 percent ").append(selectColumnList).append(" from (select x.*, ROW_NUMBER() OVER (");
        } else {
            sb.append("select ").append(selectColumnList).append(" from (select x.*, ROW_NUMBER() OVER (");
        }

        //order by
        if (!orders.isEmpty()) {
            sb.append(" order by ");

            int i = 1;
            int size = orders.size();
            for (Order order : orders) {
                boolean doUpper = (info != null && info.get(order.getOrder().toLowerCase()).equals(Types.VARCHAR));

                sb.append(" ");

                if (doUpper)
                    sb.append(" upper(");

                sb.append(order.getOrder());
                if (doUpper)
                    sb.append(") ");

                sb.append(" ")
                        .append(order.getOrderDir());

                if (i < size)
                    sb.append(", ");

                i++;
            }
        } else {
            boolean doUpper = (info != null && info.get(defaultOrderColumn.toLowerCase()).equals(Types.VARCHAR));

            sb.append(" order by ");
            if (doUpper)
                sb.append(" upper(");

            sb.append(defaultOrderColumn);
            if (doUpper)
                sb.append(") ");

            sb.append(" ")
                    .append(defaultOrderDir).append(")");
        }

        if (db.getDbType().equals(DbType.MYSQL)) {
            sb.append(" ) x ) w");

        } else {
            sb.append(") as nrow from (")
                    .append(cleanSql)
                    .append(" ) x ) w ");
        }

        // Gasim sql count inainte de paginare
        //sqlNoPage = sb.toString(); //+ " where nr > 0 "; // TODO - GASTI ALTA SOLUTIE PENTRU SORTARE LA EXPORT, ASTA FACE BUG.
        sqlBase = sb.toString();
        sqlNoPage = sqlBase + " order by nrow ASC ";
        sqlCount = "select count(0) nrow from (" + sqlBase + ") k";

        // Apoi adaugam paginarea la sql daca e cazul
        if (paginated || forceFromTo) {
            sb.append(" where nrow <= ")
                    .append(to)
                    .append(" and nrow >= ")
                    .append(from);
            if (db.getDbType().equals(DbType.MYSQL)) {
                sb.append(" order by ");

                int i = 1;
                int size = orders.size();
                for (Order order : orders) {
                    boolean doUpper = (info != null && info.get(order.getOrder().toLowerCase()).equals(Types.VARCHAR));

                    sb.append(" ");

                    if (doUpper)
                        sb.append(" upper(");

                    sb.append(order.getOrder());
                    if (doUpper)
                        sb.append(") ");

                    sb.append(" ")
                            .append(order.getOrderDir());

                    if (i < size)
                        sb.append(", ");

                    i++;
                }
            }
        } else {
            sb.append(" where nrow >= 0 ");
        }

        //salvare sql
        sql = sb.toString().trim();
        return this;
    }

    public Integer getRemotePageNum(BaseDataSource db, final String remote_id) throws Exception {
        this.db = db;

        generate();

        final Integer[] cnt = {0};
        final Integer[] pageNum = {1};
        db.sql(sqlNoPage).each(new Row() {
            @Override
            public void exec(ResultSet rs) throws Exception {
                String id = rs.getString("id");
                cnt[0] += 1;

                if (id != null && id.equals(remote_id)){
                    pageNum[0] = ((int)Math.floor(cnt[0]/numPerPage))+1;
                }
            }
        });
        return pageNum[0];
    }

    /**
     * Metoda care executa sql-ul si returneaza un obiect de tip {@link Result}
     * pentru a se putea folosi acelasi tip de sintaxa ca si cu lucrul cu bd-ul direct.
     * <p/>
     * Aceasta e versiunea care tine cont de paginare.
     *
     * @param db un obiect bd pentru a se pastra tranzactia pe acesta
     * @return Un obiect Result peste care se poate lucra in mod bd
     * @throws Exception Metoda select poate arunca mai multe exceptii
     */
    public Result select(BaseDataSource db) throws Exception {
        this.db = db;
        //if (sql == null || sql.isEmpty())
        generate();

        return db.sql(sql);
    }


    /**
     * Metoda care executa sql-ul si returneaza un obiect de tip {@link Result}
     * pentru a se putea folosi acelasi tip de sintaxa ca si cu lucrul cu bd-ul direct.
     * <p/>
     * Aceasta e versiunea care NU tine cont de paginare.
     *
     * @param db un obiect bd pentru a se pastra tranzactia pe acesta
     * @return Un obiect Result peste care se poate lucra in mod bd
     * @throws Exception Metoda select poate arunca mai multe exceptii
     */
    public Result selectNoPage(BaseDataSource db) throws Exception {
        this.db = db;
        generate();
        return db.sql(sqlNoPage);
    }

    /**
     * Intoarce numarul de inregistrari pentru nomenc.
     *
     * @param db un obiect bd pentru a se pastra tranzactia pe acesta
     * @return numarul de inregistrari
     * @throws Exception Metoda getCount poate arunca mai multe exceptii
     */
    public Integer getCount(BaseDataSource db) throws Exception {
        this.db = db;
        if (noCount){
            return 0;
        }else {
            generate();
            return Integer.valueOf(db.sql(sqlCount).get().get("nrow"));
        }
    }

    /**
     * Curata order by.
     */
    public void clearOrders() {
        orders.clear();
    }



    /*
    * Metode getter / setter pentru unele obiecte wrapped.
    */

    public int getNumPerPage() {
        return numPerPage;
    }

    public int getPage() {
        return page;
    }

    public HashMap<String, String> getConditions() {
        return conditions;
    }

    public HashSet<Order> getOrders() {
        return orders;
    }

    public String getTableName() {
        return tableName;
    }


    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public String getDefaultOrderColumn() {
        return defaultOrderColumn;
    }

    public void setDefaultOrderColumn(String defaultOrderColumn) {
        this.defaultOrderColumn = defaultOrderColumn;
    }

    public String getDefaultOrderDir() {
        return defaultOrderDir;
    }

    public void setDefaultOrderDir(String defaultOrderDir) {
        this.defaultOrderDir = defaultOrderDir;
    }

    public String getSelectColumnList() {
        return selectColumnList;
    }

    public InfraLibNomenc setSelectColumnList(String selectColumnList) {
        this.selectColumnList = selectColumnList;
        return this;
    }

    public String getSqlCount() {
        return sqlCount;
    }

    public void setSqlCount(String sqlCount) {
        this.sqlCount = sqlCount;
    }

    public String getSqlNoPage() {
        return sqlNoPage;
    }

    public void setSqlNoPage(String sqlNoPage) {
        this.sqlNoPage = sqlNoPage;
    }

    public boolean isPaginated() {
        return paginated;
    }

    public void setPaginated(boolean paginated) {
        this.paginated = paginated;
    }

    public boolean isCaseSensitiveFilter() {
        return caseSensitiveFilter;
    }

    public InfraLibNomenc setCaseSensitiveFilter(boolean caseSensitiveFilter) {
        this.caseSensitiveFilter = caseSensitiveFilter;
        return this;
    }

    public boolean isTrimFilter() {
        return trimFilter;
    }

    public void setTrimFilter(boolean trimFilter) {
        this.trimFilter = trimFilter;
    }

    public String getPk() {
        return pk;
    }

    public void setPk(String pk) {
        this.pk = pk;
    }

    public String getIds() {
        return ids;
    }

    public void setIds(String ids) {
        this.ids = ids;
    }

    public String getSqlOne() {
        return sqlOne;
    }

    public void setSqlOne(String sqlOne) {
        this.sqlOne = sqlOne;
    }

    public String getManuaSql() {
        return manuaSql;
    }

    public void setManuaSql(String manuaSql) {
        this.manuaSql = manuaSql;
    }

    public Boolean getUseManualSql() {
        return useManualSql;
    }

    public void setUseManualSql(Boolean useManualSql) {
        this.useManualSql = useManualSql;
    }

    public BaseDataSource getDb() {
        return db;
    }

    public void setDb(BaseDataSource db) {
        this.db = db;
    }

    public Set<String> getDateSearchColumns() {
        return dateSearchColumns;
    }

    public void setDateSearchColumns(Set<String> dateSearchColumns) {
        this.dateSearchColumns = dateSearchColumns;
    }

    public boolean isNoCount() {
        return noCount;
    }

    public void setNoCount(boolean noCount) {
        this.noCount = noCount;
    }

    public String getCleanSql() {
        return cleanSql;
    }

    public void setCleanSql(String cleanSql) {
        this.cleanSql = cleanSql;
    }

    public boolean isDistinct() {
        return distinct;
    }

    public void setDistinct(boolean distinct) {
        this.distinct = distinct;
    }

    public String getDistinctKey() {
        return distinctKey;
    }

    public void setDistinctKey(String distinctKey) {
        this.distinctKey = distinctKey;
    }

    public boolean isForceFromTo() {
        return forceFromTo;
    }

    public void setForceFromTo(boolean forceFromTo) {
        this.forceFromTo = forceFromTo;
    }

    public int getForceFrom() {
        return forceFrom;
    }

    public void setForceFrom(int forceFrom) {
        this.forceFrom = forceFrom;
    }

    public int getForceTo() {
        return forceTo;
    }

    public void setForceTo(int forceTo) {
        this.forceTo = forceTo;
    }
}


