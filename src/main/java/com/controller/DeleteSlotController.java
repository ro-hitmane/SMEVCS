package com.controller;

import java.io.IOException;
import java.io.PrintWriter;

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

		String back = "ManageStation.jsp?stationId=" + stationId;

		out.println("<script type=\"text/javascript\">");
		if (new AdminDao().deleteSlot(slotId, stationId)) {
			out.println("alert('Slot deleted successfully');");
		} else {
			out.println("alert('Failed to delete slot');");
		}
		out.println("location='" + back + "';");
		out.println("</script>");
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.sendRedirect("Stations.jsp");
	}
}
