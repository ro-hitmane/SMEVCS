package com.controller;

import java.io.IOException;
import java.net.URLEncoder;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.dao.AdminDao;

@WebServlet("/DeleteSlotController")
public class DeleteSlotController extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		int slotId;
		int stationId;
		try {
			slotId = Integer.parseInt(request.getParameter("slotId"));
			stationId = Integer.parseInt(request.getParameter("stationId"));
		} catch (Exception e) {
			response.sendRedirect("Stations.jsp");
			return;
		}

		String target = "ManageStation.jsp?stationId=" + stationId;

		if (new AdminDao().deleteSlot(slotId, stationId)) {
			response.sendRedirect(target + "&msg=" + URLEncoder.encode("Slot deleted successfully", "UTF-8"));
		} else {
			response.sendRedirect(target + "&error=" + URLEncoder.encode("Failed to delete slot", "UTF-8"));
		}
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.sendRedirect("Stations.jsp");
	}
}