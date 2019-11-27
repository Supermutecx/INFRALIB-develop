package ro.infrasoft.infralib.db.transaction;

import ro.infrasoft.infralib.db.datasource.BaseDataSource;

/**
 * Interfata ce foloseste loan pattern pentru a face tranzactii in bd.
 */
public interface Transaction {

    /**
     * Aceasta metoda va contine cod care va primi un {@link BaseDataSource}
     * si cu el va face operatiuni in bd. Metoda nu se va chema direct, in schimb,
     * va fi chemata intr-un try/catch de catre metoda {@link BaseDataSource#transact(Transaction)}.
     *
     * @param ds Datasource-ul peste care se va face tranzactia
     * @throws Exception In interiorul lui <b>exec</b> pot fi mai multe exceptii
     */
    void exec(BaseDataSource ds) throws Exception;
}
