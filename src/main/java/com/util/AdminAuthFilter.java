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
 * Blocks every admin page and admin-only servlet unless the session holds a
 * logged-in admin (set by AdminLoginController). AdminLogin.jsp and
 * AdminLoginController are NOT listed here, so the login flow stays public.
 */
@WebFilter(urlPatterns = {
    // Admin JSP pages
    "/AdminHome.jsp",
    "/Stations.jsp",
    "/ManageStation.jsp",
    "/ManageSlots.jsp",
    "/ViewSlots.jsp",
    "/StationBookings.jsp",
    "/ViewQuery.jsp",
    "/nav_admin.jsp",
    // Admin-only controllers
    "/CreateStationController",
    "/ManageSlotsController",
    "/UpdateSlotController",
    "/DeleteSlotController",
    "/ResolvequeryController"
})
public class AdminAuthFilter implements Filter {

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
        Object admin = (session != null) ? session.getAttribute("admin") : null;
        Object role = (session != null) ? session.getAttribute("role") : null;

        boolean isAdmin = admin instanceof String
                && !((String) admin).trim().isEmpty()
                && "ADMIN".equals(role);

        if (isAdmin) {
            chain.doFilter(request, response);
            return;
        }

        res.sendRedirect(req.getContextPath() + "/AdminLogin.jsp");
    }

    @Override
    public void destroy() {
    }
}