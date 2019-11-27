package ro.infrasoft.infralib.filter;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Filtru care seteaza cache expires la browser.
 */
public class ExpiresCacheFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        // Daca se poate, seteaza header-ul de expires in viitor departat
        if (response instanceof HttpServletResponse && response != null) {
            HttpServletResponse httpRespose = (HttpServletResponse) response;
            httpRespose.setDateHeader("Expires", System.currentTimeMillis() + 11352960000000L);
        }

        chain.doFilter(request, response);
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void destroy() {
    }
}
