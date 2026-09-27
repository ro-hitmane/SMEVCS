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

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		
		response.setContentType("text/html");
		PrintWriter out=response.getWriter();
		HttpSession ss=request.getSession();
		
		
		String email=request.getParameter("email");
		String password=request.getParameter("password");
		
		UserDao dao=new UserDao();
		User user = new User();
		user=dao.CheckUser(email, password);
		
		
		ss.setAttribute("name", user.getName());
		  ss.setAttribute("vehicleno", user.getVehicle_no());
		 ss.setAttribute("email", user.getEmail());
		if((user.getEmail()!=null && user.getEmail()!="") && (user.getPassword()!=null && user.getPassword()!=""))
		{
		     out.println("<script type=\"text/javascript\">");
			 out.println("alert('Login Successfully')");
			 out.println("location='UserHome.jsp';");
			out.println("</script>");
			
			
			
		}
		else
		{
	     out.println("<script type=\"text/javascript\">");
		 out.println("alert('Invalid Login Details')");
		 out.println("location='UserLogin.jsp';");
		out.println("</script>");
			
		}
	}

	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		doGet(request, response);
	}

}
