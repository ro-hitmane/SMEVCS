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

@WebServlet("/UpdateSlotController")
public class UpdateSlotController extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		request.setCharacterEncoding("UTF-8");
		response.setContentType("text/html; charset=UTF-8");
		PrintWriter out = response.getWriter();

		int slotId;
		int stationId;
		try {
			slotId = Integer.parseInt(request.getParameter("slotId"));
			stationId = Integer.parseInt(request.getParameter("stationId"));
		} catch (Exception e) {
			response.sendRedirect("Stations.jsp");
			return;
		}

		String slotLabel = clean(request.getParameter("slotLabel"));
		String vehicleType = clean(request.getParameter("vehicleType"));
		String chargerType = clean(request.getParameter("chargerType"));
		String status = clean(request.getParameter("status"));

		String back = "ManageStation.jsp?stationId=" + stationId;

		if (slotLabel.isEmpty() || vehicleType.isEmpty() || chargerType.isEmpty() || status.isEmpty()
				|| slotLabel.length() > 50 || vehicleType.length() > 50 || chargerType.length() > 50
				|| status.length() > 20) {
			respond(out, "Please fill in all the slot details correctly", back);
			return;
		}

		Slot slot = new Slot();
		slot.setId(slotId);
		slot.setStationId(stationId);
		slot.setSlotLabel(slotLabel);
		slot.setVehicleType(vehicleType);
		slot.setChargerType(chargerType);
		slot.setStatus(status);

		if (new AdminDao().updateSlot(slot)) {
			respond(out, "Slot updated successfully", back);
		} else {
			respond(out, "Failed to update slot", back);
		}
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.sendRedirect("Stations.jsp");
	}

	private static String clean(String v) {
		return v == null ? "" : v.trim();
	}

	private static void respond(PrintWriter out, String message, String target) {
		out.println("<script type=\"text/javascript\">");
		out.println("alert('" + message + "');");
		out.println("location='" + target + "';");
		out.println("</script>");
	}
}
