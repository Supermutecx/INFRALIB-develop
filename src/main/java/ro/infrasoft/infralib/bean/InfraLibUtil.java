package ro.infrasoft.infralib.bean;

import org.joda.time.DateTime;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

/**
 * Un repositor de functii utile.
 */
@SuppressWarnings({"NumericCastThatLosesPrecision", "CharUsedInArithmeticContext", "MethodCanBeVariableArityMethod"})
public class InfraLibUtil {
    private boolean debug;

    /**
     * NVL din oracle, transforma un string input in instead daca input e null.
     *
     * @param input   string de input
     * @param instead string de instead daca input e null
     * @return unul dintre cele doua string-uri
     */
    public String nvl(String input, String instead) {
        if (input == null)
            input = instead;

        return input;
    }

    /**
     * Concateneaza 2 array-uri.
     *
     * @param first  primul array
     * @param second al doilea
     * @param <T>    parametru de tip
     * @return array concatenat
     */
    public <T> T[] concatArrays(T[] first, T[] second) {
        T[] result = Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }


    /**
     * Versiune de nvl pentru care instead e "".
     *
     * @param input string de input
     * @return string de input sau ""
     */
    public String nvl(String input) {
        return nvl(input, "");
    }

    /**
     * Verifica un text pentru sql injection.
     *
     * @param text text de verificat
     * @return daca are sau nu sql injection
     */
    public boolean hasInjection(String text) {
        String textToCheck = nvl(text);
        return textToCheck.contains("--") || textToCheck.contains("'");
    }

    /**
     * Aplica SHA2 peste text.
     *
     * @param text text de facut sha2
     * @return valoarea sha2
     * @throws NoSuchAlgorithmException     Datorita shat2
     * @throws UnsupportedEncodingException Datorita sha2
     */
    public String sha2(String text) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MessageDigest messageDigest;
        messageDigest = MessageDigest.getInstance("SHA-256");
        byte[] sha2;
        messageDigest.update(text.getBytes("iso-8859-1"), 0, text.length());
        sha2 = messageDigest.digest();
        return bytesToHex(sha2);
    }

    /**
     * Transforma bytes in hexa.
     *
     * @param bytes bytes de convertit
     * @return valoarea hexa a bytes
     */
    private String bytesToHex(byte... bytes) {
        StringBuilder builder = new StringBuilder();
        for (byte aData : bytes) {
            int halfbyte = (aData >>> 4) & 0x0F;
            int two_halfs = 0;
            do {
                two_halfs++;
                if ((halfbyte >= 0) && (halfbyte <= 9))
                    builder.append((char) ('0' + halfbyte));
                else
                    builder.append((char) ('a' + (halfbyte - 10)));
                halfbyte = aData & 0x0F;
            } while (two_halfs < 1);
            two_halfs++;
        }
        return builder.toString();
    }

    /**
     * Capitalizeaza prima litera a fiecarui cuvant.
     *
     * @param input string de input
     * @return string de input cu primele litere din cuvinte capitalizate
     */
    public String capitalizeFirstLetters(String input) {

        for (int i = 0; i < input.length(); i++) {

            if (i == 0) {
                input = String.format("%s%s",
                        Character.toUpperCase(input.charAt(0)),
                        input.substring(1));
            }

            if (!Character.isLetterOrDigit(input.charAt(i))) {
                if (i + 1 < input.length()) {
                    input = String.format("%s%s%s",
                            input.subSequence(0, i + 1),
                            Character.toUpperCase(input.charAt(i + 1)),
                            input.substring(i + 2));
                }
            }
        }

        return input;
    }

    /**
     * Face forward.
     *
     * @param page     pagina
     * @param request  variabila de request
     * @param response variabila de response
     */
    public void forward(String page, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.getRequestDispatcher(page).forward(request, response);
    }

    /**
     * Parseaza o data selectata direct ca string din baza de date si o intoarce ca un obiect date.
     *
     * @param directDate data din baza de date ca string
     * @return un obiect DateTime
     * @throws ParseException SimpleDateFormat arunca aceasta exceptie
     */
    public DateTime parseDirectDate(String directDate) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd H:m:s");
        return new DateTime(sdf.parse(directDate));
    }

    public DateTime parseDirectDateSqls(String directDate) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return new DateTime(sdf.parse(directDate));
    }

    public DateTime parseDirectDateMysql(String directDate) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return new DateTime(sdf.parse(directDate));
    }

    /**
     * Primeste un obiect DateTime si il intoarce dupa un anumit format.
     *
     * @param dateTime obiectul DateTime
     * @return valoarea string a obiectului
     * @throws ParseException SimpleDateFormat arunca aceasta exceptie
     */
    public String parseDateTime(DateTime dateTime, String format) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(dateTime.toDate());
    }

    /**
     * Primeste un obiect Date si il intoarce dupa un anumit format.
     *
     * @param date obiectul Date
     * @return valoarea string a obiectului
     * @throws ParseException SimpleDateFormat arunca aceasta exceptie
     */
    public String parseDate(Date date, String format) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(date);
    }

    /**
     * Transoforma o data din baza de date in format dd.mm.yyyy.
     *
     * @param directDate data din baza de date
     * @return data in format dd.mm.yyyy
     * @throws ParseException SimpleDateFormat arunca aceasta exceptie
     */
    public String transformDirectDate(String directDate) throws ParseException {
        return parseDateTime(parseDirectDate(directDate), "dd.MM.yyyy");
    }

    public String transformDirectDateSqls(String directDate) throws ParseException {
        return parseDateTime(parseDirectDateSqls(directDate), "dd.MM.yyyy");
    }

    public String transformDirectDateMysql(String directDate) throws ParseException {
        return parseDateTime(parseDirectDateMysql(directDate), "dd.MM.yyyy");
    }

    /**
     * Generates a random string of characters.
     *
     * @param numberOfChars length of the string
     * @return the string
     */
    public String genRandomString(int numberOfChars) {
        final String[] source = {"a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z"};
        StringBuilder randStr = new StringBuilder();

        for (int i = 0; i <= numberOfChars; i++)
            randStr.append(source[(int) Math.floor(Math.random() * source.length)]);

        return randStr.toString();
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public X509Certificate extractCertificate(HttpServletRequest req) {
        X509Certificate[] certs = (X509Certificate[]) req.getAttribute("javax.servlet.request.X509Certificate");
        if (null != certs && certs.length > 0) {
            return certs[0];
        }
        return null;
    }
}
