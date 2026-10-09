package com.controller;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.bean.User;
import com.dao.UserDao;

@WebServlet("/UserLoginController")
public class UserLoginController extends HttpServlet {
	private static final long serialVersionUID = 1L;

	public UserLoginController() {
		super();
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		response.setContentType("text/html;charset=UTF-8");
		PrintWriter out = response.getWriter();

		String email = request.getParameter("email");
		String password = request.getParameter("password");

		User user = null;
		if (email != null && password != null) {
			user = new UserDao().CheckUser(email.trim(), password);
		}

		boolean valid = user != null
				&& user.getEmail() != null && !user.getEmail().isEmpty()
				&& user.getPassword() != null && !user.getPassword().isEmpty();

		if (valid) {
			HttpSession ss = request.getSession(true);
			ss.setAttribute("name", user.getName());
			ss.setAttribute("vehicleno", user.getVehicle_no());
			ss.setAttribute("email", user.getEmail());

			out.println("<script type=\"text/javascript\">");
			out.println("alert('Login Successfully');");
			out.println("location='UserHome.jsp';");
			out.println("</script>");
		} else {
			out.println("<script type=\"text/javascript\">");
			out.println("alert('Invalid Login Details');");
			out.println("location='UserLogin.jsp';");
			out.println("</script>");
		}
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		// Credentials should not travel in the URL, so GET just returns to the login page.
		response.sendRedirect("UserLogin.jsp");
	}

}