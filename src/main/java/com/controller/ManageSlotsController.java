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

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		PrintWriter out = response.getWriter();

		String stationId = request.getParameter("stationId");
		String slotLabel = request.getParameter("slotLabel");
		String vehicleType = request.getParameter("vehicleType");
		String chargerType = request.getParameter("chargerType");
		String status = request.getParameter("status");

		// opened directly in the browser without the form, or station not selected
		if (stationId == null || stationId.trim().isEmpty()) {
			response.sendRedirect("ManageSlots.jsp");
			return;
		}

		Slot slot = new Slot();

		slot.setStationId(Integer.parseInt(stationId));
		slot.setSlotLabel(slotLabel);
		slot.setVehicleType(vehicleType);
		slot.setChargerType(chargerType);
		slot.setStatus(status);

		AdminDao dao = new AdminDao();

		if (dao.Manageslots(slot))
		{
			out.println("<script type=\"text/javascript\">");
			out.println("alert('Slot added successfully');");
			out.println("location='ManageSlots.jsp';");
			out.println("</script>");
		}
		else
		{
			out.println("<script type=\"text/javascript\">");
			out.println("alert('Failed to add slot');");
			out.println("location='ManageSlots.jsp';");
			out.println("</script>");
		}
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		doGet(request, response);
	}

}