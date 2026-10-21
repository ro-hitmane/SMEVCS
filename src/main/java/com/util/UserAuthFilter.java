package com.util;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Blocks every user page and user-only servlet unless the session holds a
 * logged-in user (set by UserLoginController). Public pages such as index.jsp,
 * UserLogin.jsp, UserRegister.jsp and the login/registration controllers are
 * NOT listed here.
 */
@WebFilter(urlPatterns = {
    // User JSP pages
    "/UserHome.jsp",
    "/Search.jsp",
    "/SearchSlot.jsp",
    "/MyBookings.jsp",
    "/nav_user.jsp",
    // User-only controllers
    "/PayController",
    "/SearchSlotcontroller",
    "/PriceQuote"
})
public class UserAuthFilter implements Filter {

    @Override
    public void init(FilterConfig fConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        // Never create a session just to check; no session means not logged in.
        HttpSession session = req.getSession(false);
        Object email = (session != null) ? session.getAttribute("email") : null;
        Object role = (session != null) ? session.getAttribute("role") : null;

        boolean isUser = email instanceof String
                && !((String) email).trim().isEmpty()
                && "USER".equals(role);

        if (isUser) {
            chain.doFilter(request, response);
            return;
        }

        res.sendRedirect(req.getContextPath() + "/UserLogin.jsp");
    }

    @Override
    public void destroy() {
    }
}