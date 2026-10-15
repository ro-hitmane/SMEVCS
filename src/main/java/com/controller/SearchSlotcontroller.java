package com.controller;

import java.io.IOException;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.connection.DBConnection;

@WebServlet("/SearchSlotcontroller")
public class SearchSlotcontroller extends HttpServlet {
	private static final long serialVersionUID = 1L;

    public SearchSlotcontroller() {
        super();
    }

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String slotname = request.getParameter("slotname");
		String address = request.getParameter("address");
		String area = request.getParameter("area");
		String city = request.getParameter("city");
		String state = request.getParameter("state");

		String sql = "INSERT INTO tbl_booking VALUES(null,?,?,?,?,?)";

		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = DBConnection.getConnection();
			ps = con.prepareStatement(sql);
			ps.setString(1, slotname);
			ps.setString(2, address);
			ps.setString(3, area);
			ps.setString(4, city);
			ps.setString(5, state);

			int index = ps.executeUpdate();

			if (index > 0) {
				response.sendRedirect("UserHome.jsp?msg=" + URLEncoder.encode("Booking Confirmed", "UTF-8"));
			} else {
				request.setAttribute("error", "Booking could not be confirmed.");
				request.getRequestDispatcher("UserHome.jsp").forward(request, response);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			request.setAttribute("error", "Database error occurred while confirming booking.");
			request.getRequestDispatcher("UserHome.jsp").forward(request, response);
		} finally {
			if (ps != null) {
				try { ps.close(); } catch (SQLException ignored) {}
			}
			if (con != null) {
				try { con.close(); } catch (SQLException ignored) {}
			}
		}
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}
}