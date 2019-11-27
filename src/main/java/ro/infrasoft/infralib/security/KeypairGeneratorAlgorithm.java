package ro.infrasoft.infralib.security;

/**
 * Tine algoritmi pentru keypair generator.
 */
enum KeypairGeneratorAlgorithm {

    DSA("DSA");

    KeypairGeneratorAlgorithm(String name) {
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
