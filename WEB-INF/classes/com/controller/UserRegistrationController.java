package com.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import com.bean.User;
import com.dao.UserDao;

@WebServlet("/UserRegistrationController")
public class UserRegistrationController extends HttpServlet {
	private static final long serialVersionUID = 1L;
   
    public UserRegistrationController() {
        super();
        
    }

	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		
        PrintWriter out=response.getWriter();
		
		String name=request.getParameter("name");
		String address=request.getParameter("address");
		String mobile_no=request.getParameter("mobile_no");
		String email=request.getParameter("email");
		String password=request.getParameter("password");
		String vehicle_no=request.getParameter("vehicle_no");
		String vehicle_name=request.getParameter("vehicle_name");
		String vehicle_owner_name=request.getParameter("vehicle_owner_name");
		
		UserDao db=new UserDao();
		
		User bean=new User();
		bean.setName(name);
		bean.setAddress(address);
		bean.setEmail(email);
		bean.setMobile_no(mobile_no);
		bean.setPassword(password);
		bean.setVehicle_no(vehicle_no);
		bean.setVehicle_name(vehicle_name);
        bean.setVehicle_owner_name(vehicle_owner_name);
		
		UserDao dao=new UserDao();

		try {
			if(db.alreadyUser(email))
			{
				out.println("<script type=\"text/javascript\">");
				out.println("alert('User already exists')");
				out.println("location='UserLogin.jsp';");
				out.println("</script>");
				
			}
			else
			{
			if(dao.InsertUserData(bean))
			{
				out.println("<script type=\"text/javascript\">");
				out.println("alert('Registration Successful')");
				out.println("location='UserLogin.jsp';");
				out.println("</script>");
				
			}
			else
			{
				out.println("<script type=\"text/javascript\">");
				out.println("alert('Registration Fail.......')");
				out.println("location='UserRegister.jsp';");
				out.println("</script>");
			}
}
		} catch (SQLException e) {
			
			e.printStackTrace();
		}
	}

	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		doGet(request, response);
	}

}
