

package com.controller;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.bean.ContactQuery;
import com.dao.ContactDao;

@WebServlet("/ContactController")
public class ContactController extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private static final String EMAIL_REGEX = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		response.setContentType("text/plain");
		PrintWriter out = response.getWriter();

		// Honeypot: real users never fill this hidden field, bots usually do.
		// Reply "OK" so the bot thinks it worked, but store nothing.
		String honeypot = request.getParameter("website");
		if (honeypot != null && !honeypot.trim().isEmpty()) {
			out.print("OK");
			return;
		}

		String name = trim(request.getParameter("name"));
		String email = trim(request.getParameter("email"));
		String subject = trim(request.getParameter("subject"));
		String message = trim(request.getParameter("message"));

		if (name.isEmpty() || email.isEmpty() || subject.isEmpty() || message.isEmpty()) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			out.print("Please fill in all the fields.");
			return;
		}
		if (!email.matches(EMAIL_REGEX)) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			out.print("Please enter a valid email address.");
			return;
		}
		if (name.length() > 100 || email.length() > 150 || subject.length() > 200 || message.length() > 5000) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			out.print("One of the fields is too long.");
			return;
		}

		ContactQuery q = new ContactQuery();
		q.setName(name);
		q.setEmail(email);
		q.setSubject(subject);
		q.setMessage(message);

		if (new ContactDao().insertQuery(q)) {
			out.print("OK");
		} else {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			out.print("Could not save your message. Please try again later.");
		}
	}

	// Block direct GET requests to the servlet URL
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.sendRedirect("index.jsp#contact");
	}

	private static String trim(String s) {
		return s == null ? "" : s.trim();
	}
} 
