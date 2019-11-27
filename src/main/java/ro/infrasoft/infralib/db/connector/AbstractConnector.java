package ro.infrasoft.infralib.db.connector;

import ro.infrasoft.infralib.db.type.DbType;

/**
 * Interfata care specifica ce trebuie sa contina un conector
 * pentru a putea specifica setarile unei baze de date.
 */
public interface AbstractConnector {

    /**
     * Intoarce clasa de driver ca sa se poata incarca in memorie.
     *
     * @return String-ul care reprezinta clasa de driver
     */
    String driver();

    /**
     * Intoarce url-ul complet de conectare.
     *
     * @return URL-ul de conectare
     */
    String url();

    /**
     * Intoarce numele de utilizator pentru a se loga la baza de date.
     *
     * @return Numele de utilizator
     */
    String user();

    /**
     * Intoarce parola la baza de date pentru utilizatorul intors prin {@link #user}
     *
     * @return Parola utilizatorului
     */
    String password();

    /**
     * Intoarce db type-ul.
     *
     * @return db type
     */
    DbType dbType();

    /**
     * Intoarce schema name.
     *
     * @return schema name
     */
    String schemaName();
}
