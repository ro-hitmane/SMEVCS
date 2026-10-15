package com.controller;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.dao.UserAuthDao;

@WebServlet("/UserLoginController")
public class UserLoginController extends HttpServlet {
    private static final long serialVersionUID = 1L;

    public UserLoginController() {
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
            request.getRequestDispatcher("UserLogin.jsp").forward(request, response);
            return;
        }

        // 2. Secure Authentication
        UserAuthDao authDao = new UserAuthDao();
        UserAuthDao.AuthResult authResult = authDao.authenticateUser(email.trim(), password);

        if (authResult.success && authResult.user != null) {
            // 3. Session Fixation Prevention: Obliterate old session
            HttpSession oldSession = request.getSession(false);
            if (oldSession != null) {
                oldSession.invalidate();
            }
            
            // 4. Generate new clean session
            HttpSession newSession = request.getSession(true);
            newSession.setMaxInactiveInterval(1800); // 30 mins
            
            // 5. Populate safe session attributes
            newSession.setAttribute("name", authResult.user.getName());
            newSession.setAttribute("vehicleno", authResult.user.getVehicle_no());
            newSession.setAttribute("email", authResult.user.getEmail());
            newSession.setAttribute("role", "USER");

            // Success: Instantly load the dashboard without any alerts
            response.sendRedirect("UserHome.jsp");
        } else {
            // Failure: Send the error message back to the login page safely
            request.setAttribute("error", authResult.errorMessage);
            request.getRequestDispatcher("UserLogin.jsp").forward(request, response);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.sendRedirect("UserLogin.jsp");
    }
}