package com.controller;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/AdminLoginController")
public class AdminLoginController extends HttpServlet {
	private static final long serialVersionUID = 1L;
    
    public AdminLoginController() {
        super();
        
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		PrintWriter out=response.getWriter();
		
		String email=request.getParameter("email");
		String password=request.getParameter("password");
		
		if(email.equals("admin@gmail.com")&&password.equals("Admin123"))
		{
			out.println("<script type=\"text/javascript\">");
			out.println("alert('Login Successful')");
			out.println("location='AdminHome.jsp';");
			out.println("</script>");
			
			HttpSession ss=request.getSession();
			ss.setAttribute("email", email);
		}
		else
		{
			out.println("<script type=\"text/javascript\">");
			out.println("alert('Login Fail.......')");
			out.println("location='AdminLogin.jsp';");
			out.println("</script>");
		}
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		doGet(request, response);
	}

}
