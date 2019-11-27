package ro.infrasoft.infralib.email.account;

/**
 * Defineste credentialele pentru un cont de mail.
 */
public abstract class AbstractEmailAccount {
    public static final int DEFAULT_EMAIL_PORT = 25;

    private String host;
    private String from;
    private String fromName;
    private int port;
    private boolean auth;
    private String username;
    private String password;
    private boolean ssl;
    private boolean starttls;
    private boolean starttlsRequired;
    private boolean ntlm;
    private String ntlmDomain;
    private boolean debug;

    //getter si setter

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public boolean isAuth() {
        return auth;
    }

    public void setAuth(boolean auth) {
        this.auth = auth;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isSsl() {
        return ssl;
    }

    public void setSsl(boolean ssl) {
        this.ssl = ssl;
    }

    public String getFromName() {
        return fromName;
    }

    public void setFromName(String fromName) {
        this.fromName = fromName;
    }

    public boolean isNtlm() {
        return ntlm;
    }

    public void setNtlm(boolean ntlm) {
        this.ntlm = ntlm;
    }

    public String getNtlmDomain() {
        return ntlmDomain;
    }

    public void setNtlmDomain(String ntlmDomain) {
        this.ntlmDomain = ntlmDomain;
    }

    public boolean isStarttls() {
        return starttls;
    }

    public void setStarttls(boolean starttls) {
        this.starttls = starttls;
    }

    public boolean isStarttlsRequired() {
        return starttlsRequired;
    }

    public void setStarttlsRequired(boolean starttlsRequired) {
        this.starttlsRequired = starttlsRequired;
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }
}
