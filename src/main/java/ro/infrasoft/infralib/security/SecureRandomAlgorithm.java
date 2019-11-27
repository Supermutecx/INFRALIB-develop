package ro.infrasoft.infralib.security;

/**
 * Tine algoritmi pentru secure random.
 */
enum SecureRandomAlgorithm {

    SHA1_PRNG("SHA1PRNG");

    SecureRandomAlgorithm(String name) {
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
