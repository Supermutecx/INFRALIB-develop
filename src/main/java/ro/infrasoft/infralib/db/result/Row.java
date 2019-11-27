package ro.infrasoft.infralib.db.result;

import java.sql.ResultSet;

/**
 * Interfata care reprezinta un rand dintr-un select.
 * Scopul ei principal este sa fie folosita pentru a determina ce se intampla cu fiecare rand
 * selectat dintr-o baza de date, prin metoda {@link Result#each} intr-un stil elegant.
 */
public interface Row {

    /**
     * Aceasta metoda va contine cod ce se va executa pentru fiecare rand in parte.
     * Metoda nu va fi chemata direct, ci prin intermediul metodei <b>exec</b>.
     *
     * @param rs ResultSet-ul primit
     * @throws Exception In interiorul lui <b>exec</b> pot fi mai multe exceptii
     */
    void exec(ResultSet rs) throws Exception;
}
