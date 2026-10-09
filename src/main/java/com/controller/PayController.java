package com.controller;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.bean.Pay;
import com.dao.UserDao;



@WebServlet("/PayController")
public class PayController extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
   
    public PayController() {
        super();
        
    }

	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		
        PrintWriter out=response.getWriter();
		
		String ownername=request.getParameter("ownername");
		String no=request.getParameter("no");
		String bat=request.getParameter("bat");
		String amt=request.getParameter("amt");
		
		Pay pay=new Pay();
		
		pay.setOwnername(ownername);
		pay.setNo(no);
		pay.setBat(bat);
		pay.setAmt(amt);
		
		UserDao db=new UserDao();
		
		if(db.InsertPayData(pay))
		{
			out.println("<script type=\"text/javascript\">");
			out.println("alert('Amount Paid')");
			out.println("location='Pay.jsp';");
			out.println("</script>");
			
		}
		else
		{
			out.println("<script type=\"text/javascript\">");
			out.println("alert(' Fail.......')");
			out.println("location='Pay.jsp';");
			out.println("</script>");
		}
		
	}

	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		doGet(request, response);
	}

}
