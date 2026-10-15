package com.controller;

import java.io.IOException;
import java.net.URLEncoder;
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
		String stationId = request.getParameter("stationId");
		String slotLabel = request.getParameter("slotLabel");
		String vehicleType = request.getParameter("vehicleType");
		String chargerType = request.getParameter("chargerType");
		String status = request.getParameter("status");

		if (stationId == null || stationId.trim().isEmpty()) {
			response.sendRedirect("ManageSlots.jsp");
			return;
		}

		Slot slot = new Slot();
		try {
			slot.setStationId(Integer.parseInt(stationId.trim()));
		} catch (NumberFormatException e) {
			response.sendRedirect("ManageSlots.jsp?error=" + URLEncoder.encode("Invalid station ID", "UTF-8"));
			return;
		}

		slot.setSlotLabel(slotLabel);
		slot.setVehicleType(vehicleType);
		slot.setChargerType(chargerType);
		slot.setStatus(status);

		AdminDao dao = new AdminDao();

		if (dao.Manageslots(slot)) {
			response.sendRedirect("ManageSlots.jsp?msg=" + URLEncoder.encode("Slot added successfully", "UTF-8"));
		} else {
			request.setAttribute("error", "Failed to add slot.");
			request.getRequestDispatcher("ManageSlots.jsp").forward(request, response);
		}
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}
}