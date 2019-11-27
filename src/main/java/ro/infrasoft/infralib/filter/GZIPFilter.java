package ro.infrasoft.infralib.filter;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import javax.ws.rs.core.HttpHeaders;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.zip.GZIPOutputStream;

/**
 * Un filtru gzip pentru comprimarea mesajelor http.
 */
@SuppressWarnings({"MultipleTopLevelClassesInFile", "WeakerAccess"})
public class GZIPFilter implements Filter {

    @Override
    public void doFilter(ServletRequest req, ServletResponse res,
                         FilterChain chain) throws IOException, ServletException {
        if (req instanceof HttpServletRequest) {
            HttpServletRequest request = (HttpServletRequest) req;
            HttpServletResponse response = (HttpServletResponse) res;
            String ae = request.getHeader("accept-encoding");
            if (ae != null && ae.contains("gzip")) {
                GZIPResponseWrapper wrappedResponse = new GZIPResponseWrapper(response);
                chain.doFilter(req, wrappedResponse);
                wrappedResponse.finishResponse();
                return;
            }
            chain.doFilter(req, res);
        }
    }


    @Override
    public void init(FilterConfig filterConfig) {

    }


    @Override
    public void destroy() {

    }
}

/**
 * Stream-ul pentru filtru.
 */
@SuppressWarnings({"ClassNameDiffersFromFileName", "MultipleTopLevelClassesInFile", "NumericCastThatLosesPrecision", "IOResourceOpenedButNotSafelyClosed", "UnnecessaryExplicitNumericCast"})
class GZIPResponseStream extends ServletOutputStream {
    private ByteArrayOutputStream baos;
    private GZIPOutputStream gzipstream;
    private boolean closed;
    private HttpServletResponse response;
    private ServletOutputStream output;

    @Override
    public void setWriteListener(WriteListener writeListener) {

    }

    @Override
    public boolean isReady() {
        return true;
    }

    /**
     * Constructor care primeste un {@link HttpServletResponse}.
     *
     * @param response Un HttpServletResponse
     * @throws IOException In interiorul constructorului poate fi o exceptie de io
     */
    GZIPResponseStream(HttpServletResponse response) throws IOException {
        closed = false;
        this.response = response;
        output = response.getOutputStream();
        baos = new ByteArrayOutputStream();
        gzipstream = new GZIPOutputStream(baos);
    }

    @Override
    public void close() throws IOException {
        if (closed) {
            throw new IOException("Acest outpur stream a fost deja inchis");
        }
        gzipstream.finish();

        byte[] bytes = baos.toByteArray();


        response.addHeader(HttpHeaders.CONTENT_LENGTH,
                Integer.toString(bytes.length));
        response.addHeader(HttpHeaders.CONTENT_ENCODING, "gzip");
        response.addHeader(HttpHeaders.VARY, HttpHeaders.ACCEPT_ENCODING);
        output.write(bytes);
        output.flush();
        output.close();
        closed = true;
    }

    @Override
    public void flush() throws IOException {
        if (closed) {
            throw new IOException("Nu se poate varsa un output stream inchis");
        }
        gzipstream.flush();
    }

    @Override
    public void write(int b) throws IOException {
        if (closed) {
            throw new IOException("Nu se poate scrie intr-un output stream inchis");
        }
        gzipstream.write((byte) b);
    }

    @Override
    public void write(byte[] b) throws IOException {
        write(b, 0, b.length);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        if (closed) {
            throw new IOException("Nu se poate scrie intr-un output stream inchis");
        }
        gzipstream.write(b, off, len);
    }

    /**
     * Metoda care identifica daca acest stream e inhis.
     *
     * @return Un boolean care specifica daca stream-ul e inchis
     */
    public boolean isClosed() {
        return closed;
    }

    /**
     * Metoda care nu face nimic, dar api-ul are nevoie ca ea sa existe.
     */
    public void reset() {

    }
}

/**
 * Response wrapper-ul pentru filtru.
 */
@SuppressWarnings({"ClassNameDiffersFromFileName", "MultipleTopLevelClassesInFile"})
class GZIPResponseWrapper extends HttpServletResponseWrapper {
    private HttpServletResponse origResponse;
    private ServletOutputStream stream;
    private PrintWriter writer;

    /**
     * Constructor care primeste un {@link HttpServletResponse}.
     *
     * @param response Un obiect HttpServletResponse
     */
    GZIPResponseWrapper(HttpServletResponse response) {
        super(response);
        origResponse = response;
    }

    /**
     * Metoda care creeaza un {@link ServletOutputStream}.
     *
     * @return Un ServletOutputStream
     * @throws IOException In interiorul lui <b>createOutputStream</b> poate fi o exceptie de io
     */
    ServletOutputStream createOutputStream() throws IOException {
        return new GZIPResponseStream(origResponse);
    }

    /**
     * Metoda care inchide resursele si face flush la toate bufferele.
     *
     * @throws IOException In interiorul lui <b>finishResponse</b> poate fi o exceptie de io
     */
    public void finishResponse() throws IOException {
        if (writer != null) {
            writer.close();
        } else {
            if (stream != null) {
                stream.close();
            }
        }
    }

    @Override
    public void flushBuffer() throws IOException {
        stream.flush();
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        if (writer != null) {
            throw new IllegalStateException("getWriter() has already been called!");
        }

        if (stream == null)
            stream = createOutputStream();
        return stream;
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        if (writer != null) {
            return writer;
        }

        if (stream != null) {
            throw new IllegalStateException("getOutputStream() has already been called!");
        }

        stream = createOutputStream();
        writer = new PrintWriter(new OutputStreamWriter(stream, "UTF-8"));
        return writer;
    }

    @Override
    public void setContentLength(int length) {
    }
}

   