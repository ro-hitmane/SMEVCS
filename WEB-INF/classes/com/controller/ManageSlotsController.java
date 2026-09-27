package com.controller;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.bean.Slot;
import com.dao.AdminDao;

@WebServlet("/ManageSlotsController")
public class ManageSlotsController extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    
    public ManageSlotsController() {
        super();
        
    }

	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
      PrintWriter out=response.getWriter();
		
		String slotname=request.getParameter("slotname");
		String slotaddress=request.getParameter("slotaddress");
		String slotarea=request.getParameter("slotarea");
		String city=request.getParameter("city");
		String state=request.getParameter("state");
		String country=request.getParameter("country");
		
		Slot slot=new Slot();
		
		slot.setSlotname(slotname);
		slot.setSlotaddress(slotaddress);
		slot.setSlotarea(slotarea);
		slot.setCity(city);
		slot.setState(state);
		slot.setCountry(country);
		
		AdminDao dao=new AdminDao();
		
		if(dao.Manageslots(slot))
		{
			out.println("<script type=\"text/javascript\">");
			out.println("alert('Confirmed Slot')");
			out.println("location='ManageSlots.jsp';");
			out.println("</script>");
			
		}
		else
		{
			out.println("<script type=\"text/javascript\">");
			out.println("alert('Fail.........')");
			out.println("location='ManageSlots.jsp';");
			out.println("</script>");
		}
	}

	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		doGet(request, response);
	}

}
