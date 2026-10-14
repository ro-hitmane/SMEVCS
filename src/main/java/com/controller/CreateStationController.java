package com.controller;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.bean.Station;
import com.dao.AdminDao;

@WebServlet("/CreateStationController")
public class CreateStationController extends HttpServlet {
	private static final long serialVersionUID = 1L;

	public CreateStationController() {
		super();
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		request.setCharacterEncoding("UTF-8");
		response.setContentType("text/html; charset=UTF-8");
		PrintWriter out = response.getWriter();

		String name = clean(request.getParameter("stationName"));
		String address = clean(request.getParameter("address"));
		String area = clean(request.getParameter("area"));
		String city = clean(request.getParameter("city"));
		String state = clean(request.getParameter("state"));
		String country = clean(request.getParameter("country"));
		if (country.isEmpty()) country = "India";

		if (name.isEmpty() || address.isEmpty() || area.isEmpty() || city.isEmpty() || state.isEmpty()) {
			out.println("<script type=\"text/javascript\">");
			out.println("alert('Please fill in all the station details');");
			out.println("location='Stations.jsp';");
			out.println("</script>");
			return;
		}

		Station station = new Station();
		station.setStationName(name);
		station.setAddress(address);
		station.setArea(area);
		station.setCity(city);
		station.setState(state);
		station.setCountry(country);

		if (new AdminDao().addStation(station)) {
			out.println("<script type=\"text/javascript\">");
			out.println("alert('Station created successfully');");
			out.println("location='Stations.jsp';");
			out.println("</script>");
		} else {
			out.println("<script type=\"text/javascript\">");
			out.println("alert('Failed to create station');");
			out.println("location='Stations.jsp';");
			out.println("</script>");
		}
	}

	private static String clean(String v) {
		return v == null ? "" : v.trim();
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.sendRedirect("Stations.jsp");
	}
}
