package com.controller;

import java.io.IOException;
import java.net.URLEncoder;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.bean.User;
import com.dao.UserDao;
import com.util.SecurityUtil;

@WebServlet("/UserRegistrationController")
public class UserRegistrationController extends HttpServlet {
	private static final long serialVersionUID = 1L;

    public UserRegistrationController() {
        super();
    }

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.sendRedirect("UserRegister.jsp");
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");

		String name = clean(request.getParameter("name"));
		String address = clean(request.getParameter("address"));
		String mobile_no = clean(request.getParameter("mobile_no"));
		String email = clean(request.getParameter("email"));
		String password = clean(request.getParameter("password"));
		String vehicle_no = clean(request.getParameter("vehicle_no"));
		String vehicle_name = clean(request.getParameter("vehicle_name"));
		String vehicle_owner_name = clean(request.getParameter("vehicle_owner_name"));

		if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
			request.setAttribute("error", "Name, email, and password are required.");
			request.getRequestDispatcher("UserRegister.jsp").forward(request, response);
			return;
		}

		UserDao dao = new UserDao();

		try {
			if (dao.alreadyUser(email)) {
				request.setAttribute("error", "An account with that email already exists. Please log in.");
				request.getRequestDispatcher("UserLogin.jsp").forward(request, response);
				return;
			}

			// Enterprise Security: Hash password using PBKDF2 before storing in database
			String secureHash = SecurityUtil.hashPassword(password);

			User bean = new User();
			bean.setName(name);
			bean.setAddress(address);
			bean.setEmail(email);
			bean.setMobile_no(mobile_no);
			bean.setPassword(secureHash);
			bean.setVehicle_no(vehicle_no);
			bean.setVehicle_name(vehicle_name);
			bean.setVehicle_owner_name(vehicle_owner_name);

			if (dao.InsertUserData(bean)) {
				response.sendRedirect("UserLogin.jsp?msg=" + URLEncoder.encode("Registration successful! Please log in.", "UTF-8"));
			} else {
				request.setAttribute("error", "Registration failed. Please check your inputs and try again.");
				request.getRequestDispatcher("UserRegister.jsp").forward(request, response);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			request.setAttribute("error", "Database error occurred during registration.");
			request.getRequestDispatcher("UserRegister.jsp").forward(request, response);
		}
	}

	private static String clean(String v) {
		return v == null ? "" : v.trim();
	}
}