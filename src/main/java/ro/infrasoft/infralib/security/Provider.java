package ro.infrasoft.infralib.security;

/**
 * Tine provideri pentru security.
 */
enum Provider {

    SUN("SUN");

    Provider(String name) {
        this.name = name;
    }

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
