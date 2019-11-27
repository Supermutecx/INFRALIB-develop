package ro.infrasoft.infralib.security;

import org.apache.log4j.Logger;
import ro.infrasoft.infralib.logger.LoggerUtil;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;

/**
 * Clasa care se ocupa cu semnaturi digitale.
 */
public class Signer {
    private static Logger logger = LoggerUtil.getLogger("signer");

    /**
     * Semneaza file cu privateKey initializat cu algoritmul algorithm si cu providerul provider.
     *
     * @return semnatura in bytes
     * @throws Throwable
     */
    public static byte[] signFile(File file, PrivateKey privateKey, String algorithm, String provider) throws Throwable {
        byte[] realSignature = null;
        FileInputStream fis = null;

        try {
            if (file == null)
                throw new Exception("Cannot sign a null file.");

            if (privateKey == null)
                throw new Exception("Cannot sign with a null private key.");

            if (algorithm == null)
                throw new Exception("Cannot sign with a null algorithm.");

            if (provider == null)
                throw new Exception("Cannot sign with a null provider.");


            logger.info("Attempt to sign the file: " + file.getAbsolutePath().toString() + " with the algorithm: " + algorithm);

            // creaza si initializeaza obiectul de semantura
            Signature signature = Signature.getInstance(algorithm, provider);
            signature.initSign(privateKey);

            // citeste fisierul si genereaza semnatura pentru el
            fis = new FileInputStream(file);
            BufferedInputStream bufin = new BufferedInputStream(fis);
            byte[] buffer = new byte[1024];
            int len;
            while ((len = bufin.read(buffer)) >= 0) {
                signature.update(buffer, 0, len);
            }
            bufin.close();
            realSignature = signature.sign();

        } catch (Throwable t) {
            String message = t.getMessage();
            if (message == null)
                message = "null pointer exception";

            logger.error(message);
            throw (t);
        } finally {
            if (fis != null)
                fis.close();
        }
        return realSignature;
    }

    /**
     * Verifica file cu semnatura signatureBytes si cu cheia publica publickKey conform algoritmului algorithm si provicerului provider.
     *
     * @return boolean daca s-a verificat sau nu
     * @throws Throwable
     */
    public static boolean verifyFile(File file, byte[] signatureBytes, PublicKey publicKeyKey, String algorithm, String provider) throws Throwable {
        boolean valid = false;
        FileInputStream datafis = null;

        try {
            if (file == null)
                throw new Exception("Cannot verify a null file.");

            if (signatureBytes == null || signatureBytes.length == 0)
                throw new Exception("Cannot verify a null or empty signature.");

            if (publicKeyKey == null)
                throw new Exception("Cannot verify with a null private key.");

            if (algorithm == null)
                throw new Exception("Cannot verify with a null algorithm.");

            if (provider == null)
                throw new Exception("Cannot verify with a null provider.");


            logger.info("Attempt to verify the file: " + file.getAbsolutePath().toString() + " with the algorithm: " + algorithm);

            // creaza si initializeaza obiectul de semantura
            Signature signature = Signature.getInstance(algorithm, provider);
            signature.initVerify(publicKeyKey);

            // citeste fisierul si verifica
            datafis = new FileInputStream(file);
            BufferedInputStream bufin = new BufferedInputStream(datafis);

            byte[] buffer = new byte[1024];
            int len;
            while (bufin.available() != 0) {
                len = bufin.read(buffer);
                signature.update(buffer, 0, len);
            }
            bufin.close();

            valid = signature.verify(signatureBytes);
        } catch (Throwable t) {
            String message = t.getMessage();
            if (message == null)
                message = "null pointer exception";

            logger.error(message);
            throw (t);
        } finally {
            if (datafis != null)
                datafis.close();
        }
        return valid;
    }

    /**
     * Genereaza o pereche public key / private key bazata pe algorimul keypairGeneratorAlgorithm/secureRandomAlgorithm cu providerul provider de lungime numBytes.
     *
     * @return o pereche cheie publica / cheie privata
     * @throws Throwable
     */
    public static KeyPair generateKeyPair(String keypairGeneratorAlgorithm, String secureRandomAlgorithm, String provider, Integer numBytes) throws Throwable {
        KeyPair keyPair = null;

        if (keypairGeneratorAlgorithm == null)
            throw new Exception("Cannot generate with a null algorithm.");

        if (secureRandomAlgorithm == null)
            throw new Exception("Cannot generate with a null algorithm.");

        if (provider == null)
            throw new Exception("Cannot generate with a null provider.");

        if (numBytes == null || numBytes.equals(0))
            throw new Exception("Cannot generate with null or 0 num bytes keys.");

        try {
            //initializeaza keygen
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance(keypairGeneratorAlgorithm, provider);

            // initializeaza secure random
            SecureRandom random = SecureRandom.getInstance(secureRandomAlgorithm, provider);
            keyGen.initialize(numBytes, random);

            // genereaza keypair
            keyPair = keyGen.generateKeyPair();
        } catch (Throwable t) {
            String message = t.getMessage();
            if (message == null)
                message = "null pointer exception";

            logger.error(message);
            throw (t);
        }

        return keyPair;
    }

    /**
     * Salveaza cheia publica publickKey in fisierul file.
     *
     * @return fisierul
     */
    public static File savePublicKeyInFile(PublicKey publicKey, File file) throws Throwable {
        FileOutputStream keyfos = null;
        try {
            if (file == null)
                throw new Exception("Cannot save to a null file.");

            //get encoded
            byte[] key = publicKey.getEncoded();

            // create output stream for file
            keyfos = new FileOutputStream(file);

            //write data
            keyfos.write(key);
            keyfos.close();
        } catch (Throwable t) {
            String message = t.getMessage();
            if (message == null)
                message = "null pointer exception";

            logger.error(message);
            throw (t);
        } finally {
            if (keyfos != null)
                keyfos.close();
        }

        return file;
    }

    /**
     * Salveaza cheia privata privateKey in fisierul file.
     *
     * @return fisierul
     */
    public static File savePrivateKeyInFile(PrivateKey privateKey, File file) throws Throwable {
        FileOutputStream keyfos = null;
        try {
            if (file == null)
                throw new Exception("Cannot save to a null file.");

            //get encoded
            byte[] key = privateKey.getEncoded();

            // create output stream for file
            keyfos = new FileOutputStream(file);

            //write data
            keyfos.write(key);
            keyfos.close();
        } catch (Throwable t) {
            String message = t.getMessage();
            if (message == null)
                message = "null pointer exception";

            logger.error(message);
            throw (t);
        } finally {
            if (keyfos != null)
                keyfos.close();
        }

        return file;
    }

    /**
     * Citeste o cheie publica din fisierul file pentru providerul provider si algoritmul algorithm.
     *
     * @return cheia publica
     * @throws Throwable
     */
    public static PublicKey readPublicKeyFromFile(File file, String algorithm, String provider) throws Throwable {
        PublicKey publicKey = null;
        FileInputStream keyfis = null;
        try {
            if (file == null)
                throw new Exception("Cannot read to a null file.");

            if (algorithm == null)
                throw new Exception("Cannot read with a null algorithm.");

            if (provider == null)
                throw new Exception("Cannot read with a null provider.");

            // read key bytes
            keyfis = new FileInputStream(file);
            byte[] encKey = new byte[keyfis.available()];
            keyfis.read(encKey);
            keyfis.close();

             /*
              * Determina public key in functie de algoritm si provider.
              * Daca nu e ceva supported, atunci arunca exceptie.
              */

            if (provider.equals(KeypairGeneratorAlgorithm.DSA.getName())) {
                X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(encKey);
                KeyFactory keyFactory = KeyFactory.getInstance(algorithm, provider);
                publicKey = keyFactory.generatePublic(pubKeySpec);
            } else {
                throw new Exception("Algorithm not supported: " + algorithm);
            }

        } catch (Throwable t) {
            String message = t.getMessage();
            if (message == null)
                message = "null pointer exception";

            logger.error(message);
            throw (t);
        } finally {
            if (keyfis != null)
                keyfis.close();
        }
        return publicKey;
    }

    /**
     * Citeste o cheie privata din fisierul file pentru providerul provider si algoritmul algorithm.
     *
     * @return cheia privata
     * @throws Throwable
     */
    public static PrivateKey readPrivateKeyFromFile(File file, String algorithm, String provider) throws Throwable {
        PrivateKey privateKey = null;
        FileInputStream keyfis = null;
        try {
            if (file == null)
                throw new Exception("Cannot read to a null file.");

            if (algorithm == null)
                throw new Exception("Cannot read with a null algorithm.");

            if (provider == null)
                throw new Exception("Cannot read with a null provider.");

            // read key bytes
            keyfis = new FileInputStream(file);
            byte[] encKey = new byte[keyfis.available()];
            keyfis.read(encKey);
            keyfis.close();

             /*
              * Determina private key in functie de algoritm si provider.
              * Daca nu e ceva supported, atunci arunca exceptie.
              */

            if (provider.equals(KeypairGeneratorAlgorithm.DSA.getName())) {
                X509EncodedKeySpec privKeySpec = new X509EncodedKeySpec(encKey);
                KeyFactory keyFactory = KeyFactory.getInstance(algorithm, provider);
                privateKey = keyFactory.generatePrivate(privKeySpec);
            } else {
                throw new Exception("Algorithm not supported: " + algorithm);
            }

        } catch (Throwable t) {
            String message = t.getMessage();
            if (message == null)
                message = "null pointer exception";

            logger.error(message);
            throw (t);
        } finally {
            if (keyfis != null)
                keyfis.close();
        }
        return privateKey;
    }
}
