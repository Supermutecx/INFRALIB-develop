package ro.infrasoft.infralib.security;

/**
 * Tine algoritmi pentru signer.
 */
enum SignerAlgorithm {

    SHA1_WITH_DSA("SHA1withDSA");

    SignerAlgorithm(String name) {
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
