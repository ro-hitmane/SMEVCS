package com.util;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletResponse;

// The @WebFilter annotation automatically applies this to every URL in the project
@WebFilter("/*")
public class SecurityFilter implements Filter {

    public void init(FilterConfig fConfig) throws ServletException {
        // Initialization code if needed
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // 1. Defeat the "Back Button" Exploit (Vulnerability #2)
        // Forces the browser to delete the page from history the moment they leave or log out.
        httpResponse.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        httpResponse.setHeader("Pragma", "no-cache");
        httpResponse.setDateHeader("Expires", 0);

        // 2. Prevent Clickjacking (X-Frame-Options)
        // Stops malicious websites from embedding your Plugy platform inside a hidden iframe.
        httpResponse.setHeader("X-Frame-Options", "DENY");

        // 3. Prevent MIME-Sniffing & Enforce Strict XSS blocking
        httpResponse.setHeader("X-Content-Type-Options", "nosniff");
        httpResponse.setHeader("X-XSS-Protection", "1; mode=block");

        // Pass the request down the chain to the actual JSP or Servlet
        chain.doFilter(request, response);
    }

    public void destroy() {
        // Cleanup code if needed
    }
}