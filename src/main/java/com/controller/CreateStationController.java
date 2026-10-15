package com.controller;

import java.io.IOException;
import java.net.URLEncoder;
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

		String name = clean(request.getParameter("stationName"));
		String address = clean(request.getParameter("address"));
		String area = clean(request.getParameter("area"));
		String city = clean(request.getParameter("city"));
		String state = clean(request.getParameter("state"));
		String country = clean(request.getParameter("country"));
		if (country.isEmpty()) country = "India";

		if (name.isEmpty() || address.isEmpty() || area.isEmpty() || city.isEmpty() || state.isEmpty()) {
			request.setAttribute("error", "Please fill in all the station details.");
			request.getRequestDispatcher("Stations.jsp").forward(request, response);
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
			response.sendRedirect("Stations.jsp?msg=" + URLEncoder.encode("Station created successfully", "UTF-8"));
		} else {
			request.setAttribute("error", "Failed to create station. Please check your connection or database.");
			request.getRequestDispatcher("Stations.jsp").forward(request, response);
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