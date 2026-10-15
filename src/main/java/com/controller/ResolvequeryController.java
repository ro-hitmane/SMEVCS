package com.controller;

import java.io.IOException;
import java.net.URLEncoder;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.dao.ContactDao;

@WebServlet("/ResolvequeryController")
public class ResolvequeryController extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
			int queryId = Integer.parseInt(request.getParameter("queryId"));
			
			if (new ContactDao().resolveQuery(queryId)) {
				response.sendRedirect("ViewQuery.jsp?msg=" + URLEncoder.encode("Query marked as resolved.", "UTF-8"));
			} else {
				response.sendRedirect("ViewQuery.jsp?error=" + URLEncoder.encode("Failed to resolve query.", "UTF-8"));
			}
		} catch (Exception e) {
			response.sendRedirect("ViewQuery.jsp?error=" + URLEncoder.encode("Invalid query ID.", "UTF-8"));
		}
	}
}