package com.controller;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.dao.AdminAuthDao;

@WebServlet("/AdminLoginController")
public class AdminLoginController extends HttpServlet {
    private static final long serialVersionUID = 1L;

    public AdminLoginController() {
        super();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String email = request.getParameter("email");
        String password = request.getParameter("password");

        // 1. Strict Input Validation
        if (email == null || email.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            request.setAttribute("error", "Email and password are required.");
            request.getRequestDispatcher("AdminLogin.jsp").forward(request, response);
            return;
        }

        // 2. Database Authentication
        AdminAuthDao authDao = new AdminAuthDao();
        AdminAuthDao.AuthResult authResult = authDao.authenticateAdmin(email.trim(), password);

        if (authResult.success) {
            // 3. OWASP Session Fixation Prevention
            HttpSession oldSession = request.getSession(false);
            if (oldSession != null) {
                oldSession.invalidate();
            }

            // 4. Generate new secure session
            HttpSession newSession = request.getSession(true);
            newSession.setMaxInactiveInterval(1800); // 30 mins

            // 5. Populate safe session attributes
            newSession.setAttribute("admin", authResult.adminEmail);
            newSession.setAttribute("role", "ADMIN");

            // Success: Redirect to Admin Dashboard
            response.sendRedirect("AdminHome.jsp");
        } else {
            // Failure: Send the error message back to the login page securely
            request.setAttribute("error", authResult.errorMessage);
            request.getRequestDispatcher("AdminLogin.jsp").forward(request, response);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.sendRedirect("AdminLogin.jsp");
    }
}